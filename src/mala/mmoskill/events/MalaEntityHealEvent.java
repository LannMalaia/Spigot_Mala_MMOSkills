package mala.mmoskill.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MalaEntityHealEvent extends Event implements Cancellable
{
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	
	Player player;
	LivingEntity target;
	double amount;
	boolean is_cancelled;
	
	public MalaEntityHealEvent(Player _player, LivingEntity _target, double _amount)
	{
		player = _player;
		target = _target;
		amount = _amount;
		is_cancelled = false;
	}
	
	public Player getCaster()
	{
		return player;
	}
	public LivingEntity getTarget()
	{
		return target;
	}
	public double getAmount()
	{
		return amount;
	}
	public void setAmount(double _amount)
	{
		amount = _amount;
	}
	
	@Override
	public @NotNull HandlerList getHandlers()
	{
		return HANDLERS_LIST;
	}
	
	public static HandlerList getHandlerList()
	{
		return HANDLERS_LIST;
	}

	@Override
	public boolean isCancelled()
	{
		return is_cancelled;
	}

	@Override
	public void setCancelled(boolean arg0)
	{
		is_cancelled = arg0;
	}

}
