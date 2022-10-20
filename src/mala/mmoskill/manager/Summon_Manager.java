package mala.mmoskill.manager;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import mala_mmoskill.main.MalaMMO_Skill;

public class Summon_Manager implements Listener
{
	private static Summon_Manager Instance = null;
	ArrayList<Summoned_OBJ> m_Objects;
	
	public static Summon_Manager Get_Instance()
	{
		if (Instance == null)
			Instance = new Summon_Manager();
		return Instance;
	}

	public Summon_Manager()
	{
		m_Objects = new ArrayList<Summoned_OBJ>();

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		
		Bukkit.getScheduler().runTaskTimer(MalaMMO_Skill.plugin, new Runnable() 
		{
			public void run()
			{
				for (int i = 0; i >= 0 && i < m_Objects.size(); i++)
				{
					Summoned_OBJ so = m_Objects.get(i);
					if (--so.tick <= 0
							|| !so.entity.isValid())
					{
						so.Remove();
						m_Objects.remove(i--);
						continue;
					}
				}
			}
		}, 0, 1);
	}

	@EventHandler
	public void summon_manager_quit(PlayerQuitEvent event)
	{
		Remove_All_Summoned_Object(event.getPlayer());
	}
	@EventHandler
	public void summon_manager_quit(PlayerChangedWorldEvent event)
	{
		Remove_All_Summoned_Object(event.getPlayer());
	}
	@EventHandler
	public void summon_manager_death(PlayerDeathEvent event)
	{
		Remove_All_Summoned_Object(event.getEntity());
	}
	
	// 이 키워드 현재 소환 가능해?
	public boolean Check_Summon(Player _player, String _keyword, int _max)
	{
		int count = 0;
		for (Summoned_OBJ so : m_Objects)
		{
			if (so.player == _player)
			{
				if (so.keyword.equals(_keyword))
					count++;
			}
		}
		return count < _max;
	}
	
	public Entity Summon(Summoned_OBJ so)
	{
		m_Objects.add(so);
		return so.entity;
	}
	
	public ArrayList<Summoned_OBJ> Get_Summoned_OBJs(Player _player, String _keyword)
	{
		ArrayList<Summoned_OBJ> list = new ArrayList<Summoned_OBJ>();
		for (Summoned_OBJ so : m_Objects)
		{
			if (so.player == _player && so.keyword.equals(_keyword))
				list.add(so);
		}
		return list;
	}
	
	public void Remove_All_Summoned_Object(Player _player)
	{
		for (int i = 0; i >= 0 && i < m_Objects.size(); i++)
		{
			Summoned_OBJ so = m_Objects.get(i);
			if (so.player == _player)
			{
				so.Remove();
				m_Objects.remove(i--);
				continue;
			}
		}
	}
}