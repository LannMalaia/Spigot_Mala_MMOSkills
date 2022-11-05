package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Frost extends RegisteredSkill
{
	public Invoke_Frost()
	{	
		super(new Invoke_Frost_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class FrostSpell extends MalaSpell
	{
		public FrostSpell(PlayerData playerData)
		{
			super(playerData, 1.5);
		}
		
		int arcCount = 3;
		double[] randPitch, randYaw;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
			randPitch = new double[arcCount];
			randYaw = new double[arcCount];
			for (int i = 0; i < arcCount; i++)
			{
				randPitch[i] = -30.0 + Math.random() * 60.0;
				randYaw[i] = Math.random() * 360.0;
			}
		}
		
		@Override
		public void whenCount() {
			Particle_Drawer_EX.drawSquare(targetLocation, Particle.CRIT, 3.0, 0, 0,
					this.durationCounter * 4.0, 0.0);
			Particle_Drawer_EX.drawSquare(targetLocation, Particle.CRIT, 3.0, 0, 0,
					this.durationCounter * 4.0 + 45.0, 0.0);

			for (int i = 0; i < arcCount; i++)
			{
				Particle_Drawer_EX.drawArc(targetLocation, Particle.SNOWFLAKE, 3.0, 60.0,
						randPitch[i], randYaw[i], this.durationCounter * 12.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.05);
			}
		}
		
		@Override
		public void whenEnd() {
			world.playSound(targetLocation, Sound.BLOCK_GLASS_BREAK, 2.0f, 2.0f);
			world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.2f);

			for (int i = 0; i < 12; i++)
			{
				double randPitch = Math.random() * 360.0;
				double randYaw = Math.random() * 360.0;
				double length = 3.0 + Math.random() * 7.0;
				Particle_Drawer_EX.drawLine(targetLocation, Particle.CLOUD, length,
						randPitch, randYaw);
				Particle_Drawer_Expand.drawLine(targetLocation, Particle.CLOUD, length * 0.5,
						randPitch, randYaw, 0.5);
			}
			
			world.spawnParticle(Particle.FLASH, targetLocation, 1, 0, 0, 0, 0);
		}
	}
}

class Invoke_Frost_Handler extends MalaPassiveSkill
{
	public Invoke_Frost_Handler()
	{
		super(	"INVOKE_FROST",
				"마술식 - 프로스트",
				Material.WRITABLE_BOOK,
				"§91단계 술식",
				MsgTBL.MAGIC_ICE,
				"",
				"&7바라보는 위치 주변 3m의 온도를 크게 낮춥니다.",
				"&7냉기에 휘말린 적들은 피해를 입고 구속 상태가 됩니다.");
	}
}

