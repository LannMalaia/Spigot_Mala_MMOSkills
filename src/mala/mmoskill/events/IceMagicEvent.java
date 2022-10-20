package mala.mmoskill.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.damage.DamageMetadata;

public class IceMagicEvent extends Event
{
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	Player player;
	DamageMetadata damage_meta;
	boolean super_slow = false;
	
	public IceMagicEvent(Player _player, @NotNull DamageMetadata _damage_meta)
	{
		player = _player;
		damage_meta = _damage_meta;
	}
	
	public Player getCaster()
	{
		return player;
	}
	
	public DamageMetadata getAttack()
	{
		return damage_meta;
	}
	public void setAttack(DamageMetadata _damage_meta)
	{
		damage_meta = _damage_meta;
	}
	
	public boolean getSuperSlow()
	{
		return super_slow;
	}
	public void setSuperSlow(boolean _enable)
	{
		super_slow = _enable;
	}
	
	@Override
	public @NotNull HandlerList getHandlers() {
		// TODO Auto-generated method stub
		return HANDLERS_LIST;
	}
	public static HandlerList getHandlerList()
	{
		return HANDLERS_LIST;
	}
}
