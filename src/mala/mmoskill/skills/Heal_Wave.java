package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Mastery_Heal;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Heal_Wave extends RegisteredSkill
{
	public Heal_Wave()
	{	
		super(new Heal_Wave_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(5.5, 1.5, 2, 35));
		addModifier("cooldown", new LinearValue(35, 0));
		addModifier("mana", new LinearValue(34, 4));
	}
}

class Heal_Wave_Handler extends MalaSkill implements Listener
{
	public Heal_Wave_Handler()
	{
		super(	"HEAL_WAVE",
				"에리어 힐",
				Material.MUSIC_DISC_CHIRP,
				"&7자신을 포함한 주변 &8{distance}&7 거리의 아군을 회복시킵니다.",
				"&7이 때 효과는 기존 힐을 따릅니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "HEAL", 1))
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
		
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("HEAL");
		int lv = data.getSkillLevel(skill);
		double damage = skill.getModifier("heal", lv);
		damage *= Mastery_Heal.Get_Mult(data.getPlayer());
		double distance = cast.getModifier("distance");

		// 자신에게도 적용
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Heal_Task(data.getPlayer(), data.getPlayer(), damage * 0.5));
		
		for(Entity e : data.getPlayer().getNearbyEntities(distance, distance, distance))
		{
			if(e instanceof Player)
			{
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
						new Heal_Task(data.getPlayer(), (Player)e, damage));
			}
		}
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new HealWave_Task(data.getPlayer(), distance));
		
		Aggro.Add_Threat_Area(data.getPlayer(), distance * 1.5, 5.0);
	}
}

class HealWave_Task implements Runnable
{
	Player player;
	double current_distance = 2.0;
	double distance = 4.0;
	int time = 0;
	Location loc;
	
	public HealWave_Task(Player _player, double _distance)
	{
		player = _player;
		loc = player.getLocation().add(0, 0.1, 0).clone();
		
		time = 4;
		distance = _distance;
	}
	
	public void run()
	{
		Location origin_loc = loc.clone();
		Location temp_loc = loc.clone();
		for(double angle = 0.0; angle < 360.0; angle += distance / current_distance)
		{
			temp_loc.setX(origin_loc.getX() + Math.cos(Math.toRadians(angle)) * current_distance);
			temp_loc.setZ(origin_loc.getZ() + Math.sin(Math.toRadians(angle)) * current_distance);
			temp_loc.getWorld().spawnParticle(Particle.BUBBLE_POP, temp_loc, 1, 0, 0, 0, 0);
		}
		temp_loc.getWorld().playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 1, (float) (1f + current_distance / distance));
		
		current_distance += 1.0;
		if(current_distance < distance)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
	}
}