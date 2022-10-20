package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Roar extends RegisteredSkill
{
	public Roar()
	{	
		super(new Roar_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(15, 0.5));
		addModifier("cooldown", new LinearValue(25, -2, 5, 25));
		addModifier("stamina", new LinearValue(20, 0.0));
	}
}

class Roar_Handler extends MalaSkill implements Listener
{
	public Roar_Handler()
	{
		super(	"ROAR",
				"외침",
				Material.NETHER_STAR,
				"&7주변 &8{distance}&7m 내 몬스터들의 시선을 끕니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ITEM_TOTEM_USE, 2, 1);
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 2, 1);
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2, 0.5f);
		
		double distance = cast.getModifier("distance");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Roar_Task(data.getPlayer(), distance));

		Aggro.Taunt_Area(data.getPlayer(), distance, 9999);
		
		RegisteredSkill roar_echo = MMOCore.plugin.skillManager.getSkill("ROAR_ECHO");
		int level = data.getSkillLevel(roar_echo);
		if (data.getProfess().hasSkill(roar_echo))
		{
			int weakness_sec = (int)(roar_echo.getModifier("second", level) * 20.0);
			int max_amp = (int)roar_echo.getModifier("power", level);
			for (Entity e : data.getPlayer().getNearbyEntities(distance, distance, distance))
			{
				if (!(e instanceof LivingEntity))
					continue;
				
				LivingEntity le = (LivingEntity)e;
				if (le instanceof Player)
				{
					Buff_Manager.Increase_Buff(le, PotionEffectType.INCREASE_DAMAGE, 0, 20, PotionEffectType.WEAKNESS, max_amp);
				}
				else
				{
					Buff_Manager.Add_Buff(le, PotionEffectType.WEAKNESS, 0, weakness_sec, PotionEffectType.INCREASE_DAMAGE);
				}
			}
		}
		RegisteredSkill roar_surge = MMOCore.plugin.skillManager.getSkill("ROAR_SURGE");
		level = data.getSkillLevel(roar_surge);
		if (data.getProfess().hasSkill(roar_surge))
		{
			int weakness_sec = (int)(roar_surge.getModifier("second", level) * 20.0);
			int max_amp = (int)roar_surge.getModifier("power", level);
			for (Entity e : data.getPlayer().getNearbyEntities(distance, distance, distance))
			{
				if (!(e instanceof LivingEntity))
					continue;
				
				LivingEntity le = (LivingEntity)e;
				if (le instanceof Player)
				{
					Buff_Manager.Increase_Buff(le, PotionEffectType.SPEED, 0, 20, PotionEffectType.SLOW, max_amp);
				}
				else
				{
					Buff_Manager.Add_Buff(le, PotionEffectType.SLOW, 0, weakness_sec, PotionEffectType.SPEED);
				}
			}
		}
	}
}

class Roar_Task implements Runnable
{
	Player player;
	double current_distance = 2.0;
	double distance = 4.0;
	int time = 0;
	Location loc;
	
	public Roar_Task(Player _player, double _distance)
	{
		player = _player;
		loc = player.getLocation().add(0, player.getHeight() * 0.2, 0).clone();
		
		time = 4;
		distance = _distance;
	}
	
	public void run()
	{
		Location origin_loc = loc.clone();
		Location temp_loc = loc.clone();
		for(double angle = 0.0; angle < 360.0; angle += 30.0 / current_distance)
		{
			temp_loc.setX(origin_loc.getX() + Math.cos(Math.toRadians(angle)) * current_distance);
			temp_loc.setZ(origin_loc.getZ() + Math.sin(Math.toRadians(angle)) * current_distance);
			temp_loc.getWorld().spawnParticle(Particle.CRIT, temp_loc, 1, 0, 0, 0, 0);
		}
		
		current_distance += 2.0;
		if(current_distance < distance)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}