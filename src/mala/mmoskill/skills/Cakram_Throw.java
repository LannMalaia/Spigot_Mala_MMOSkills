package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.skills.passive.Make_Doppel;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Cakram_Throw extends RegisteredSkill
{
	public Cakram_Throw()
	{	
		super(new Cakram_Throw_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("fly_damage", new LinearValue(22, 2));
		addModifier("wall_damage", new LinearValue(11, 1));
		addModifier("sec", new LinearValue(1.1, 0.1));
		addModifier("cooldown", new LinearValue(24.6, -0.4));
		addModifier("stamina", new LinearValue(14.5, 0.5));
	}
}

class Cakram_Throw_Handler extends MalaSkill implements Listener
{
	public Cakram_Throw_Handler()
	{
		super(	"CAKRAM_THROW",
				"차크람",
				Material.MUSIC_DISC_STAL,
				MsgTBL.NeedSkills,
				"&e 표창 투척 - lv.20",
				"",
				MsgTBL.WEAPON + MsgTBL.PROJECTILE,
				"",
				"&750m 거리까지 나아가는 3m 크기의 차크람을 던집니다.",
				"&7차크람에 부딪힌 적들은 &e{fly_damage}&7의 피해를 받습니다.",
				"&7이후 벽에 박혀 &e{sec}&7초간 유지되며,",
				"&7이 때에는 1초마다 주변 5m에 &8{wall_damage}&7의 피해를 줍니다.",
				"&7이 공격은 스킬로 취급되지 않습니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "비행 피해 50% 증가",
				MsgTBL.WEAPON_WHIP + "폭발 피해, 범위 30% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "DAGGER_THROW", 20))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double duration = cast.getModifier("sec");
		double flyDamage = cast.getModifier("fly_damage");
		if (Weapon_Identify.Hold_Sword(data.getPlayer()))
			flyDamage *= 1.5;
		double wallDamage = cast.getModifier("wall_damage");
		double wallSize = 5.0;
		if (Weapon_Identify.Hold_MMO_Whip(data.getPlayer()))
		{
			wallDamage *= 1.3;
			wallSize *= 1.3;
		}
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
				new Cakram_Throw_Skill(cast, data.getPlayer().getEyeLocation(), data.getPlayer(),
				data.getPlayer().getLocation().getDirection(), flyDamage, wallDamage, duration, wallSize));
	}
}

class Cakram_Throw_Skill implements Runnable
{
	SkillMetadata cast;
	World world;
	Player player;
	double damageFly;
	double damageWall;

	double distance = 50.0;
	double curSpeed = -1.0;
	double targetSpeed = 3.0;
	double duration = 3.0;
	double wallSize = 5.0;
	boolean isWall = false;
	
	Location start_loc;
	Vector dir;

	double size = 3.0;
	Location before_loc, current_loc;
	Vector[] vecs;
	
	public Cakram_Throw_Skill(SkillMetadata cast, Location _start_loc, Player _player, Vector _dir,
			double _damageFly, double _damageWall, double _duration, double _wallSize)
	{
		this.cast = cast;
		start_loc = _start_loc;
		player = _player;
		dir = _dir;
		damageFly = _damageFly;
		damageWall = _damageWall;
		duration = _duration;
		wallSize = _wallSize;

		world = player.getWorld();
		current_loc = start_loc.clone().add(dir.clone().multiply(0.5));
		before_loc = current_loc.clone();
		
		player.setVelocity(player.getVelocity().add(player.getLocation().getDirection().multiply(-1.0)));
		
		world.playSound(start_loc, Sound.ENTITY_BLAZE_SHOOT, 1, 2);
		makeVecs();
	}
	
	public void makeVecs()
	{
		vecs = new Vector[(int) (size * 90)];
		double additive = 360.0 / (size * 30);
		for(int i = 0; i < vecs.length; i += 3)
		{
			double _angle = additive * i;
			vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			vecs[i + 1] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.9, 0, Math.sin(Math.toRadians(_angle)) * 0.9);
			vecs[i + 2] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.8, 0, Math.sin(Math.toRadians(_angle)) * 0.8);
		}
		vecs = TRS.Scale(vecs, size, size, size);
	}
	double correctionAngle = 0.0;
	public void drawVecs()
	{
		correctionAngle += 3.0;
		Vector[] tempvecs = TRS.Rotate_Y(vecs, correctionAngle);
		tempvecs = TRS.Rotate_Z(tempvecs, 30.0);
		tempvecs = TRS.Rotate_X(tempvecs, start_loc.getPitch());
		tempvecs = TRS.Rotate_Y(tempvecs, start_loc.getYaw());
		for (Vector vec : tempvecs)
			world.spawnParticle(Particle.CRIT, current_loc.clone().add(vec), 1, 0d, 0d, 0d, 0d);
	}
	
	public void run()
	{
		if (isWall)
			wall();
		else
			flying();
		drawVecs();
		
		// 마무리
		before_loc = current_loc.clone();
		if (distance < 0.0 || duration >= 0.0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	
	int flyCounter = 0;
	public void flying()
	{
		// 이동 처리
		flyCounter++;
		distance -= curSpeed;
		curSpeed = curSpeed + (targetSpeed - curSpeed) * 0.1;
		current_loc.add(dir.clone().multiply(curSpeed));
		
		if (flyCounter % 3 == 0)
			world.playSound(current_loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 0.5f);
		
		// 벽 박히나 확인하기
		RayTraceResult rtr = world.rayTraceBlocks(current_loc, dir, curSpeed, FluidCollisionMode.NEVER, true);
		if (rtr == null) // 어딘가에 안 부딪힘
		{
			Location loc = before_loc.clone().add(dir.clone().multiply(curSpeed * 0.5));
			List<Entity> entities = Hitbox.Targets_In_the_Box(loc.toVector(),
					new Vector(current_loc.getPitch(), current_loc.getYaw(), 0),
					new Vector(size, size, curSpeed),
					new ArrayList<Entity>(world.getNearbyEntities(before_loc, targetSpeed, targetSpeed, targetSpeed)));
			for (Entity entity : entities)
				if (Damage.Is_Possible(player, entity) && entity instanceof LivingEntity)
					Damage.SkillAttack(cast, (LivingEntity)entity, damageFly, DamageType.WEAPON, DamageType.PROJECTILE);
		}
		else // 부딪힘
		{
			if (rtr.getHitBlock() != null) // 블록에 부딪힘
			{
//				// player.sendMessage("block hit");
				isWall = true;
				current_loc = rtr.getHitPosition().toLocation(world);
				world.playSound(current_loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.5f);
			}
		}
	}
	
	int wallCounter = 0;
	public void wall()
	{
		duration -= 0.05;
		if (++wallCounter % 20 == 0)
		{
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new WallEffect(start_loc, current_loc, size, wallSize));
			for (Entity entity : world.getNearbyEntities(current_loc, wallSize, wallSize, wallSize))
				if (Damage.Is_Possible(player, entity) && entity instanceof LivingEntity)
					Damage.SkillAttack(cast, (LivingEntity)entity, damageFly, DamageType.WEAPON, DamageType.PROJECTILE);
		}
	}
}

