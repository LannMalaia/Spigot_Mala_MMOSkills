package mala.mmoskill.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitPlayer;
import io.lumine.mythic.core.mobs.ActiveMob;

public class Aggro
{
	public static void Add_Threat_Target(Player _player, Entity _target, double _amount)
	{
		BukkitAPIHelper mythicapi = MythicBukkit.inst().getAPIHelper();
		BukkitPlayer bp = new BukkitPlayer(_player);
		if (mythicapi.isMythicMob(_target))
		{
			ActiveMob mob = mythicapi.getMythicMobInstance(_target);
			if (_amount > 10000000)
				mythicapi.taunt(_target, _player);
			else if (mob.hasThreatTable())
			{
				mythicapi.addThreat(_target, _player, _amount);
				return;
			}
			else
			{
				mob.resetTarget();
				mob.setTarget(bp);
			}
		}
		if (_target instanceof Monster)
		{
			Monster mob = (Monster)_target;
			mob.setTarget(_player);
		}
	}
	public static void Add_Threat_Area(Player _player, double _radius, double _amount)
	{
		for (Entity e : _player.getNearbyEntities(_radius, _radius, _radius))
		{
			Add_Threat_Target(_player, e, _amount);
		}
	}
	public static void Taunt_Area(Player _player, double _radius, int _mob_count)
	{
		int i = 0;
		for (Entity e : _player.getNearbyEntities(_radius, _radius, _radius))
		{
			Add_Threat_Target(_player, e, 99999999.0);
			if (++i >= _mob_count)
				break;
		}
	}
	public static void Taunt_Area(LivingEntity _target, double _radius)
	{
		BukkitAPIHelper mythicapi = MythicBukkit.inst().getAPIHelper();
		for (Entity e : _target.getNearbyEntities(_radius, _radius, _radius))
		{
			mythicapi.addThreat(e, _target, 99999999.0);
			mythicapi.taunt(e, _target);
		}
	}

}
