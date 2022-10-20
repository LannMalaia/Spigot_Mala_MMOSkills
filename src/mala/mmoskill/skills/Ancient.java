package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.skills.passive.Mastery_Ice;
import mala.mmoskill.skills.passive.Mastery_Lightning;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Ancient extends RegisteredSkill
{
	public static Ancient skill;
	public Ancient()
	{	
		super(new Ancient_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(44, 4));
		addModifier("radius", new LinearValue(15, 0));
		//addModifier("cooldown", new LinearValue(2, 0));
		//addModifier("cooldown", new LinearValue(120, -2));
		addModifier("cooldown", new LinearValue(120, 0));
		addModifier("mana", new LinearValue(300, 20));
		
		skill = this;
	}
}

class Ancient_Handler extends MalaLocationSkill implements Listener
{
	public Ancient_Handler()
	{
		super(	"ANCIENT",
				"에인션트",
				Material.KNOWLEDGE_BOOK,
				MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC,
				"",
				"&7지정한 위치에 화염, 전격, 빙결 마법을 순서대로 영창합니다.",
				"&7각 마법의 피해는 아래 스킬의 영향을 받습니다.",
				"&c&l화염 &7- &c&l헬파이어&7의 레벨 * &c{damage}",
				"&e&l전격 &7- &e&l플라즈마 블래스트&7의 레벨 * &e{damage}",
				"&b&l빙결 &7- &b&l퍼펙트 프리즈&7의 레벨 * &b{damage}",
				"&c각 마법에 맞춰 마스터리 보정이 적용됩니다.",
				"", MsgTBL.Cooldown_Fixed, MsgTBL.ManaCost);
		registerModifiers("damage", "radius");
	}

	@Override
	public void whenCast(LocationSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Location loc = _data.getTarget();
				
		double damage = cast.getModifier("damage");
		double radius = cast.getModifier("radius");
		
		CooldownFixer.Fix_Cooldown(data, Ancient.skill);
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Ancient_Skill(data.getPlayer(), loc.clone().add(0, 1, 0), damage, radius));
	}
}

class Ancient_Skill implements Runnable
{
	Player player;
	double damage;
	Location fixed_loc, memorized_loc;
	Location cast_loc;
	
	double radius = 5.0;
	
	public Ancient_Skill(Player _player, Location _cast_loc, double _damage, double _radius)
	{
		player = _player;
		cast_loc = _cast_loc;
		memorized_loc = _player.getLocation();
		fixed_loc = _player.getLocation().add(0.0, 1.0, 0.0).add(_player.getLocation().getDirection().multiply(2.0));
		damage = _damage;
		radius = _radius;
		
		Make_Crystal_Vec();
		
		cast_loc.getWorld().playSound(cast_loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2.0f);
	}
	
