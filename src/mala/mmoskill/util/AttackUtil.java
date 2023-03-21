package mala.mmoskill.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import laylia_core.main.Damage;

/**
 * 공격과 관련된 기능을 담당합니다.
 */
public class AttackUtil
{
	/**
	 * 대상을 공격
	 */
	public static void attack(LivingEntity attacker, Entity entity,
			double damage, WhenHit whenHit, DamageType... _types)
	{
		if (attacker instanceof Player) {
			// 플레이어의 공격
			Player player = (Player)attacker;
			if (!(entity instanceof LivingEntity))
				return;
			LivingEntity target = (LivingEntity)entity;
			if (Damage.Is_Possible(attacker, target)) {
				Damage.NormalAttack(player, target, damage, _types);
				if (whenHit != null)
					whenHit.run(target);
			}
		}
		else {
			// 다른 엔티티의 공격
			if (!(entity instanceof LivingEntity) || entity == attacker)
				return;
			LivingEntity target = (LivingEntity)entity;
			target.damage(damage, attacker);
			if (whenHit != null)
				whenHit.run(target);
		}
	}
	
	/**
	 * 대상을 공격
	 */
	public static void skillAttack(SkillMetadata cast, LivingEntity attacker, Entity entity,
			double damage, WhenHit whenHit, DamageType... _types)
	{
		if (attacker instanceof Player) {
			// 플레이어의 공격
			Player player = (Player)attacker;
			if (!(entity instanceof LivingEntity))
				return;
			LivingEntity target = (LivingEntity)entity;
			if (Damage.Is_Possible(attacker, target)) {
				Damage.SkillAttack(cast, target, damage, _types);
				if (whenHit != null)
					whenHit.run(target);
			}
		}
		else {
			// 다른 엔티티의 공격
			if (!(entity instanceof LivingEntity) || entity == attacker)
				return;
			LivingEntity target = (LivingEntity)entity;
			target.damage(damage, attacker);
			if (whenHit != null)
				whenHit.run(target);
		}
	}
	
	/**
	 * 구 범위를 공격
	 */
	public static void attackSphere(LivingEntity attacker,
			Location targetLocation, double radius,
			double damage, WhenHit whenHit, DamageType... _types)
	{
		if (attacker instanceof Player) {
			// 플레이어의 공격
			Player player = (Player)attacker;
			for (Entity entity : targetLocation.getWorld().getNearbyEntities(targetLocation, radius, radius, radius)) {
				if (!(entity instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)entity;
				if (Damage.Is_Possible(attacker, target)) {
					Damage.NormalAttack(player, target, damage, _types);
					if (whenHit != null)
						whenHit.run(target);
				}
			}
		}
		else {
			// 다른 엔티티의 공격
			for (Entity entity : targetLocation.getWorld().getNearbyEntities(targetLocation, radius, radius, radius)) {
				if (!(entity instanceof LivingEntity) || entity == attacker)
					continue;
				LivingEntity target = (LivingEntity)entity;
				target.damage(damage, attacker);
				if (whenHit != null)
					whenHit.run(target);
			}
		}
	}
	
	/**
	 * 원통형 범위를 공격
	 */
	public static void attackCylinder(LivingEntity attacker,
			Location targetLocation, double radius, double height,
			double damage, WhenHit whenHit, DamageType... _types)
	{ attackCylinder(attacker, targetLocation, radius, height, damage, whenHit, false, _types); }
	public static void attackCylinder(LivingEntity attacker,
			Location targetLocation, double radius, double height,
			double damage, WhenHit whenHit, boolean ignoreNodamage,
			DamageType... _types)
	{
		if (attacker instanceof Player) {
			// 플레이어의 공격
			Player player = (Player)attacker;
			for (Entity entity : targetLocation.getWorld().getNearbyEntities(targetLocation, radius, height, radius)) {
				if (!(entity instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)entity;
				if (ignoreNodamage)
					target.setNoDamageTicks(0);
				if (Damage.Is_Possible(attacker, target)) {
					Damage.NormalAttack(player, target, damage, _types);
					if (whenHit != null)
						whenHit.run(target);
				}
			}
		}
		else {
			// 다른 엔티티의 공격
			for (Entity entity : targetLocation.getWorld().getNearbyEntities(targetLocation, radius, height, radius)) {
				if (!(entity instanceof LivingEntity) || entity == attacker)
					continue;
				LivingEntity target = (LivingEntity)entity;
				if (ignoreNodamage)
					target.setNoDamageTicks(0);
				target.damage(damage, attacker);
				if (whenHit != null)
					whenHit.run(target);
			}
		}
	}
	
	/**
	 * 사각기둥 범위를 공격
	 */
	public static void attackHitbox(LivingEntity attacker,
			Location targetLocation, Vector size, Vector rotation,
			double damage, WhenHit whenHit, DamageType... _types)
	{
		double maxLength = Math.max(size.getX(), Math.max(size.getY(), size.getZ()));
		List<Entity> entities = Hitbox.Targets_In_the_Box(targetLocation.toVector(), rotation, size,
				new ArrayList<Entity>(targetLocation.getWorld().getNearbyEntities(targetLocation, maxLength, maxLength, maxLength)));
		
		if (attacker instanceof Player) {
			// 플레이어의 공격
			Player player = (Player)attacker;
			for (Entity entity : entities) {
				if (!(entity instanceof LivingEntity))
					continue;
				LivingEntity target = (LivingEntity)entity;
				if (Damage.Is_Possible(attacker, target)) {
					Damage.NormalAttack(player, target, damage, _types);
					if (whenHit != null)
						whenHit.run(target);
				}
			}
		}
		else {
			// 다른 엔티티의 공격
			for (Entity entity : entities) {
				if (!(entity instanceof LivingEntity) || entity == attacker)
					continue;
				LivingEntity target = (LivingEntity)entity;
				target.damage(damage, attacker);
				if (whenHit != null)
					whenHit.run(target);
			}
		}
	}
}
