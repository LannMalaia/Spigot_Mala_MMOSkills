package mala.mmoskill.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class RayUtil
{
	public static Location getLocation(Player player)
	{
		Location loc = player.getEyeLocation();
		loc.add(loc.getDirection().multiply(0.7));
		RayTraceResult rtr = player.getLocation().getWorld().rayTrace(loc, loc.getDirection(), 75.0, FluidCollisionMode.NEVER,
			true, 0.1, null);
		if (rtr == null)
			return loc.add(loc.getDirection().multiply(10.0));
		return rtr.getHitPosition().toLocation(player.getWorld());
	}

}
