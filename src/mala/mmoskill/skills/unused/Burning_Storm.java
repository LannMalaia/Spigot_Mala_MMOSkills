package mala.mmoskill.skills.unused;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mala.mmoskill.skills.passive.Mastery_Fire;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.skill.metadata.LocationSkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Burning_Storm extends Skill
{
	public Burning_Storm()
	{
		super();
		
		setName("버닝 스톰");
		setLore(MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&7상공에  &8{count}&7개의 플레어 디스크를 소환합니다.",
				"&7디스크들은 목표 지점을 향해 빠르게 떨어집니다.",
				"&7디스크는 총 &8{count}&7개 생성됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.SUNFLOWER);

		addModifier("damage", new LinearValue(25, 5));
		addModifier("count", new LinearValue(30, 3));
		
		addModifier("cooldown", new LinearValue(120, 0));
		//addModifier("cooldown", new LinearValue(120, -3));
		addModifier("mana", new LinearValue(120, 10));
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		LocationSkillMetadata cast = new LocationSkillMetadata(data, skill, 40);
		
		if (!cast.isSuccessful())
			return cast;
		
		double damage = cast.getModifier("damage");
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		Vector dir = data.getPlayer().getLocation().getDirection();
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ITEM_TOTEM_USE, 2, 1.5f);
		
		int count = (int)cast.getModifier("count");
		double radius = 12.0;
		Location axis = cast.getHit().clone().add(0.5, 1.1, 0.5);
		Draw_Call_Particle(data.getPlayer());
		Meteor_Skill meteor = new Meteor_Skill(axis, data.getPlayer(), damage, radius, count);
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, meteor, 30);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Meteor_Zone(axis, radius));
		return cast;
	}
	
	void Draw_Call_Particle(Player player)
	{
		Location pos = player.getLocation().add(0, 2, 0);
		for(double y = pos.getY(); y <= 255.0; y += 0.1)
		{
			pos.add(0, 0.1, 0);
			pos.getWorld().spawnParticle(Particle.END_ROD, pos, 1, 0, 0, 0, 0);
		}
	}
}

class Meteor_Zone implements Runnable
{
	Location pos;
	double radius;
	double time = 3.0f;
	
	public Meteor_Zone(Location _pos, double _radius)
	{
		pos = _pos;
		radius = _radius;
	}
	
	public void run()
	{
		World world = pos.getWorld();
		DustOptions dop = new Particle.DustOptions(Color.fromRGB(255, 80, 80), 2f);
		for(double angle = 0.0; angle <= 360.0; angle += 30.0 / radius)
		{
			Location temp_pos = pos.clone().add(Math.cos(Math.toRadians(angle)) * radius, 0, Math.sin(Math.toRadians(angle)) * radius);
			world.spawnParticle(Particle.REDSTONE, temp_pos, 1, 0, 0, 0, 0, dop);
		}
		
		if(time > 0)
		{
			time -= 0.1;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
		}
	}
}

class Meteor_Skill implements Runnable
{
	Player player;
	double damage;
	double radius;
	double speed = 0.5;

	int max_count = 30;
	int count = 0;
	int delay = 0;
	
	Location start_loc, end_loc;
	Vector dir;

	Vector[] vecs;
	Vector[] trace_vecs;
	
	public Meteor_Skill(Location _end_pos, Player _player, double _damage, double _radius, int _max_count)
	{
		end_loc = _end_pos;
		player = _player;
		damage = _damage;
		radius = _radius;
		max_count = _max_count;
		delay = max_count > 50 ? max_count > 100 ? 1 : 2 : 3;
		
		dir = player.getLocation().subtract(end_loc).toVector().normalize();
		dir.setY(0);

		start_loc = end_loc.clone().add(dir.clone().multiply(50.0)).add(0, 50d, 0);
		start_loc.setYaw(player.getLocation().getYaw());
		dir = end_loc.clone().subtract(start_loc).toVector().normalize();
	}
	
	public void run()
	{
		if(count++ >= max_count)
			return;
		
		Location disc_loc = start_loc.clone().add(-radius + Math.random() * radius * 2.0,
				-radius + Math.random() * radius * 2.0,
				-radius + Math.random() * radius * 2.0);
		disc_loc.setPitch(45f);
		disc_loc.setYaw(start_loc.getYaw());
		// Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Flare_Disc_Disc(disc_loc, player, dir, damage, 2, 100, Math.random() * 360.0));
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, delay);
	}
}
