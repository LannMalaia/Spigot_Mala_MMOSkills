package mala.mmoskill.skills.passive;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

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

public class Invoke_VaporBlast extends RegisteredSkill
{
	private static Invoke_VaporBlast instance;
	
	public Invoke_VaporBlast()
	{	
		super(new Invoke_VaporBlast_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_base", new LinearValue(75.0, 0.0));
		addModifier("damage_add", new LinearValue(7.5, 0.0));
		addModifier("distance_base", new LinearValue(5.0, 0.0));
		addModifier("distance_add", new LinearValue(0.5, 0.0));
		
		instance = this;
	}

	public static class VaporBlastSpell extends MalaSpellEffect
	{
		DustTransition dts = new DustTransition(Color.WHITE, Color.GRAY, 0.5f);
		Vector dir = null;
		double maxDistance = 25.0, currDistance = 0.0;
		double speed = 2;
		double damage;
		
		public VaporBlastSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 10.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Ignis.getLevel(playerData);
			damage *= spellPower;
			maxDistance = instance.getModifier("distance_base", 1)
					+ instance.getModifier("distance_add", 1) * Cast_Glacia.getLevel(playerData);
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 7.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 7.0);
		}
		public VaporBlastSpell(LivingEntity attacker, double damage, double maxDistance)
		{
			super(attacker, 10.0);
			this.damage = damage * spellPower;
			this.maxDistance = maxDistance;
		}
		
		double height = 0.5;
		
		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
		}
		
		@Override
		public void whenCount() {
			if (this.durationCounter < 10)
			{
				Particle_Drawer_EX.drawTriangle(frontLocation, dts, 3.0,
						frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
						this.durationCounter);
				if (!Particle_Manager.isReduceMode(attacker)) {
					Particle_Drawer_EX.drawTriangle(frontLocation, dts, 3.0,
							frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
							this.durationCounter + 180.0);
					Particle_Drawer_EX.drawCircle(frontLocation, dts, 1.5,
							frontLocation.getPitch() - 90.0f, frontLocation.getYaw(),
							0, this.durationCounter);
				}
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
				if (Particle_Manager.isReduceMode(attacker)) {
					Particle_Drawer_EX.drawArcVelocity(targetLocation, Particle.EXPLOSION_LARGE,
							3.0, 240.0,
							targetLocation.getPitch(), targetLocation.getYaw(), randRoll,
							90.0 - 120.0, dir, 0.1);
					for (int i = 0; i < 1; i++)
					{
						double randPitch = Math.random() * 360.0, randYaw = Math.random() * 360.0;
						Particle_Drawer_EX.drawCircle(targetLocation, Particle.CRIT, 4.0,
								randPitch, randYaw,
								0, 0.0, 0.05);
					}
				} else {
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
				}
				
				AttackUtil.attackSphere(attacker,
						targetLocation, 4.0,
						damage, (target) -> {
							target.setVelocity(dir.clone().multiply(2.5));
							Buff_Manager.Increase_Buff(target, PotionEffectType.BLINDNESS,
									0, 100, null, 0);	
						},
						DamageType.SKILL, DamageType.MAGIC);
				
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
		super(	"INVOKE_VAPOR_BLAST",
				"마술식 - 베이퍼 블래스트",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE,
				"",
				"&7바라보는 방향으로 수증기의 폭발을 일으킵니다.",
				"&7수증기는 피해를 주며 밀어내고, 일시적으로 실명을 부여합니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 이그니스 &f&l]",
				"&f&l[ &e사거리 &f{distance_base} + {distance_add} * 글래시아 &f&l]");
	}
}

