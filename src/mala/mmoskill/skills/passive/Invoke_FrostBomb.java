package mala.mmoskill.skills.passive;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_FrostBomb extends RegisteredSkill
{
	public Invoke_FrostBomb()
	{	
		super(new Invoke_FrostBomb_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class FrostBombSpell extends MalaSpell
	{
		public FrostBombSpell(PlayerData playerData)
		{
			super(playerData, 12.0);
		}

		Vector dir = null;
		double speed = 0.2, accelerate = 0.005;
		double size = 0.05;
		ArrayList<Vector> points;
		DustTransition dts = new DustTransition(Color.WHITE, Color.fromRGB(200, 200, 255), 0.5f);

		int arcCount = 3;
		double[] randPitch, randYaw;
		boolean exploded = false;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);

			points = new ArrayList<Vector>();
			for (int i = 0; i < 12; i++)
				points.add(new Vector(-1.0 + Math.random() * 2.0, -1.0 + Math.random() * 2.0, -1.0 + Math.random() * 2.0));
			
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
			if (this.durationCounter < 40)
			{
				Particle_Drawer_EX.drawStar(frontLocation, Particle.ELECTRIC_SPARK, 3.0,
						frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
						this.durationCounter, 0.0);
				Particle_Drawer_EX.drawCircle(frontLocation, Particle.ELECTRIC_SPARK, 3.0,
						frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
						0, this.durationCounter, 0.0);
			}
			else
			{
				if (dir == null)
				{
					dir = frontLocation.getDirection();
					targetLocation = frontLocation.clone();
					world.playSound(targetLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.8f);
				}
				ArrayList<Vector> newPoints = TRS.Rotate_X(points, targetLocation.getPitch());
				newPoints = TRS.Rotate_Y(newPoints, targetLocation.getYaw() + this.durationCounter * 4.0);
				newPoints = TRS.Scale(newPoints, size, size, size);

				Particle_Drawer_EX.drawCircle(targetLocation, dts, size,
						targetLocation.getPitch(), targetLocation.getYaw(),
						0, this.durationCounter * 4.0);
				for (Vector point : newPoints)
					Particle_Drawer.Draw_Line(targetLocation, targetLocation.clone().add(point), dts, 0.07);
				for (int i = 0; i < arcCount; i++)
				{
					Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT_MAGIC, 0.1 + size, 120.0,
							randPitch[i], randYaw[i], this.durationCounter * 24.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.1);
				}
				
				boolean detected = false;
				if (targetLocation.getBlock().getType().isSolid())
					detected = true;
				else
				{
					for (Entity e : world.getNearbyEntities(targetLocation, size, size, size))
					{
						if (!(e instanceof LivingEntity))
							continue;
						if (e == player)
							continue;
						
						// 찾은 경우
						detected = true;
						break;
					}
				}
				if (detected)
				{
					explode();
					durationCounter = 9999;
				}
				
				
				targetLocation.add(dir.clone().multiply(speed));
				size = Math.min(3.0, size + 0.03);
			}
		}
		
		@Override
		public void whenEnd() {
			if (!exploded)
				explode();
		}
		
		private void explode() {
			exploded = true;

			world.playSound(targetLocation, Sound.ITEM_TOTEM_USE, 2.0f, 1.3f);
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
			world.playSound(targetLocation, Sound.BLOCK_GLASS_BREAK, 2.0f, 1.0f);
			
			double sizeMult = Math.max(1.0, size);

			if (size > 2.0)
				world.spawnParticle(Particle.EXPLOSION_HUGE, targetLocation,
						1, 0.0, 0.0, 0.0, 0.0);
			
			Particle_Drawer_Expand.drawRandomSphere(targetLocation, Particle.SOUL_FIRE_FLAME,
					(int)(120 * sizeMult), 2.0 * sizeMult,
					0.1 * sizeMult, 0.1 * sizeMult);
			
			for (int i = 0; i < 8; i++)
			{
				double randPitch = Math.random() * 360.0, randYaw = Math.random() * 360.0, randRoll = Math.random() * 360.0;
				Particle_Drawer_Expand.drawCrystal(targetLocation, Particle.FIREWORKS_SPARK,
						5, 0.2 * sizeMult, 2.0 * sizeMult,
						randPitch, randYaw, randRoll,
						0.2 * sizeMult);
			}
			for (int i = 0; i < 5; i++)
			{
				double randPitch = Math.random() * 360.0, randYaw = Math.random() * 360.0;
				Particle_Drawer_Expand.drawArc(targetLocation, Particle.CLOUD,
						5.0, 720.0,
						randPitch, randYaw,
						0.0, 1.6, 24, false);
			}
			for (int i = 0; i < 9; i++)
			{
				double randPitch = Math.random() * 360.0, randYaw = Math.random() * 360.0;
				Particle_Drawer_Expand.drawArc(targetLocation, Particle.SOUL_FIRE_FLAME,
						5.0, 240.0,
						randPitch, randYaw,
						0.0, 1.6, 12, false);
			}
		}
	}
}

class Invoke_FrostBomb_Handler extends MalaPassiveSkill implements Listener
{
	public Invoke_FrostBomb_Handler()
	{
		super(	"INVOKE_FLOSTBOMB",
				"마술식 - 프로스트 봄",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7바라보는 방향으로 얼음 구슬을 발사합니다.",
				"&7얼음 구슬의 속도는 점점 빨라지며, 어딘가에 부딪히면",
				"&7큰 폭발과 함께 주변 적에게 피해를 줍니다.",
				"&7나아간 거리가 멀수록 폭발 범위 및 피해가 증가합니다.");
	}
}

