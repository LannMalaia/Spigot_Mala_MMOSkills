package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.skills.Stance_Change.Stance_Type;
import mala.mmoskill.skills.passive.Mastery_Fire;
// import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;


public class Sword_Circle extends RegisteredSkill
{
	public Sword_Circle()
	{	
		super(new Sword_Circle_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("sec", new LinearValue(2.05, 0.05));
		addModifier("power", new LinearValue(20, 4));
		addModifier("size", new LinearValue(1.5, 0.2));
		addModifier("cooldown", new LinearValue(20, 0));
		addModifier("stamina", new LinearValue(15.5, 0.5));
	}
}

class Sword_Circle_Handler extends MalaSkill implements Listener
{
	public Sword_Circle_Handler()
	{
		super(	"SWORD_CIRCLE",
				"선풍검",
				Material.GLOWSTONE_DUST,
				MsgTBL.NeedSkills,
				"&e 부메랑 칼날 - lv.15",
				"",
				MsgTBL.PROJECTILE + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&e{sec}&7초 동안 유지되는 검기를 생성합니다.",
				"&7검기에 닿은 적들은 계속해서 &8{power}&7의 피해를 받습니다.",
				"",
				"&c[ 버서크 스탠스 ]",
				"&7전방으로 검기를 날려보냅니다.",
				"&7검기는 일정거리 나아가다가 멈춥니다.",
				"",
				"&f[ &b이베이드 스탠스 &f& &7스탠스 해제 &f]",
				"&7시전한 곳을 중심으로 검기를 회전시킵니다.",
				"&7검기가 3배 더 오래 지속됩니다. (최대 15초)",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "범위 30% 감소, 3개 생성",
				MsgTBL.WEAPON_AXE + "피해량 50% 증가",
				MsgTBL.WEAPON_SPEAR + "범위 100% 증가",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
		registerModifiers("power", "size");
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "AERIAL_SLASH", 15))
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
		Stance_Type type = !data.getPlayer().hasMetadata(Stance_Change.meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(data.getPlayer().getMetadata(Stance_Change.meta_name).get(0).asString());
		
		double damage = cast.getModifier("power"); // 공격력
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		double size = (int)cast.getModifier("size");
		double sec = cast.getModifier("sec");
		int attack_count = 1;
		
		// 무기 효과
		if (Weapon_Identify.Hold_MMO_Sword(data.getPlayer()) || Weapon_Identify.Hold_Sword(data.getPlayer()))
		{
			size *= 0.7;
			attack_count = 3;
		}
		if (Weapon_Identify.Hold_MMO_Spear(data.getPlayer()) || Weapon_Identify.Hold_Spear(data.getPlayer()))
			size *= 2.0;
		if (Weapon_Identify.Hold_Axe(data.getPlayer()))
			damage *= 1.5;

		if (type == Stance_Type.BERSERK)
		{
			data.getPlayer().swingMainHand();
			for (int i = 0; i < attack_count; i++)
			{
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new Sword_Circle_Aerial(data, damage, size, sec), 10 * i);
			}
		}
		else
		{
			sec = Math.min(15.0, sec * 3.0);
			data.getPlayer().swingMainHand();
			double angle = 360.0 / attack_count;
			for (int i = 0; i < attack_count; i++)
			{
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new Sword_Circle_Turn(data.getPlayer(), damage, size, angle * i, sec), 0);
			}
		}
	}

	class Sword_Circle_Aerial implements Runnable
	{
		PlayerData player;
		Location pos;
		Vector dir;
		
		List<Entity> entities;
		double damage;
		double size;
		double sec;
		
		double max_speed = 0.6;
		double current_speed = 0.6;
		double decelerate = -0.015;
		Vector[] vecs;
		
		double yaw, pitch;
		
		public Sword_Circle_Aerial(PlayerData p, double _damage, double _size, double _sec)
		{
			player = p;
			damage = _damage;
			size = _size;
			sec = _sec;
			
			max_speed = Math.min(size * 0.4, 1.6);
			current_speed = Math.min(size * 0.4, 1.6);
			decelerate = current_speed * 0.05;
			
			// 피해 보정
			DamageMetadata dm = new DamageMetadata(damage, DamageType.PROJECTILE, DamageType.SKILL, DamageType.MAGIC, DamageType.PHYSICAL);
			Bukkit.getPluginManager().callEvent(new FireMagicEvent(player.getPlayer(), dm));
			damage = dm.getDamage();
			
			Make_Vecs();
			vecs = TRS.Scale(vecs, size, size, size);
			// correct_Zangle = 20.0;
			
			player.getPlayer().getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
		}
		
		void Make_Vecs()
		{
			vecs = new Vector[12];
			vecs[0] = new Vector(0, 0, 0.01);
			vecs[1] = new Vector(0.45, 0, 0.55);
			vecs[2] = new Vector(0, 0, 1.0);
			vecs[3] = new Vector(0, 0, -0.01);
			vecs[4] = new Vector(-0.45, 0, -0.55);
			vecs[5] = new Vector(0, 0, -1.0);
			vecs[6] = new Vector(0.01, 0, 0);
			vecs[7] = new Vector(0.55, 0, -0.45);
			vecs[8] = new Vector(1.0, 0, 0);
			vecs[9] = new Vector(-0.01, 0, 0);
			vecs[10] = new Vector(-0.55, 0, 0.45);
			vecs[11] = new Vector(-1.0, 0, 0);
		}
		
		int count = 0;
		public void run()
		{
			if (dir == null)
			{
				pos = player.getPlayer().getEyeLocation().add(0, -0.3, 0);
				dir = player.getPlayer().getLocation().getDirection();
				yaw = player.getPlayer().getLocation().getYaw();
				pitch = player.getPlayer().getLocation().getPitch();
			}
			
			// 검기
			if (count++ % 4 == 0)
				player.getPlayer().getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f);
			
			// 파티클 그리기
			Vector[] temp_vecs = TRS.Rotate_Y(vecs, -13.0 * count);
			temp_vecs = TRS.Rotate_X(temp_vecs, pitch);
			temp_vecs = TRS.Rotate_Y(temp_vecs, yaw);
			for (int i = 0; i < temp_vecs.length; i += 3)
			{
				Location one = pos.clone().add(temp_vecs[i]);
				Location two = pos.clone().add(temp_vecs[i + 1]);
				Location three = pos.clone().add(temp_vecs[i + 2]);

				Particle_Drawer.Draw_Line(one, two, Particle.ELECTRIC_SPARK, 0.1);
				Particle_Drawer.Draw_Line(two, three, Particle.ELECTRIC_SPARK, 0.1);				
			}
			Particle_Drawer.Draw_Circle(pos, Particle.ELECTRIC_SPARK, size, pitch, yaw);
			
			// 범위 판정
			List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, size * 3, size * 3, size * 3));
			List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(),
					new Vector(pos.getPitch(), pos.getYaw(), 0),
					new Vector(size * 2, 2.5, size * 2),
					abc);
			for(Entity temp : entities)
			{
				if(!(temp instanceof LivingEntity))
					continue;
				if(temp == player.getPlayer())
					continue;
				
				LivingEntity temp2 = (LivingEntity)temp;
				if (temp2.getNoDamageTicks() == 0)
				{
					Damage.Attack(player.getPlayer(), temp2, damage,
							DamageType.PROJECTILE, DamageType.SKILL, DamageType.PHYSICAL);
					temp2.setNoDamageTicks(10);
				}
			}
			
			// 잠시 쉬는 시간
			if(current_speed > 0)
			{
				pos.add(dir.clone().multiply(current_speed));
				current_speed -= decelerate;
			}
			else
			{
				current_speed = 0;
				sec -= 0.05;
			}
			
			if (sec > 0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
		}
	}

	class Sword_Circle_Turn implements Runnable
	{
		Player player;
		Location origin_pos;
		Vector dir;
		
		List<Entity> entities;
		double damage;
		double size;

		Vector[] vecs;
		
		double angle = 0.0;
		double correction_angle = 0.0;
		double duration = 15.0;
		
		public Sword_Circle_Turn(Player p, double _damage, double _size, double _angle, double _duration)
		{
			player = p;
			damage = _damage;
			size = _size;
			angle = _angle;
			duration = _duration;
			
			origin_pos = p.getEyeLocation().add(0, -0.4, 0);
			dir = p.getLocation().getDirection();

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2, 1);
			
			vecs = new Vector[(int) (size * 15)];
			double additive = 360.0 / (size * 15);
			for(int i = 0; i < vecs.length; i++)
			{
				double _temp_angle = additive * i;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_temp_angle)), 0, Math.sin(Math.toRadians(_temp_angle)));
			}
			vecs = TRS.Scale(vecs, size, size, size);
		}
		
		public void run()
		{			
			// 파티클 그리기
			Location pos = origin_pos.clone().add(Math.cos(Math.toRadians(angle)) * (size + 2.5), 0, Math.sin(Math.toRadians(angle)) * (size + 2.5));
			angle += 5.0;
			correction_angle += 3.5;
			Vector[] temp_vecs = TRS.Rotate_Y(vecs, correction_angle);
			temp_vecs = TRS.Rotate_Z(temp_vecs, 0.0);
			temp_vecs = TRS.Rotate_Y(temp_vecs, angle - 90);
			// temp_vecs = TRS.Rotate_X(temp_vecs, pos.getPitch());

			for(int i = 0; i < temp_vecs.length; i++)
			{
				Location loc = pos.clone().add(temp_vecs[i]);
				player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 1, 0d, 0d, 0d, 0d);//, dop);//, new Particle.DustOptions(Color.fromRGB(255, 200, 255), 0.1f));
			}
			
			// 범위 판정
			double range = Math.max(8, size * 2.5);
			List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, range, range, range));
			abc.remove(player);
			List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(),
					new Vector(0, 0, 0),
					new Vector(size * 2, 2.5, size * 2),
					abc);
			for(Entity temp : entities)
			{
				if(!(temp instanceof LivingEntity))
					continue;
				if(temp == player)
					continue;
				
				LivingEntity temp2 = (LivingEntity)temp;
				if (temp2.getNoDamageTicks() == 0)
				{
					Damage.Attack(player, temp2, damage,
							DamageType.PROJECTILE, DamageType.PHYSICAL, DamageType.SKILL);

					EntityDamageEvent ede = new EntityDamageByEntityEvent(player, temp2, DamageCause.ENTITY_ATTACK, 1);
					Bukkit.getPluginManager().callEvent(ede);
					if (!ede.isCancelled())					
					{
						temp2.getWorld().spawnParticle(Particle.SWEEP_ATTACK, temp2.getEyeLocation(), 1, 0, 0, 0, 0);
						temp2.getWorld().playSound(temp2.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.3f);
						Vector dir = temp2.getLocation().subtract(player.getLocation()).toVector().normalize();
						dir.setY(dir.getY() + 0.1);
						dir.multiply(1.5);
						temp2.setVelocity(dir);
					}
				}
			}
			
			// 잠시 쉬는 시간
			duration -= 0.05;
			if(duration > 0.0)
			{
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
			}
		}
	}

}
