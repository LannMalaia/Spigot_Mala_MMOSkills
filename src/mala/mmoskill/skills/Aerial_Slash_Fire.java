package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.skills.passive.Mastery_Fire;
// import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;


public class Aerial_Slash_Fire extends RegisteredSkill
{
	public Aerial_Slash_Fire()
	{	
		super(new Aerial_Slash_Fire_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(5.5, 1.5));
		addModifier("explode_power", new LinearValue(48, 8));
		addModifier("size", new LinearValue(1.4, 0.1));
		addModifier("cooldown", new LinearValue(18, 0));
		addModifier("mana", new LinearValue(23.5, 3.5));
		addModifier("stamina", new LinearValue(13, 1));
	}
}

class Aerial_Slash_Fire_Handler extends MalaSkill implements Listener
{
	public Aerial_Slash_Fire_Handler()
	{
		super(	"AERIAL_SLASH_FIRE",
				"버닝 부메랑",
				Material.SUNFLOWER,
				MsgTBL.NeedSkills,
				"&e 부메랑 칼날 - lv.10",
				"&e 파이어 마스터리 - lv.10",
				"",
				MsgTBL.PROJECTILE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC + MsgTBL.SKILL,
				"",
				"&7전방으로 화염의 검기를 날려보냅니다.",
				"&7검기는 일정 거리 나아간 뒤, 폭발을 일으키고서 다시 돌아옵니다.",
				"&7검기에 닿은 적들은 계속해서 &8{power}&7의 피해를 받고 불탑니다.",
				"&7폭발에 휘말린 적들은 &8{explode_power}&7의 피해를 받습니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "피해량 50% 증가",
				MsgTBL.WEAPON_SPEAR + "범위 100% 증가",
				"",
				MsgTBL.Cooldown,
				MsgTBL.ManaCost,
				MsgTBL.StaCost);
		registerModifiers("power", "size");
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "AERIAL_SLASH", 10) || !Skill_Util.Has_Skill(data, "MASTERY_FIRE", 10))
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
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		double explode_damage = cast.getModifier("explode_power"); // 공격력
		explode_damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		double size = (int)cast.getModifier("size");

		// 무기 효과
		if (Weapon_Identify.Hold_MMO_Sword(data.getPlayer()))
		{
			damage *= 1.5;
			explode_damage *= 1.5;
		}
		else if (Weapon_Identify.Hold_MMO_Spear(data.getPlayer()))
			size *= 2.0;
		
		data.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Aerial_Slash_Fire_Skill(data, damage, size, explode_damage));
	}

	class Aerial_Slash_Fire_Skill implements Runnable
	{
		PlayerData player;
		Location pos;
		Vector dir;
		
		List<Entity> entities;
		double damage, explode_damage;
		double size;

		double max_speed = 0.4;
		double current_speed = 0.4;
		double decelerate = -0.01;
		Vector[] vecs;
		
		double correct_angle = 0.0;
		double correct_Zangle = 0.0;
		
		public Aerial_Slash_Fire_Skill(PlayerData p, double _damage, double _size, double _explode_damage)
		{
			player = p;
			damage = _damage;
			explode_damage = _explode_damage;
			size = _size;

			max_speed = size * 0.6;
			current_speed = size * 0.6;
			decelerate = current_speed * 0.05;
			
			pos = p.getPlayer().getEyeLocation().add(0, -0.3, 0);
			dir = p.getPlayer().getLocation().getDirection();
			
			// 피해 보정
			DamageMetadata dm = new DamageMetadata(damage, DamageType.PROJECTILE, DamageType.SKILL, DamageType.MAGIC, DamageType.PHYSICAL);
			Bukkit.getPluginManager().callEvent(new FireMagicEvent(player.getPlayer(), dm));
			damage = dm.getDamage();
			
			vecs = new Vector[(int) (size * 80)];
			double additive = 360.0 / (size * 80);
			for(int i = 0; i < vecs.length; i += 2)
			{
				double _angle = -180.0 + additive * i;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
				vecs[i + 1] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.85, 0, Math.sin(Math.toRadians(_angle)) * 0.85);// * 0.7);
			}
			vecs = TRS.Scale(vecs, size, size, size);
			correct_Zangle = -30.0 + Math.random() * 60.0;
			
			player.getPlayer().getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
		}
		
		boolean bomb = false;
		int count = 0;
		public void run()
		{
			// 검기
			if (count++ % 2 == 0)
				player.getPlayer().getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f);
			
			// 파티클 그리기
			correct_angle += 40.0;
			Vector[] temp_vecs = TRS.Rotate_Y(vecs, correct_angle);
			temp_vecs = TRS.Rotate_Z(temp_vecs, correct_Zangle);
			temp_vecs = TRS.Rotate_X(temp_vecs, pos.getPitch());
			temp_vecs = TRS.Rotate_Y(temp_vecs, pos.getYaw());

			for(int i = 0; i < temp_vecs.length; i++)
			{
				Location loc = pos.clone().add(temp_vecs[i]);
				player.getPlayer().getWorld().spawnParticle(Particle.CRIT, loc, 1, 0d, 0d, 0d, 0d);
				if (i % 4 == 0)
					player.getPlayer().getWorld().spawnParticle(Particle.FLAME, loc, 1, 0d, 0d, 0d, 0d);
			}
			
			// 범위 판정
			List<Entity> abc = null;
			List<Entity> entities = null;
			if (bomb == false && current_speed <= 0)
			{
				bomb = true;
				abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, size * 7, size * 7, size * 7));
				entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(),
						new Vector(pos.getPitch(), pos.getYaw(), 0),
						new Vector(size * 5, 5, size * 5),
						abc);
				pos.getWorld().playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 2, 1.5f);
				pos.getWorld().playSound(pos, Sound.ITEM_TOTEM_USE, 2, 1.5f);
				// current_loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, current_loc, 50, 3, 3, 3, 0);
				pos.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, pos, 45, 3, 3, 3, 0);
				pos.getWorld().spawnParticle(Particle.LAVA, pos, 70, 3, 3, 3, 0);
				pos.getWorld().spawnParticle(Particle.FLAME, pos, 70, 3, 3, 3, 0);

				for (Entity temp : entities)
				{
					if(!(temp instanceof LivingEntity))
						continue;
					if(temp == player.getPlayer())
						continue;
					
					LivingEntity temp2 = (LivingEntity)temp;
					Damage.Attack(player.getPlayer(), temp2, explode_damage,
							DamageType.PROJECTILE, DamageType.SKILL, DamageType.MAGIC);
					temp2.setFireTicks(100);
				}
			}
			else
			{
				abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, size * 3, size * 3, size * 3));
				entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(),
						new Vector(pos.getPitch(), pos.getYaw(), 0),
						new Vector(size * 2, 2.5, size * 2.5),
						abc);
				for (Entity temp : entities)
				{
					if(!(temp instanceof LivingEntity))
						continue;
					if(temp == player.getPlayer())
						continue;
					
					LivingEntity temp2 = (LivingEntity)temp;
					//if (temp2.getNoDamageTicks() == 0)
					//{
						Damage.Attack(player.getPlayer(), temp2, damage,
								DamageType.PROJECTILE, DamageType.SKILL, DamageType.MAGIC);
						temp2.setFireTicks(100);
						temp2.setNoDamageTicks(10);
					//}
				}
			}
			
			// 잠시 쉬는 시간
			if(current_speed > -max_speed)
			{
				pos.add(dir.clone().multiply(current_speed));
				current_speed -= decelerate;
				
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
			}
		}
	}
}
