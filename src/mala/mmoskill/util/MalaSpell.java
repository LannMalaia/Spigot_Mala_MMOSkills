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
	protected Location targetLocation;
	protected World world;
	protected double targetDuration;
	
	protected int durationCounter = 0;

	public MalaSpell(PlayerData playerData, double duration)
	{
		this.playerData = playerData;
		this.player = playerData.getPlayer();
		this.targetLocation = RayUtil.getLocation(player);
		this.world = targetLocation.getWorld();
		this.targetDuration = duration;
	}
	
	@Override
	public void run()
	{
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
