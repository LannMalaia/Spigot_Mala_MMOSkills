package mala.mmoskill.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mala_mmoskill.main.MalaMMO_Skill;

public class Summoned_OBJ implements Listener
{
	public Player player;
	public String keyword;
	public Entity entity;
	public int tick;
	
	public Summoned_OBJ(Player _player, String _keyword, EntityType _entity_type, Location _loc, int _tick)
	{
		player = _player;
		keyword = _keyword;
		entity = _loc.getWorld().spawnEntity(_loc, _entity_type);
		entity.setMetadata("summoned", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
		tick = _tick;
		
		entity.setInvulnerable(true);
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, () -> {
			entity.setInvulnerable(false);
		}, 60);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	public void Remove()
	{
		EntityDamageByEntityEvent.getHandlerList().unregister(this);
		player.sendMessage("§c소환한 개체가 사라졌습니다.");
		entity.remove();
		
	}
	
	@EventHandler
	public void Summoned_OBJ_Damaged(EntityDamageByEntityEvent event)
	{
		if (!entity.isValid())
		{
			EntityDamageByEntityEvent.getHandlerList().unregister(this);
			return;
		}
		if (event.isCancelled())
			return;
		if (event.getDamager() instanceof Projectile)
		{
			Projectile proj = (Projectile)event.getDamager();
			if (event.getEntity() == entity && proj.getShooter() == player)
				event.setCancelled(true);
		}
		else if (event.getDamager() == player && event.getEntity() == entity)
			event.setCancelled(true);
		else if (event.getDamager() == entity && event.getEntity() == player)
			event.setCancelled(true);
	}
}
