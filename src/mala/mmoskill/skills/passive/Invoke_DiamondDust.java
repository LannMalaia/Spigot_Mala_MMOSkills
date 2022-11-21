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

public class Invoke_DiamondDust extends RegisteredSkill
{
	public Invoke_DiamondDust()
	{	
		super(new Invoke_DiamondDust_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class DiamondDustSpell extends MalaSpell
	{
		DustTransition dts = new DustTransition(Color.fromRGB(200, 200, 255), Color.fromRGB(255, 255, 255), 0.5f);
		double size = 1.0 / 12.0, targetSize = 15.0 / 12.0;
		Location circleLocation;

		public DiamondDustSpell(PlayerData playerData)
		{
			super(playerData, 13.0);
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
				world.spawnParticle(Particle.SNOW_SHOVEL, circleLocation, (int)(realSize * 30.0), realSize * 0.5, 0.0, realSize * 0.5, 0);
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
				"&7�ֺ� 15m���� ���鿡�� ���ظ� ���ϸ� ������ ������ �ο��մϴ�.",
				"&7������ ���� ���� �̻����� ���� ������",
				"&7������ �����ʰ� ���ÿ� ū ���ظ� �Խ��ϴ�.",
				"&7������ ��� ������ ��ҵ˴ϴ�.");
	}
}

