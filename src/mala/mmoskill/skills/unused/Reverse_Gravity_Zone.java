package mala.mmoskill.skills.unused;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.LocationSkillMetadata;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Reverse_Gravity_Zone extends Skill
{
	public Reverse_Gravity_Zone()
	{
		super();
		
		setName("리버스 그라비티 존");
		setLore("&7바라보고 있는 곳의 반경 {radius} 블럭의 중력을 뒤집습니다.", "장소에 들어온 대상들은 계속해서 치솟아 오릅니다.", "", MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.BUBBLE_CORAL);

		addModifier("radius", new LinearValue(4, 0.3));
		addModifier("cooldown", new LinearValue(60, 0)); 
		addModifier("mana", new LinearValue(35, 2.75));
		
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		LocationSkillMetadata cast = new LocationSkillMetadata(data, skill, 50);
		
		if (!cast.isSuccessful())
			return cast;
				
		double radius = cast.getModifier("radius");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Reverse_Gravity_Zone_Task(data.getPlayer(), cast.getHit().add(0, 1.1, 0), radius));

		return cast;
	}

}

class Reverse_Gravity_Zone_Task implements Runnable
{
	Player player;
	Location loc;
	double radius = 6.0;
	
	double timer = 10.0;
	long interval = 6;
	int counter = 0;
	World world;
	int height = 0;
	
	public Reverse_Gravity_Zone_Task(Player _player, Location _loc, double _radius)
	{
		player = _player;
		loc = _loc;
		radius = _radius;
		world = loc.getWorld();

		int i = 0;
		for(i = loc.getBlockY() + 1; i <= 256; i++)
		{
			if(!world.getBlockAt(loc.getBlockX(), i, loc.getBlockZ()).isPassable())
				break;
		}
		height = i - loc.getBlockY();
	}
	
	public void run()
	{
		timer -= interval / 20.0;
		// interval = Math.max(1, interval / 2);
		
		if(timer > 0.0) // 타이머가 남았으면 위로 올리기
		{
			Location temp_loc = loc.clone();
			for(double angle = 0; angle < 360.0; angle += 360.0 / 128.0)
			{
				temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
				temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
				world.spawnParticle(Particle.SPELL_WITCH, temp_loc, 1, 0, 0, 0, 0);
			}
			for(double angle = 0; angle < 360.0; angle += 72)
			{
				Location start = new Location(world, loc.getX() + Math.cos(Math.toRadians(angle - 120)) * radius,
						loc.getY(),
						loc.getZ() + Math.sin(Math.toRadians(angle - 120)) * radius);
				Location end = new Location(world, loc.getX() + Math.cos(Math.toRadians(angle)) * radius,
						loc.getY(),
						loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
				Location lerped = start.clone();
				for(double i = 0; i < 1.0; i += 0.025)
				{
					lerped.setX(start.getX() + (end.getX() - start.getX()) * i);
					lerped.setZ(start.getZ() + (end.getZ() - start.getZ()) * i);
					world.spawnParticle(Particle.SPELL_WITCH, lerped, 1, 0, 0, 0, 0);
				}
			}
			
			if(--counter <= 0)
			{
				world.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.3f, 0.2f);
				counter = 4;
			}
			world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1.5f, 1.5f);
			
			for(Entity e : world.getNearbyEntities(loc, radius, height, radius))
			{
				if(!(e instanceof LivingEntity))
					continue;
				Location org = loc.clone();
				org.setY(0);
				Location trg = e.getLocation().clone();
				trg.setY(0);

				if(org.distance(trg) > radius)
					continue;
				
				LivingEntity target = (LivingEntity)e;
				target.setVelocity(target.getVelocity().add(new Vector(0, 0.8, 0)));
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, interval);
		}
	}
}



