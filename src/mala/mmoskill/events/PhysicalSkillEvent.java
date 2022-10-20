package mala.mmoskill.events;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;

public class PhysicalSkillEvent extends Event
{
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	Player player;
	DamageMetadata damage_meta;
	
	public PhysicalSkillEvent(Player _player, @NotNull DamageMetadata _damage_meta)
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
	
	public void addType(DamageType _type)
	{
		//Set<DamageType> types = damage_meta.collectTypes();
		//if (!types.contains(_type))
		//	types.add(_type);
		damage_meta.add(0, _type);
		// AttackResult new_attack = new AttackResult(attack.isSuccessful(), attack.getDamage(), types);
		// attack = new_attack;
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
