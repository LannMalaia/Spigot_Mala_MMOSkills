package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala.mmoskill.util.WhenHit;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Frost extends RegisteredSkill
{
	private static Invoke_Frost instance;
	
	public Invoke_Frost()
	{	
		super(new Invoke_Frost_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage_base", new LinearValue(16.0, 0.0));
		addModifier("damage_add", new LinearValue(3.8, 0.0));
		
		instance = this;
	}

	public static class FrostSpell extends MalaSpellEffect
	{
		double damage;
		
		public FrostSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 0.1);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Glacia.getLevel(playerData);
			damage *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 3.0);
		}
		public FrostSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 1.0);
			this.damage = damage * spellPower;
		}
		
		int arcCount = 3;
		double[] randPitch, randYaw;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
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
				Particle_Drawer_EX.drawSquare(targetLocation, Particle.CRIT, 3.0 * spellPower, 0, 0,
						this.durationCounter * 4.0, 0.0);
			} else {
				Particle_Drawer_EX.drawSquare(targetLocation, Particle.CRIT, 3.0 * spellPower, 0, 0,
						this.durationCounter * 4.0, 0.0);
				Particle_Drawer_EX.drawSquare(targetLocation, Particle.CRIT, 3.0 * spellPower, 0, 0,
						this.durationCounter * 4.0 + 45.0, 0.0);

				for (int i = 0; i < arcCount; i++)
				{
					Particle_Drawer_EX.drawArc(targetLocation, Particle.SNOWFLAKE, 3.0, 60.0,
							randPitch[i], randYaw[i], this.durationCounter * 12.0 * (i % 2 == 0 ? -1.0 : 1.0), 0.05);
				}
			}
		}
		
		@Override
		public void whenEnd() {
			world.playSound(targetLocation, Sound.BLOCK_GLASS_BREAK, 2.0f, 2.0f);
			world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.2f);

			if (Particle_Manager.isReduceMode(attacker)) {
				for (int i = 0; i < 3; i++)
				{
					double randPitch = -90.0 + Math.random() * 180.0;
					double randYaw = Math.random() * 360.0;
					double length = 3.0 + Math.random() * 7.0;
					Particle_Drawer_EX.drawLine(targetLocation, Particle.CLOUD, length,
							randPitch, randYaw);
					Particle_Drawer_Expand.drawLine(targetLocation, Particle.CLOUD, length * 0.5,
							randPitch, randYaw, 0.5);
				}
			} else {
				for (int i = 0; i < 12; i++)
				{
					double randPitch = -90.0 + Math.random() * 180.0;
					double randYaw = Math.random() * 360.0;
					double length = 3.0 + Math.random() * 7.0;
					Particle_Drawer_EX.drawLine(targetLocation, Particle.CLOUD, length,
							randPitch, randYaw);
					Particle_Drawer_Expand.drawLine(targetLocation, Particle.CLOUD, length * 0.5,
							randPitch, randYaw, 0.5);
				}
			}
			
			world.spawnParticle(Particle.FLASH, targetLocation, 1, 0, 0, 0, 0);

			AttackUtil.attackSphere(attacker,
					targetLocation, 3.0 * spellPower,
					damage, (target) -> {
						Buff_Manager.Increase_Buff(target, PotionEffectType.SLOW,
								0, 100, PotionEffectType.SPEED, 9);
					},
					DamageType.SKILL, DamageType.MAGIC);
		}
	}
}

class Invoke_Frost_Handler extends MalaPassiveSkill implements Listener
{
	public Invoke_Frost_Handler()
	{
		super(	"INVOKE_FROST",
				"마술식 - 프로스트",
				Material.WRITABLE_BOOK,
				"§91단계 술식",
				MsgTBL.MAGIC_ICE,
				"",
				"&7바라보는 위치 주변 3m의 온도를 크게 낮춥니다.",
				"&7냉기에 휘말린 적들은 피해를 입고 구속 상태가 됩니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 글래시아 &f&l]",
				"&f&l[ &b구속 &f1단계를 5초간 부여(최대 9) &f&l]"
				);
	}
}

