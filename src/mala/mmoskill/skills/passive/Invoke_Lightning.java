package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Lightning extends RegisteredSkill
{
	private static Invoke_Lightning instance;
	
	public Invoke_Lightning()
	{	
		super(new Invoke_Lightning_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("damage_base", new LinearValue(14.5, 0.0));
		addModifier("damage_add", new LinearValue(1.45, 0.0));
		
		instance = this;
	}

	public static class LightningSpell extends MalaSpellEffect
	{
		double damage;
		public LightningSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 3.0);
			damage = Invoke_Lightning.instance.getModifier("damage_base", 1)
					+ Invoke_Lightning.instance.getModifier("damage_add", 1) * Cast_Ventus.getLevel(playerData);
			targetDuration *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 3.0);
		}
		public LightningSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 3.0);
			this.damage = damage;
			targetDuration *= spellPower;
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
			if (Particle_Manager.isReduceMode(attacker)) {
				Particle_Drawer_Expand.drawStar(targetLocation, Particle.FIREWORKS_SPARK, 2.0,
						randPitch[0], randYaw[0],
						this.durationCounter * 6.0, 0.3);
			} else {
				Particle_Drawer_Expand.drawCircle(targetLocation, Particle.FIREWORKS_SPARK, 3.0,
						randPitch[0], randYaw[0],
						0, this.durationCounter * 6.0,  0.3);
				Particle_Drawer_Expand.drawStar(targetLocation, Particle.FIREWORKS_SPARK, 3.0,
						randPitch[0], randYaw[0],
						this.durationCounter * 6.0, 0.3);
			}
		}
		
		@Override
		public void whenCount() {

			if (this.durationCounter % 3 == 0)
			{
				if (Particle_Manager.isReduceMode(attacker)) {
					
				} else {
					for (int i = 0; i < arcCount; i++)
					{
						randPitch[i] = -30.0 + Math.random() * 60.0;
						randYaw[i] = Math.random() * 360.0;
						Particle_Drawer_EX.drawArc(targetLocation, Particle.CRIT_MAGIC, 3.0, 120.0,
								randPitch[i], randYaw[i],
								this.targetDuration * 4.0, -0.4);
					}
				}
			}
			if (this.durationCounter % 20 == 0)
			{
				// world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.5f);
				world.playSound(targetLocation, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 2.0f);
				world.spawnParticle(Particle.FLASH, targetLocation, 1, 0, 0, 0, 0);
				if (Particle_Manager.isReduceMode(attacker)) {
					for (int i = 0; i < 2; i++)
					{
						double randPitch = Math.random() * 360.0;
						double randYaw = Math.random() * 360.0;
						double length = 3.0 + Math.random() * 4.0;
						Particle_Drawer_EX.drawLightningLine(targetLocation, Particle.CRIT,
								length, 1.0, 4,
								randPitch, randYaw);
					}
				} else {
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
				double randDamage = damage * 0.5 + Math.random() * damage;

				AttackUtil.attackSphere(attacker,
						targetLocation, 3.0,
						randDamage, null,
						DamageType.SKILL, DamageType.MAGIC);
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
				"마술식 - 라이트닝",
				Material.WRITABLE_BOOK,
				"§91단계 술식",
				MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7바라보는 위치 주변 3m의 적들에게 전격을 발사합니다.",
				"&7전격에 맞은 적들은 50%~150%의 피해를 받습니다.",
				"&7전격은 일정 시간 지속되며 계속해서 피해를 줍니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 벤투스 &f&l]");
	}
}

