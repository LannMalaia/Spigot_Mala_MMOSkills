package mala.mmoskill.skills.passive;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustTransition;

import mala.mmoskill.skills.Lightning_Bolt;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala.mmoskill.util.RayUtil;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_LordOfVermilion extends RegisteredSkill
{
	public Invoke_LordOfVermilion()
	{	
		super(new Invoke_LordOfVermilion_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class LordOfVermilionSpell extends MalaSpell
	{
		public LordOfVermilionSpell(PlayerData playerData)
		{
			super(playerData, 3.0);
		}

		Location savedLoc;
		ArrayList<Location> savedLocList = new ArrayList<Location>();
		
		@Override
		public void whenStart() {
			savedLoc = player.getLocation().clone();
		}
		
		@Override
		public void whenCount() {
			Particle_Drawer_EX.drawCircle(savedLoc, Particle.SMALL_FLAME, 20.0,
					0.0, 0.0,
					0, durationCounter * 0.2, 0.0);
			Particle_Drawer_EX.drawStar(savedLoc, Particle.SMALL_FLAME, 20.0,
					0.0, 0.0,
					durationCounter * 0.2, 0.0);
			if (durationCounter % 2 == 0) {
				// 위치 산출
				Location loc = null;
				int counter = 0;
				while (counter++ < 100) {
					boolean ok = true;
					loc = RayUtil.getLocation(
							savedLoc.clone().add(-20.0 + Math.random() * 40.0, 10.0, -20.0 + Math.random() * 40.0),
							new Vector(0, -1, 0),
							20.0);

					// 다른 위치들과의 거리 비교
//					for (Location tempLoc : savedLocList) {
//						if (tempLoc.distance(loc) < 7.0)
//							ok = false;
//					}
					if (ok)
						break;
				}
				savedLocList.add(loc);
				
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Thunder(playerData, loc));
			}
		}
		
		@Override
		public void whenEnd() {
		}
		
		
		public class Thunder extends MalaSpell
		{
			Location location;
			int arcCount = 2;
			double[] randPitch, randYaw;
			DustTransition dts = new DustTransition(Color.ORANGE, Color.YELLOW, 1.0f);
			
			public Thunder(PlayerData playerData, Location location)
			{
				super(playerData, 3.0);
				this.location = location;
			}

			
			@Override
			public void whenStart() {
				world.playSound(location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);

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
				Particle_Drawer_EX.drawCircle(location, dts, 5.0,
						0.0, 0.0,
						0, this.durationCounter * 0.5);

				for (int i = 0; i < arcCount; i++)
				{
					Particle_Drawer_EX.drawArc(location, Particle.WAX_ON, 5.0, 60.0,
							randPitch[i], randYaw[i], this.durationCounter * 12.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.0);
//					Particle_Drawer_EX.drawArc(location, Particle.WAX_ON, 3.0 + Math.random() * 2.0, 60.0,
//							randPitch[i], randYaw[i], this.durationCounter * 24.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.02);
				}
				
			}
			
			@Override
			public void whenEnd() {
				Particle_Drawer_EX.drawCircle(location, Particle.LAVA, 5.0,
						0.0, 0.0,
						0, this.durationCounter * 0.0, 0.0);
				for (int i = 0; i < 1; i++)
				{
					Location tempLoc = location.clone().add(
							-40.0 + Math.random() * 80.0,
							100.0,
							-40.0 + Math.random() * 80.0);
					Lightning_Bolt.Draw_Lightning_Line(tempLoc, location, Particle.FLAME);
				}
				
				Particle_Drawer_Expand.drawRandomSphere(location, Particle.SOUL_FIRE_FLAME,
						360, 5.0,
						0.1, 0.4);
				for (int i = 0; i < 4; i++)
				{
					double randPitch = -10.0 + Math.random() * -30.0, randYaw = Math.random() * 360.0;
					Particle_Drawer_EX.drawArc(location, Particle.CRIT,
							1.7 + i * 0.6, 120.0,
							randPitch, randYaw,
							0.0, 2.0);
					Particle_Drawer_EX.drawArc(location, Particle.CLOUD,
							1.7 + i * 0.6, 120.0,
							randPitch, randYaw,
							0.0, 0.02);
				}

				world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
				world.playSound(location, Sound.ITEM_TOTEM_USE, 2f, 1.1f);
				world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 1.1f);
				
				world.spawnParticle(Particle.EXPLOSION_LARGE, location, 30, 3, 3, 3, 0);
				world.spawnParticle(Particle.LAVA, location, 60, 0.3, 0.3, 0.3, 0);
			}
		}
	}
}

class Invoke_LordOfVermilion_Handler extends MalaPassiveSkill
{
	public Invoke_LordOfVermilion_Handler()
	{
		super(	"INVOKE_LORDOFVERMILION",
				"마술식 - 로드 오브 버밀리온",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7자신 주변 20m 내 무작위 지역에 불타는 번개를 내리칩니다.");
	}
}

