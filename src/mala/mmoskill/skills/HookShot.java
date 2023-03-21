package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class HookShot extends RegisteredSkill
{
	public static HookShot skill;
	
	public HookShot()
	{	
		super(new HookShot_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("length", new LinearValue(10.5, 0.5));
		addModifier("damage", new LinearValue(27.0, 7.0));
		addModifier("cooldown", new LinearValue(2, 0));
		addModifier("stamina", new LinearValue(26, 1));
		
		skill = this;
	}
}

class HookShot_Handler extends MalaSkill implements Listener
{
	public HookShot_Handler()
	{
		super(	"HOOKSHOT",
				"훅 샷",
				Material.FISHING_ROD,
				MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&e{length}&7m 길이의 밧줄을 던집니다.",
				"&7벽에 닿을 경우 해당 벽을 향해 빠르게 이동합니다.",
				"&7적에게 닿을 경우 &e{damage}&7의 피해를 가하며,",
				"&7이후 상대가 무거우면 끌려가고, 가벼우면 끌어옵니다.",
				"&7언제든지 웅크려서 취소할 수 있습니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_WHIP + "길이 50% 증가, 주변의 적을 같이 끌어옵니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double length = cast.getModifier("length");
		double damage = cast.getModifier("damage");
		boolean isWhip = false;
		if (Weapon_Identify.Hold_MMO_Whip(data.getPlayer()))
		{
			length *= 1.5;
			isWhip = true;
		}
		
		Vector dir = data.getPlayer().getLocation().getDirection();
		RayTraceResult rtr = data.getPlayer().getWorld().rayTrace(
				data.getPlayer().getEyeLocation().add(dir.multiply(1.5)),
				dir,
				length, FluidCollisionMode.NEVER, true, 0.1, null);
		if (rtr == null)
		{
			for (ClassSkill cs : data.getProfess().getSkills())
			{
				if (cs.getSkill() != HookShot.skill)
					continue;
				CooldownInfo ci = data.getCooldownMap().getInfo(cs);
				if (ci == null)
					continue;
				ci.reduceFlat(999.0);
			}
			return;
		}
		
		Location loc = rtr.getHitPosition().toLocation(data.getPlayer().getWorld());
		loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 2.0f);
		loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0d, 0d, 0d, 0d);
		
		if (data.getPlayer().getLocation().toVector().distance(rtr.getHitPosition()) > length)
		{
			data.getPlayer().sendMessage("§c너무 먼 곳을 지정했어요.");
			for (ClassSkill cs : data.getProfess().getSkills())
			{
				if (cs.getSkill() != HookShot.skill)
					continue;
				CooldownInfo ci = data.getCooldownMap().getInfo(cs);
				if (ci == null)
					continue;
				ci.reduceFlat(999.0);
			}
		}
		if (rtr.getHitEntity() != null)
		{
			// data.getPlayer().sendMessage("entity hit - " + rtr.getHitEntity().getType());
			if (Damage.Is_Possible(data.getPlayer(), rtr.getHitEntity()) && rtr.getHitEntity() instanceof LivingEntity)
			{
				Damage.SkillAttack(cast, (LivingEntity)rtr.getHitEntity(), damage, DamageType.PHYSICAL, DamageType.SKILL);
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
						new HookShot_Skill_Entity(data.getPlayer(),
								(LivingEntity)rtr.getHitEntity(),
								isWhip));
			}
		}
		else if (rtr.getHitBlock() != null)
		{
			// data.getPlayer().sendMessage("block hit - " + rtr.getHitBlock().getType());
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
					new HookShot_Skill_Block(data.getPlayer(),
							rtr.getHitPosition().toLocation(data.getPlayer().getWorld()),
							isWhip));
		}
		else
		{
			for (ClassSkill cs : data.getProfess().getSkills())
			{
				if (cs.getSkill() != HookShot.skill)
					continue;
				CooldownInfo ci = data.getCooldownMap().getInfo(cs);
				if (ci == null)
					continue;
				ci.reduceFlat(999.0);
			}
		}
	}
}

