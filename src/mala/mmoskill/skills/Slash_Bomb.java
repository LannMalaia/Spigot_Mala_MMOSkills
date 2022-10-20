package mala.mmoskill.skills;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mala.mmoskill.events.IceMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Ice;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.Hitbox;
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
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Slash_Bomb extends RegisteredSkill
{
	public Slash_Bomb()
	{	
		super(new Slash_Bomb_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(30, -0.4));
		addModifier("distance", new LinearValue(8, 1));
		addModifier("power", new LinearValue(34, 4));
		addModifier("stamina", new LinearValue(22, 2));
	}
}

class Slash_Bomb_Handler extends MalaSkill implements Listener
{
	public Slash_Bomb_Handler()
	{
		super(	"SLASH_BOMB",
				"극폭참",
				Material.BLAZE_ROD,
				MsgTBL.NeedSkills,
				"&e 순간베기 - lv.10",
				"&e 폭발 화살 - lv.10",
				"",
				MsgTBL.WEAPON + MsgTBL.SKILL,
				"",
				"&7전방 &8{distance}&7m의 적 전체에게 &e{power}&7의 피해를 줍니다.",
				"&7이후 궤적이 터지며 5번의 추가 피해를 줍니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "피해량 50% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "SLASH", 10)
			&& !Skill_Util.Has_Skill(data, "EXPLODE_ARROW", 10))
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
		damage *= Mastery_Ice.Get_Mult(data.getPlayer());

		double distance = cast.getModifier("distance");

		// 무기 효과
		if (Weapon_Identify.Hold_MMO_Sword(data.getPlayer()))
			damage *= 1.5;
		
		data.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Slash_Bomb_Skill(data.getPlayer(), distance, damage));
	}

	// 검술 베어가르기  효과
	class Slash_Bomb_Skill implements Runnable
	{
		Player player;
		double damage;
		double distance;
		List<Entity> entities;
		
		Vector[] vecs;
		Location loc;
		double angle = 200;
		
		public Slash_Bomb_Skill(Player p, double _distance, double _damage)
		{
			player = p;
			distance = _distance;
			damage = _damage;
			loc = player.getEyeLocation();
			
			Make_Vecs();
			Check_Damage_Entity();
		}
		
		public void run()
		{
			// 좀 기다렸다가
			Draw_After_Effect();
		}

		void Make_Vecs()
		{
			int size = (int)distance * 15;
			vecs = new Vector[size * 3];
			for(int i = 0; i < vecs.length; i += 3)
			{
				double _angle = 90.0 + (angle * -0.5) + (i / 3) * (angle / (double)size);
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
				vecs[i + 1] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.925, 0, Math.sin(Math.toRadians(_angle)) * 0.95);
				vecs[i + 2] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.85, 0, Math.sin(Math.toRadians(_angle)) * 0.9);
			}
			vecs = TRS.Scale(vecs, 2.0, 2.0, distance);
			vecs = TRS.Rotate_Z(vecs, -60.0 + Math.random() * 120.0);
			vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch());
			vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw());
		}
		
		void Check_Damage_Entity()
		{
			Location temp_loc = player.getLocation();
			Vector temp_dir = player.getLocation().getDirection();
			
			temp_loc.getWorld().playSound(temp_loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 1f);
			Location hitbox_axis = player.getLocation().add(temp_dir.clone().multiply(distance * 0.5));
			entities = Hitbox.Targets_In_the_Box(hitbox_axis.toVector(),
					new Vector(temp_loc.getPitch(), temp_loc.getYaw(), 0),
					new Vector(4.0, 4.0, distance),
					player.getNearbyEntities(distance, distance, distance));

			for(Entity en : entities)
			{
				en.getWorld().spawnParticle(Particle.FLAME, en.getLocation().add(0, 2, 0), 1, 0d, 0d, 0d, 0d);
			}
		}
		
		double time = 1.5;
		int attackCounter = 5;
		boolean hit = false;
		Particle.DustTransition dtr = new DustTransition(Color.fromRGB(255, 128, 128), Color.BLACK, 2.0f);
		void Draw_After_Effect()
		{
			if (time >= 1.5)
				for (int i = 0; i < vecs.length; i++)
				{
					Location temp_loc = loc.clone().add(vecs[i]);
					player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, temp_loc, 1, 0d, 0d, 0d, 0d, dtr);
				}
			if (!hit)
			{
				hit = true;
				for(Entity en : entities)
				{
					if (!(en instanceof LivingEntity))
						continue;
					
					Draw_Entity_Effect((LivingEntity)en);
	
					if (Damage.Is_Possible(player, en))
					{
						Damage.Attack(player, (LivingEntity)en, damage, DamageType.WEAPON, DamageType.SKILL);
					}
				}
			}
			
			if (time > -2.0)
			{
				time -= 0.1;
				if (time <= 0.0)
				{
					if (attackCounter-- <= 0)
						return;
					int t_c = 0;
					loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2f, 1.5f);
					for (int i = 0; i < vecs.length; i += 3)
					{
						if (t_c++ % 6 != 0)
							continue;
						Location temp_loc = loc.clone().add(vecs[i + 1]);
						if (t_c++ % 12 != 0)
							player.getWorld().spawnParticle(Particle.FLASH, temp_loc, 1, 0d, 0d, 0d, 2.0d);
						player.getWorld().spawnParticle(Particle.CRIT, temp_loc, 50, 0d, 0d, 0d, 2.0d);
					}
					for (Entity en : entities)
					{
						if (!(en instanceof LivingEntity))
							continue;
						if (Damage.Is_Possible(player, en))
						{
							LivingEntity le = (LivingEntity)en;
							Damage.Attack(player, le, damage, DamageType.WEAPON, DamageType.SKILL);
							le.setNoDamageTicks(0);
						}
					}
					Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 5);
				}
				else
					Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
			}
		}
		void Draw_Entity_Effect(LivingEntity _target)
		{
			Random rand = new Random();
			
			for(int count = 0; count < 2; count++)
			{
				Vector rand_vec = new Vector(-1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0).normalize();
				Vector from = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(-3.0));
				Vector to = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(3.0));
		
				Location loc = from.toLocation(_target.getWorld());
				for(double i = 0.0; i < from.distance(to); i += 0.2)
				{
					loc.add(rand_vec.clone().multiply(0.1));
					loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0d, 0d, 0d, 0d, new Particle.DustOptions(Color.WHITE, 2));
				}
			}
			
			Location loc = _target.getEyeLocation();
			loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2f, 1.5f);
			loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0d, 0d, 0d, 0d);
			
		}
	}
}