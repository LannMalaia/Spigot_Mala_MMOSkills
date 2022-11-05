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

public class Invoke_Fire extends RegisteredSkill
{
	public Invoke_Fire()
	{	
		super(new Invoke_Fire_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class FireSpell extends MalaSpell
	{
		public FireSpell(PlayerData playerData)
		{
			super(playerData, 1.5);
		}
		
		double randPitch, randYaw;
		
		@Override
		public void whenStart() {
			// world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.4f);
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
			randPitch = Math.random() * 360.0;
			randYaw = Math.random() * 360.0;
			Particle_Drawer_Expand.drawCircle(targetLocation, Particle.FLAME, 3.0, 0.0, randYaw, 0, 0, 0.15);
			Particle_Drawer_Expand.drawTriangle(targetLocation, Particle.FLAME, 3.0, 0.0, randYaw, 0, 0.15);
		}
		
		@Override
		public void whenCount() {
			Particle_Drawer_EX.drawCircle(targetLocation, Particle.CRIT, 3.0, 0.0, randYaw,
					3, this.durationCounter * 8.0, -0.2);
			Particle_Drawer_EX.drawCircle(targetLocation, Particle.CRIT, 3.0, 0.0, randYaw,
					3, this.durationCounter * 8.0, 0.2);
		}
		
		@Override
		public void whenEnd() {
			world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
			for (int i = 0; i < 9; i++)
			{
				randPitch = Math.random() * 360.0;
				randYaw = Math.random() * 360.0;
				Particle_Drawer_Expand.drawArc(targetLocation, Particle.SMALL_FLAME, 5.0, 240.0,
						randPitch, randYaw, 0.0, 0.6, 12, false);
				if (i % 2 == 0)
					Particle_Drawer_Expand.drawArc(targetLocation, Particle.SMOKE_NORMAL, 5.0, 240.0,
							randPitch, randYaw, 0.0, 0.58, 10, false);
				if (i % 3 == 0)
					Particle_Drawer_Expand.drawArc(targetLocation, Particle.SMOKE_LARGE, 5.0, 240.0,
							randPitch, randYaw, 0.0, 0.62, 8, false);
			}
			Particle_Drawer_EX.drawCircleUpRandomize(targetLocation, Particle.FLAME, 3.0,
					0.0, 0.0,
					0, 0.0, 0.2);
			Particle_Drawer_EX.drawCircleUpRandomize(targetLocation, Particle.FLAME, 2.6,
					0.0, 0.0,
					0, 0.0, 0.4);
			Particle_Drawer_EX.drawCircleUpRandomize(targetLocation, Particle.FLAME, 2.2,
					0.0, 0.0,
					0, 0.0, 0.6);
			Particle_Drawer_EX.drawCircleUpRandomize(targetLocation, Particle.FLAME, 1.0,
					0.0, 0.0,
					0, 0.0, 1.0);
			
			world.spawnParticle(Particle.LAVA, targetLocation, 80, 0.5, 0.5, 0.5, 0);
			world.spawnParticle(Particle.FLASH, targetLocation, 1, 0, 0, 0, 0);
		}
	}
}

class Invoke_Fire_Handler extends MalaPassiveSkill
{
	public Invoke_Fire_Handler()
	{
		super(	"INVOKE_FIRE",
				"마술식 - 파이어",
				Material.WRITABLE_BOOK,
				"§91단계 술식",
				MsgTBL.MAGIC_FIRE,
				"",
				"&7바라보는 위치 주변 3m에 작은 불꽃을 일으킵니다.",
				"&7불길에 휘말린 적들은 피해를 입고 발화 상태가 됩니다.");
	}
}

