package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.skills.Lightning_Bolt;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala.mmoskill.util.RayUtil;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_LordOfVermilion extends RegisteredSkill
{
	private static Invoke_LordOfVermilion instance;
	
	public Invoke_LordOfVermilion()
	{	
		super(new Invoke_LordOfVermilion_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_base", new LinearValue(35.0, 0.0));
		addModifier("damage_add", new LinearValue(5.0, 0.0));
		addModifier("duration_base", new LinearValue(3.0, 0.0));
		addModifier("duration_add", new LinearValue(0.15, 0.0));
		
		instance = this;
	}

	public static class LordOfVermilionSpell extends MalaSpellEffect
	{
		double damage;
		public LordOfVermilionSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 3.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Ventus.getLevel(playerData);
			targetDuration = instance.getModifier("duration_base", 1)
					+ instance.getModifier("duration_add", 1) * Cast_Ignis.getLevel(playerData);
			damage *= spellPower;
			targetDuration *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 30.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 30.0);
		}
		public LordOfVermilionSpell(LivingEntity attacker, double damage, double duration)
		{
			super(attacker, 3.0);
			this.damage = damage * spellPower;
			this.targetDuration = duration * spellPower;
		}

		Location savedLocation;
		double tempSize = 1.0, targetSize = 20.0;
		
		@Override
		public void whenStart() {
			savedLocation = attacker.getLocation().clone();
		}
		
		@Override
		public void whenCount() {
			if (savedLocation.distance(attacker.getLocation()) > 2) {
				cancelled = true;
				return;
			}
			tempSize = tempSize + (targetSize - tempSize) * 0.05;
			Particle_Drawer_EX.drawCircle(targetLocation, Particle.SMALL_FLAME, tempSize,
					0.0, 0.0,
					0, durationCounter * 0.2, 0.0);
//			Particle_Drawer_EX.drawStar(savedLoc, Particle.SMALL_FLAME, 20.0,
//					0.0, 0.0,
//					durationCounter * 0.2, 0.0);
			if (durationCounter % 10 == 0) {
				for (int i = 0; i < 4; i++) {
					// 위치 산출
					Location loc = RayUtil.getLocation(
								targetLocation.clone().add(-20.0 + Math.random() * 40.0, 10.0, -20.0 + Math.random() * 40.0),
								new Vector(0, -1, 0),
								20.0);
					Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Thunder(attacker, loc, damage));
				}
			}
		}
		
		@Override
		public void whenEnd() {
		}
		
		
		public class Thunder extends MalaSpellEffect
		{
			Location location;
			int arcCount = 2;
			double[] randPitch, randYaw;
			DustTransition dts = new DustTransition(Color.ORANGE, Color.YELLOW, 1.0f);
			double damage;
			
			public Thunder(LivingEntity attacker, Location location, double damage)
			{
				super(attacker, 0.0);
				this.location = location;
				this.damage = damage;
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
//				Particle_Drawer_EX.drawCircle(location, dts, 5.0,
//						0.0, 0.0,
//						0, this.durationCounter * 0.5);
//
//				for (int i = 0; i < arcCount; i++) {
//					Particle_Drawer_EX.drawArc(location, Particle.CRIT, 5.0, 60.0,
//							randPitch[i], randYaw[i], this.durationCounter * 12.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.0);
//				}
			}
			
			@Override
			public void whenEnd() {
				Particle_Drawer_EX.drawCircle(location, Particle.LAVA, 5.0,
						0.0, 0.0,
						0, this.durationCounter * 0.0, 0.0);
				for (int i = 0; i < 1; i++) {
					Location tempLoc = location.clone().add(
							-40.0 + Math.random() * 80.0,
							100.0,
							-40.0 + Math.random() * 80.0);
					Lightning_Bolt.Draw_Lightning_Line(tempLoc, location, Particle.FLAME);
				}

				if (!Particle_Manager.isReduceMode(attacker)) {
					Particle_Drawer_Expand.drawRandomSphere(location, Particle.SOUL_FIRE_FLAME,
							360, 5.0,
							0.1, 0.4);
					for (int i = 0; i < 4; i++) {
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
					world.spawnParticle(Particle.EXPLOSION_LARGE, location, 30, 3, 3, 3, 0);
					world.spawnParticle(Particle.LAVA, location, 60, 0.3, 0.3, 0.3, 0);
				}

				world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
				world.playSound(location, Sound.ITEM_TOTEM_USE, 1f, 1.1f);
				world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1.1f);
				
				AttackUtil.attackSphere(attacker,
						location, 5.0,
						damage, (target) -> {
							target.setFireTicks(200);
						},
						DamageType.MAGIC, DamageType.SKILL);
			}
		}
	}
}

class Invoke_LordOfVermilion_Handler extends MalaPassiveSkill
{
	public Invoke_LordOfVermilion_Handler()
	{
		super(	"INVOKE_LORD_OF_VERMILION",
				"마술식 - 로드 오브 버밀리온",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7지정한 위치 20m 내 무작위 지역에 불타는 번개를 내리칩니다.",
				"&7번개에 휘말린 적들은 발화 상태가 됩니다.",
				"&7움직일 경우 시전이 취소됩니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 벤투스 &f&l]",
				"&f&l[ &b지속 시간 &f{duration_base} + {duration_add} * 이그니스 &f&l]");
	}
}

