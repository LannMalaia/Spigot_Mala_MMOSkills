package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;

import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.LivingEntity;

import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Force extends RegisteredSkill
{
	private static Invoke_Force instance;
	
	public Invoke_Force()
	{	
		super(new Invoke_Force_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("damage", new LinearValue(7.5, 0.0));
		
		instance = this;
	}

	public static class ForceSpell extends MalaSpellEffect
	{
		Location forceLocation;
		double size = 0.5, targetSize = 10.0;
		double damage;
		
		public ForceSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 1.0);
			forceLocation = centerLocation.clone();
			damage = instance.getModifier("damage", 1) * 
					(Cast_Ignis.getLevel(playerData) + Cast_Glacia.getLevel(playerData) + Cast_Ventus.getLevel(playerData));
			damage *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 7.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 7.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 7.0);
		}
		public ForceSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 1.0);
			forceLocation = centerLocation.clone();
			this.damage = damage * spellPower;
		}

		@Override
		public void whenStart() {
			world.playSound(forceLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);

			if (Particle_Manager.isReduceMode(attacker)) {
				for (int i = 0; i < 3; i++) {
					for (int j = 0; j < 1; j++) {
						new Effect(forceLocation, Particle.CLOUD)
						.append2DArc(120.0, 2.0 + 2.0 * i + 0.3 * j, 0.5)
						.scaleVelocity(0.15)
						.rotate(-15.0, Math.random() * 360.0, 0.0)
						.playEffect();
					}
				}
				new Effect(forceLocation, Particle.FIREWORKS_SPARK)
					.addSound(Sound.ENTITY_GENERIC_EXPLODE, 1.0, 2.0)
					.addSound(Sound.ITEM_TOTEM_USE, 1.5, 2.0)
					.append3DSphere(10.0, 0.6)
					.scalePoint(0.0)
					.scaleVelocity(0.2)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.5, 1.2)
					.playEffect();
			} else {
				for (int i = 0; i < 5; i++) {
					for (int j = 0; j < 3; j++) {
						new Effect(forceLocation, Particle.CLOUD)
						.append2DArc(120.0, 2.0 + 2.0 * i + 0.3 * j)
						.scaleVelocity(0.15)
						.rotate(-15.0, Math.random() * 360.0, 0.0)
						.playEffect();
					}
				}
				new Effect(forceLocation, Particle.FIREWORKS_SPARK)
					.addSound(Sound.ENTITY_GENERIC_EXPLODE, 1.0, 2.0)
					.addSound(Sound.ITEM_TOTEM_USE, 1.5, 2.0)
					.append3DSphere(10.0, 1.3)
					.scalePoint(0.0)
					.scaleVelocity(0.2)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.5, 1.2)
					.playEffect();
			}

			world.spawnParticle(Particle.FLASH, forceLocation, 1, 0, 0, 0, 0);
			world.spawnParticle(Particle.EXPLOSION_HUGE, forceLocation, 1, 0, 0, 0, 0);
			
			AttackUtil.attackSphere(attacker, forceLocation, targetSize, damage, (target) -> {
				Vector vec = target.getLocation().subtract(attacker.getLocation()).toVector().normalize();
				target.setVelocity(vec.multiply(3.0));
			}, DamageType.MAGIC, DamageType.SKILL);
		}
		
		@Override
		public void whenCount() {
			size += (targetSize - size) * 0.1;
			if (Particle_Manager.isReduceMode(attacker)) {
				new Effect(forceLocation, Particle.CRIT)
					.append2DCircle(size, 0.5)
					.scaleVelocity(-0.05)
					.rotate(-30.0, 0.0, 0.0)
					.playEffect();
				new Effect(forceLocation, Particle.CRIT)
					.append2DCircle(size, 0.5)
					.scaleVelocity(-0.05)
					.rotate(0.0, 0.0, 0.0)
					.playEffect();
				new Effect(forceLocation, Particle.CRIT)
					.append2DCircle(size, 0.5)
					.scaleVelocity(-0.05)
					.rotate(30.0, 0.0, 0.0)
					.playEffect();
			} else {
				new Effect(forceLocation, Particle.CRIT)
					.append2DCircle(size)
					.scaleVelocity(-0.05)
					.rotate(-30.0, 0.0, 0.0)
					.playEffect();
				new Effect(forceLocation, Particle.CRIT)
					.append2DCircle(size)
					.scaleVelocity(-0.05)
					.rotate(0.0, 0.0, 0.0)
					.playEffect();
				new Effect(forceLocation, Particle.CRIT)
					.append2DCircle(size)
					.scaleVelocity(-0.05)
					.rotate(30.0, 0.0, 0.0)
					.playEffect();
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_Force_Handler extends MalaPassiveSkill
{
	public Invoke_Force_Handler()
	{
		super(	"INVOKE_FORCE",
				"마술식 - 포스",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_LIGHTNING,
				"&e즉시 시전",
				"",
				"&7세 원소를 충돌시켜 큰 압력을 지닌 폭발을 만들어냅니다.",
				"&7주변 적들을 멀리 밀어내며, 큰 피해를 줍니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage} * (이그니스 + 글래시아 + 벤투스) &f&l]"
				);
	}
}

