package mala.mmoskill.skills.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
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

public class Invoke_MasterSpark extends RegisteredSkill
{
	private static Invoke_MasterSpark instance;
	
	public Invoke_MasterSpark()
	{	
		super(new Invoke_MasterSpark_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_base", new LinearValue(28.0, 0.0));
		addModifier("damage_add", new LinearValue(2.8, 0.0));
		
		instance = this;
	}

	public static class MasterSparkSpell extends MalaSpellEffect
	{
		double damage;
		public MasterSparkSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 7.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Ventus.getLevel(playerData);
			targetDuration *= 1.0 + ((spellPower - 1.0) * 0.3);
			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 90.0);
		}
		public MasterSparkSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 7.0);
			this.damage = damage;
		}
		
		Location tempLocation, originLocation;
		double length = 50.0;
		double height = 0.5;
		
		@Override
		public void whenStart() {
			tempLocation = this.frontLocation.clone();
			originLocation = attacker.getLocation().clone();
			// 마법진
			if (Particle_Manager.isReduceMode(attacker)) {
			} else {
				new Effect(tempLocation, Particle.END_ROD)
					.append2DImage("mastersparkCircle.png", 2)
					.scalePoint(0.0)
					.scaleVelocity(0.1)
					//.rotate(0, durationCounter, 0)
					.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0.0)
					.playEffect();
			}
		}
		
		@Override
		public void whenCount() {
			if (originLocation.distance(attacker.getLocation()) > 2) {
				cancelled = true;
				return;
			}
			if (!Particle_Manager.isReduceMode(attacker)) {
				if (durationCounter < 20) {
					tempLocation = this.frontLocation.clone();
					if (durationCounter % 5 == 0) {
						int count = durationCounter / 5;
						// 전조 궤적
						Effect beam = new Effect(tempLocation, Particle.END_ROD);
						for (double len = 0; len <= length; len += 0.1) {
							beam.append2DArc(3.0, 2.0 + 0.5 * count).rotate(0, 180, 0)
								.append2DArc(3.0, 2.0 + 0.5 * count).rotate(0, 180, 0)
								.rotate(0, 3.0 * (count % 2 == 0 ? -1 : 1), 0)
								.translatePoint(0, 0.1, 0);
						}
						beam.scalePoint(0.0, 1.0, 0.0)
							.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
							.scaleVelocity(0.15)
							.reverse()
							.addSound(Sound.ENTITY_TNT_PRIMED, 0.5 + 0.3 * count, 1.0 + 0.15 * count)
							.playAnimation(40 + 20 * count);
					}
				}
			}
			if (durationCounter == 20) {
				new Effect(tempLocation, Particle.END_ROD)
					.addSound(Sound.ITEM_TOTEM_USE, 2.0, 2.0)
					.playEffect();
				// 마법진
				if (!Particle_Manager.isReduceMode(attacker)) {
					new Effect(tempLocation, Particle.SOUL_FIRE_FLAME)
						.append2DImage("mastersparkCircle.png", 2)
						.scalePoint(0.0)
						.scaleVelocity(0.08)
						//.rotate(0, durationCounter, 0)
						.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0.0)
						.playEffect();
				}
			}
			if (durationCounter >= 20) {
				// 발사 이펙트
				if (!Particle_Manager.isReduceMode(attacker)) {
					for (int i = 0; i < 1; i++) {
						new Effect(tempLocation, Particle.END_ROD)
							.append2DCircle(4.0 + 1.5 * i)
							.scale(0.2)
							.scaleVelocity(0.7)
							.translateVelocity(0, 0.8 + 0.2 * i, 0)
							.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.75, 1.0)
							.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
							.playEffect();
					}
					// 빔
					if (durationCounter % 2 == 0) {
						for (double d = 0.5; d <= length; d += 2.0) {
							double vel = Math.cos(Math.min(Math.PI * 0.5, d * 0.25)) * 0.2;
							new Effect(tempLocation, Particle.END_ROD)
								.append2DCircle(0.2 + (2.8 * Math.sin(Math.min(Math.PI * 0.5, d * 0.25))))
								.translatePoint(0, d, 0)
								.scaleVelocity(vel)
								.translateVelocity(0, 1.0, 0)
								.randomizePoint(RANDOMIZE_TYPE.MULTIPLY, 1.0, 0.7, 1.0, 1.0, 1.0, 1.0)
								.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 1.0)
								.rotate(0, Math.random() * 360.0, 0)
								.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
								.playEffect();
						}
					}
				}
				// 빔 궤적
				for (int i = 0; i < 4; i++) {
					Effect ring = new Effect(tempLocation, Particle.CRIT)
						.append2DCircle(4.0)
						.translatePoint(0, (durationCounter + i * (length / 4.0)) % length, 0)
						.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
						.scaleVelocity(0.2);
					if (durationCounter % 3 == 0)
						ring.addSound(Sound.ENTITY_BLAZE_SHOOT, 2.0, 2.0);
					ring.playEffect();
				}
				
				if (durationCounter % 10 == 0) {
					AttackUtil.attackHitbox(attacker,
							tempLocation, new Vector(6.0, 6.0, 50.0),
							new Vector(tempLocation.getPitch(), tempLocation.getYaw(), 0),
							damage, null,
							DamageType.MAGIC, DamageType.SKILL);
				}
			}
			else {
				new Effect(tempLocation, Particle.CRIT)
					.append2DLine(length)
					.rotate(tempLocation.getPitch(), tempLocation.getYaw(), 0)
					.scaleVelocity(0)
					.playEffect();
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_MasterSpark_Handler extends MalaPassiveSkill
{
	public Invoke_MasterSpark_Handler()
	{
		super(	"INVOKE_MASTER_SPARK",
				"마술식 - 마스터 스파크",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7바라보는 방향으로 거대한 전격의 대포를 발사합니다.",
				"&7대포에 휩쓸린 적들은 큰 피해를 입습니다.",
				"&7움직일 경우 시전이 취소됩니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 벤투스 &f&l]");
	}
}

