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

			// 캐시값 비교, 갱신
			int ocCount = player.getMetadata(OVERCHARGE_META).get(0).asInt();
			if (ocCountCache != ocCount)
			{
				durationCounter = 1;
				ocCountCache = ocCount;
				player.sendMessage("§6§l[ " +
						(ocCount == 1 ? "§e§l" : ocCount == 2 ? "§6§l" : "§b§l") +
						"오버차지 " + ocCount + "단계" + " §6§l]");
			}

			// 회전 회오리
			rollCounter++;
			size = size + (((1.0 + 1.0 * ocCount) - size) * 0.05);
			Particle flame = ocCount == 1 ? Particle.SMALL_FLAME : ocCount == 2 ? Particle.FLAME : Particle.SOUL_FIRE_FLAME;
			Particle smoke = ocCount <= 2 ? Particle.SMOKE_NORMAL : Particle.SOUL;
			
			// 단계에 맞춰 효과를 추가 제거
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
			
			player.sendMessage("§6§l[ 오버차지 해제 ]");
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
				"마술식 - 오버 차지",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_LIGHTNING,
				"&e즉시 시전",
				"",
				"&7다음에 시전하는 마술식 마법의 피해량을 강화합니다.",
				"&7아무것도 하지 않고 10초가 지나면 해제되며,",
				"&7다시 시전할 경우 유지 시간이 갱신되고 효과가 더욱 강화됩니다.",
				"&7최대 3회까지 강화할 수 있습니다.",
				"&c오버 차지 도중에 적에게 피해를 받을 경우,",
				"&c오버 차지 효과 및 HP의 일정량을 잃습니다.");
	}
}

