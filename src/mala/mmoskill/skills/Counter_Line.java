package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.ArrowSkill_Manager;
import mala.mmoskill.manager.ArrowTip;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.util.AttackUtil;
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

public class Counter_Line extends RegisteredSkill
{
	public static String metaname = "mala.mmoskill.armored_charge";
	public static Counter_Line skill;
	
	public Counter_Line()
	{	
		super(new Counter_Line_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(48, 8));
		addModifier("cooldown", new LinearValue(20, 0));
		addModifier("stamina", new LinearValue(0, 0));
		
		skill = this;
	}
}

class Counter_Line_Handler extends MalaSkill implements Listener
{
	public Counter_Line_Handler()
	{
		super(	"COUNTER_LINE",
				"카운터 라인",
				Material.COMPARATOR,
				MsgTBL.NeedSkills,
				"&e 후방 습격 - lv.15",
				"",
				MsgTBL.PHYSICAL + MsgTBL.WEAPON,
				"",
				"&71초간 반격 자세가 됩니다.",
				"&7이 때 2m 이내의 적에게 공격을 받으면,",
				"&7피해를 무효화한 뒤 해당 적의 뒤로 순간이동하여",
				"&7주변 3m내의 적들에게 &e{damage}&7의 피해를 줍니다.",
				"&7이 공격은 스킬로 취급되지 않습니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "피해량 30% 증가",
				MsgTBL.WEAPON_WHIP + "범위 50% 증가, 적들에게 나약함을 5초간 부여",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "BACK_ATTACK", 15))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return super.getResult(cast);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		Player player = data.getPlayer();
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new CounterLine_Wait(player));
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void CounterLine_Damaged_Event(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		if (!(event.getEntity() instanceof Player))
			return;
		if (!(event.getDamager() instanceof LivingEntity))
			return;
		
		Player player = (Player)event.getEntity();
		if (player.hasMetadata(Counter_Line.metaname))
		{
			event.setCancelled(true);
			
			PlayerData data = PlayerData.get(player);
			int level = data.getSkillLevel(Counter_Line.skill);
			double damage = Counter_Line.skill.getModifier("damage", level);
			double radius = 3.0;
			boolean isWeakness = false;
			player.removeMetadata(Counter_Line.metaname, MalaMMO_Skill.plugin);
			
			if (Weapon_Identify.Hold_MMO_Sword(player))
				damage *= 1.3;
			if (Weapon_Identify.Hold_Crossbow(player))
			{
				radius *= 1.5;
				isWeakness = true;
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
					new CounterLine_Skill(player, (LivingEntity)event.getDamager(), damage, radius, isWeakness),
					10);
		}
	}
}

class CounterLine_Wait implements Runnable
{
	Player player;
	Location loc;
	double duration = 1.0;
	
	public CounterLine_Wait(Player _player)
	{
		player = _player;
		loc = player.getLocation();
		player.setMetadata(Counter_Line.metaname, new FixedMetadataValue(MalaMMO_Skill.plugin, true));
		player.playSound(player, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.5f);
		draw();
	}
	
