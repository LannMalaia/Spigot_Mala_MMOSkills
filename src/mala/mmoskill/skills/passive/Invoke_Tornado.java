package mala.mmoskill.skills.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Tornado extends RegisteredSkill
{
	public Invoke_Tornado()
	{	
		super(new Invoke_MasterSpark_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class TornadoSpell extends MalaSpell
	{
		public TornadoSpell(PlayerData playerData)
		{
			super(playerData, 10.0);
		}
		
		Vector dir;
		int ringCount = 12; // 회오리 높이
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
		}
		
		@Override
		public void whenCount() {
			// targetLocation.add(dir.clone().multiply(0.03));
			
			// 회오리 밑 빨아들이는 그거
			if (durationCounter % 4 == 0) {
				for (int count = 0; count < 8; count++) {
					new Effect(targetLocation, Particle.CRIT)
						.append2DArc(120.0, 2.0 + count * 1.0)
						.rotate(-10.0, Math.random() * 360.0, 0)
						.scaleVelocity(-0.15)
						.playEffect();
				}
			}
			
			// 회오리 그리기
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
			
			// 회오리 주변 잔상
			if (durationCounter % 5 == 0) {
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
			
			// 전기 발싸
			if (durationCounter % randomDuration == 0) {
				for (int count = 0; count < ringCount / 4; count += 1) {
					Location loc = targetLocation.clone().add(0, Math.random() * height * ringCount, 0);
					new Effect(loc, Particle.CRIT)
						.append3DLightningLine(10.0, 2.0 + Math.random() * 4.0, 3)
						.rotate(Math.random() * 20.0, Math.random() * 360.0, 0)
						.scaleVelocity(0.0)
						.playEffect();
				}
				randomDuration = (int)(5 + Math.random() * 10.0);
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
				"",
				"&7해당 위치에 소용돌이를 일으킵니다.",
				"&7소용돌이에 휘말린 적들은 위로 떠오르며 피해를 입습니다.");
	}
}

