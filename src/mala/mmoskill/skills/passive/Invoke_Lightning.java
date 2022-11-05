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

public class Invoke_Lightning extends RegisteredSkill
{
	public Invoke_Lightning()
	{	
		super(new Invoke_Lightning_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class LightningSpell extends MalaSpell
	{
		public LightningSpell(PlayerData playerData)
		{
			super(playerData, 4.0);
		}
		
		int arcCount = 2;
		double[] randPitch, randYaw;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);
			randPitch = new double[arcCount];
			randYaw = new double[arcCount];

			randPitch[0] = Math.random() * 360.0;
			randYaw[0] = Math.random() * 360.0;
			Particle_Drawer_Expand.drawCircle(targetLocation, Particle.FIREWORKS_SPARK, 3.0,
					randPitch[0], randYaw[0],
					0, this.durationCounter * 6.0,  0.3);
			Particle_Drawer_Expand.drawStar(targetLocation, Particle.FIREWORKS_SPARK, 3.0,
					randPitch[0], randYaw[0],
					this.durationCounter * 6.0, 0.3);
		}
		
		@Override
		public void whenCount() {

			if (this.durationCounter % 3 == 0)
			{
				for (int i = 0; i < arcCount; i++)
				{
					randPitch[i] = -30.0 + Math.random() * 60.0;
					randYaw[i] = Math.random() * 360.0;
					Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT_MAGIC, 3.0, 120.0,
							randPitch[i], randYaw[i],
							this.targetDuration * 4.0, -0.4);
				}
			}
			if (this.durationCounter > 30)
			{
				if (this.durationCounter % 15 == 0)
				{
					// world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.5f);
					world.playSound(targetLocation, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 2.0f);
					world.spawnParticle(Particle.FLASH, targetLocation, 1, 0, 0, 0, 0);
					for (int i = 0; i < 8; i++)
					{
						double randPitch = Math.random() * 360.0;
						double randYaw = Math.random() * 360.0;
						double length = 3.0 + Math.random() * 4.0;
						Particle_Drawer_EX.drawLightningLine(targetLocation, Particle.CRIT,
								length, 1.0, 4,
								randPitch, randYaw);
					}
				}
			}
		}
		
		@Override
		public void whenEnd() {
			
		}
	}
}

class Invoke_Lightning_Handler extends MalaPassiveSkill
{
	public Invoke_Lightning_Handler()
	{
		super(	"INVOKE_LIGHTNING",
				"������ - ����Ʈ��",
				Material.WRITABLE_BOOK,
				"��91�ܰ� ����",
				MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7�ٶ󺸴� ��ġ �ֺ� 3m�� ���鿡�� ������ �߻��մϴ�.",
				"&7���ݿ� ���� ������ 50%~150%�� ���ظ� �޽��ϴ�.",
				"&7������ ���� �ð� ���ӵǸ� ����ؼ� ���ظ� �ݴϴ�.");
	}
}

