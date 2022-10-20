package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Lightning;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Boltage_Circle extends RegisteredSkill
{
	public Boltage_Circle()
	{	
		super(new Boltage_Circle_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(12, 4));
		addModifier("radius", new LinearValue(4.2, 0.2));
		addModifier("cooldown", new LinearValue(20, -0.4));
		addModifier("mana", new LinearValue(20, 3));
		addModifier("stamina", new LinearValue(12.7, 0.7));
	}
}

class Boltage_Circle_Handler extends MalaSkill implements Listener
{
	public Boltage_Circle_Handler()
	{
		super(	"BOLTAGE_CIRCLE",
				"볼티지 서클",
				Material.SUNFLOWER,
				MsgTBL.NeedSkills,
				"&e 라이트닝 마스터리 - lv.20",
				"",
				MsgTBL.WEAPON + MsgTBL.SKILL + MsgTBL.PHYSICAL + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC,
				"",
				"&7무기를 크게 휘둘러 &8{radius}&7m 내 주변의 적들을 벱니다.",
				"&7적들은 &8{power}&7의 전격 피해를 받습니다.",
				"&7체인 라이트닝이 적용됩니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "피해량 30% 증가",
				MsgTBL.WEAPON_SPEAR + "공격 범위 30% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "MASTERY_LIGHTNING", 20))
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
		double damage = cast.getModifier("power"); // 공격력
		
		damage *= Mastery_Lightning.Get_Mult(data.getPlayer());
		double radius = (int)cast.getModifier("radius");
		
		// 무기 효과
		if (Weapon_Identify.Hold_MMO_Sword(data.getPlayer()))
			damage *= 1.5;
		else if (Weapon_Identify.Hold_MMO_Spear(data.getPlayer()))
			radius *= 1.5;

		data.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Boltage_Circle_Skill(data, damage, radius));
		
	}

	// 검술 베어가르기  효과
	class Boltage_Circle_Skill implements Runnable
	{
		PlayerData data;
		Player player;
		Location pos;
		
		List<Entity> entities;
		double damage;
		double radius;

		Vector[] vecs;
		Vector[] vecs_lightning;
		
		boolean is_attack = false;
		RegisteredSkill skill_chain_lightning;
		int count;
		double reduce;
		
		public Boltage_Circle_Skill(PlayerData p, double _damage, double _radius)
		{
			data = p;
			player = p.getPlayer();
			damage = _damage;
			radius = _radius;
			pos = player.getEyeLocation();

			// 체인 라이트닝
			skill_chain_lightning = MMOCore.plugin.skillManager.getSkill("CHAIN_LIGHTNING");
			boolean chain = p.getProfess().hasSkill(skill_chain_lightning);

			int lv = p.getSkillLevel(skill_chain_lightning);
			count = chain ? (int)skill_chain_lightning.getModifier("move_count", lv) : 0;
			reduce = chain ? skill_chain_lightning.getModifier("dam_reduce", lv) * 0.01 : 0;

			DamageMetadata dm = new DamageMetadata(damage);
			Bukkit.getPluginManager().callEvent(new LightningMagicEvent(data.getPlayer(), dm));
			damage = dm.getDamage();
			
			vecs = new Vector[(int) (radius * 40)];
			vecs_lightning = new Vector[9];

			double additive = 360.0 / (radius * 40);
			double lightning_angle = 45.0;
			for(int i = 0; i < vecs.length; i++)
			{
				double _angle = -180.0 + additive * i;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			}
			for (int i = 0; i < vecs_lightning.length; i++)
			{
				lightning_angle += 45.0 * i;
				vecs_lightning[i] = new Vector(Math.cos(Math.toRadians(lightning_angle)), 0, Math.sin(Math.toRadians(lightning_angle)));
			}
			vecs = TRS.Scale(vecs, radius, radius, radius);
			vecs_lightning = TRS.Scale(vecs_lightning, radius, radius, radius);
			double y_rand = Math.random() * 360.0;
			double x_rand = Math.random() * 15.0;
			vecs = TRS.Rotate_Y(vecs, y_rand);
			vecs_lightning = TRS.Rotate_Y(vecs_lightning, y_rand);
			vecs = TRS.Rotate_X(vecs, x_rand);
			vecs_lightning = TRS.Rotate_X(vecs_lightning, x_rand);
			
			player.getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
		}
		
		public void run()
		{
			if (is_attack == false)
			{
				// 검기
				player.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
				
				// 파티클 그리기
				for(int i = 0; i < vecs.length; i++)
				{
					Location loc = pos.clone().add(vecs[i]);
					player.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0d, 0d, 0d, 0d);
				}

				is_attack = true;
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20L);
			}
			else
			{
				pos.getWorld().playSound(pos, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 1.1f);
				pos.getWorld().playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
				// 파티클 그리기
				for(int i = 0; i < vecs_lightning.length - 1; i++)
				{
					Location start = pos.clone().add(vecs_lightning[i]);
					Location end = pos.clone().add(vecs_lightning[i + 1]);
					Lightning_Bolt.Draw_Lightning_Line(start, end);
				}
				// 범위 판정
				List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, radius * 3, radius * 3, radius * 3));
				for(Entity temp : abc)
				{
					if (!(temp instanceof LivingEntity))
						continue;
					if (temp == player)
						continue;
					if (pos.distance(temp.getLocation()) > radius)
						continue;
					
					LivingEntity temp2 = (LivingEntity)temp;
					
					Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
							new Lightning_Bolt_Chain(data.getPlayer(), temp2, damage, count, reduce, true));
				}
			}
		}
	}
}

