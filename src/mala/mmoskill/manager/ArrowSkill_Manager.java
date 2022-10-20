package mala.mmoskill.manager;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;

import mala_mmoskill.main.MalaMMO_Skill;

public class ArrowSkill_Manager implements Listener, Runnable
{
	private static ArrowSkill_Manager Instance;
	HashMap<Player, ArrowTip> pas_map;

	public static ArrowSkill_Manager Get_Instance()
	{
		if (Instance == null)
			Instance = new ArrowSkill_Manager();
		return Instance;
	}
	
	public ArrowSkill_Manager()
	{
		pas_map = new HashMap<Player, ArrowTip>();
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		Bukkit.getScheduler().runTaskTimer(MalaMMO_Skill.plugin, this, 10, 10);
	}
	
	/**
	 * @author jimja
	 * @version 2021. 10. 16.
	 * @apiNote 매니저에 화살촉 기술을 등록한다
	 * @param _player
	 * @param _as
	 */
	public void Register_ArrowSkill(Player _player, ArrowTip _as)
	{
		if (pas_map.containsKey(_player))
		{
			ArrowTip as = pas_map.get(_player);
			as.Send_Disable_Msg();
		}
		pas_map.put(_player, _as);
		_as.Send_Enable_Msg();
	}
	
	public void run()
	{
		ArrayList<Player> remove_list = new ArrayList<Player>();
		for (Player player : pas_map.keySet())
		{
			ArrowTip as = pas_map.get(player);
			if (as.Subtract_Duration(0.5))
			{
				as.Send_Disable_Msg();
				remove_list.add(player);
			}
		}
		for (Player player : remove_list)
		{
			pas_map.remove(player);
		}
	}
	
	@EventHandler
	public void When_ShootArrow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		if (!(event.getProjectile() instanceof Arrow))
			return;
		if (!(event.getBow().getType() == Material.BOW || event.getBow().getType() == Material.CROSSBOW))
			return;
		
		if (pas_map.containsKey(event.getEntity()))
		{
			ArrowTip as = pas_map.get(event.getEntity());
			as.Subtract_Duration(0.75);
			as.Run();
			event.setCancelled(true);
		}
	}
}
