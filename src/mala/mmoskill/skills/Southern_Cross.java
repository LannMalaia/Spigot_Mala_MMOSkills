package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.RayUtil;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Southern_Cross extends RegisteredSkill
{
	public static Southern_Cross skill;
	
	public Southern_Cross()
	{	
		super(new Southern_Cross_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(27, 2));
		addModifier("count", new LinearValue(3.25, 0.25));
		addModifier("cooldown", new LinearValue(1, 0));
		addModifier("stamina", new LinearValue(31.0, 1.0));
		
		skill = this;
	}
}

class Southern_Cross_Handler extends MalaSkill implements Listener
{
	public Southern_Cross_Handler()
	{
		super(	"SOUTHERN_CROSS",
				"남십자성",
				Material.NETHER_STAR,
				MsgTBL.PHYSICAL + MsgTBL.WEAPON,
				"",
				"&7높게 뛰어올라 지점 하나를 선택해 강하합니다.",
				"&7이후 해당 지점에 거대한 십자 모양의 검기를 분출시켜",
				"&7주변 적들에게 &e{damage}&7의 피해를 줍니다.",
				"&7공중에 있으면 바로 강하를 시도합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double damage = cast.getModifier("damage");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Southern_Cross_Skill(cast, data.getPlayer(), damage));
	}
}

class Southern_Cross_Skill implements Runnable
{
	SkillMetadata cast;
	Player player;
	double damage;
	
	double radius = 12.0;
	double waitTime = 0.0, remainedTime = 3.0;
	Particle particle = Particle.FIREWORKS_SPARK;
	double savedYaw, savedPitch;
	Location targetLocation = null;
	
	public Southern_Cross_Skill(SkillMetadata cast, Player player, double damage)
	{
		this.cast = cast;
		this.player = player; this.damage = damage;
		
		// 지상에 있을 경우 점프
		if (((LivingEntity)player).isOnGround()) {
			Vector dir = player.getLocation().getDirection()
					.setY(0)
					.normalize()
					.multiply(0.1)
					.setY(1.5);
			player.setVelocity(dir);
			waitTime = 0.6;
		}
	}
	
	private void drawChargeParticle() {
		Location location = player.getEyeLocation();
		new Effect(location, Particle.DUST_COLOR_TRANSITION)
			.append2DArc(120.0, 5.0)
			.rotate(0, -60, 0)
			.scale(3.5, 1.0, 1.5)
			.translate(-5.0, 0, 0)
			.rotate(45, 0, 0)
			.rotate(0, savedYaw, -savedPitch)
			.setDustTransition(new DustTransition(Color.BLACK, Color.RED, 2.0f))
			.playEffect();
		new Effect(location, Particle.DUST_COLOR_TRANSITION)
			.append2DArc(120.0, 5.0)
			.rotate(0, -60, 0)
			.scale(3.5, 1.0, 1.5)
			.translate(-5.0, 0, 0)
			.rotate(-45, 0, 0)
			.rotate(0, savedYaw, -savedPitch)
			.setDustTransition(new DustTransition(Color.BLACK, Color.RED, 2.0f))
			.playEffect();
		new Effect(location, Particle.DUST_COLOR_TRANSITION)
			.append2DCrossedStar(6.0)
			.translate(0, -6.5, 0)
			.rotate(0, 0, -savedPitch)
			.rotate(0, savedYaw, 90)
			.setDustTransition(new DustTransition(Color.RED, Color.BLACK, 2.0f))
			.playEffect();
	}
	
