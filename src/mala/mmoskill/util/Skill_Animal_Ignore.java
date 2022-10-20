package mala.mmoskill.util;

import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;

public class Skill_Animal_Ignore implements Listener
{
	@EventHandler
	public void passive_absorb(PlayerAttackEvent event)
	{
		if (event.getAttack().getDamage().hasType(DamageType.SKILL) || event.getAttack().getDamage().hasType(DamageType.MAGIC))
		{
			if (event.getEntity() instanceof Animals)
				event.setCancelled(true);
		}
	}
	
}
