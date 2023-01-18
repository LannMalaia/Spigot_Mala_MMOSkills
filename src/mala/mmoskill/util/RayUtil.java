package mala.mmoskill.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class RayUtil
{
	public static Location getLocation(LivingEntity caster)
	{
		Location loc = caster.getEyeLocation();
		loc.add(loc.getDirection().multiply(0.1));
		RayTraceResult rtr = caster.getLocation().getWorld().rayTrace(loc, loc.getDirection(), 75.0, FluidCollisionMode.NEVER,
			true, 0.1, entity -> (entity instanceof LivingEntity && entity != caster));
		if (rtr == null)
			return loc.add(loc.getDirection().multiply(10.0));
		return rtr.getHitPosition().toLocation(caster.getWorld());
	}
	public static List<LivingEntity> getLivingEntities(LivingEntity caster) {
		return getLivingEntities(caster, 75.0);
	}
	public static List<LivingEntity> getLivingEntities(LivingEntity caster, double length)
	{
		return getLivingEntities(caster, caster.getLocation().getDirection(), length);
	}
	public static List<LivingEntity> getLivingEntities(LivingEntity caster, Vector dir, double length)
	{
		ArrayList<LivingEntity> result = new ArrayList<LivingEntity>();
		Location loc = caster.getEyeLocation();
		loc.add(loc.getDirection().multiply(0.1));
		
		while (true) {
			RayTraceResult rtr = caster.getLocation().getWorld().rayTrace(loc, dir, length, FluidCollisionMode.NEVER,
					true, 0.1, entity -> (entity instanceof LivingEntity && entity != caster && !result.contains(entity)));
			if (rtr == null)
				break;
			if (rtr.getHitEntity() == null)
				break;
			if (rtr.getHitEntity() instanceof LivingEntity)
				result.add((LivingEntity)rtr.getHitEntity());
		}
		
		return result;
	}
	public static LivingEntity getLivingEntity(LivingEntity caster) {
		return getLivingEntity(caster, 75.0);
	}
	public static LivingEntity getLivingEntity(LivingEntity caster, double length) {
		return getLivingEntity(caster, caster.getLocation().getDirection(), length);
	}
	public static LivingEntity getLivingEntity(LivingEntity caster, Vector dir, double length)
	{
		Location loc = caster.getEyeLocation();
		loc.add(loc.getDirection().multiply(0.1));
		RayTraceResult rtr = caster.getLocation().getWorld().rayTrace(loc, dir, length, FluidCollisionMode.NEVER,
			true, 0.1, entity -> (entity instanceof LivingEntity && entity != caster));
		if (rtr == null)
			return null;
		if (rtr.getHitEntity() == null)
			return null;
		if (rtr.getHitEntity() instanceof LivingEntity)
			return (LivingEntity)rtr.getHitEntity();
		return null;
	}
	public static Player getPlayer(LivingEntity caster) {
		return getPlayer(caster, 75.0);
	}
	public static Player getPlayer(LivingEntity caster, double length)
	{
		Location loc = caster.getEyeLocation();
		loc.add(loc.getDirection().multiply(0.1));
		RayTraceResult rtr = caster.getLocation().getWorld().rayTrace(loc, loc.getDirection(), length, FluidCollisionMode.NEVER,
			true, 0.1, entity -> (entity instanceof Player && entity != caster));
		if (rtr == null)
			return null;
		if (rtr.getHitEntity() == null)
			return null;
		if (rtr.getHitEntity() instanceof LivingEntity)
			return (Player)rtr.getHitEntity();
		return null;
	}
	
	public static Location getLocation(Location loc, Vector dir, double length)
	{
		RayTraceResult rtr = loc.getWorld().rayTrace(loc, dir, length, FluidCollisionMode.NEVER,
			true, 0.1, null);
		if (rtr == null)
			return loc.clone().add(dir.clone().multiply(length));
		return rtr.getHitPosition().toLocation(loc.getWorld());
	}

}
