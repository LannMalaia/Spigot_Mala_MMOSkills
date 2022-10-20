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

import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Fire;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.LocationSkillMetadata;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;

public class Fire_Pillar extends Skill
{
	public Fire_Pillar()
	{
		super();
		
		setName("파이어 필라");
		setLore(MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&7바라보고 있는 곳에 불기둥을 일으킵니다.",
				"&7장소에 들어온 대상들은 &8{damage}&7 만큼의 피해를 받으며 불탑니다.",
				"", MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.LANTERN);

		addModifier("damage", new LinearValue(8, 1.5));
		addModifier("cooldown", new LinearValue(35, -0.25));
		addModifier("mana", new LinearValue(50, 2));
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		LocationSkillMetadata cast = new LocationSkillMetadata(data, skill, 50);
		
		if (!cast.isSuccessful())
			return cast;
				
		double damage = cast.getModifier("damage");
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());

		DamageMetadata ar = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new FireMagicEvent(data.getPlayer(), ar));
		damage = ar.getDamage();
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Fire_Pillar_Task(data.getPlayer(), cast.getHit().add(0.5, 0.5, 0.5), damage));

		return cast;
	}

}

class Fire_Pillar_Task implements Runnable
{
	Player player;
	double damage;
	Location loc;
	double radius = 4.0;
	double height = 20.0;
	
	double timer = 10.0;
	int counter = 0;
	double y_offset = 0.0;
	double angle_offset = 0.0;
	World world;
	
	public Fire_Pillar_Task(Player _player, Location _loc, double _damage)
	{
		player = _player;
		loc = _loc;
		damage = _damage;
		world = loc.getWorld();
	}
	
	public void run()
	{
		timer -= 0.05;
		angle_offset -= 25;
		y_offset = (y_offset + 0.5) % 5.0;
		if (timer < 0.0)
			return;
		
		Location temp_loc = loc.clone();
		double angle = angle_offset;
		for(double temp_y = 0.0; temp_y < height; temp_y += 0.125)
		{
			angle += 6.0;
			temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
			temp_loc.setY(loc.getY() + temp_y);
			temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
			world.spawnParticle(Particle.FLAME, temp_loc, 1, 0, 0, 0, 0);
		}
		
		if(--counter <= 0)
		{
			world.playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 0.3f, 0.2f);
			counter = 4;
		}
		world.spawnParticle(Particle.LAVA, loc.clone().add(0, height * 0.5, 0), 45, radius * 0.5, height * 0.5, radius * 0.5, 0);
		
		if (counter % 2 == 0)
		{
			for(Entity e : world.getNearbyEntities(loc, radius, height, radius))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e == player)
					continue;
				if(loc.distance(e.getLocation()) > radius)
					continue;
				
				LivingEntity target = (LivingEntity)e;
				target.setNoDamageTicks(0);
				Damage.Attack(player, target, damage, DamageType.MAGIC, DamageType.SKILL);
				target.setFireTicks(100);
			}
		}
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}