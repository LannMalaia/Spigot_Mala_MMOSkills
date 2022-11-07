package mala.mmoskill.skills.passive;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_OverCharge extends RegisteredSkill
{
	public static final String OVERCHARGE_META = "malammo_skill.overcharge.count";
	
	public Invoke_OverCharge()
	{	
		super(new Invoke_OverCharge_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class OverChargeSpell extends MalaSpell
	{
		public OverChargeSpell(PlayerData playerData)
		{
			super(playerData, 10.0);
		}
		
		boolean isDisabled = false;
		int ocCountCache = 0;
		int rollCounter = 0;
		double height = 0.5;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
			
			int ocCount = 0;
			if (player.hasMetadata(OVERCHARGE_META))
			{
				ocCount = player.getMetadata(OVERCHARGE_META).get(0).asInt() + 1;
				player.setMetadata(OVERCHARGE_META, new FixedMetadataValue(MalaMMO_Skill.plugin, Math.min(3, ocCount)));
				disable();
			}
			else
			{
				player.setMetadata(OVERCHARGE_META, new FixedMetadataValue(MalaMMO_Skill.plugin, 1));
			}
		}
		
		DustTransition dts = new DustTransition(Color.ORANGE, Color.YELLOW, 0.5f);
		double size = 0.5, rollingSpeed = 5.0;
		
		@Override
		public void whenCount() {
			if (!player.hasMetadata(OVERCHARGE_META))
				disable();
			if (isDisabled) return;

			// ĳ�ð� ��, ����
			int ocCount = player.getMetadata(OVERCHARGE_META).get(0).asInt();
			if (ocCountCache != ocCount)
			{
				durationCounter = 1;
				ocCountCache = ocCount;
				player.sendMessage("��6��l[ " +
						(ocCount == 1 ? "��e��l" : ocCount == 2 ? "��6��l" : "��b��l") +
						"�������� " + ocCount + "�ܰ�" + " ��6��l]");
			}

			// ȸ�� ȸ����
			rollCounter++;
			size = size + (((1.0 + 1.0 * ocCount) - size) * 0.05);
			Particle flame = ocCount == 1 ? Particle.SMALL_FLAME : ocCount == 2 ? Particle.FLAME : Particle.SOUL_FIRE_FLAME;
			Particle smoke = ocCount <= 2 ? Particle.SMOKE_NORMAL : Particle.SOUL;
			
			// �ܰ迡 ���� ȿ���� �߰� ����
			switch (ocCount)
			{
			case 3:
				Particle_Drawer_Expand.drawCircle(playerCenterLocation, flame,
						size,
						0.0, 0.0,
						3, rollCounter * -rollingSpeed, 0.25);
			case 2:
				Particle_Drawer_EX.drawCircleUpRandomize(playerCenterLocation, smoke,
						size,
						0.0, 0.0,
						3, rollCounter * -rollingSpeed, 0.1);
				Particle_Drawer_EX.drawCircle(playerCenterLocation, flame,
						size,
						0.0, 0.0,
						3, rollCounter * -rollingSpeed, 0.01);
			case 1:
				Particle_Drawer_EX.drawCircleUpRandomize(playerCenterLocation, flame,
						size,
						0.0, 0.0,
						3, rollCounter * -rollingSpeed, 0.03 + 0.03 * size);
				Particle_Drawer_EX.drawCircle(playerCenterLocation, flame,
						size,
						0.0, 0.0,
						3, rollCounter * -rollingSpeed, -0.01 * size);
			}
		}
		
		@Override
		public void whenEnd() {
			if (isDisabled) return;
			
			player.sendMessage("��6��l[ �������� ���� ]");
			player.removeMetadata(OVERCHARGE_META, MalaMMO_Skill.plugin);
		}
		
		private void disable()
		{
			isDisabled = true;
			this.durationCounter = 9999;
		}
	}
}

class Invoke_OverCharge_Handler extends MalaPassiveSkill
{
	public Invoke_OverCharge_Handler()
	{
		super(	"INVOKE_OVERCHARGE",
				"������ - ���� ����",
				Material.WRITABLE_BOOK,
				"��92�ܰ� ����",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_LIGHTNING,
				"&e��� ����",
				"",
				"&7������ �����ϴ� ������ ������ ���ط��� ��ȭ�մϴ�.",
				"&7�ƹ��͵� ���� �ʰ� 10�ʰ� ������ �����Ǹ�,",
				"&7�ٽ� ������ ��� ���� �ð��� ���ŵǰ� ȿ���� ���� ��ȭ�˴ϴ�.",
				"&7�ִ� 3ȸ���� ��ȭ�� �� �ֽ��ϴ�.",
				"&c���� ���� ���߿� ������ ���ظ� ���� ���,",
				"&c���� ���� ȿ�� �� HP�� �������� �ҽ��ϴ�.");
	}
}

