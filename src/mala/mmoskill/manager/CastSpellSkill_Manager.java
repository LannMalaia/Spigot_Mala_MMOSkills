package mala.mmoskill.manager;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import mala_mmoskill.main.MalaMMO_Skill;

public class CastSpellSkill_Manager implements Listener, Runnable
{
	private static CastSpellSkill_Manager Instance;
	private HashMap<Player, CastChain> castChainMap;

	public static CastSpellSkill_Manager Get_Instance()
	{
		if (Instance == null)
			Instance = new CastSpellSkill_Manager();
		return Instance;
	}
	
	public CastSpellSkill_Manager()
	{
		castChainMap = new HashMap<Player, CastChain>();
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		Bukkit.getScheduler().runTaskTimer(MalaMMO_Skill.plugin, this, 10, 1);
	}
	
	/**
	 * @author jimja
	 * @version 2022. 11. 4.
	 * @apiNote 매니저에 스펠 체인 등록
	 */
	public void PutSpellChain(Player _player, SpellChainType _sct)
	{
		CastChain as = null;
		if (castChainMap.containsKey(_player))
			as = castChainMap.get(_player);
		else
		{
			as = new CastChain(_player);
			castChainMap.put(_player, as);
		}
		as.putElement(_sct);
	}
	public CastChain getSpellChain(Player _player)
	{
		return castChainMap.get(_player);
	}
	public void removeSpellChain(Player _player)
	{
		castChainMap.remove(_player);
	}
	
	// 갱신
	private int counter = 0;
	public void run()
	{
		ArrayList<Player> remove_list = new ArrayList<Player>();
		for (Player player : castChainMap.keySet())
		{
			CastChain cc = castChainMap.get(player);
			
			cc.draw();
			if (counter % 10 == 0)
			{
				if (cc.subtractDuration(0.5))
				{
					cc.sendDisableMsg();
					remove_list.add(player);
				}
			}
		}
		for (Player player : remove_list)
		{
			castChainMap.remove(player);
		}
		if (++counter >= 10)
			counter = 0;
	}
	
//	@EventHandler (priority = EventPriority.HIGHEST)
//	public void whenPlayerDamaged(EntityDamageByEntityEvent event)
//	{
//		if (event.isCancelled())
//			return;
//		if (event.getEntity() instanceof Player) {
//			Player player = (Player)event.getEntity();
//			if (castChainMap.containsKey(player)) {
//				castChainMap.get(player).sendDisableMsg();
//				castChainMap.remove(player);
//			}
//		}
//	}
}
