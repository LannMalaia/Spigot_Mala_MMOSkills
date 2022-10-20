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
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Recovery_Wave extends RegisteredSkill
{
	public Recovery_Wave()
	{	
		super(new Recovery_Wave_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(3.75, 0.75, 2, 20));
		addModifier("cooldown", new LinearValue(30, 0));
		addModifier("mana", new LinearValue(40, 4));
	}
}

class Recovery_Wave_Handler extends MalaSkill implements Listener
{
	public Recovery_Wave_Handler()
	{
		super(	"RECOVERY_WAVE",
				"에리어 리커버리",
				Material.MUSIC_DISC_FAR,
				"&7자신을 포함한 주변 &8{distance}&7 거리의 아군을 회복시킵니다.",
				"&7이 때 효과는 기존 리커버리 레벨을 따릅니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "RECOVERY", 1))
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
		
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("RECOVERY");
		double distance = cast.getModifier("distance");

		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Recovery_Task(data.getPlayer(), data.getPlayer(), data.getSkillLevel(skill)));
		
		for(Entity e : data.getPlayer().getNearbyEntities(distance, distance, distance))
		{
			if(e instanceof Player)
			{
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
						new Recovery_Task(data.getPlayer(), (Player)e, data.getSkillLevel(skill)));
			}
		}
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new RecoveryWave_Task(data.getPlayer(), distance));
	}
}

class RecoveryWave_Task implements Runnable
{
	Player player;
	double current_distance = 1.0;
	double distance = 4.0;
	Location loc;
	
	public RecoveryWave_Task(Player _player, double _distance)
	{
		player = _player;
		loc = player.getLocation().add(0, player.getHeight() * 0.5, 0).clone();
		
		distance = _distance;
	}
	
	public void run()
	{
		Location origin_loc = loc.clone();
		Location temp_loc = loc.clone();
		for(double angle = 0.0; angle < 360.0; angle += 5)
		{
			temp_loc.setX(origin_loc.getX() + Math.cos(Math.toRadians(angle)) * current_distance);
			temp_loc.setZ(origin_loc.getZ() + Math.sin(Math.toRadians(angle)) * current_distance);
			temp_loc.getWorld().spawnParticle(Particle.CLOUD, temp_loc, 1, 0, 0, 0, 0);
		}
		temp_loc.getWorld().playSound(temp_loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1, (float) (1f + current_distance / distance));
		
		current_distance += 1.0;
		if(current_distance < distance)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
	}
}