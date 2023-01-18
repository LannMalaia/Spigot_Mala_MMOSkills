package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;

import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.LivingEntity;

import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_SoulZet extends RegisteredSkill
{
	private static Invoke_SoulZet instance;
	
	public Invoke_SoulZet()
	{	
		super(new Invoke_SoulZet_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_base", new LinearValue(50.0, 0.0));
		addModifier("damage_add", new LinearValue(5.0, 0.0));
		addModifier("duration_base", new LinearValue(0.5, 0.0));
		addModifier("duration_add", new LinearValue(0.015, 0.0));
		
		instance = this;
	}

	public static class SoulZetSpell extends MalaSpellEffect
	{
		double damage;
		public SoulZetSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 1.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Glacia.getLevel(playerData);
			targetDuration = instance.getModifier("duration_base", 1)
					+ instance.getModifier("duration_add", 1) * Cast_Ignis.getLevel(playerData);
			damage *= spellPower;
			targetDuration *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 10.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 10.0);
		}
		public SoulZetSpell(LivingEntity attacker, double damage, double duration)
		{
			super(attacker, 1.0);
			this.damage = damage * spellPower;
			this.targetDuration = duration * spellPower;
		}

		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
			Effect force = new Effect(centerLocation, Particle.END_ROD);
			force.append2DCircle(5.0)
				.scalePoint(0.2)
				.scaleVelocity(0.1)
				.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
				.playEffect();
			force.setParticle(Particle.END_ROD)
				.scaleVelocity(1.5)
				.playEffect();
			force.setParticle(Particle.END_ROD)
				.scaleVelocity(1.5)
				.playEffect();
		}
		
		@Override
		public void whenCount() {
			Vector dir = frontLocation.getDirection();

			if (Particle_Manager.isReduceMode(attacker)) {
				Effect zet = new Effect(centerLocation.clone().add(dir.clone().multiply(-0.5)), Particle.SOUL_FIRE_FLAME);
				zet.append2DArc(40, 3.0).rotate(0.0, 180.0, 0.0)
					.append2DArc(40, 3.0).rotate(0.0, -60.0, 0.0)
					.scalePoint(0.2)
					.scaleVelocity(0.2)
					.rotate(0, durationCounter * 4.0, 0.0)
					.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
					.playEffect();
				Effect soulBurn = new Effect(centerLocation.clone().add(dir.clone().multiply(-0.3)), Particle.SOUL);
				soulBurn.append2DCircle(3.0, 0.4)
					.scalePoint(0.2)
					.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
					.setVelocity(-dir.getX(), -dir.getY(), -dir.getZ())
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.5)
					.playEffect();
			} else {
				Effect zet = new Effect(centerLocation.clone().add(dir.clone().multiply(-0.5)), Particle.SOUL_FIRE_FLAME);
				zet.append2DArc(120, 3.0).rotate(0.0, 180.0, 0.0)
					.append2DArc(120, 3.0).rotate(0.0, -60.0, 0.0)
					.scalePoint(0.2)
					.scaleVelocity(0.2)
					.rotate(0, durationCounter * 4.0, 0.0)
					.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
					.playEffect();
				Effect zet2 = new Effect(centerLocation.clone().add(dir.clone().multiply(-0.4)), Particle.SOUL_FIRE_FLAME);
				zet2.append2DArc(120, 3.0).rotate(0.0, 180.0, 0.0)
					.append2DArc(120, 3.0).rotate(0.0, -60.0, 0.0)
					.scalePoint(0.2)
					.scaleVelocity(0.13)
					.rotate(0, 90.0 - durationCounter * 4.0, 0.0)
					.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
					.playEffect();
				Effect soulBurn = new Effect(centerLocation.clone().add(dir.clone().multiply(-0.3)), Particle.SOUL);
				soulBurn.append2DCircle(3.0)
					.scalePoint(0.2)
					.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
					.setVelocity(-dir.getX(), -dir.getY(), -dir.getZ())
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.5)
					.playEffect();
				Effect force = new Effect(centerLocation.clone().add(dir.clone().multiply(0.5)), Particle.CRIT);
				force.append2DCircle(5.0)
					.scalePoint(0.2)
					.scaleVelocity(0.3)
					.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
					.playEffect();
			}
			
			attacker.setVelocity(attacker.getLocation().getDirection().multiply(2.0));
			
			AttackUtil.attackSphere(attacker, centerLocation, 3.0, damage, null, DamageType.MAGIC, DamageType.SKILL);
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_SoulZet_Handler extends MalaPassiveSkill
{
	public Invoke_SoulZet_Handler()
	{
		super(	"INVOKE_SOULZET",
				"마술식 - 소울 제트",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE,
				"&e즉시 시전",
				"",
				"&7전방을 향해 영혼의 불길을 내뿜으며 돌진합니다.",
				"&7돌진 중 부딪히는 적들은 큰 피해를 입습니다.",
				"&c탑승물을 움직이지 못하며, 발동시 취소가 불가능합니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 글래시아 &f&l]",
				"&f&l[ &b지속 시간 &f{duration_base} + {duration_add} * 이그니스 &f&l]");
	}
}

