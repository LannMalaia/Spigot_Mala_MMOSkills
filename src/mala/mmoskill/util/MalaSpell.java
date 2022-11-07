package mala.mmoskill.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.api.player.PlayerData;

public abstract class MalaSpell implements Runnable
{
	protected PlayerData playerData;
	protected Player player;
	protected Location playerCenterLocation;
	protected Location frontLocation;
	protected Location targetLocation;
	protected World world;
	protected double targetDuration;
	
	protected int durationCounter = 0;

	public MalaSpell(PlayerData playerData, double duration)
	{
		this.playerData = playerData;
		this.player = playerData.getPlayer();
		this.playerCenterLocation = player.getLocation().add(0.0, 0.5, 0.0);
		this.frontLocation = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(2.0));
		this.targetLocation = RayUtil.getLocation(player).add(0.0, 0.1, 0.0);
		this.world = targetLocation.getWorld();
		this.targetDuration = duration;
	}
	
	@Override
	public void run()
	{
		this.playerCenterLocation = player.getLocation().add(0.0, 0.5, 0.0);
		this.frontLocation = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(2.0));
		if (durationCounter == 0)
			whenStart();
		
		durationCounter += 1;
		if (durationCounter > targetDuration * 20)
		{
			whenEnd();
			return;
		}
		
		whenCount();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	public abstract void whenStart();
	public abstract void whenCount();
	public abstract void whenEnd();
}
