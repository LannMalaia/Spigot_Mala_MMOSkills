package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustTransition;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Meteor extends RegisteredSkill
{
	public Invoke_Meteor()
	{	
		super(new Invoke_Meteor_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class MeteorSpell extends MalaSpell
	{
		public MeteorSpell(PlayerData playerData)
		{
			super(playerData, 15.0);
		}

		int arcCount = 2;
		double startY = 100.0;
		double[] randPitch, randYaw;
		double size = 4.0, speed = startY / 15.0 / 20.0;
		double pitch = 90.0, yaw = 0.0, roll = 0.0;
		Location ballLocation;
		DustTransition dts = new DustTransition(Color.ORANGE, Color.BLACK, 1.5f);

		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
			
			ballLocation = targetLocation.clone().add(0, startY, 0);
			randPitch = new double[arcCount];
			randYaw = new double[arcCount];
			for (int i = 0; i < arcCount; i++)
			{
				randPitch[i] = Math.random() * -10.0;
				randYaw[i] = Math.random() * 360.0;
			}
			Particle_Drawer_EX.drawCircleUpRandomize(targetLocation, Particle.FLAME, 5.0,
					0.0, 0.0,
					0, this.durationCounter * 0.3, 0.5);
			Particle_Drawer_EX.drawTriangle(targetLocation, Particle.FLAME, 5.0,
					0.0, 0.0,
					this.durationCounter * 0.3, 0.0);
			Particle_Drawer_EX.drawTriangle(targetLocation, Particle.FLAME, 5.0,
					0.0, 180.0,
					this.durationCounter * 0.3, 0.0);
		}
		
		@Override
		public void whenCount() {
			Particle_Drawer_EX.drawRandomSphere(ballLocation, Particle.FLAME,
					(int)(size * 100), size, -0.0);
			ballLocation.add(0, -speed, 0);

			Location waveLoc = ballLocation.clone().add(0.0, -size * 1.4, 0.0);
			for (int i = 0; i < 3; i++)
			{
				Particle_Drawer_EX.drawArc(ballLocation.clone().add(0, 0.2, 0), Particle.FLAME,
				size, 120.0,
				90.0, 0.0, Math.random() * 360.0,
				60.0, 0.02 * i);
				Particle_Drawer_EX.drawArc(ballLocation, Particle.SMOKE_NORMAL,
				size + 0.1, 120.0,
				90.0, 0.0, Math.random() * 360.0,
				60.0, Math.random() * -0.03);
			}
			Particle_Drawer_EX.drawCircleRandomize(ballLocation, Particle.CAMPFIRE_COSY_SMOKE, size * 0.9,
					0.0, 0.0,
					0, 0, size * -0.0015, size * -0.0017);
			Particle_Drawer_Expand.drawCircle(waveLoc, Particle.CRIT, size,
					0.0, 0.0,
					0, 0, size * 2.0);
			Particle_Drawer_Expand.drawCircle(waveLoc, Particle.SMOKE_LARGE, size,
					0.0, 0.0,
					3, this.durationCounter * -4.0, 0.6);
			Particle_Drawer_Expand.drawCircle(waveLoc, Particle.SMOKE_LARGE, size,
					0.0, 0.0,
					3, this.durationCounter * 4.0, 0.6);
			
			if (targetLocation.distance(ballLocation) <= size)
				durationCounter = 9999;
		}
		
		@Override
		public void whenEnd() {
			Particle_Drawer_EX.drawRandomSphere(ballLocation, Particle.CAMPFIRE_COSY_SMOKE,
					(int)(size * 100), size, size * -0.004);
			
			Particle_Drawer_Expand.drawRandomSphere(targetLocation, Particle.FLAME,
					360, 5.0,
					0.1, 0.4);
			world.playSound(targetLocation, Sound.ITEM_TOTEM_USE, 2.0f, 0.8f);
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Runnable() {
				double circleSize = 1.0;
				double targetSize = 25.0;
				
				public void run()
				{
					for (int i = 0; i < 2; i++, circleSize += 0.6)
					{
						double randPitch = -10.0 + Math.random() * -30.0, randYaw = Math.random() * 360.0;
						Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT,
								circleSize, 120.0,
								randPitch, randYaw,
								0.0, 2.0);
						Particle_Drawer_EX.drawArc(targetLocation, Particle.CLOUD,
								circleSize, 120.0,
								randPitch, randYaw,
								0.0, 0.02);
						Particle_Drawer_Expand.drawArc(targetLocation, Particle.CLOUD,
								circleSize, 120.0,
								randPitch, randYaw,
								0.0, 1.2 + i * 0.6, 12, false);
						Particle_Drawer_EX.drawCircleUpRandomize(targetLocation, Particle.SMOKE_LARGE,
								circleSize,
								0.0, 0.0,
								0, 0.0, 2.0);
						Particle_Drawer_EX.drawCircle(targetLocation, Particle.LAVA,
								circleSize,
								0.0, 0.0,
								0, 0.0, 2.0);
					}
					Particle_Drawer_EX.drawRandomSphere(targetLocation, Particle.EXPLOSION_LARGE,
							(int)(circleSize * 5), circleSize, 0.0);
					world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, Math.min(2.0f, (float)size * 0.1f), 0.8f);
					world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, Math.min(2.0f, (float)size * 0.1f), 0.8f);
				
					if (circleSize < targetSize)
						Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
				}
			});
		}
	}
}

class Invoke_Meteor_Handler extends MalaPassiveSkill
{
	public Invoke_Meteor_Handler()
	{
		super(	"INVOKE_METEOR",
				"마술식 - 메테오 스트라이크",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE,
				"",
				"&7바라보는 위치에 거대한 운석을 불러옵니다.",
				"&7운석은 매우 천천히 떨어져, 주변 25m를 강타해 커대한 피해를 가합니다.",
				"&7적들은 운석과 가까울수록 큰 피해와 함께 멀리 날아가며, 발화에 걸립니다.");
	}
}

