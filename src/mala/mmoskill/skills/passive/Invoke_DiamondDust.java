package mala.mmoskill.skills.passive;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.damage.DamageType;

import org.bukkit.Sound;

import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_DiamondDust extends RegisteredSkill
{
	private static Invoke_DiamondDust instance;
	
	public Invoke_DiamondDust()
	{	
		super(new Invoke_DiamondDust_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("damage_base", new LinearValue(45.0, 0.0));
		addModifier("damage_add", new LinearValue(0.45, 0.0));
		
		instance = this;
	}

	public static class DiamondDustSpell extends MalaSpellEffect
	{
		DustTransition dts = new DustTransition(Color.fromRGB(200, 200, 255), Color.fromRGB(255, 255, 255), 0.5f);
		double size = 1.0 / 12.0, targetSize = 15.0 / 12.0;
		Location circleLocation, originLocation, saveLocation;
		double damage;

		public DiamondDustSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 11.0);

			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Glacia.getLevel(playerData);
			damage *= spellPower;
			
			circleLocation = targetLocation.clone().add(0, 10.0, 0);
			originLocation = targetLocation.clone();
			saveLocation = attacker.getLocation().clone();
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 90.0);
		}
		public DiamondDustSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 11.0);
			this.damage = damage * spellPower;
		}

		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
		}
		
		@Override
		public void whenCount() {
			if (saveLocation.distance(attacker.getLocation()) > 2.0) {
				cancelled = true;
				return;
			}

			double realSize = size * 12.0;
			if (durationCounter < 20) {
				// ������
				if (Particle_Manager.isReduceMode(attacker)) {
					new Effect(originLocation, Particle.DUST_COLOR_TRANSITION)
						.setDustTransition(dts)
						.append2DCircle(realSize)
						.setVelocity(0, 0, 0)
						.rotatePoint(0, durationCounter * 0.4, 0)
						.playEffect();	
				} else {
					new Effect(circleLocation, Particle.DUST_COLOR_TRANSITION)
						.setDustTransition(dts)
						.append2DImage("prismaticCircle.png", 3)
						.scale(size)
						.setVelocity(0, 0, 0)
						.rotatePoint(0, durationCounter * 0.4, 0)
						.playEffect();					
				}
			}
			else {
				// �� ������
				for (int i = 0; i < 4; i++) {
					Location loc = circleLocation.clone().add(
							-realSize + Math.random() * realSize * 2.0,
							0,
							-realSize + Math.random() * realSize * 2.0);
					if (Particle_Manager.isReduceMode(attacker)) {
						new Effect(originLocation, Particle.DUST_COLOR_TRANSITION)
							.setDustTransition(dts)
							.append2DCircle(realSize)
							.setVelocity(0, 0, 0)
							.rotatePoint(0, durationCounter * 0.4, 0)
							.playEffect();
					} else {
						new Effect(loc.clone().add(0, 10, 0), Particle.SNOWFLAKE)
							.append2DLine(20.0, 6.0)
							.rotate(70.0, 0.0, 0.0)
							.scalePoint(0.0)
							.scaleVelocity(0.2)
							.playEffect();
						new Effect(originLocation, Particle.SNOWFLAKE)
							.append2DArc(120.0, 4.0 + 1.0 * i)
							.rotate(-10.0 + Math.random() * 20.0, Math.random() * 360.0, 0.0)
							.translatePoint(0, Math.random() * 10.0, 0.0)
							.scaleVelocity(0.0 + 0.02 * i)
							.playAnimation(20);
					}
				}
				if (durationCounter % 20 == 0)
					world.spawnParticle(Particle.SNOW_SHOVEL, circleLocation, (int)(realSize * 30.0), realSize * 0.5, 0.0, realSize * 0.5, 0);
				
				if (durationCounter % 10 == 0) {
					AttackUtil.attackCylinder(attacker,
							originLocation.clone().add(0, -10, 0), realSize, 20.0, 
							damage, (target) -> {
								Buff_Manager.Increase_Buff(target, PotionEffectType.SLOW,
										0, 100, PotionEffectType.SPEED, 9);
								if (target.hasPotionEffect(PotionEffectType.SLOW)) {
									if (target.getPotionEffect(PotionEffectType.SLOW).getAmplifier() > 8) {
										target.removePotionEffect(PotionEffectType.SLOW);
										new Effect(target.getEyeLocation(), Particle.CRIT)
											.addSound(Sound.BLOCK_GLASS_BREAK, 1.0, 2.0)
											.append3DSphere(3.0)
											.scalePoint(0.0)
											.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 1.0)
											.playEffect();
										AttackUtil.attack(attacker, target,
												damage * 3, null,
												DamageType.MAGIC, DamageType.SKILL);
									}
								}
							}, DamageType.MAGIC, DamageType.SKILL);
				}
			}
			
			size = size + (targetSize - size) * 0.05;
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_DiamondDust_Handler extends MalaPassiveSkill
{
	public Invoke_DiamondDust_Handler()
	{
		super(	"INVOKE_DIAMOND_DUST",
				"������ - ���̾Ƹ�� ����Ʈ",
				Material.WRITABLE_BOOK,
				"��93�ܰ� ����",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE,
				"",
				"&7������ ��ġ 15m���� ���鿡�� ���ظ� ���ϸ� ������ ������ �ο��մϴ�.",
				"&7������ ���� ���� �̻����� ���� ������",
				"&7������ �����ʰ� ���ÿ� ū ���ظ� �Խ��ϴ�.",
				"&7������ ��� ������ ��ҵ˴ϴ�.",
				"",
				"&f&l[ &9���� ���� &f&l]",
				"&f&l[ &c���ط� &f{damage_base} + {damage_add} * �۷��þ� &f&l]");
	}
}