	// max ~1
	void Draw_Magic_Circle(double _radius, boolean _reverse, double _progress)
	{
		double target_angle = 360.0 * _progress;
		for (double angle = 0.0; angle <= target_angle; angle += 360.0 / 240.0)
		{
			double temp_angle = angle * (_reverse ? -1.0 : 1.0);
			double rad = Math.toRadians(temp_angle);
			Vector vec = new Vector(Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
			Location loc = cast_loc.clone().add(vec);
			loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
		}
	}
	
	void Draw_Line(Location _src, Location _dst, double _progress, double _precision, Particle _particle)
	{
		Vector gap = _dst.clone().subtract(_src).toVector();
		for (double d = 0.0; d <= _progress; d += _precision)
		{
			Location loc = _src.clone().add(gap.clone().multiply(d));
			loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	
	// max ~3
	void Draw_HexaStar(double _radius, double _progress)
	{
		Location[] star_points = new Location[8];
		double rad = Math.toRadians(0);
		star_points[0] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[0].add(cast_loc);
		rad = Math.toRadians(120);
		star_points[1] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[1].add(cast_loc);
		rad = Math.toRadians(240);
		star_points[2] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[2].add(cast_loc);
		rad = Math.toRadians(0);
		star_points[3] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[3].add(cast_loc);
		
		rad = Math.toRadians(60);
		star_points[4] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[4].add(cast_loc);
		rad = Math.toRadians(180);
		star_points[5] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[5].add(cast_loc);
		rad = Math.toRadians(300);
		star_points[6] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[6].add(cast_loc);
		rad = Math.toRadians(60);
		star_points[7] = new Location(player.getWorld(), Math.cos(rad) * _radius, 0.5, Math.sin(rad) * _radius);
		star_points[7].add(cast_loc);

		Draw_Line(star_points[0], star_points[1], Math.min(1.0, _progress), 0.015, Particle.END_ROD);
		Draw_Line(star_points[4], star_points[5], Math.min(1.0, _progress), 0.015, Particle.END_ROD);
		if (_progress > 1.0)
		{
			Draw_Line(star_points[1], star_points[2], Math.min(1.0, _progress - 1.0), 0.015, Particle.END_ROD);
			Draw_Line(star_points[5], star_points[6], Math.min(1.0, _progress - 1.0), 0.015, Particle.END_ROD);
		}
		if (_progress > 2.0)
		{
			Draw_Line(star_points[2], star_points[3], Math.min(1.0, _progress - 2.0), 0.015, Particle.END_ROD);
			Draw_Line(star_points[6], star_points[7], Math.min(1.0, _progress - 2.0), 0.015, Particle.END_ROD);
		}
	}
	
	double m_Progress = 0.0;
	int m_TimerCount = 0;
	public void run()
	{
		m_TimerCount += 1;
		m_Progress += 0.05;

		if(m_Progress < 16.5)
		{
			Draw_Magic_Circle(radius, false, Math.min(1.0, m_Progress * 0.5));
			Draw_Magic_Circle(radius + 0.75, true, Math.min(1.0, m_Progress * 0.5));
			if (m_Progress > 2.0)
				Draw_HexaStar(radius, Math.min(3.0, m_Progress - 2.0));
		}

		switch(m_TimerCount)
		{
		case 20:
		case 40:
		case 60:
		case 80:
		case 100:
			cast_loc.getWorld().playSound(cast_loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
			break;
		}
		
		if (m_Progress > 3.5 && m_Progress < 8.0)
		{
			if (m_TimerCount % 4 == 0)
				Fall_Disc();
		}
		if (m_Progress > 10.0 && m_Progress < 15.0)
		{
			if (m_TimerCount % 10 == 0)
			{
				Fall_Thunder();
				Fall_Thunder();
				Fall_Thunder();
			}
		}
		if (m_Progress > 15.0 && m_Progress < 16.5)
		{
			if (m_TimerCount % 1 == 0)
			{
				Rise_Crystal();
				Rise_Crystal();
				Rise_Crystal();
				Rise_Crystal();
			}
		}
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(m_Progress > 17.0)
			return;
					
		// 마무리
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	
	
	void Fall_Disc()
	{
		// 피해량 계산
		double dmg = damage;
		PlayerData data = PlayerData.get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("HELLFIRE");
		int level = data.getSkillLevel(skill);
		if (data.getProfess().hasSkill(skill))
			dmg *= level;
		dmg *= Mastery_Fire.Get_Mult(player);
			
		// 떨어질 위치 지정
		Location dst = cast_loc.clone();
		double rad = Math.toRadians(Math.random() * 360.0);
		double temp_radius = Math.random() * (radius - 2.0);
		dst.add(Math.cos(rad) * temp_radius, 0, Math.sin(rad) * temp_radius);
		
		// 시작 위치 및 디스크 각도 지정
		Location src = dst.clone();
		double angle = Math.random() * 360.0;
		rad = Math.toRadians(angle);
		src.add(0, 15, 0);
		src.setPitch(-45f);
		src.setYaw((float) angle);
		
		// 떨어질 속도 지정
		Vector dir = new Vector(0, -1, 0);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Small_Meteor(src, player, dir, dmg, 2, 100, Math.random() * 360.0));
	}
	
	Location before_thunder_loc;
	void Fall_Thunder()
	{
		// 피해량 계산
		double dmg = damage;
		PlayerData data = PlayerData.get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTER_SPARK_ONCE");
		int level = data.getSkillLevel(skill);
		if (data.getProfess().hasSkill(skill))
			dmg *= level;
		dmg *= Mastery_Lightning.Get_Mult(player);
		
		Location dst = cast_loc.clone();
		while(true)
		{
			dst = cast_loc.clone();
			double rad = Math.toRadians(Math.random() * 360.0);
			double temp_radius = Math.random() * (radius - 2.0);
			dst.add(Math.cos(rad) * temp_radius, 0, Math.sin(rad) * temp_radius);
	
			if (before_thunder_loc == null)
			{
				before_thunder_loc = dst;
				break;
			}
			else
			{
				if (before_thunder_loc.distance(dst) > radius * 0.6)
				{
					before_thunder_loc = dst;
					break;
				}
			}
		}
		
		World world = dst.getWorld();
		Location temp_loc = dst.clone();
		double temp_rad = radius * 5.0;
		double dmg_rad = 5.0;
		for(int i = 0; i < 5; i++)
		{
			temp_loc.setX(dst.getX() + (-temp_rad + Math.random() * temp_rad * 2.0));
			temp_loc.setY(256);
			temp_loc.setZ(dst.getZ() + (-temp_rad + Math.random() * temp_rad * 2.0));
			Lightning_Bolt.Draw_Lightning_Line(temp_loc, dst, Particle.CRIT);
		}
		temp_loc.setX(dst.getX() + (-temp_rad + Math.random() * temp_rad * 2.0));
		temp_loc.setY(256);
		temp_loc.setZ(dst.getZ() + (-temp_rad + Math.random() * temp_rad * 2.0));
		Lightning_Bolt.Draw_Lightning_Line(temp_loc, dst, Particle.SOUL_FIRE_FLAME);

		// 피해
		for(Entity e : world.getNearbyEntities(dst, dmg_rad, dmg_rad, dmg_rad))
		{
			if(!(e instanceof LivingEntity))
				continue;
			if(e == player)
				continue;
			
			LivingEntity target = (LivingEntity)e;
			Damage.Attack(player, target, dmg,
					DamageType.MAGIC, DamageType.SKILL);
		}
		
		world.playSound(dst, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
		world.playSound(dst, Sound.ITEM_TOTEM_USE, 2f, 1.1f);
		world.playSound(dst, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 1.1f);
		
		world.spawnParticle(Particle.EXPLOSION_HUGE, dst, 1, 0, 0, 0, 0);
		// world.spawnParticle(Particle.EXPLOSION_LARGE, dst, 30, 3, 3, 3, 0);
		world.spawnParticle(Particle.LAVA, dst, 60, 0.3, 0.3, 0.3, 0);
	}

	Vector[] vecs;
	void Make_Crystal_Vec()
	{
		double width = 2.0;
		double height = 8.0;
		
		vecs = new Vector[16];
		ArrayList<Vector> temp_vec = new ArrayList<Vector>();
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(0, width, 0));
		
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(width, 0, 0));
		
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(0, -width, 0));
		
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(-width, 0, 0));
		
		temp_vec.add(new Vector(0, width, 0));
		temp_vec.add(new Vector(width, 0, 0));

		temp_vec.add(new Vector(width, 0, 0));
		temp_vec.add(new Vector(0, -width, 0));
		
		temp_vec.add(new Vector(0, -width, 0));
		temp_vec.add(new Vector(-width, 0, 0));
		
		temp_vec.add(new Vector(-width, 0, 0));
		temp_vec.add(new Vector(0, width, 0));
		
		vecs = new Vector[temp_vec.size()];
		for (int i = 0; i < temp_vec.size(); i++)
			vecs[i] = temp_vec.get(i);
	}

	Location before_crystal_loc;
	void Rise_Crystal()
	{
		// 피해량 계산
		double dmg = damage;
		PlayerData data = PlayerData.get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("PERFECT_FREEZE");
		int level = data.getSkillLevel(skill);
		if (data.getProfess().hasSkill(skill))
			dmg *= level;
		dmg *= Mastery_Ice.Get_Mult(player);
		
		Location dst = cast_loc.clone();
		while(true)
		{
			dst = cast_loc.clone();
			double rad = Math.toRadians(Math.random() * 360.0);
			double temp_radius = Math.random() * radius;
			dst.add(Math.cos(rad) * temp_radius, -2, Math.sin(rad) * temp_radius);

			if (before_crystal_loc == null)
			{
				before_crystal_loc = dst;
				break;
			}
			else
			{
				if (before_crystal_loc.distance(dst) > radius * 0.35)
				{
					before_crystal_loc = dst;
					break;
				}
			}
		}

		double rand_range_pitch = 30d;
		double rand_pitch = -90 + -rand_range_pitch + Math.random() * rand_range_pitch * 2.0;

		double rand_range_yaw = 360d;
		double rand_yaw = -rand_range_yaw + Math.random() * rand_range_yaw * 2.0;
		Vector[] temp_vecs;
		temp_vecs = TRS.Rotate_X(vecs, rand_pitch);
		temp_vecs = TRS.Rotate_Y(temp_vecs, rand_yaw);
		
		for(int i = 0; i < temp_vecs.length; i += 2)
		{
			Location start_loc = dst.clone().add(temp_vecs[i]);
			Location end_loc = dst.clone().add(temp_vecs[i + 1]);
			Location lerped = start_loc.clone();
			double add = 0.2d / start_loc.distance(end_loc);
			for(double j = 0; j < 1.0; j += add)
			{
				lerped.setX(start_loc.getX() + (end_loc.getX() - start_loc.getX()) * j);
				lerped.setY(start_loc.getY() + (end_loc.getY() - start_loc.getY()) * j);
				lerped.setZ(start_loc.getZ() + (end_loc.getZ() - start_loc.getZ()) * j);
				dst.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, lerped, 1, 0, 0, 0, 0);
				dst.getWorld().spawnParticle(Particle.SNOWBALL, lerped, 1, 0, 0, 0, 0);
			}
		}
		

		// 피해
		for(Entity e : dst.getWorld().getNearbyEntities(dst, 5, 10, 5))
		{
			if(!(e instanceof LivingEntity))
				continue;
			if(e == player)
				continue;
			
			LivingEntity target = (LivingEntity)e;
			Damage.Attack(player, target, dmg,
					DamageType.MAGIC, DamageType.SKILL);
		}
		
		
		dst.getWorld().playSound(fixed_loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
		dst.getWorld().playSound(fixed_loc, Sound.BLOCK_GLASS_BREAK, 2.0f, 1.0f);
	}
	
}


class Small_Meteor implements Runnable
{
	Player player;
	double damage;
	double max_distance;
	double speed;
	Location start_loc;
	Vector dir;

