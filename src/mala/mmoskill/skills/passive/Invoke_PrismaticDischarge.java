package mala.mmoskill.skills.passive;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;

import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_PrismaticDischarge extends RegisteredSkill
{
	public Invoke_PrismaticDischarge()
	{	
		super(new Invoke_PrismaticDischarge_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class PrismaticDischargeSpell extends MalaSpell
	{
		DustTransition dts = new DustTransition(Color.fromRGB(200, 200, 255), Color.fromRGB(255, 255, 255), 0.5f);
		double size = 1.0 / 12.0, targetSize = 15.0 / 12.0;
		Location circleLocation;

		public PrismaticDischargeSpell(PlayerData playerData)
		{
			super(playerData, 15.0);
			circleLocation = playerCenterLocation.clone().add(0, 7.0, 0);
		}

		@Override
		public void whenStart() {
			world.playSound(playerCenterLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
		}
		
		@Override
		public void whenCount() {
			// 마법진
			new Effect(circleLocation, Particle.DUST_COLOR_TRANSITION)
				.setDustTransition(dts)
				.append2DImage("prismaticCircle.png", 2)
				.scale(size)
				.setVelocity(0, 0, 0)
				.rotatePoint(0, durationCounter * 0.4, 0)
				.playEffect();
			
			if (durationCounter > 60) {
				// 눈 내리기
				double realSize = size * 12.0;
				world.spawnParticle(Particle.SNOW_SHOVEL, circleLocation, (int)(realSize * 30.0), realSize, 0.0, realSize, 0);
			}
			
			size = size + (targetSize - size) * 0.05;
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_PrismaticDischarge_Handler extends MalaPassiveSkill
{
	public Invoke_PrismaticDischarge_Handler()
	{
		super(	"INVOKE_PRISMATIC_DISCHARGE",
				"마술식 - 프리즈매틱 디스차지",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE,
				"",
				"&7주변 15m내의 적들에게 피해를 가하며 서서히 구속을 부여합니다.",
				"&7이후 구속에 비례한 피해를 주며 구속을 해제시킵니다.",
				"&7움직일 경우 시전이 취소됩니다.");
	}
}

