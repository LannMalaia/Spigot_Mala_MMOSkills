package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Vehicle_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Electric_Tackle extends RegisteredSkill
{
	public static String metaname = "mala.mmoskill.electric_tackle";
	public static DustTransition dtr = new DustTransition(Color.YELLOW, Color.WHITE, 2f);
	
	public Electric_Tackle()
	{	
		super(new Electric_Tackle_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("sec", new LinearValue(0.12, 0.02));
		addModifier("dash_damage", new LinearValue(28, 8));
		addModifier("cooldown", new LinearValue(15.6, -0.4, 4.0, 16.0));
		addModifier("mana", new LinearValue(13.5, 1.5));
		addModifier("stamina", new LinearValue(16.5, 0.5));
	}
}

class Electric_Tackle_Handler extends MalaSkill implements Listener
{
	public Electric_Tackle_Handler()
	{
		super(	"ELECTRIC_TACKLE",
				"일렉트릭 태클",
				Material.RABBIT_FOOT,
				MsgTBL.NeedSkills,
				"&e 스피어 차지 - lv.10",
				"&e 라이트닝 마스터리 - lv.10",
				"",
				MsgTBL.WEAPON + MsgTBL.PHYSICAL + MsgTBL.SKILL + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC,
				"",
				"&e{sec}&7초간 바라보는 곳을 향해 매우 빠르게 돌진합니다.",
				"&7돌진 궤적에 있던 적들은 &e{dash_damage}&7의 피해를 받습니다.",
				"&7웅크리거나 스킬을 재사용하여 돌진을 취소할 수 있습니다.",
				"&7공중으로도 이동할 수 있습니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "피해량 40% 증가",
				MsgTBL.WEAPON_SPEAR + "범위 50% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost, MsgTBL.StaCost,
				"",
				"&8idea by hoon050824");
	}
	
	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		if (player.hasMetadata(Electric_Tackle.metaname))
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
			player.removeMetadata(Electric_Tackle.metaname, MalaMMO_Skill.plugin);
			return new SimpleSkillResult(false);
		}
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "SPEAR_CHARGE", 10)
				|| !Skill_Util.Has_Skill(data, "MASTERY_LIGHTNING", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double sec = cast.getModifier("sec");
		double dash_damage = cast.getModifier("dash_damage");
		double radius = 2.0;
		
		if (Weapon_Identify.Hold_Sword(data.getPlayer()) || Weapon_Identify.Hold_MMO_Sword(data.getPlayer()))
			dash_damage *= 1.4;
		if (Weapon_Identify.Hold_Spear(data.getPlayer()) || Weapon_Identify.Hold_MMO_Spear(data.getPlayer()))
			radius *= 1.5;
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Electric_Tackle_Aerial(data.getPlayer(), sec, dash_damage, radius));
	}
	
	class Electric_Tackle_Aerial implements Runnable
	{
		Player player;
		double sec;
		double damage;
		double radius = 2.0;
		
		int count = 0;
		double velocity = 2.0;
		double angle = 0.0;
		double y_angle = 0.0;
		
		Vector[] vecs;
		RegisteredSkill skill_chain_lightning;
		int lightning_count;
		double reduce;
		
		public Electric_Tackle_Aerial(Player _player, double _sec, double _damage, double _radius)
		{
			player = _player;
			sec = _sec;
			damage = _damage;
			radius = _radius;
			
			// 체인 라이트닝
			PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
			skill_chain_lightning = MMOCore.plugin.skillManager.getSkill("CHAIN_LIGHTNING");
			boolean chain = data.getProfess().hasSkill(skill_chain_lightning);

			int lv = data.getSkillLevel(skill_chain_lightning);
			count = chain ? (int)skill_chain_lightning.getModifier("move_count", lv) : 0;
			reduce = chain ? skill_chain_lightning.getModifier("dam_reduce", lv) * 0.01 : 0;

			DamageMetadata dm = new DamageMetadata(damage);
			Bukkit.getPluginManager().callEvent(new LightningMagicEvent(player, dm));
			damage = dm.getDamage();
			
			angle = player.getLocation().getYaw();
			y_angle = player.getLocation().getPitch();
			Make_Vecs();
			
			player.setMetadata(Electric_Tackle.metaname, new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
		}
		
		void Make_Vecs()
		{
			vecs = new Vector[8];
			vecs[0] = new Vector(-1.5, 1.5, -1.5);
			vecs[1] = new Vector(-0.5, 0.0, 3.5);
			vecs[2] = new Vector(1.5, 1.5, -1.5);
			vecs[3] = new Vector(0.5, 0.0, 3.5);
			vecs[4] = new Vector(-1.5, -1.5, -1.5);
			vecs[5] = new Vector(-0.5, 0.0, 3.5);
			vecs[6] = new Vector(1.5, -1.5, -1.5);
			vecs[7] = new Vector(0.5, 0.0, 3.5);
		}
		
		void Slerp()
		{
			double new_angle = player.getLocation().getYaw();
			double new_y_angle = player.getLocation().getPitch();
			
			double gap = new_angle - angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			gap = Math.min(2.0, Math.max(-2.0, gap));
			angle += gap;
			
			gap = new_y_angle - y_angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			gap = Math.min(2.0, Math.max(-2.0, gap));
			y_angle = Math.min(90.0, Math.max(-90.0, y_angle + gap));
		}
		
		public void run()
		{
			sec -= 0.05;
			count += 1;
			Slerp();
			if (player.isSneaking())
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
				player.removeMetadata(Electric_Tackle.metaname, MalaMMO_Skill.plugin);
				return;
			}
			if (sec <= 0.0 || !player.hasMetadata(Electric_Tackle.metaname))
			{
				player.removeMetadata(Electric_Tackle.metaname, MalaMMO_Skill.plugin);
				return;
			}
			
			velocity = Math.min(1.4, velocity + 0.05);
			
			double y_amount = 1.0 - Math.abs(y_angle / 90.0);
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)) * y_amount, y_angle / -90.0, Math.sin(Math.toRadians(angle + 90)) * y_amount);
			Vector vc = dir.multiply(velocity);
			// vc.setY(player.getVelocity().getY());
			
			Vehicle_Util.Get_Last_Vehicle(player).setVelocity(vc);
			// player.setVelocity(vc);
			
			Vector[] new_vecs = TRS.Rotate_Z(vecs, sec * 360.0);
			new_vecs = TRS.Rotate_X(new_vecs, y_angle);
			new_vecs = TRS.Rotate_Y(new_vecs, angle);
			for (int i = 0; i < 8; i += 2)
			{
				Location start = player.getEyeLocation().add(new_vecs[i]);
				Location end = player.getEyeLocation().add(new_vecs[i + 1]);
				Particle_Drawer.Draw_Line(start, end, Particle.WAX_ON, 0.25);
				player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, start, 1, 0.0, 0.0, 0.0, 0.0, Electric_Tackle.dtr);
				player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, end, 1, 0.0, 0.0, 0.0, 0.0, Electric_Tackle.dtr);
			}
			
			for(Entity en : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius))
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;

				LivingEntity le = (LivingEntity)en;
				if (Damage.Is_Possible(player, le) && le.getNoDamageTicks() == 0)
				{
					Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
							new Lightning_Bolt_Chain(player, (LivingEntity)en, damage, count, reduce, true));
				}
//				if (Damage.Is_Possible(player, en))
//				{
//					Damage.Attack(player, (LivingEntity)en, damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
//					Location loc = en.getLocation().add(0.0, en.getHeight() * 0.5, 0.0);
//					player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.4, 0.4, 0.4, 0.0);
//					player.getWorld().playSound(en.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
//				}
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

}















