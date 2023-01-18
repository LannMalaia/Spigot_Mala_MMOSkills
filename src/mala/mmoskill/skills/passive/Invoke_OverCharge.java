package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.checkerframework.common.reflection.qual.Invoke;

import mala.mmoskill.events.SpellCastEvent;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.skills.passive.Invoke_OverCharge.OverChargeSpell;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_OverCharge extends RegisteredSkill
{
	public static final String OVERCHARGE_META = "malammo_skill.overcharge.count";
	
	public Invoke_OverCharge()
	{	
		super(new Invoke_OverCharge_Handler(), MalaMMO_Skill.plugin.getConfig());
	}
	
	/**
	 * 오버차지 사용
	 */
	public static double useOvercharge(LivingEntity caster)
	{
		double result = 1.0;
		if (caster.hasMetadata(OVERCHARGE_META)) {
			result += caster.getMetadata(OVERCHARGE_META).get(0).asInt() * 0.25;
			caster.removeMetadata(OVERCHARGE_META, MalaMMO_Skill.plugin);
		}
		return result;
	}
	/**
	 * 오버차지 피해
	 */
	public static void damageOvercharge(LivingEntity caster)
	{
		if (caster.hasMetadata(OVERCHARGE_META)) {
			double damage = caster.getMetadata(OVERCHARGE_META).get(0).asInt();
			caster.removeMetadata(OVERCHARGE_META, MalaMMO_Skill.plugin);
			
			caster.sendMessage("&c&l[ 충전중이던 마나가 폭발했다! ]");
			new Effect(caster.getEyeLocation(), Particle.FLAME)
				.addSound(Sound.ITEM_TOTEM_USE, 1.0, 2.0)
				.append3DSphere(3.0)
				.scalePoint(0)
				.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.4)
				.playEffect();
			new Effect(caster.getEyeLocation(), Particle.SOUL)
				.append3DSphere(3.0)
				.scalePoint(0)
				.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.2)
				.playEffect();
			
			double hpValue = caster.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			damage *= hpValue * 0.15;
			caster.setHealth(caster.getHealth() - damage);
		}
	}

	public static class OverChargeSpell extends MalaSpellEffect
	{
		public OverChargeSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 10.0);
