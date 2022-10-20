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
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class BindUp extends RegisteredSkill
{
	public static BindUp skill;
	
	public BindUp()
	{	
		super(new BindUp_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("sec", new LinearValue(1.15, 0.15));
		addModifier("damage", new LinearValue(8, 1));
		addModifier("cooldown", new LinearValue(20.5, 0.5));
		addModifier("stamina", new LinearValue(25.5, 0.5));
		
		skill = this;
	}
}

class BindUp_Handler extends MalaTargetSkill implements Listener
{
	public BindUp_Handler()
	{
		super(	"BINDUP",
				"포박술",
				Material.STRING,
				MsgTBL.NeedSkills,
				"&e 훅 샷 - lv.10",
				"",
				MsgTBL.WEAPON + MsgTBL.SKILL,
				"",
				"&720m 내에 있는 적 하나를 제자리에 묶습니다.",
				"&7이 때 해당 적 주변 5m 내의 적들도 함께 묶입니다.",
				"&7묶어둔 적은 0.5초마다 &e{damage}&7의 피해를 받습니다.",
				"&7포박은 &e{sec}&7초 유지됩니다.",
				"&c채찍을 들고 있어야 합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		super.range = 20;
	}

	@Override
	public TargetSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "HOOKSHOT", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			
			return new TargetSkillResult(cast, 0, InteractionType.OFFENSE_SKILL);
		}
		if (!Weapon_Identify.Hold_MMO_Whip(cast.getCaster().getPlayer()))
		{
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			
			return new TargetSkillResult(cast, 0, InteractionType.OFFENSE_SKILL);
		}
		return super.getResult(cast);
	}
	
	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double sec = cast.getModifier("sec");
		double damage = cast.getModifier("damage");
		
		
		if (Damage.Is_Possible(data.getPlayer(), _data.getTarget()))
		{
			Location loc = _data.getTarget().getEyeLocation();
			loc.getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
			loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 64, 2.0d, 2.0d, 2.0d, 0d);
			
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
					new BindUp_Skill(data.getPlayer(), _data.getTarget(), damage, sec));
		}
		else
		{
			for (ClassSkill cs : data.getProfess().getSkills())
			{
				if (cs.getSkill() != BindUp.skill)
					continue;
				CooldownInfo ci = data.getCooldownMap().getInfo(cs);
				if (ci == null)
					continue;
				ci.reduceFlat(999.0);
			}
		}
	}
}

class BindUp_Skill implements Runnable
{
	Player player;
	LivingEntity target;
	List<LivingEntity> targets = new ArrayList<LivingEntity>();
	
	double size = 5.0;
	double damage, duration;
	Particle particle = Particle.WAX_ON;
	Location loc;
	
	public BindUp_Skill(Player _player, LivingEntity _target, double _damage, double _duration)
	{
		player = _player; target = _target; damage = _damage; duration = _duration;
		loc = target.getLocation();
		
		Particle_Drawer.Draw_Line(player.getEyeLocation(), target.getEyeLocation(), particle, 0.1);
		
		AddNearbyEntities();
	}
	
	public void AddNearbyEntities()
	{
		Particle_Drawer.Draw_Circle(target.getEyeLocation(), particle, size);
		for (Entity e : target.getNearbyEntities(size, size, size))
		{
			if (Damage.Is_Possible(player, e) && e instanceof LivingEntity)
			{
				targets.add((LivingEntity)e);
			}
		}
	}
	
	int counter = 0;
	public void run()
	{
		if (counter % 20 == 0)
		{
			player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
			Random rand = new Random();
			double drawSize = target.getHeight();
			Location drawLoc = loc.clone().add(0, target.getEyeHeight() * 0.5, 0);
			Particle_Drawer.Draw_Line(player.getLocation().add(0, player.getHeight() * 0.5, 0), drawLoc, particle, 0.1);
			for (int i = 0; i < 3; i++)
			{
				Particle_Drawer.Draw_Circle(drawLoc.clone().add(0, rand.nextDouble(target.getHeight()), 0),
					particle, drawSize, -60.0 + rand.nextDouble(120.0), rand.nextDouble(360.0));
			}
		}
		
		target.teleport(loc);
		if (counter % 10 == 0)
			Damage.Attack(player, target, damage, DamageType.PHYSICAL, DamageType.SKILL);
		for (LivingEntity le : targets)
		{
			if (counter % 10 == 0)
				Damage.Attack(player, le, damage, DamageType.PHYSICAL, DamageType.SKILL);
			le.teleport(loc);
		}
		
		counter++;
		duration -= 0.05;
		if (duration > 0.0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}