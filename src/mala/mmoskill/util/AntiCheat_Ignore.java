package mala.mmoskill.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.vagdedes.spartan.api.API;
import me.vagdedes.spartan.system.Enums.HackType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;

public class AntiCheat_Ignore implements Listener
{
	public static void Ignore_Anti_Cheat(Player _player)
	{
		for (HackType ht : HackType.values())
			API.cancelCheck(_player, ht, 100);
	}

	@EventHandler
	public void passive_absorb(PlayerCastSkillEvent event)
	{
		// Ignore_Anti_Cheat(event.getPlayer());
	}
}
