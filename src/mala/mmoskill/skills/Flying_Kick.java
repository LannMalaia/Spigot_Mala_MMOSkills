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
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Flying_Kick extends RegisteredSkill
{
	public static String metaname = "mala.mmoskill.flying_kick.dash";
	public Flying_Kick()
	{	
		super(new Flying_Kick_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("sec", new LinearValue(0.32, 0.02));
		addModifier("damage", new LinearValue(21, 6));
		addModifier("cooldown", new LinearValue(8.0, 0));
		addModifier("stamina", new LinearValue(10.5, 0.5));
	}
}

class Flying_Kick_Handler extends MalaSkill implements Listener
{
	public Flying_Kick_Handler()
	{
		super(	"FLYING_KICK",
				"비천각",
				Material.RABBIT,
				MsgTBL.NeedSkills,
				"&e 격투 마스터리 - lv.10",
				"",
				MsgTBL.UNARMED + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&e{sec}&7초간 바라보는 곳을 향해 빠르게 돌진합니다.",
				"&7가장 처음 부딪힌 적에게 &e{damage}&7의 피해를 줍니다.",
				"&7이후 자신은 바라보는 방향의 뒤편으로 잽싸게 점프합니다.",
				"&7공중으로도 이동할 수 있습니다.",
				"&c양손에 무기가 없어야 합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		if (player.hasMetadata(Flying_Kick.metaname))
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
			player.sendMessage("§c§l[ 비천각 취소 ]");
			player.removeMetadata(Flying_Kick.metaname, MalaMMO_Skill.plugin);
			return new SimpleSkillResult(false);
		}
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if (!Skill_Util.Has_Skill(data, "MASTERY_FIST", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		
		if (!Weapon_Identify.Has_No_Item(player))
		{
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double sec = cast.getModifier("sec");
		double damage = cast.getModifier("damage");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Tongbei_Shoot(cast, data.getPlayer(), sec, damage));
	}
	
	class Tongbei_Shoot implements Runnable
	{
		SkillMetadata cast;
		Player player;
		double sec;
		double damage;
		
		int count = 0;
		double velocity = 1.4;
		double angle = 0.0;
		double y_angle = 0.0;
		
		public Tongbei_Shoot(SkillMetadata cast, Player _player, double _sec, double _damage)
		{
			this.cast = cast;
			player = _player;
			sec = _sec;
			damage = _damage;
			
			angle = player.getLocation().getYaw();
			y_angle = player.getLocation().getPitch();
			
			player.setMetadata(Flying_Kick.metaname, new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
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
				player.sendMessage("§c§l[ 비천각 취소 ]");
				player.removeMetadata(Flying_Kick.metaname, MalaMMO_Skill.plugin);
				return;
			}
			if (sec <= 0.0 || !player.hasMetadata(Flying_Kick.metaname))
			{
				player.removeMetadata(Flying_Kick.metaname, MalaMMO_Skill.plugin);
				return;
			}
			
			velocity = Math.min(1.4, velocity + 0.05);
			
			double y_amount = 1.0 - Math.abs(y_angle / 90.0);
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)) * y_amount, y_angle / -90.0, Math.sin(Math.toRadians(angle + 90)) * y_amount);
			Vector vc = dir.multiply(velocity);
			// vc.setY(player.getVelocity().getY());
			
			Vehicle_Util.Get_Last_Vehicle(player).setVelocity(vc);
			// player.setVelocity(vc);
			
			Particle_Drawer.Draw_Circle(player.getEyeLocation(), Particle.CRIT, 2.0, player.getLocation().getPitch() + 90f, player.getLocation().getYaw());
//			Vector[] new_vecs = TRS.Rotate_Z(vecs, count * 9.0d);
//			new_vecs = TRS.Rotate_X(new_vecs, y_angle);
//			new_vecs = TRS.Rotate_Y(new_vecs, angle);
//			for (int i = 0; i < 8; i += 2)
//			{
//				Location start = player.getEyeLocation().add(new_vecs[i]);
//				Location end = player.getEyeLocation().add(new_vecs[i + 1]);
//				Particle_Drawer.Draw_Line(start, end, Particle.CRIT, 0.2);
//			}
			player.getWorld().spawnParticle(Particle.SMOKE_LARGE, player.getEyeLocation(), 5, 0.0, 0.4, 0.0, 0.0);
			
			boolean attacked = false;
			for(Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 2.0, 2.0, 2.0))
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;
				
				if (Damage.Is_Possible(player, en))
				{
					Damage.SkillAttack(cast, (LivingEntity)en, damage, DamageType.UNARMED, DamageType.SKILL, DamageType.PHYSICAL);
					player.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, en.getLocation().add(0, 0.5, 0), 1, 0, 0, 0, 0.0);
					attacked = true;
				}
			}
			
			if (attacked)
			{
				Location loc = player.getLocation().add(0.0, player.getHeight() * 0.5, 0.0);
				player.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0.0);
				player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
				player.removeMetadata(Flying_Kick.metaname, MalaMMO_Skill.plugin);
				Vector velocity = player.getLocation().getDirection();
				velocity.multiply(-1).setY(0.3);
				player.setVelocity(velocity);
			}
			else
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}















