package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.Buff_Remover;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Daybreak_Sunshine extends RegisteredSkill
{
	public static Daybreak_Sunshine skill;
	public Daybreak_Sunshine()
	{	
		super(new Daybreak_Sunshine_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(5.5, 0.5));
		addModifier("cooldown", new LinearValue(180, 0));
		addModifier("mana", new LinearValue(80, 0));
		
		skill = this;
	}
}

class Daybreak_Sunshine_Handler extends MalaSkill implements Listener
{
	public Daybreak_Sunshine_Handler()
	{
		super(	"DAYBREAK_SUNSHINE",
				"여명의 빛",
				Material.END_CRYSTAL,
				MsgTBL.NeedSkills,
				"&e 회복의 진 - lv.15",
				"&e 에리어 가드 - lv.15",
				"&e 에리어 힐 - lv.10",
				"",
				"&7주변 10m내 아군들에게 &e{second}&7초간 유지되는 축복을 내립니다.",
				"&7유지되는 동안에는 모든 공격에 면역이 되며,",
				"&7디버프 또한 전부 해제됩니다.",
				"",
				MsgTBL.Cooldown_Fixed, MsgTBL.ManaCost);
	}
	
	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "AURA_RECOVER", 15)
			|| !Skill_Util.Has_Skill(data, "AREA_GUARD", 15)
			|| !Skill_Util.Has_Skill(data, "HEAL_WAVE", 10))
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
		
		double duration = cast.getModifier("second");
		double radius = 10.0;

		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Sunshine_Effect_Task(data.getPlayer(), radius));		
		for (Entity e : data.getPlayer().getNearbyEntities(radius, radius, radius))
		{
			if (e instanceof Player)
			{
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
						new Sunshine_Task((Player)e, duration));			
			}
		}
		
		CooldownFixer.Fix_Cooldown(data, Daybreak_Sunshine.skill);
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Sunshine_Task(data.getPlayer(), duration));
	}
	
	class Sunshine_Effect_Task implements Runnable
	{
		Player player;
		double radius;
		double height = 0.0;
		double max_height = 5.0;
		
		double timer = 3.0;
		
		Location loc;
		
		public Sunshine_Effect_Task(Player _player, double _radius)
		{
			player = _player;
			radius = _radius;
			loc = player.getLocation();
		}

		double cur_angle = 0.0;
		public void run()
		{
			timer -= 0.05;
			cur_angle += 2.5;
			
			height = height + ((max_height - height) * 0.1);
			Location temp_loc = loc.clone().add(0, height, 0);
			if (timer > 1.5)
				Particle_Drawer.Draw_Circle(temp_loc, Particle.END_ROD, radius);

			for (double angle = 0.0; angle <= 360.0; angle += 90.0)
			{
				double x = Math.cos(Math.toRadians(angle + cur_angle)) * radius;
				double z = Math.sin(Math.toRadians(angle + cur_angle)) * radius;
				double x2 = Math.cos(Math.toRadians(angle + cur_angle + 90)) * radius;
				double z2 = Math.sin(Math.toRadians(angle + cur_angle + 90)) * radius;
				Location start = loc.clone().add(x, height, z);
				Location end = loc.clone().add(x2, height, z2);
				Particle_Drawer.Draw_Line(start, end, Particle.CRIT_MAGIC, 0.3);
				x = Math.cos(Math.toRadians(angle - cur_angle)) * radius;
				z = Math.sin(Math.toRadians(angle - cur_angle)) * radius;
				x2 = Math.cos(Math.toRadians(angle - cur_angle + 90)) * radius;
				z2 = Math.sin(Math.toRadians(angle - cur_angle + 90)) * radius;
				start = loc.clone().add(x, height, z);
				end = loc.clone().add(x2, height, z2);
				Particle_Drawer.Draw_Line(start, end, Particle.CRIT_MAGIC, 0.3);
			}
			
			if (timer > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

	class Sunshine_Task implements Runnable, Listener
	{
		Player player;
		double duration = 30.0;
		World world;
		
		int tick = 0;
		
		public Sunshine_Task(Player _player, double _duration)
		{
			player = _player;
			duration = _duration;
			world = player.getWorld();
	
			Location loc = player.getLocation().add(3.0, 20.0, 3.0);
			Particle_Drawer.Draw_Line(loc, player.getEyeLocation(), new DustOptions(Color.YELLOW, 2.0f), 0.1);

			player.sendMessage("§e§l[ 여명의 빛 축복을 받아 일시적으로 무적이 됩니다. ]");
			world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.5f, 2.0f);
			Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		}

		@EventHandler (priority = EventPriority.HIGHEST)
		public void When_Attack(EntityDamageEvent event)
		{
			if (event.isCancelled())
				return;
			
			if (event.getEntity() == player)
			{
				event.setDamage(0);
				event.setCancelled(true);
			}
		}
		@EventHandler (priority = EventPriority.HIGHEST)
		public void When_Attack(EntityDamageByEntityEvent event)
		{
			if (event.isCancelled())
				return;
			
			if (event.getEntity() == player)
			{
				event.setDamage(0);
				event.setCancelled(true);
			}
		}
		
		public void run()
		{
			duration -= 0.05;
			tick += 1;
			
			Location loc = player.getLocation().add(0, player.getHeight() + 0.5, 0);
			
			if (tick % 2 == 0)
				Particle_Drawer.Draw_Circle(loc, Particle.END_ROD, 1.0, 4.0 * tick);
			
			if (tick % 10 == 0)
				world.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 2.0f);
			
			Buff_Remover.Remove_Player_Bad_Buff(player);
			
			if (duration > 0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			else
			{
				player.sendMessage("§e§l[ 축복이 끝났습니다. ]");
				EntityDamageEvent.getHandlerList().unregister(this);
				EntityDamageByEntityEvent.getHandlerList().unregister(this);
			}
		}
	}
}

