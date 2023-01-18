package mala.mmoskill.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.damage.DamageMetadata;
import mala.mmoskill.util.MalaSpellEffect;


public class SpellCastEvent extends Event
{
	private static final HandlerList HANDLERS_LIST = new HandlerList();
	
	private LivingEntity caster;
	private MalaSpellEffect spell;
	
	public SpellCastEvent(LivingEntity caster, MalaSpellEffect spell) {
		this.caster = caster;
		this.spell = spell;
	}
	
	public LivingEntity getCaster() {
		return caster;
	}
	
	public MalaSpellEffect getSpell() {
		return spell;
	}
	
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS_LIST;
	}
	
	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

}