//			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 2.0);
//			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 2.0);
		}
		public OverChargeSpell(LivingEntity attacker)
		{
			super(attacker, 10.0);
		}
		
		boolean isDisabled = false;
		int ocCountCache = 0;
		int rollCounter = 0;
		double height = 0.5;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
			
			int ocCount = 0;
			if (attacker.hasMetadata(OVERCHARGE_META))
			{
				ocCount = attacker.getMetadata(OVERCHARGE_META).get(0).asInt() + 1;
				attacker.setMetadata(OVERCHARGE_META, new FixedMetadataValue(MalaMMO_Skill.plugin, Math.min(3, ocCount)));
				disable();
			}
			else
			{
				attacker.setMetadata(OVERCHARGE_META, new FixedMetadataValue(MalaMMO_Skill.plugin, 1));
			}
		}
		
		DustTransition dts = new DustTransition(Color.ORANGE, Color.YELLOW, 0.5f);
		double size = 0.5, rollingSpeed = 5.0;
		
		@Override
		public void whenCount() {
			if (!attacker.hasMetadata(OVERCHARGE_META))
				disable();
			if (isDisabled) return;

			// 캐시값 비교, 갱신
			int ocCount = attacker.getMetadata(OVERCHARGE_META).get(0).asInt();
			if (ocCountCache != ocCount)
			{
				durationCounter = 1;
				ocCountCache = ocCount;
				attacker.sendMessage("§6§l[ " +
						(ocCount == 1 ? "§e§l" : ocCount == 2 ? "§6§l" : "§b§l") +
						"오버차지 " + ocCount + "단계" + " §6§l]");
			}

			// 회전 회오리
			rollCounter++;
			size = size + (((1.0 + 1.0 * ocCount) - size) * 0.05);
			Particle flame = ocCount == 1 ? Particle.SMALL_FLAME : ocCount == 2 ? Particle.FLAME : Particle.SOUL_FIRE_FLAME;
			
			// 단계에 맞춰 효과를 추가 제거
			if (Particle_Manager.isReduceMode(attacker)) {
				if (durationCounter % 10 == 0) {
					switch (ocCount)
					{
					case 3:
						new Effect(centerLocation, Particle.SOUL_FIRE_FLAME)
							.append2DShape(3, 2.0, 1.5)
							.rotate(0, rollCounter * rollingSpeed * 0.6, 0)
							.scaleVelocity(0.0)
							.playEffect();
						break;
					case 2:
						new Effect(centerLocation, Particle.FLAME)
							.append2DShape(3, 2.0, 1.5)
							.rotate(0, rollCounter * rollingSpeed * 0.4, 0)
							.scaleVelocity(0.0)
							.playEffect();
						break;
					case 1:
						new Effect(centerLocation, Particle.SMALL_FLAME)
							.append2DShape(3, 2.0, 1.5)
							.rotate(0, rollCounter * rollingSpeed * 0.2, 0)
							.scaleVelocity(0.0)
							.playEffect();
						break;
					}
				}
			} else {
				switch (ocCount)
				{
				case 3:
					new Effect(centerLocation, flame)
						.append2DArc(90.0, size, 0.7)
						.rotate(0, 180.0 + rollCounter * rollingSpeed, 0)
						.rotate(15.0, 0, 0)
						.scaleVelocity(0.04)
						.playEffect();
				case 2:
					new Effect(centerLocation, flame)
						.append2DArc(90.0, size, 0.7)
						.rotate(0, rollCounter * rollingSpeed, 0)
						.rotate(15.0, 0, 0)
						.scaleVelocity(0.04)
						.playEffect();
				case 1:
					new Effect(centerLocation, flame)
						.append2DShape(3, 3.0, 2.0).rotate(0, 180.0, 0)
						.append2DShape(3, 3.0, 2.0).rotate(0, 180.0, 0)
						.rotate(0, rollCounter * rollingSpeed * 0.2, 0)
						.rotate(15.0, 0, 0)
						.scaleVelocity(0.0)
						.playEffect();
				}
			}
		}
		
		@Override
		public void whenEnd() {
			if (isDisabled) return;
			
			attacker.sendMessage("§6§l[ 오버차지 해제 ]");
			attacker.removeMetadata(OVERCHARGE_META, MalaMMO_Skill.plugin);
		}
		
		private void disable()
		{
			isDisabled = true;
			this.durationCounter = 9999;
		}
	}
}

class Invoke_OverCharge_Handler extends MalaPassiveSkill implements Listener
{
	public Invoke_OverCharge_Handler()
	{
		super(	"INVOKE_OVERCHARGE",
				"마술식 - 오버 차지",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_LIGHTNING,
				"&e즉시 시전",
				"",
				"&7다음에 시전하는 마술식 마법의 위력을 33% 강화합니다.",
				"&7마법에 따라 강화되는 수치가 다릅니다.",
				"&7아무것도 하지 않고 10초가 지나면 해제되며,",
				"&7다시 시전할 경우 유지 시간이 갱신되고 효과가 더욱 강화됩니다.",
				"&7최대 3회까지 강화할 수 있습니다.",
				"&c오버 차지 도중에 적에게 피해를 받을 경우,",
				"&c오버 차지 효과 및 HP의 일정량을 잃습니다.");
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler
	public void overchargeDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof LivingEntity)
			Invoke_OverCharge.damageOvercharge((LivingEntity)event.getEntity());
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void overchargeUse(SpellCastEvent event) {
		if (event.getSpell() instanceof OverChargeSpell)
			return;
		event.getSpell().spellPower *= Invoke_OverCharge.useOvercharge(event.getCaster());
	}
}

