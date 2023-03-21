package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Fire extends RegisteredSkill
{
	private static Invoke_Fire instance;
	
	public Invoke_Fire()
	{	
		super(new Invoke_Fire_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage_base", new LinearValue(23.0, 0.0));
		addModifier("damage_add", new LinearValue(5.0, 0.0));
		
		instance = this;
	}

	public static class FireSpell extends MalaSpellEffect
	{
		double damage;

		public FireSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 0.1);
			damage = Invoke_Fire.instance.getModifier("damage_base", 1)
					+ Invoke_Fire.instance.getModifier("damage_add", 1) * Cast_Ignis.getLevel(playerData);
			damage *= spellPower;
			
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 3.0);
		}
		public FireSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 1.0);
			this.damage = damage * spellPower;
		}
		
		double randPitch, randYaw;
		
		@Override
		public void whenStart() {
			// world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.4f);
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
			randPitch = Math.random() * 360.0;
			randYaw = Math.random() * 360.0;
			if (Particle_Manager.isReduceMode(attacker)) {
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(3.0, 0.5).append2DShape(3, 3.0, 2.0)
					.rotate(randPitch, randYaw, 0.0)
					.setPoint(0, 0, 0)
					.scaleVelocity(0.15)
					.addSound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0, 0.8)
					.playEffect();
			} else {
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(3.0).append2DShape(3, 3.0)
					.rotate(randPitch, randYaw, 0.0)
					.setPoint(0, 0, 0)
					.scaleVelocity(0.15)
					.addSound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0, 0.8)
					.playEffect();
			}
		}
		
		@Override
		public void whenCount() {
			Effect arcRing = new Effect(targetLocation, Particle.CRIT)
				.append2DArc(60.0, 3.0).rotate(0, 120, 0)
				.append2DArc(60.0, 3.0).rotate(0, 120, 0)
				.append2DArc(60.0, 3.0).rotate(0, 120, 0)
				.rotate(0, this.durationCounter * 8.0, 0)
				.scaleVelocity(0.2);
			arcRing.playEffect();
			arcRing.scaleVelocity(-1.0).playEffect();
		}
		
		@Override
		public void whenEnd() {
			world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
			if (Particle_Manager.isReduceMode(attacker)) {
				for (int i = 0; i < 3; i++)
				{
					randPitch = Math.random() * 360.0;
					randYaw = Math.random() * 360.0;
					new Effect(targetLocation, Particle.SMALL_FLAME)
						.append2DArc(240.0, 5.0, 0.5)
						.rotate(randPitch, randYaw, 0)
						.scale(0.12)
						.playAnimation(12);
						
					Particle_Drawer_Expand.drawArc(targetLocation, Particle.SMALL_FLAME, 5.0, 240.0,
							randPitch, randYaw, 0.0, 0.6, 12, false);
					if (i % 2 == 0)
						new Effect(targetLocation, Particle.SMOKE_NORMAL)
							.append2DArc(240.0, 5.0, 0.5)
							.rotate(randPitch, randYaw, 0)
							.scale(0.1)
							.playAnimation(10);
					if (i % 3 == 0)
						new Effect(targetLocation, Particle.SMOKE_LARGE)
							.append2DArc(240.0, 5.0, 0.5)
							.rotate(randPitch, randYaw, 0)
							.scale(0.08)
							.playAnimation(8);
				}
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(3.0, 0.5)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.2);
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(2.6, 0.5)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.4);
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(2.2, 0.5)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.6);
				
				world.spawnParticle(Particle.LAVA, targetLocation, 20, 0.5, 0.5, 0.5, 0);
				world.spawnParticle(Particle.FLASH, targetLocation, 1, 0, 0, 0, 0);
			} else {
				for (int i = 0; i < 9; i++)
				{
					randPitch = Math.random() * 360.0;
					randYaw = Math.random() * 360.0;
					new Effect(targetLocation, Particle.SMALL_FLAME)
						.append2DArc(240.0, 5.0)
						.rotate(randPitch, randYaw, 0)
						.scale(0.12)
						.playAnimation(12);
						
					Particle_Drawer_Expand.drawArc(targetLocation, Particle.SMALL_FLAME, 5.0, 240.0,
							randPitch, randYaw, 0.0, 0.6, 12, false);
					if (i % 2 == 0)
						new Effect(targetLocation, Particle.SMOKE_NORMAL)
							.append2DArc(240.0, 5.0)
							.rotate(randPitch, randYaw, 0)
							.scale(0.1)
							.playAnimation(10);
					if (i % 3 == 0)
						new Effect(targetLocation, Particle.SMOKE_LARGE)
							.append2DArc(240.0, 5.0)
							.rotate(randPitch, randYaw, 0)
							.scale(0.08)
							.playAnimation(8);
				}
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(3.0)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.2);
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(2.6)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.4);
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(2.2)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.6);
				new Effect(targetLocation, Particle.FLAME)
					.append2DCircle(1.0)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 1.0);
				
				world.spawnParticle(Particle.LAVA, targetLocation, 80, 0.5, 0.5, 0.5, 0);
				world.spawnParticle(Particle.FLASH, targetLocation, 1, 0, 0, 0, 0);
			}
			
			AttackUtil.attackSphere(attacker,
					targetLocation, 3.0,
					damage, null,
					DamageType.SKILL, DamageType.MAGIC);
		}
	}
}

class Invoke_Fire_Handler extends MalaPassiveSkill
{
	public Invoke_Fire_Handler()
	{
		super(	"INVOKE_FIRE",
				"마술식 - 파이어",
				Material.WRITABLE_BOOK,
				"§91단계 술식",
				MsgTBL.MAGIC_FIRE,
				"",
				"&7바라보는 위치 주변 3m에 작은 불꽃을 일으킵니다.",
				"&7불길에 휘말린 적들은 피해를 입고 발화 상태가 됩니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 이그니스 &f&l]"
			);
	}
}