	public void draw()
	{
		Vector[] vecs = new Vector[6];
		vecs[0] = new Vector(-1, 1, 1.0);
		vecs[1] = new Vector(1, 1, 1.0);
		vecs[2] = new Vector(1.5, 0, 1.0);
		vecs[3] = new Vector(1, -1, 1.0);
		vecs[4] = new Vector(-1, -1, 1.0);
		vecs[5] = new Vector(-1.5, 0, 1.0);
		
		Location drawLoc = player.getLocation().add(0, player.getHeight() * 0.5, 0);
		Vector[] tempvecs = TRS.Rotate_Y(vecs, drawLoc.getYaw());		
		for (int i = 0; i < 5; i++)
		{
			Location start = drawLoc.clone().add(tempvecs[i]);
			Location end = drawLoc.clone().add(tempvecs[i + 1]);
			Particle_Drawer.Draw_Line(start, end, Particle.SCRAPE, 0.1);
		}
		Location a = drawLoc.clone().add(tempvecs[5]);
		Location b = drawLoc.clone().add(tempvecs[0]);
		Particle_Drawer.Draw_Line(a, b, Particle.SCRAPE, 0.1);
		
		tempvecs = TRS.Scale(vecs, 0.8, 0.8, 1.0);	
		tempvecs = TRS.Rotate_Y(tempvecs, drawLoc.getYaw());		
		for (int i = 0; i < 5; i++)
		{
			Location start = drawLoc.clone().add(tempvecs[i]);
			Location end = drawLoc.clone().add(tempvecs[i + 1]);
			Particle_Drawer.Draw_Line(start, end, Particle.SCRAPE, 0.1);
		}
		a = drawLoc.clone().add(tempvecs[5]);
		b = drawLoc.clone().add(tempvecs[0]);
		Particle_Drawer.Draw_Line(a, b, Particle.SCRAPE, 0.1);
	}
	
	@Override
	public void run()
	{
		if (!player.hasMetadata(Counter_Line.metaname))
			return;
		
		duration -= 0.05;
		if (duration > 0 || player.getLocation().distance(loc) < 0.2)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		else
		{
			player.removeMetadata(Counter_Line.metaname, MalaMMO_Skill.plugin);
			player.sendMessage("§c§l[ 카운터 라인 해제 ]");
			player.playSound(player, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);
		}
	}
}

// 광폭 화살 효과
class CounterLine_Skill implements Runnable
{
	Player player;
	LivingEntity target;
	
	double radius;
	double damage;
	boolean isWeakness;
	
	public CounterLine_Skill(Player _p, LivingEntity _target, double _damage, double _radius, boolean _weakness)
	{
		player = _p;
		target = _target;
		damage = _damage;
		radius = _radius;
		isWeakness = _weakness;
		
		player.sendMessage("§a§l[ 카운터 라인 발동 ]");
		Vector gap = player.getLocation().subtract(target.getLocation()).toVector().normalize();
		gap.setY(0.5);
		player.setVelocity(gap.multiply(2.0));
		player.playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 1.5f, 1.5f);
		player.playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1.5f, 2.0f);
		player.spawnParticle(Particle.FLASH, player.getEyeLocation(), 1, 0, 0, 0, 0);
	}
	
	public void run()
	{
		player.spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, target.getHeight() / 2, 0), 24, 0.5, 1, 0.5, .7);
		
		Vector dir = target.getLocation().getDirection().normalize();
		dir.setX(dir.getX() * -1.5d);
		dir.setZ(dir.getZ() * -1.5d);
		dir.setY(0.0d);
		player.teleport(target.getLocation().add(dir),
				TeleportCause.ENDER_PEARL);

		Location loc = player.getEyeLocation();
		player.playSound(target, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
		// player.playSound(target, Sound.ITEM_TOTEM_USE, 1.5f, 1.5f);
		player.playSound(target, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.5f);
		for (int i = 0; i < 3; i++)
		{
			double rad = radius * 0.8 + i * 0.1;
			Particle_Drawer.Draw_Circle(loc, Particle.SWEEP_ATTACK,
					rad, -40 + Math.random() * 80.0, Math.random() * 360.0);
		}
		player.getLocation().setDirection(target.getLocation().getDirection());
		
		for(Entity e : player.getNearbyEntities(radius, radius, radius))
		{
			if (Damage.Is_Possible(player, e) && e instanceof LivingEntity)
			{
				LivingEntity le = (LivingEntity)e;
				AttackUtil.attack(player, le, damage, null, DamageType.PHYSICAL, DamageType.WEAPON);
				if (isWeakness)
					Buff_Manager.Increase_Buff(le, PotionEffectType.WEAKNESS, 0, 100, PotionEffectType.INCREASE_DAMAGE, 0);
				Vector gap = le.getLocation().subtract(player.getLocation()).toVector().normalize();
				gap.setY(0.3);
				le.setVelocity(gap);
			}
		}
	}
}
