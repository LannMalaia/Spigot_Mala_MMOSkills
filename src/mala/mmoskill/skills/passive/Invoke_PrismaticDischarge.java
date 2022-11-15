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
			// ������
			new Effect(circleLocation, Particle.DUST_COLOR_TRANSITION)
				.setDustTransition(dts)
				.append2DImage("prismaticCircle.png", 2)
				.scale(size)
				.setVelocity(0, 0, 0)
				.rotatePoint(0, durationCounter * 0.4, 0)
				.playEffect();
			
			if (durationCounter > 60) {
				// �� ������
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
				"������ - �������ƽ ������",
				Material.WRITABLE_BOOK,
				"��93�ܰ� ����",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE,
				"",
				"&7�ֺ� 15m���� ���鿡�� ���ظ� ���ϸ� ������ ������ �ο��մϴ�.",
				"&7���� ���ӿ� ����� ���ظ� �ָ� ������ ������ŵ�ϴ�.",
				"&7������ ��� ������ ��ҵ˴ϴ�.");
	}
}