class HookShot_Skill_Block implements Runnable
{
	Player player;
	Location targetLoc;
	
	Particle particle;
	
	public HookShot_Skill_Block(Player _player, Location _targetLoc, boolean _isWhip)
	{
		player = _player; targetLoc = _targetLoc;
		particle = _isWhip ? Particle.WAX_ON : Particle.CRIT;
	}
	
	double duration = 5.0;
	public void run()
	{
		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 2.0f);
		Particle_Drawer.Draw_Line(player.getLocation().add(0, 0.75, 0), targetLoc, particle, 0.1);
		
		Vector gap = targetLoc.clone().subtract(player.getLocation().add(0, 0.75, 0)).toVector();
		Vector dir = gap.clone().normalize();
		player.setVelocity(dir.multiply(1.5));
		
		duration -= 0.05;
		if (gap.distance(new Vector()) <= 1.5 || player.isSneaking())
		{
			player.setVelocity(dir);
			return;
		}
		if (duration > 0.0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
class HookShot_Skill_Entity implements Runnable
{
	Player player;
	LivingEntity target;
	List<LivingEntity> targets = new ArrayList<LivingEntity>();
	
	double distance = 0.0;
	Particle particle;
	
	public HookShot_Skill_Entity(Player _player, LivingEntity _target, boolean _isWhip)
	{
		player = _player; target = _target;
		particle = _isWhip ? Particle.CRIT_MAGIC : Particle.CRIT;
		distance = player.getLocation().add(0, 0.75, 0).distance(target.getLocation());
		
		AddNearbyEntities();
	}
	
	public void AddNearbyEntities()
	{
		Particle_Drawer.Draw_Circle(target.getEyeLocation(), particle, 3.0);
		for (Entity e : target.getNearbyEntities(3.0, 3.0, 3.0))
		{
			if (Damage.Is_Possible(player, e) && e instanceof LivingEntity
					&& isBig(target.getType()) == isBig(e.getType()))
			{
				targets.add((LivingEntity)e);
			}
		}
	}
	
	public static boolean isBig(EntityType type)
	{
		switch(type)
		{
		case DOLPHIN:
		case COW:
		case ELDER_GUARDIAN:
		case ENDER_DRAGON:
		case GIANT:
		case HOGLIN:
		case HORSE:
		case IRON_GOLEM:
		case LLAMA:
		case MINECART:
		case MINECART_CHEST:
		case MINECART_COMMAND:
		case MINECART_FURNACE:
		case MINECART_HOPPER:
		case MINECART_MOB_SPAWNER:
		case MINECART_TNT:
		case MULE:
		case MUSHROOM_COW:
		case PANDA:
		case POLAR_BEAR:
		case TRADER_LLAMA:
		case WITHER:
		case ZOGLIN:
		case ZOMBIE_HORSE:
			return true;
		}
		return false;
	}
	
	double duration = 5.0;
	public void run()
	{
		Location loc = target.getLocation().add(0, target.getHeight() * 0.5, 0);
		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 2.0f);
		Particle_Drawer.Draw_Line(player.getLocation().add(0, 0.75, 0), loc, particle, 0.1);
		
		if (isBig(target.getType()))
		{

			Vector gap = target.getLocation().clone().subtract(player.getLocation().add(0, 0.75, 0)).toVector();
			Vector dir = gap.clone().normalize();
			player.setVelocity(dir.multiply(1.5));
			if (gap.distance(new Vector()) <= 1.5 || player.isSneaking())
			{
				player.setVelocity(dir);
				return;
			}
		}
		else
		{
			Vector gap = player.getLocation().add(0, 0.75, 0).subtract(target.getLocation()).toVector();
			Vector dir = gap.clone().normalize();
			
			distance -= 0.4;
			target.teleport(target.getLocation().add(dir.multiply(0.4)));
			for (LivingEntity le : targets)
				le.teleport(target);
			if (!target.isValid() || gap.distance(new Vector()) <= 1.5 || player.isSneaking())
				return;
		}

		duration -= 0.05;
		if (duration > 0.0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}