	double current_distance = 0;
	Location before_loc, current_loc;
	
	public Small_Meteor(Location _start_loc, Player _player, Vector _dir, double _damage, double _speed, double _max_distance, double _roll)
	{
		start_loc = _start_loc;
		player = _player;
		dir = _dir;
		damage = _damage;
		speed = _speed;
		max_distance = _max_distance;

		current_loc = start_loc.clone();
		before_loc = start_loc.clone();
	}
	
	public void run()
	{
		current_distance += speed;
		if(max_distance < current_distance)
			speed = max_distance - current_distance;
		current_loc.add(dir.clone().multiply(speed));
		
		// 라인 그리기
		Vector gap = current_loc.clone().subtract(before_loc).toVector();
		if(gap.length() <= 0.01)
			return;

		current_loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, current_loc, 1, 0, 0, 0, 0);
		current_loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, current_loc, 1, 0, 0, 0, 0);

		
		// 주변 적 찾기
		boolean detected = false;
		for(double i = 0; i <= gap.length(); i += 1)
		{
			Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
			if(loc.getBlock().getType().isSolid())
				detected = true;
			else
			{
				for(Entity e : loc.getWorld().getNearbyEntities(loc, 1, 1, 1))
				{
					if(!(e instanceof LivingEntity))
						continue;
					if(e == player)
						continue;
					
					// 찾은 경우
					detected = true;
					break;
				}
			}
			if(detected)
			{
				current_loc = loc;
				break;
			}
		}
		
		if(detected)
		{
			for(Entity e : current_loc.getWorld().getNearbyEntities(current_loc, 4, 4, 4))
			{
				if (!(e instanceof LivingEntity))
					continue;
				if (e.isDead())
					continue;
				if (e == player)
					continue;
				
				LivingEntity target = (LivingEntity)e;

				Damage.Attack(player, target, damage,
						DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
				target.setFireTicks(100);
			}

			current_loc.getWorld().playSound(current_loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
			current_loc.getWorld().playSound(current_loc, Sound.ITEM_TOTEM_USE, 2, 1);
			// current_loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, current_loc, 50, 3, 3, 3, 0);
			current_loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, current_loc, 45, 3, 3, 3, 0);
			current_loc.getWorld().spawnParticle(Particle.LAVA, current_loc, 70, 3, 3, 3, 0);
			current_loc.getWorld().spawnParticle(Particle.FLAME, current_loc, 70, 3, 3, 3, 0);
			return;
		}
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(current_distance >= max_distance)
			return;
					
		// 마무리
		before_loc = current_loc.clone();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}

















