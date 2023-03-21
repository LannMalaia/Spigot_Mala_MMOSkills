package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Energy_Bullet extends RegisteredSkill
{
	public Energy_Bullet()
	{	
		super(new Energy_Bullet_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(6, 1.0));
		addModifier("damage", new LinearValue(5, 4));
		addModifier("cooldown", new LinearValue(0.5, 0));
		addModifier("stamina", new LinearValue(11.5, 1.5));
	}
}

class Energy_Bullet_Handler extends MalaSkill implements Listener
{
	public Energy_Bullet_Handler()
	{
		super(	"ENERGY_BULLET",
				"기공탄",
				Material.GHAST_TEAR,
				MsgTBL.NeedSkills,
				"&e 격투 마스터리 - lv.5",
				"",
				MsgTBL.UNARMED + MsgTBL.PHYSICAL + MsgTBL.PROJECTILE,
				"",
				"&e{distance}&7 거리까지 나아가는 기공탄을 발사합니다.",
				"&7기공탄은 끝까지 나아가거나 부딪혔을 때 폭발하며,",
				"&7폭발 시 주변 적들에게 &e{damage}&7의 피해를 줍니다.",
				"&7멀리 나아갈수록 폭발 범위도 증가합니다.",
				"&c양손에 무기가 없어야 합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if (!Skill_Util.Has_Skill(data, "MASTERY_FIST", 5))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		
		if (!Weapon_Identify.Has_No_Item(player))
		{
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double distance = cast.getModifier("distance");
		double damage = cast.getModifier("damage");
		Vector dir = data.getPlayer().getLocation().getDirection();
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1.5f);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Energy_Bullet_Bolt(cast, data.getPlayer().getEyeLocation().subtract(0, 0.2, 0), data.getPlayer(), dir, damage, 1.4, distance));
	}
}

class Energy_Bullet_Bolt implements Runnable
{
	SkillMetadata cast;
	Player player;
	double damage;
	double max_distance;
	double speed;
	Location start_loc;
	Vector dir;

	double explosion_size = 2.0;
	double max_explosion_size = 7.0;
	double current_distance = 0;
	Location before_loc, current_loc;
	
	public Energy_Bullet_Bolt(SkillMetadata cast, Location _start_loc, Player _player, Vector _dir, double _damage, double _speed, double _max_distance)
	{
		this.cast = cast;
		start_loc = _start_loc;
		player = _player;
		dir = _dir;
		damage = _damage;
		speed = _speed;
		max_distance = _max_distance;

		current_loc = start_loc.clone();
		before_loc = start_loc.clone();
		player.getWorld().getChunkAt(player.getLocation()).getEntities();
	}
	
	public void run()
	{
		current_distance += speed;
		explosion_size += speed * 0.15;
		if (max_distance < current_distance)
			speed = max_distance - current_distance;
		if (max_explosion_size < explosion_size)
			explosion_size = max_explosion_size;
		current_loc.add(dir.clone().multiply(speed));

		boolean attacked = false;
		
		// 라인 그리기
		Vector gap = current_loc.clone().subtract(before_loc).toVector();
		if(gap.length() <= 0.01)
			attacked = true;
		
		for(double i = 0; i < gap.length(); i += 0.1)
		{
			Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
			current_loc.getWorld().spawnParticle(
				explosion_size < 4 ? Particle.CRIT : explosion_size < 6.0 ? Particle.EXPLOSION_NORMAL : Particle.EXPLOSION_LARGE,
				loc, 1, 0, 0, 0, 0);
		}

		
		// 주변 적 찾기
		for(double i = 0; i <= gap.length(); i += 1)
		{
			if (attacked)
				break;
			Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
			for (Entity e : loc.getWorld().getNearbyEntities(loc, 1, 1, 1))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e == player)
					continue;
				
				current_loc = loc;
				attacked = true;
				break;
			}
			if (!loc.getBlock().isPassable())
				attacked = true;
		}
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(current_distance > max_distance || attacked)
		{
			for (Entity e2 : current_loc.getWorld().getNearbyEntities(current_loc, explosion_size, explosion_size, explosion_size))
			{
				if(!(e2 instanceof LivingEntity))
					continue;
				if(e2 == player)
					continue;
				
				// 찾은 경우 피해 주고 그냥 끝냄
				LivingEntity target = (LivingEntity)e2;
				Damage.SkillAttack(cast, target, damage,
						DamageType.UNARMED, DamageType.PROJECTILE);
			}
			current_loc.getWorld().playSound(current_loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
			current_loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, current_loc,
					(int)(explosion_size * 30),
					explosion_size * 0.5, explosion_size * 0.5, explosion_size * 0.5, 0);
			for (int i = 0; i < explosion_size - 1; i++)
				Particle_Drawer.Draw_Circle(current_loc, Particle.CLOUD, explosion_size, Math.random() * 360.0, Math.random() * 360.0);
			return;
		}
					
		// 마무리
		before_loc = current_loc.clone();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}