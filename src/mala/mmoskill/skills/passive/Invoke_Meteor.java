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
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
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

public class Invoke_Meteor extends RegisteredSkill
{
	private static Invoke_Meteor instance;
	
	public Invoke_Meteor()
	{	
		super(new Invoke_Meteor_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_base", new LinearValue(300.0, 0.0));
		addModifier("damage_add", new LinearValue(30.0, 0.0));
		
		instance = this;
	}

	public static class MeteorSpell extends MalaSpellEffect
	{
		int arcCount = 2;
		double startY = 100.0;
		double[] randPitch, randYaw;
		double size = 4.0, speed = startY / 15.0 / 20.0;
		double pitch = 90.0, yaw = 0.0, roll = 0.0;
		Location ballLocation, originLocation;
		DustTransition dts = new DustTransition(Color.ORANGE, Color.BLACK, 1.5f);
		double damage;
		
		public MeteorSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 9.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Ignis.getLevel(playerData);
			damage *= 1.0 + 0.5 * (spellPower - 1.0);
			speed = startY / targetDuration / 20.0;
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 90.0);
		}
		public MeteorSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 9.0);
			this.damage = damage;
		}

		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2.0f);
			
			ballLocation = targetLocation.clone().add(0, startY, 0);
			originLocation = attacker.getLocation().clone();
			randPitch = new double[arcCount];
			randYaw = new double[arcCount];
			for (int i = 0; i < arcCount; i++)
			{
				randPitch[i] = Math.random() * -10.0;
				randYaw[i] = Math.random() * 360.0;
			}
		}
		
		@Override
		public void whenCount() {
			if (durationCounter % 15 == 0 && ballLocation.distance(targetLocation) > 20.0) {
				if (Particle_Manager.isReduceMode(attacker)) {
					new Effect(targetLocation, Particle.FLAME)
						.append2DCircle(25, 0.5)
						.setVelocity(0, 0, 0)
						.playEffect();
				} else {
					new Effect(targetLocation, Particle.FLAME)
						.append2DImage("meteorCircle.png", 2)
						.setVelocity(0, 0, 0)
						.playEffect();
				}
			}
			if (Particle_Manager.isReduceMode(attacker)) {
				Particle_Drawer_EX.drawRandomSphere(ballLocation, Particle.FLAME,
						(int)(size * 30), size, -0.0);
			} else {
				Particle_Drawer_EX.drawRandomSphere(ballLocation, Particle.FLAME,
						(int)(size * 100), size, -0.0);
			}
			ballLocation.add(0, -speed, 0);

			Location waveLoc = ballLocation.clone().add(0.0, -size * 1.4, 0.0);

			if (Particle_Manager.isReduceMode(attacker)) {
				Particle_Drawer_Expand.drawCircle(waveLoc, Particle.CRIT, size,
						0.0, 0.0,
						0, 0, size * 2.0);
			} else {
				for (int i = 0; i < 3; i++)
				{
					Particle_Drawer_EX.drawArc(ballLocation.clone().add(0, 0.2, 0), Particle.FLAME,
					size, 120.0,
					90.0, 0.0, Math.random() * 360.0,
					60.0, 0.02 * i);
					Particle_Drawer_EX.drawArc(ballLocation, Particle.SMOKE_NORMAL,
					size + 0.1, 120.0,
					90.0, 0.0, Math.random() * 360.0,
					60.0, Math.random() * -0.03);
				}
				Particle_Drawer_EX.drawCircleRandomize(ballLocation, Particle.CAMPFIRE_COSY_SMOKE, size * 0.9,
						0.0, 0.0,
						0, 0, size * -0.0015, size * -0.0017);
				Particle_Drawer_Expand.drawCircle(waveLoc, Particle.CRIT, size,
						0.0, 0.0,
						0, 0, size * 2.0);
				Particle_Drawer_Expand.drawCircle(waveLoc, Particle.SMOKE_LARGE, size,
						0.0, 0.0,
						3, this.durationCounter * -4.0, 0.6);
				Particle_Drawer_Expand.drawCircle(waveLoc, Particle.SMOKE_LARGE, size,
						0.0, 0.0,
						3, this.durationCounter * 4.0, 0.6);
			}
			
			if (targetLocation.distance(ballLocation) <= size)
				durationCounter = 9999;
		}
		
		@Override
		public void whenEnd() {

			AttackUtil.attackSphere(attacker, ballLocation, 25.0, damage, (target) -> {
				Vector vec = target.getLocation().subtract(ballLocation).toVector();
				double power = (25.0 - vec.length()) * 0.3;
				vec.normalize().setY(0.5);
				target.setVelocity(vec.multiply(power));
				target.setFireTicks(400);
			}, DamageType.MAGIC, DamageType.SKILL);

			if (Particle_Manager.isReduceMode(attacker)) {
				world.playSound(targetLocation, Sound.ITEM_TOTEM_USE, 2.0f, 0.8f);
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Runnable() {
					double circleSize = 1.0;
					double targetSize = 25.0;
					
					public void run()
					{
						Particle_Drawer_EX.drawRandomSphere(targetLocation, Particle.EXPLOSION_LARGE,
								(int)(circleSize * 5), circleSize, 0.0);
						world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, Math.min(2.0f, (float)size * 0.1f), 0.8f);
						world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, Math.min(2.0f, (float)size * 0.1f), 0.8f);

						circleSize += 1.5;
						if (circleSize < targetSize)
							Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
					}
				});
			} else {
				Particle_Drawer_EX.drawRandomSphere(ballLocation, Particle.CAMPFIRE_COSY_SMOKE,
						(int)(size * 100), size, size * -0.004);
				
				Particle_Drawer_Expand.drawRandomSphere(targetLocation, Particle.FLAME,
						360, 5.0,
						0.1, 0.4);
				world.playSound(targetLocation, Sound.ITEM_TOTEM_USE, 2.0f, 0.8f);
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Runnable() {
					double circleSize = 1.0;
					double targetSize = 25.0;
					
					public void run()
					{
						for (int i = 0; i < 2; i++, circleSize += 0.6)
						{
							double randPitch = -10.0 + Math.random() * -30.0, randYaw = Math.random() * 360.0;
							Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT,
									circleSize, 120.0,
									randPitch, randYaw,
									0.0, 2.0);
							Particle_Drawer_EX.drawArc(targetLocation, Particle.CLOUD,
									circleSize, 120.0,
									randPitch, randYaw,
									0.0, 0.02);
							Particle_Drawer_Expand.drawArc(targetLocation, Particle.CLOUD,
									circleSize, 120.0,
									randPitch, randYaw,
									0.0, 1.2 + i * 0.6, 12, false);
							Particle_Drawer_EX.drawCircleUpRandomize(targetLocation, Particle.SMOKE_LARGE,
									circleSize,
									0.0, 0.0,
									0, 0.0, 2.0);
							Particle_Drawer_EX.drawCircle(targetLocation, Particle.LAVA,
									circleSize,
									0.0, 0.0,
									0, 0.0, 2.0);
						}
						Particle_Drawer_EX.drawRandomSphere(targetLocation, Particle.EXPLOSION_LARGE,
								(int)(circleSize * 5), circleSize, 0.0);
						world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, Math.min(2.0f, (float)size * 0.1f), 0.8f);
						world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, Math.min(2.0f, (float)size * 0.1f), 0.8f);
					
						if (circleSize < targetSize)
							Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
					}
				});
			}
		}
	}
}

class Invoke_Meteor_Handler extends MalaPassiveSkill
{
	public Invoke_Meteor_Handler()
	{
		super(	"INVOKE_METEOR",
				"마술식 - 메테오 스트라이크",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE,
				"",
				"&7바라보는 위치에 거대한 운석을 불러옵니다.",
				"&7운석은 매우 천천히 떨어져, 주변 25m를 강타해 커대한 피해를 가합니다.",
				"&7적들은 운석과 가까울수록 큰 피해와 함께 멀리 날아가며, 발화에 걸립니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 이그니스 &f&l]");
	}
}

