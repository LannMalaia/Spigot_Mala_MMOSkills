package mala.mmoskill.skills.passive;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
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

public class Invoke_VaporBlast extends RegisteredSkill
{
	public Invoke_VaporBlast()
	{	
		super(new Invoke_VaporBlast_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class VaporBlastSpell extends MalaSpell
	{
		public VaporBlastSpell(PlayerData playerData)
		{
			super(playerData, 5.0);
		}
		
		double height = 0.5;
		
		@Override
		public void whenStart() {
			// world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.4f);
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
//			Particle_Drawer_Expand.drawCircle(targetLocation, Particle.FLAME, 3.0, 0.0, 0.0, 0, 0, 0.15);
//			Particle_Drawer_Expand.drawTriangle(targetLocation, Particle.FLAME, 3.0, 0.0, 0.0, 0, 0.15);
		}
		
		DustTransition dts = new DustTransition(Color.WHITE, Color.GRAY, 0.5f);
		Vector dir = null;
		double maxDistance = 25.0, currDistance = 0.0;
		double speed = 2;
		
		@Override
		public void whenCount() {
			if (this.durationCounter < 40)
			{
				Particle_Drawer_EX.drawTriangle(frontLocation, dts, 3.0,
						frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
						this.durationCounter);
				Particle_Drawer_EX.drawTriangle(frontLocation, dts, 3.0,
						frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
						this.durationCounter + 180.0);
				Particle_Drawer_EX.drawCircle(frontLocation, dts, 1.5,
						frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
						0, this.durationCounter);
			}
			else
			{
				if (dir == null)
				{
					dir = frontLocation.getDirection();
					targetLocation = frontLocation.clone();
				}
				
				double randRoll = Math.random() * 360.0;
				
				if (this.durationCounter % 3 == 0)
				{
					world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 2.0f, 0.7f);
					world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.6f);
				}
				Particle_Drawer_EX.drawArcVelocity(targetLocation, Particle.CRIT,
						4.0, 240.0,
						targetLocation.getPitch(), targetLocation.getYaw(), randRoll,
						90.0 - 120.0, dir, 0.2);
				Particle_Drawer_EX.drawArcVelocity(targetLocation, Particle.EXPLOSION_LARGE,
						3.0, 240.0,
						targetLocation.getPitch(), targetLocation.getYaw(), randRoll,
						90.0 - 120.0, dir, 0.1);
				Particle_Drawer_EX.drawCircle(targetLocation, Particle.WAX_OFF, 4.0,
						targetLocation.getPitch() - 80f + Math.random() * 20f, targetLocation.getYaw() - 10f + Math.random() * 20f,
						0, 0.0, 0.03);
				for (int i = 0; i < 3; i++)
				{
					double randPitch = Math.random() * 360.0, randYaw = Math.random() * 360.0;
					Particle_Drawer_EX.drawCircle(targetLocation, Particle.CRIT, 4.0,
							randPitch, randYaw,
							0, 0.0, 0.05);
					Particle_Drawer_EX.drawArc(targetLocation, Particle.CLOUD,
							3.0, 240.0,
							randPitch, randYaw,
							90.0 - 120.0, 0.1);
					Particle_Drawer_EX.drawArc(targetLocation, Particle.CLOUD,
							3.0, 240.0,
							randPitch, randYaw,
							90.0 - 120.0, 0.05);
				}
				
				targetLocation.add(dir.clone().multiply(speed));
				currDistance += speed;
				if (currDistance >= maxDistance)
					this.durationCounter = 9999;
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_VaporBlast_Handler extends MalaPassiveSkill
{
	public Invoke_VaporBlast_Handler()
	{
		super(	"INVOKE_VAPORBLAST",
				"마술식 - 베이퍼 블래스트",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE,
				"",
				"&7바라보는 방향으로 수증기의 폭발을 일으킵니다.",
				"&7수증기는 25m까지 나아가며 피해를 주며 밀어내고, 실명을 부여합니다.");
	}
}

