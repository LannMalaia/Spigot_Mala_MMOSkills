package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class LightSpeed_Blade extends RegisteredSkill
{
	public static LightSpeed_Blade skill;
	
	public LightSpeed_Blade()
	{	
		super(new LightSpeed_Blade_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(57.5, 17.5));
		addModifier("count", new LinearValue(3.3, 0.3));
		addModifier("cooldown", new LinearValue(25, -0.5));
		addModifier("stamina", new LinearValue(31.5, 1.5));
		
		skill = this;
	}
}

class LightSpeed_Blade_Handler extends MalaTargetSkill implements Listener
{
	public LightSpeed_Blade_Handler()
	{
		super(	"LIGHTSPEED_BLADE",
				"섬광검무",
				Material.DRAGON_BREATH,
				MsgTBL.PHYSICAL + MsgTBL.WEAPON + MsgTBL.SKILL,
				"",
				"&720m 거리의 적 하나를 표적으로 삼습니다.",
				"&7이후 잽싸게 움직여,",
				"&7적들 사이를 오가며 &e{damage}&7의 피해를 줍니다.",
				"&7이후 관성에 의해 잠시동안은 움직일 수 없습니다.",
				"&7최대 &e{count}&7마리의 적을 공격합니다.",
				"&7대상이 죽거나 2.5초동안 접근하지 못할 경우 취소됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		super.range = 20;
	}

	
	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double damage = cast.getModifier("damage");
		int count = (int)cast.getModifier("count");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new LightSpeed_Blade_Skill(cast, data.getPlayer(), _data.getTarget(), damage, count));
	}
}

class LightSpeed_Blade_Skill implements Runnable
{
	SkillMetadata cast;
	Player player;
	LivingEntity target;
	List<LivingEntity> targets = new ArrayList<LivingEntity>();
	
	int count;
	double damage;
	Particle particle = Particle.FIREWORKS_SPARK;
	Location startLocation;
	
	public LightSpeed_Blade_Skill(SkillMetadata cast, Player player, LivingEntity target, double damage, int maxCount)
	{
		this.cast = cast;
		this.player = player; this.target = target; this.damage = damage; this.count = maxCount;
		startLocation = player.getLocation().add(0, player.getHeight() * 0.5, 0);
		
		targets.add(target);
		count--;
		
		addTargets();
		//drawParticle();
		
		startLocation.getWorld().playSound(startLocation, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 2.0f);
		/*
		LivingEntity lastTarget = targets.get(targets.size() - 1);
		Vector dir = lastTarget.getLocation().getDirection().setY(0).multiply(-1);
		Location teleportLocation = lastTarget.getLocation().add(dir);
		teleportLocation.setDirection(dir);
		player.teleport(teleportLocation);
		*/
	}
	
	/** 타겟 리스트 추가 */
	private void addTargets() {
		LivingEntity lastTarget = target;
		while (count > 0) {
			LivingEntity finded = null;
			for (Entity e : lastTarget.getNearbyEntities(8.0, 4.0, 8.0)) {
				if (e instanceof LivingEntity && !targets.contains(e) && e != player) {
					LivingEntity temp = (LivingEntity)e;
					if (finded == null) {
						finded = temp;
					} else {
						if (lastTarget.getLocation().distance(temp.getLocation())
								< lastTarget.getLocation().distance(finded.getLocation())) {
							finded = temp;
						}
					}
				}
			}
			if (finded != null) {
				targets.add(finded);
				count--;
				lastTarget = finded;
			} else {
				break;
			}
		}
	}
	/** 시작 이펙트 그리기 */
	private void drawParticle() {
		Location lastLocation = targets.get(0).getLocation().add(0, targets.get(0).getHeight() * 0.5, 0);
		Lightning_Bolt.Draw_Lightning_Line(startLocation, lastLocation, particle);
		for (int i = 0; i < targets.size() - 1; i++) {
			LivingEntity newTarget = targets.get(i + 1);
			startLocation = lastLocation;
			lastLocation = newTarget.getLocation().add(0, newTarget.getHeight() * 0.5, 0);
			Lightning_Bolt.Draw_Lightning_Line(startLocation, lastLocation, particle);
		}
	}
	
	double remainedSecond = 3.0;
	Vector vc = new Vector();
	public void run()
	{
//		// 묶여진 적들은 움직이지 못한다
//		for (LivingEntity enemy : targets) {
//		}
		
		// 적이 죽었거나 월드가 다르면 이동 X
		if (target.isDead() || player.getWorld() != target.getWorld()) {
			return;
		}
		
		// 거리가 어느정도 가까워지면
		if (player.getLocation().distance(target.getLocation()) < 2.5) {
			Location location = target.getLocation().add(0, target.getHeight() * 0.5, 0);
			location.getWorld().spawnParticle(Particle.FLASH, location, 1, 0, 0, 0, 0);
			double yRot = Math.random() * 360.0;
			new Effect(location, Particle.CRIT)
				.append2DCrossedStar(3.0, 2.0)
				.scalePoint(0.1)
				.scaleVelocity(0.7)
				.rotate(45.0, yRot, 0.0)
				//.addSound(Sound.ENTITY_BLAZE_HURT, 0.8, 2.0)
				.playEffect();
			new Effect(location, Particle.FIREWORKS_SPARK)
				.append2DCrossedStar(3.0, 2.0)
				.scalePoint(0.1)
				.scaleVelocity(0.25)
				.rotate(-45.0, yRot, 0.0)
				//.addSound(Sound.ENTITY_WITHER_SHOOT, 2.0, 0.5)
				.playEffect();
			location.getWorld().playSound(location, "mala_sound:skill.slash", SoundCategory.NEUTRAL, 1.0f, 1.0f);
			Damage.SkillAttack(cast, target, damage, DamageType.PHYSICAL, DamageType.WEAPON, DamageType.SKILL);
			
			// 타겟 갱신
			targets.remove(0);
			if (targets.size() > 0) {
				target = targets.get(0);
			}
			else {
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new LightSpeed_Blade_AfterEffect(player, vc.clone()));
				return;
			}
			remainedSecond = 2.5;
		}
		
		// 적 방향으로 이동
		Location lastLocation = player.getLocation().add(0, player.getHeight() * 0.5, 0);
		remainedSecond -= 0.05;
		Vector dir = target.getLocation().subtract(player.getLocation()).toVector().normalize();
		vc = dir.multiply(4.0);
		player.setVelocity(vc);
		player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, lastLocation, 10, 0.4, 0.2, 0.4, 0.0);
		Lightning_Bolt.Draw_Lightning_Line(startLocation, lastLocation, particle);
		startLocation = lastLocation;
		
		if (targets.size() > 0 && remainedSecond > 0) {
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}

class LightSpeed_Blade_AfterEffect implements Runnable {
	Player player;
	Vector velocity;
	double remainedSecond = 0.7;
	
	public LightSpeed_Blade_AfterEffect(Player player, Vector velocity) {
		this.player = player;
		this.velocity = velocity;
	}
	
	public void run() {

		velocity = new Vector(velocity.getX() * 0.75, velocity.getY() * 0.75, velocity.getZ() * 0.75);
		player.setVelocity(velocity);
		
		remainedSecond -= 0.05;
		if (remainedSecond > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}