class WallEffect implements Runnable
{
	Location startLoc, curLoc;
	double curSize = 3.0;
	double targetSize;
	Vector[] vecs;
	public WallEffect(Location _startloc, Location _loc, double _wallSize, double _targetSize)
	{
		curSize = _wallSize;
		targetSize = _targetSize;
		startLoc = _startloc;
		curLoc = _loc;
		curLoc.getWorld().playSound(curLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.5f, 1.5f);
		makeVecs();
	}

	public void makeVecs()
	{
		vecs = new Vector[(int) (targetSize * 20)];
		double additive = 360.0 / (targetSize * 20);
		for(int i = 0; i < vecs.length; i++)
		{
			double _angle = additive * i;
			vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
		}
		vecs = TRS.Rotate_Z(vecs, 30.0);
		vecs = TRS.Rotate_X(vecs, startLoc.getPitch());
		vecs = TRS.Rotate_Y(vecs, startLoc.getYaw());
	}
	public void drawVecs()
	{
		for (Vector vec : TRS.Scale(vecs, curSize, curSize, curSize))
			curLoc.getWorld().spawnParticle(Particle.CRIT, curLoc.clone().add(vec), 1, 0d, 0d, 0d, 0d);
	}
	double timer = 0.5;
	public void run()
	{
		curSize = curSize + (targetSize - curSize) * 0.2;
		drawVecs();

		timer -= 0.05;
		if (timer >= 0.0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}





