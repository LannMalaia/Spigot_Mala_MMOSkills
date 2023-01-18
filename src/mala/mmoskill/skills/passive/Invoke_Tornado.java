package mala.mmoskill.skills.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Tornado extends RegisteredSkill
{
	private static Invoke_Tornado instance;
	
	public Invoke_Tornado()
	{	
		super(new Invoke_Tornado_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_base", new LinearValue(15.0, 0.0));
		addModifier("damage_add", new LinearValue(1.5, 0.0));
		addModifier("duration_base", new LinearValue(2.0, 0.0));
		addModifier("duration_add", new LinearValue(0.03, 0.0));
		
		instance = this;
	}

	public static class TornadoSpell extends MalaSpellEffect
	{
		double damage;
		public TornadoSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 10.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Ventus.getLevel(playerData);
			targetDuration = instance.getModifier("duration_base", 1)
					+ instance.getModifier("duration_add", 1) * Cast_Glacia.getLevel(playerData);
			targetDuration *= spellPower;
			
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 10.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 17.0);
		}
		public TornadoSpell(LivingEntity attacker, double damage, double duration)
		{
			super(attacker, 10.0);
			this.damage = damage;
			this.targetDuration = duration * spellPower;
		}
		
		Vector dir;
		int ringCount = 12; // 쌓을 링의 수
		double height = 1.0; // 링간 높이
		double ringHeight = 2.2; // 링 자체의 높이
		double additive = 0.1; // 링 높이 간의 갭
		double[] randPitch, randYaw;
		int randomDuration = 1;
		
		@Override
		public void whenStart() {
			dir = frontLocation.getDirection().clone().setY(0.0);
			randPitch = new double[ringCount];
			randYaw = new double[ringCount];
			for (int i = 0; i < ringCount; i++) {
				randPitch[i] = Math.random() * 20.0;
				randYaw[i] = Math.random() * 360.0;
			}
			new Effect(targetLocation, Particle.CLOUD)
				.addSound(Sound.ITEM_ELYTRA_FLYING, 2.0, 2.0)
				.append2DCircle(10.0)
				.playEffect();
		}
		
		@Override
		public void whenCount() {
			// targetLocation.add(dir.clone().multiply(0.03));
			
			// 회오리 밑 빨아들이는 그거
			if (durationCounter % 4 == 0) {
				if (Particle_Manager.isReduceMode(attacker)) {
					for (int count = 0; count < 2; count++) {
						new Effect(targetLocation, Particle.CRIT)
							.append2DArc(120.0, 2.0 + count * 1.0)
							.rotate(-10.0, Math.random() * 360.0, 0)
							.scaleVelocity(-0.15)
							.playEffect();
					}
				} else {
					for (int count = 0; count < 8; count++) {
						new Effect(targetLocation, Particle.CRIT)
							.append2DArc(120.0, 2.0 + count * 1.0)
							.rotate(-10.0, Math.random() * 360.0, 0)
							.scaleVelocity(-0.15)
							.playEffect();
					}
				}
			}
			
			// 회오리 그리기
			if (!Particle_Manager.isReduceMode(attacker)) {
				for (int count = 0; count < ringCount; count += 1) {
					if (durationCounter % ringCount != count)
						continue;
					Location loc = targetLocation.clone().add(0, height * count, 0);
					Effect ring = new Effect(loc, Particle.CLOUD);
					for (double d = 0.0; d < ringHeight; d += additive) {
						ring.append2DCircle(1.0 + count * 0.3, 0.4).translate(0, additive, 0);
					}
					ring.rotate(0, durationCounter * 1.5, 0);
					if (count % 2 == 0)
						ring.reverse();
					ring.velocityToAfterPoint()
						.scaleVelocity(0.3)
						.rotate(randPitch[count], randYaw[count], 0)
						.playEffect();
				}
			}
			
			// 회오리 주변 잔상
			if (durationCounter % 5 == 0) {
				if (Particle_Manager.isReduceMode(attacker)) {
					for (int count = 0; count < ringCount / 2; count += 2) {
						Location loc = targetLocation.clone().add(0, height * count, 0);
						Effect arc = new Effect(loc, Particle.CLOUD)
							.append2DArc(120.0, 1.4 + count * 0.5)
							.translate(0, ringHeight * 0.5, 0)
							.rotate(Math.random() * 20.0, Math.random() * 360.0, 0);
						if (count % 2 == 0)
							arc.reverse();
						arc.velocityToAfterPoint()
							.scaleVelocity(0.3)
							.rotate(randPitch[count], randYaw[count], 0)
							.playEffect();
					}
				} else {
					for (int count = 0; count < ringCount; count += 1) {
						Location loc = targetLocation.clone().add(0, height * count, 0);
						Effect arc = new Effect(loc, Particle.CLOUD)
							.append2DArc(120.0, 1.4 + count * 0.5)
							.translate(0, ringHeight * 0.5, 0)
							.rotate(Math.random() * 20.0, Math.random() * 360.0, 0);
						if (count % 2 == 0)
							arc.reverse();
						arc.velocityToAfterPoint()
							.scaleVelocity(0.3)
							.rotate(randPitch[count], randYaw[count], 0)
							.playEffect();
					}
				}
			}
			
			// 전기 발싸
			if (durationCounter % randomDuration == 0) {
				if (!Particle_Manager.isReduceMode(attacker)) {
					for (int count = 0; count < ringCount / 4; count += 1) {
						Location loc = targetLocation.clone().add(0, Math.random() * height * ringCount, 0);
						new Effect(loc, Particle.CRIT)
							.append3DLightningLine(10.0, 2.0 + Math.random() * 4.0, 3)
							.rotate(Math.random() * 20.0, Math.random() * 360.0, 0)
							.scaleVelocity(0.0)
							.playEffect();
					}
				}
				randomDuration = (int)(5 + Math.random() * 10.0);
			}
			
			if (durationCounter % 5 == 0) {
				AttackUtil.attackCylinder(attacker,
						targetLocation, 7.0, 22.0,
						damage, (target) -> {
							Vector dir = target.getLocation().subtract(targetLocation).toVector();
							target.setVelocity(dir.multiply(-0.3).setY(0.6));
						}, DamageType.MAGIC, DamageType.SKILL);
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_Tornado_Handler extends MalaPassiveSkill
{
	public Invoke_Tornado_Handler()
	{
		super(	"INVOKE_TORNADO",
				"마술식 - 토네이도",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_ICE,
				"&e즉시 시전",
				"",
				"&7해당 위치에 소용돌이를 일으킵니다.",
				"&7소용돌이에 휘말린 적들은 위로 떠오르며 피해를 입습니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 벤투스 &f&l]",
				"&f&l[ &b지속 시간 &f{duration_base} + {duration_add} * 글래시아 &f&l]");
	}
}

