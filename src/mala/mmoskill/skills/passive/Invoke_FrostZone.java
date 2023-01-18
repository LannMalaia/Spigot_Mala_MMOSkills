package mala.mmoskill.skills.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_FrostZone extends RegisteredSkill
{
	private static Invoke_FrostZone instance;
	
	public Invoke_FrostZone()
	{	
		super(new Invoke_FrostZone_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("damage_base", new LinearValue(22.0, 0.0));
		addModifier("damage_add", new LinearValue(2.2, 0.0));
		
		instance = this;
	}

	public static class FrostZoneSpell extends MalaSpellEffect
	{
		double damage;
		
		public FrostZoneSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 6.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Glacia.getLevel(playerData);
			targetDuration *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 14.0);
		}
		public FrostZoneSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 6.0);
			this.damage = damage;
			targetDuration *= spellPower;
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
			if (Particle_Manager.isReduceMode(attacker)) {
				Particle_Drawer_EX.drawSquare(targetLocation, Particle.ELECTRIC_SPARK, 5.0,
						0, 0,
						this.durationCounter * 1.0, 0.0);
			} else {
				Particle_Drawer_EX.drawCircle(targetLocation, Particle.ELECTRIC_SPARK, 5.0,
						0.0, 0.0,
						0, this.durationCounter * 0.5, 0.005);
				Particle_Drawer_EX.drawSquare(targetLocation, Particle.ELECTRIC_SPARK, 5.0,
						0, 0,
						this.durationCounter * 1.0, 0.0);
				Particle_Drawer_EX.drawSquare(targetLocation, Particle.ELECTRIC_SPARK, 5.0,
						0, 0,
						this.durationCounter * 1.0 + 45.0, 0.0);
			}
			
			if (this.durationCounter >= 20)
			{
				if (Particle_Manager.isReduceMode(attacker)) {
					for (int i = 0; i < arcCount; i++)
					{
						Particle_Drawer_EX.drawArc(targetLocation, Particle.SNOWFLAKE, 0.5 + Math.random() * 2.5, 60.0,
								randPitch[i], randYaw[i], this.durationCounter * 36.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.05);
					}
				} else {
					for (int i = 0; i < arcCount; i++)
					{
						Particle_Drawer_EX.drawArc(targetLocation, Particle.SNOWFLAKE, 5.0, 60.0,
								randPitch[i], randYaw[i], this.durationCounter * 12.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.0);
						Particle_Drawer_EX.drawArc(targetLocation, Particle.SNOWFLAKE, 3.0 + Math.random() * 2.0, 60.0,
								randPitch[i], randYaw[i], this.durationCounter * 24.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.02);
						Particle_Drawer_EX.drawArc(targetLocation, Particle.SNOWFLAKE, 0.5 + Math.random() * 2.5, 60.0,
								randPitch[i], randYaw[i], this.durationCounter * 36.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.05);
					}
				}
				
				if (this.durationCounter % 10 == 0)
					world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.2f);
				if (this.durationCounter % 20 <= 2)
				{
					Location flakeLoc = targetLocation.clone().add(-3.0 + Math.random() * 6.0,
							-3.0 + Math.random() * 6.0, -3.0 + Math.random() * 6.0);

					if (this.durationCounter % 20 == 0)
						world.playSound(flakeLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 2.0f);

					if (Particle_Manager.isReduceMode(attacker)) {
						for (int i = 0; i < 2; i++)
						{
							double randPitch = -90.0 + Math.random() * 180.0;
							double randYaw = Math.random() * 360.0;
							double length = 2.0 + Math.random() * 2.0;
							Particle_Drawer_Expand.drawLine(flakeLoc, Particle.FIREWORKS_SPARK, length * 0.5,
									randPitch, randYaw, 0.2);
						}
					} else {
						for (int i = 0; i < 8; i++)
						{
							double randPitch = -90.0 + Math.random() * 180.0;
							double randYaw = Math.random() * 360.0;
							double length = 2.0 + Math.random() * 2.0;
							Particle_Drawer_Expand.drawLine(flakeLoc, Particle.FIREWORKS_SPARK, length * 0.5,
									randPitch, randYaw, 0.2);
						}
					}
				}
				if (durationCounter % 20 == 0) {
					AttackUtil.attackCylinder(attacker,
							targetLocation, 5.0, 3.0,
							damage, (target) -> {
								Buff_Manager.Increase_Buff(target, PotionEffectType.SLOW,
										0, 100, PotionEffectType.SPEED, 9);		
							},
							DamageType.SKILL, DamageType.MAGIC);					
				}
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_FrostZone_Handler extends MalaPassiveSkill
{
	public Invoke_FrostZone_Handler()
	{
		super(	"INVOKE_FROST_ZONE",
				"마술식 - 프로스트 존",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE,
				"",
				"&7바라보는 위치에 5m 크기의 냉기의 마법진을 펼칩니다.",
				"&7마법진 위의 적들은 서서히 구속 상태가 되며,",
				"&73레벨 이상의 구속에 걸리면 구속 대신에 피해를 받습니다.",
				"&7마법진은 5초간 유지됩니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f1초당 {damage_base} + {damage_add} * 글래시아 &f&l]");
	}
}

