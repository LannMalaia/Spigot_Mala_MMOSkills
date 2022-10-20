package mala.mmoskill.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;

public class Vehicle_Util
{
	public static boolean Is_Riding_Horse(LivingEntity _entity)
	{
		if (_entity.getVehicle() == null)
			return false;
		if (_entity.getVehicle() instanceof Horse)
			return true;
		return false;
	}
	
	public static Entity Get_Last_Vehicle(LivingEntity _entity)
	{
		Entity vehicle = _entity;
		while (true)
		{
			if (vehicle.getVehicle() == null)
				return vehicle;
			vehicle = vehicle.getVehicle();
		}
	}
}
