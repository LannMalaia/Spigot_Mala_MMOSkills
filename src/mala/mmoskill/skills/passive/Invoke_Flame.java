package mala.mmoskill.skills.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.util.AttackUtil;
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

public class Invoke_Flame extends RegisteredSkill
{
	private static Invoke_Flame instance;
	
	public Invoke_Flame()
	{	
		super(new Invoke_Flame_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("damage_base", new LinearValue(25.0, 0.0));
		addModifier("damage_add", new LinearValue(2.5, 0.0));
		
		instance = this;
	}

	public static class FlameSpell extends MalaSpellEffect
	{
		double damage;
		
		public FlameSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 4.5);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Ignis.getLevel(playerData);
			targetDuration *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 12.0);
		}
		public FlameSpell(LivingEntity attacker, double damage)
		{
			super(attacker, 4.5);
			this.damage = damage;
			this.targetDuration *= spellPower;
		}
		
		double width = 0.5, height = 0.5;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
		}
		
		@Override
		public void whenCount() {
			if (this.durationCounter < 20)
			{
				// 마법진
				if (this.durationCounter % 3 == 0)
				{
					if (Particle_Manager.isReduceMode(attacker)) {
						Particle_Drawer_EX.drawCircle(targetLocation, Particle.SMALL_FLAME, 5.0,
								0.0, 0.0,
								0, this.durationCounter * 0.5, 0.005);
					} else {
						Particle_Drawer_EX.drawCircle(targetLocation, Particle.SMALL_FLAME, 5.0,
								0.0, 0.0,
								0, this.durationCounter * 0.5, 0.005);
						Particle_Drawer_EX.drawTriangle(targetLocation, Particle.SMALL_FLAME, 5.0,
								0.0, 0.0,
								this.durationCounter * 0.5, 0.005);
						Particle_Drawer_EX.drawTriangle(targetLocation, Particle.SMALL_FLAME, 5.0,
								0.0, 180.0,
								this.durationCounter * 0.5, 0.005);
					}
				}
			}
			else
			{
				// 공격
				width = Math.min(5.0, width + 0.2);
				height = Math.min(15.0, height + 1.0);
				if (this.durationCounter % 6 <= 2)
					world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);

				if (Particle_Manager.isReduceMode(attacker)) {
					for (double d = 0; d <= height; d += 8.0) {
						Location loc = targetLocation.clone().add(0, d, 0);
						Particle_Drawer_EX.drawCircleUp(loc, Particle.FLAME, width,
								0.0, 0.0,
								6, this.durationCounter * 5.0 + d * 5.0, 0.8);
					}
				} else {
					for (double d = (this.durationCounter % 10) * 0.15; d <= height; d += 1.5) {
						Location loc = targetLocation.clone().add(0, d, 0);
						Particle_Drawer_Expand.drawCircleRandomize(loc, Particle.FLAME, 1.0,
								0.0, 0.0,
								2, this.durationCounter * 5.0 + d * 5.0, width * 0.12);
					}
					for (double d = 0; d <= height; d += 2.0) {
						Location loc = targetLocation.clone().add(0, d, 0);
						Particle_Drawer_EX.drawCircleUpRandomize(loc, Particle.FLAME, width * 0.9,
								0.0, 0.0,
								6, this.durationCounter * 5.0 + d * 5.0, 1.0);
						Particle_Drawer_EX.drawCircleUp(loc, Particle.FLAME, width,
								0.0, 0.0,
								6, this.durationCounter * 5.0 + d * 5.0, 0.8);
					}
				}
				if (durationCounter % 10 == 0) {
					AttackUtil.attackCylinder(attacker,
							targetLocation, width, height,
							damage, (target) -> {
								target.setFireTicks(100);
								target.setVelocity(target.getVelocity().setY(2.0));
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

class Invoke_Flame_Handler extends MalaPassiveSkill
{
	public Invoke_Flame_Handler()
	{
		super(	"INVOKE_FLAME",
				"마술식 - 플레임",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE,
				"",
				"&7바라보는 위치에 5m 크기의 불기둥을 일으킵니다.",
				"&7불길에 휘말린 적들은 피해를 입고 발화 상태가 되며, 위로 떠오릅니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f0.5초당 {damage_base} + {damage_add} * 이그니스 &f&l]");
	}
}

