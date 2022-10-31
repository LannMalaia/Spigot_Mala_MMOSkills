package mala.mmoskill.skills;

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

import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
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
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Burning_Zone extends RegisteredSkill
{
	public Burning_Zone()
	{	
		super(new Burning_Zone_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(14, 4));
		addModifier("radius", new LinearValue(4, 0.3));
		addModifier("cooldown", new LinearValue(25, 0));
		addModifier("mana", new LinearValue(22, 2));
	}
}

class Burning_Zone_Handler extends MalaLocationSkill implements Listener
{
	public Burning_Zone_Handler()
	{
		super(	"BURNING_ZONE",
				"버닝 존",
				Material.LANTERN,
				MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&7바라보고 있는 곳의 반경 {radius} 블럭을 불바다로 만듭니다.",
				"장소에 들어온 대상들은 &8{damage}&7 만큼의 피해를 받으며 불탑니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}
	
	@Override
	public void whenCast(LocationSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("damage");
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		
		double radius = cast.getModifier("radius");

		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Burning_Zone_Task(data.getPlayer(), _data.getTarget().add(0, 1.5, 0), damage, radius));

	}

}

class Burning_Zone_Task implements Runnable
{
	Player player;
	double damage;
	Location loc;
	double radius = 6.0;
	
	double timer = 10.0;
	long interval = 6;
	int counter = 0;
	World world;
	
	public Burning_Zone_Task(Player _player, Location _loc, double _damage, double _radius)
	{
		player = _player;
		loc = _loc;
		damage = _damage;
		radius = _radius;
		world = loc.getWorld();
		

		DamageMetadata dm = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new FireMagicEvent(player, dm));
		damage = dm.getDamage();
		
	}
	
	public void run()
	{
		timer -= interval / 20.0;
		// interval = Math.max(1, interval / 2);
		
		if(timer > 0.0) // 타이머가 남았으면 불타기
		{
			Location temp_loc = loc.clone();
			for(double angle = 0; angle < 360.0; angle += 360.0 / 128.0)
			{
				temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
				temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
				world.spawnParticle(Particle.FLAME, temp_loc, 1, 0, 0, 0, 0);
			}
			for(double angle = 0; angle < 360.0; angle += 60)
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
					world.spawnParticle(Particle.FLAME, lerped, 1, 0, 0, 0, 0);
				}
			}
			for(double angle = 0; angle < 360.0; angle += 360.0 / 128.0)
			{
				temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
				temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
				world.spawnParticle(Particle.FLAME, temp_loc, 1, 0, 0, 0, 0);
			}
			
			if(--counter <= 0)
			{
				world.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.3f, 0.2f);
				counter = 4;
			}
			world.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1.5f, 1.5f);
			world.spawnParticle(Particle.LAVA, loc, 15, radius * 0.5, 0, radius * 0.5, 0);
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, interval);
			

			for(Entity e : world.getNearbyEntities(loc, radius, 4, radius))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e == player)
					continue;
				if(loc.distance(e.getLocation()) > radius)
					continue;
				
				LivingEntity target = (LivingEntity)e;
				if (target.getNoDamageTicks() == 0)
				{
					Damage.Attack(player, target, damage,
							DamageType.SKILL, DamageType.MAGIC);
				}
				target.setFireTicks(100);
			}
		}
	}
}


