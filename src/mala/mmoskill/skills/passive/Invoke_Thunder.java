package mala.mmoskill.skills.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;
import mala.mmoskill.skills.Lightning_Bolt;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Thunder extends RegisteredSkill
{
	public Invoke_Thunder()
	{	
		super(new Invoke_Thunder_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class ThunderSpell extends MalaSpell
	{
		public ThunderSpell(PlayerData playerData)
		{
			super(playerData, 3.0);
		}

		int arcCount = 3;
		double[] randPitch, randYaw;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);

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
			Particle_Drawer_EX.drawCircle(targetLocation, Particle.CRIT, 5.0,
					0.0, 0.0,
					0, this.durationCounter * 0.5, 0.005);
			Particle_Drawer_EX.drawCircle(targetLocation, Particle.CRIT, 5.0,
					0.0, 0.0,
					4, this.durationCounter * -8.0, -0.4);

			for (int i = 0; i < arcCount; i++)
			{
				Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT, 5.0, 60.0,
						randPitch[i], randYaw[i], this.durationCounter * 12.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.0);
				Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT, 3.0 + Math.random() * 2.0, 60.0,
						randPitch[i], randYaw[i], this.durationCounter * 24.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.02);
			}
		}
		
		@Override
		public void whenEnd() {
			Particle_Drawer_EX.drawCircle(targetLocation, Particle.LAVA, 5.0,
					0.0, 0.0,
					0, this.durationCounter * 0.0, 0.0);
			for (int i = 0; i < 4; i++)
			{
				Location tempLoc = targetLocation.clone().add(
						-40.0 + Math.random() * 80.0,
						100.0,
						-40.0 + Math.random() * 80.0);
				Lightning_Bolt.Draw_Lightning_Line(tempLoc, targetLocation, Particle.END_ROD);
			}
			
			Particle_Drawer_Expand.drawRandomSphere(targetLocation, Particle.SOUL_FIRE_FLAME,
					360, 5.0,
					0.1, 0.4);
			for (int i = 0; i < 12; i++)
			{
				double randPitch = -10.0 + Math.random() * -30.0, randYaw = Math.random() * 360.0;
				Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT,
						1.7 + i * 0.6, 120.0,
						randPitch, randYaw,
						0.0, 2.0);
				Particle_Drawer_EX.drawArc(targetLocation, Particle.CLOUD,
						1.7 + i * 0.6, 120.0,
						randPitch, randYaw,
						0.0, 0.02);
			}

//			for (Entity e : world.getNearbyEntities(loc, radius, radius, radius))
//			{
//				if(!(e instanceof LivingEntity))
//					continue;
//				if(e == player)
//					continue;
//				
//				LivingEntity target = (LivingEntity)e;
//				Damage.Attack(player, target,damage, DamageType.MAGIC, DamageType.SKILL);
//			}

			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
			world.playSound(targetLocation, Sound.ITEM_TOTEM_USE, 2f, 1.1f);
			world.playSound(targetLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 1.1f);
			
			// world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 50, 3, 3, 3, 0);
			world.spawnParticle(Particle.EXPLOSION_LARGE, targetLocation, 30, 3, 3, 3, 0);
			world.spawnParticle(Particle.LAVA, targetLocation, 60, 0.3, 0.3, 0.3, 0);
		}
	}
}

class Invoke_Thunder_Handler extends MalaPassiveSkill
{
	public Invoke_Thunder_Handler()
	{
		super(	"INVOKE_THUNDER",
				"마술식 - 썬더",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7바라보는 위치에 전격의 마법진을 펼칩니다.",
				"&7일정 시간이 지나면 마법진이 있던 위치로 번개가 내리칩니다.",
				"&7번개에 휘말린 적들은 큰 피해를 받습니다.");
	}
}