	public void run()
	{
		if (waitTime > 0.0) {
			waitTime -= 0.05;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			return;
		}
		
		if (targetLocation == null) {
			targetLocation = RayUtil.getLocation(player);
			if (targetLocation == null) {
				player.sendMessage("§c[ 강하 위치를 찾지 못했거나 너무 멀리 있습니다. ]");
				return;
			}
			if (targetLocation.getY() >= player.getLocation().getY() - 5) {
				player.sendMessage("§c[ 강하 위치가 플레이어 위에 있거나 너무 가깝습니다. ]");
				player.sendMessage("" + targetLocation.getY() + " :: " + player.getLocation().getY());
				return;
			}
			savedYaw = player.getLocation().getYaw() + 90.0;
			savedPitch = player.getLocation().getPitch();
		}
		
		// 거리가 어느정도 가까워지면
		if (player.getLocation().distance(targetLocation) < 2.5) {
			for (int i = 0; i < 8; i++) {
				double size = (radius / 8.0) * i;
				double maxY = Math.pow(5.0 - 0.5 * i, 2.0);
				new Effect(targetLocation, Particle.DUST_COLOR_TRANSITION)
					.setDustTransition(new DustTransition(Color.BLACK, Color.RED, 2.0f))
					.append2DCrossedStar(size, 3.0)
					.randomizePoint(RANDOMIZE_TYPE.ADD, 0, 0, 0, 0, maxY, 0)
					.scaleVelocity(0)
					.playEffect();
			}
			new Effect(targetLocation, Particle.DUST_COLOR_TRANSITION)
				.setDustTransition(new DustTransition(Color.BLACK, Color.RED, 2.0f))
				.append2DCrossedStar(radius, 3.0)
				.randomizePoint(RANDOMIZE_TYPE.ADD, 0, 0, 0, 0, 0.5, 0)
				.scaleVelocity(0)
				.playEffect();
			new Effect(targetLocation, Particle.DUST_COLOR_TRANSITION)
				.setDustTransition(new DustTransition(Color.BLACK, Color.RED, 2.0f))
				.append2DCircle(radius, 3.0)
				.randomizePoint(RANDOMIZE_TYPE.ADD, 0, 0, 0, 0, 1.5, 0)
				.scaleVelocity(0)
				.playEffect();
			
			for (Entity en : targetLocation.getWorld().getNearbyEntities(targetLocation, radius, radius, radius)) {
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;

				LivingEntity le = (LivingEntity)en;
				if (Damage.Is_Possible(player, le) && le.getNoDamageTicks() == 0) {
					Damage.SkillAttack(cast, le, damage, DamageType.WEAPON, DamageType.PHYSICAL);
				}
			}

//			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
//					new Southern_Cross_AfterEffect(player, targetLocation, 0));
			for (int i = 0; i < 360; i += 45) {
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
						new Southern_Cross_AfterEffect(player, targetLocation, i));
			}
			return;
		}
		
		// 적 방향으로 이동
		Location lastLocation = player.getLocation().add(0, player.getHeight() * 0.5, 0);
		remainedTime -= 0.05;
		Vector dir = targetLocation.clone().subtract(player.getLocation()).toVector().normalize();
		Vector vc = dir.multiply(2.0);
		player.setVelocity(vc);
		player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, lastLocation, 10, 0.4, 0.2, 0.4, 0.0);
		drawChargeParticle();
		
		if (remainedTime > 0) {
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}


class Southern_Cross_AfterEffect implements Runnable {
	
	Player player;
	Location startLocation;
	double angle;
	
	Vector direction;
	double lastTime = 2.0;
	double size = 2.0, maxSize = 7.0;
	double distance = 5.0, maxDistance = 20.0;
	
	public Southern_Cross_AfterEffect(Player player, Location startLocation, double angle) {
		this.player = player; this.startLocation = startLocation; this.angle = angle;
		
		direction = new Vector(Math.cos(Math.toRadians(angle)), 0, Math.sin(Math.toRadians(angle)));
	}
	
	public void run() {
		Location location = startLocation.clone().add(direction.clone().multiply(distance));
		new Effect(location, Particle.DUST_COLOR_TRANSITION)
			.append2DArc(120.0, size * 0.5, 0.8)
			.rotate(0, -60, 0)
			.scale(1.4, 1.0, 2.0)
			.rotate(90, angle, 0)
			.setDustTransition(new DustTransition(Color.RED, Color.BLACK, 2.0f))
			.playEffect();
//		new Effect(location, Particle.DUST_COLOR_TRANSITION)
//			.append2DArc(120.0, 5.0, 0.7)
//			.rotate(0, -60, 0)
//			.scale(1.4, 1.0, 2.0)
//			.rotate(135, angle, 0)
//			.setDustTransition(new DustTransition(Color.RED, Color.BLACK, 2.0f))
//			.playEffect();
//		new Effect(location, Particle.DUST_COLOR_TRANSITION)
//			.append2DCrossedStar(6.0, 0.4)
//			.translate(0, -6.5, 0)
//			.rotate(0, angle, 90)
//			.setDustTransition(new DustTransition(Color.RED, Color.BLACK, 2.0f))
//			.playEffect();
		
		distance = distance + (maxDistance - distance) * 0.1;
		size = size + (maxSize - size) * 0.1;
		lastTime -= 0.05;
		if (lastTime > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}