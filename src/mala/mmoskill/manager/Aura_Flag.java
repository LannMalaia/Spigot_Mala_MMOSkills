package mala.mmoskill.manager;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;

import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Aura_Flag extends Summoned_OBJ implements Runnable
{
	protected ArrayList<Player> players = new ArrayList<Player>();
	
	protected Particle m_Particle;
	protected DustOptions m_Dust;
	
	protected double radius = 3.0;
	
	public Aura_Flag(Player _player, Location _loc, int _tick, EntityType _type, double _radius)
	{
		super(_player, "Aura_Flag", _type, _loc, _tick);
		_loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, _loc, 60, 0.5, 1.0, 0.5, 0);
		_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
		radius = _radius;
		
		Particle_Setup();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	
	protected void Particle_Setup()
	{
		m_Particle = Particle.SPELL;
	}
	
	public static Aura_Flag Spawn_Flag(PlayerData _data, Location _loc)
	{
		EntityType type = EntityType.ARMOR_STAND;

		double second = 5;
		double temp_radius = 3.0;
		int max = 1;
		RegisteredSkill mastery = MMOCore.plugin.skillManager.getSkill("MASTERY_FLAG");
		int level = _data.getSkillLevel(mastery);
		if(_data.getProfess().hasSkill(mastery))
		{
			second = mastery.getModifier("second", level);
			temp_radius = mastery.getModifier("radius", level);
			max = (int)mastery.getModifier("max", level);
		}

		if (!Summon_Manager.Get_Instance().Check_Summon(_data.getPlayer(), "Aura_Flag", max))
			return null;
		
		Aura_Flag flag = new Aura_Flag(_data.getPlayer(), _loc, (int)(second * 20), type, temp_radius);

		Entity flag_en = Summon_Manager.Get_Instance().Summon(flag);
		ArmorStand as = (ArmorStand)flag_en;
		as.setCustomName(_data.getPlayer().getName());
		as.setCustomNameVisible(true);
		
		return flag;
	}
	
	double cur_radius;
	double cur_sec;
	public void run()
	{
		if (!entity.isValid())
			return;
		
		cur_sec += 0.05;
		cur_radius += (radius - cur_radius) * 0.1;
		
		Draw_Particle();
		Effect();
		
		if (cur_sec >= 2.5)
		{
			cur_sec = 0;
			cur_radius = 0;
			Effect_Sec();
		}
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}

	protected void Effect_Sec()
	{
		
	}
	
	protected void Effect()
	{
		players = new ArrayList<Player>();
		for (Entity e : entity.getNearbyEntities(radius, radius, radius))
		{
			if (e instanceof Player && !players.contains(e))
				players.add((Player)e);
		}
		for (int i = players.size() - 1; i >= 0; i--)
		{
			Player p = players.get(i);
			if (entity.getWorld() != p.getWorld()
				|| entity.getLocation().distance(p.getLocation()) > radius)
			{
				players.remove(p);
			}
		}
	}
	protected void Draw_Particle()
	{
		if (m_Dust == null)
			Particle_Drawer.Draw_Circle(entity.getLocation(), m_Particle, cur_radius);
		else
			Particle_Drawer.Draw_Circle(entity.getLocation(), m_Dust, cur_radius);
	}
	
	@Override
	public void Remove()
	{
		entity.getWorld().spawnParticle(Particle.SNOWBALL, entity.getLocation(), 60, 0.5, 1.0, 0.5, 0);
		super.Remove();
	}

	@EventHandler
	public void When_ItemDrop(EntityDropItemEvent event)
	{
		if (!entity.isValid())
		{
			EntityDropItemEvent.getHandlerList().unregister(this);
			return;
		}
		if (event.getEntity() == entity)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void When_Enemy_Damaged(EntityDamageEvent event)
	{
		if (!entity.isValid())
		{
			EntityDamageEvent.getHandlerList().unregister(this);
			return;
		}
		if (event.getEntity() == entity)
		{
			event.setCancelled(true);
			return;		
		}
	}
}
