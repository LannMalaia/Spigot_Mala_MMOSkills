package mala.mmoskill.skills.unused;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.skill.metadata.TargetSkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;

public class Turn_Undead extends Skill implements Listener
{
	public Turn_Undead()
	{
		super();
		
		setName("턴 언데드");
		setLore("&8{distance}&7 거리 내 언데드 적에게 &8{damage}&7의 피해를 줍니다.", "&7언데드가 아닌 적에게는 피해를 주지 못합니다.", "", MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.BONE);

		addModifier("distance", new LinearValue(15, 0.5, 15, 30));
		addModifier("damage", new LinearValue(20, 10, 20, 200));
		addModifier("cooldown", new LinearValue(8, 0));
		addModifier("mana", new LinearValue(18, 0.3));
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		TargetSkillMetadata cast = new TargetSkillMetadata(data, skill, 99);
		
		if (!cast.isSuccessful())
			return cast;
		
		if(data.getPlayer().getEyeLocation().distance(cast.getTarget().getEyeLocation()) > cast.getModifier("distance"))
		{
			cast.abort();
			return cast;
		}
		
		double damage = cast.getModifier("damage");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new TurnUndeadEffect(data.getPlayer(), cast.getTarget(), 10, damage));
		
		return cast;
	}
}

class TurnUndeadEffect implements Runnable
{
	Player player;
	int current_angle = 0;
	int additive_angle = 0;
	double current_distance = 2.0;
	double additive_distance = 0.0;
	int time = 0;
	double damage;
	LivingEntity target;
	
	public TurnUndeadEffect(Player _player, LivingEntity _target, int _time, double _damage)
	{
		player = _player;
		target = _target;
		time = _time;
		damage = _damage;
		additive_angle = 90 / time;
		additive_distance = current_distance / time;
	}
	
	public void run()
	{
		for(int i = 0; i < 4; i++)
		{
			Location loc = target.getLocation();

			double x = Math.cos(Math.toRadians(i * 90.0 + current_angle)) * current_distance;
			double z = Math.sin(Math.toRadians(i * 90.0 + current_angle)) * current_distance;
			for(double y = 0; y <= target.getHeight(); y += target.getHeight() * 0.5)
				player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, y, z), 1, 0, 0, 0, 0);
			x = Math.cos(Math.toRadians(i * 90.0 - current_angle)) * current_distance;
			z = Math.sin(Math.toRadians(i * 90.0 - current_angle)) * current_distance;
			for(double y = 0; y <= target.getHeight(); y += target.getHeight() * 0.5)
				player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, y, z), 1, 0, 0, 0, 0);
		}
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, (float) (2.0 - time * 0.1));
		current_angle += additive_angle;
		current_distance -= additive_distance;
		if(time-- > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
		else
		{
			if(MythicLib.plugin.getVersion().getWrapper().isUndead(target))
			{
				Location loc = target.getLocation().add(0, target.getHeight() * 0.5, 0);
				target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 2);
				target.getWorld().playSound(target.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 1);
				player.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
				player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, (int) (target.getHeight() * 20), target.getHeight(), target.getHeight(), target.getHeight(), 0);
				Damage.Attack(player, target, damage, DamageType.MAGIC, DamageType.SKILL);
			}
		}
	}
}
