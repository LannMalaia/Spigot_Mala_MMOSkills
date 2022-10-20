package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Dauntless_Knight extends RegisteredSkill
{
	public Dauntless_Knight()
	{	
		super(new Dauntless_Knight_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("hp", new LinearValue(30, 30));
		// addModifier("cool", new LinearValue(10, 0));
		addModifier("cool", new LinearValue(1170, -30, 600, 1200));
	}
}

class Dauntless_Knight_Handler extends MalaPassiveSkill implements Listener
{
	public Dauntless_Knight_Handler()
	{
		super(	"DAUNTLESS_KNIGHT",
				"불굴의 기사",
				Material.TOTEM_OF_UNDYING,
				"&7죽을만큼의 피해를 입었을 때,",
				"&75초간 무적이 되며 &e{hp}&7의 생명력을 회복합니다.",
				"&e{cool}&7초의 재사용 대기시간이 있습니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	boolean Dauntless_Effect(Player player)
	{
		PlayerData data = PlayerData.get(player);

		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("DAUNTLESS_KNIGHT");
		
		// 메타값이 있는 경우
		if (data.getPlayer().hasMetadata("malammo.skill.dk"))
			return false; // 부활 불가

		int level = data.getSkillLevel(skill);
		player.setHealth(Math.min(skill.getModifier("hp", level), player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
				new DK_Cooldown(data.getPlayer(), skill.getModifier("cool", level), skill.getModifier("hp", level)), 1);
		// player.spigot().respawn();
		return true;
	}
	
	@EventHandler
	public void attack_dauntless_1(EntityResurrectEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player)event.getEntity();
		if (!PlayerData.has(player))
			return;
		
		PlayerData data = PlayerData.get((Player)event.getEntity());
		if (!Skill_Util.Has_Skill(data, "DAUNTLESS_KNIGHT", 1))
			return;
		
		// 이벤트가 취소되지 않은 경우
		if (!event.isCancelled())
		{
			boolean cancel = Dauntless_Effect(player);
			event.setCancelled(cancel);
		}
	}
	
	@EventHandler
	public void attack_dauntless_2(PlayerDeathEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
	}
	
	class DK_Cooldown implements Runnable
	{
		Player player;
		int ticks = 0;
		double hp;
		boolean first = false;
		
		public DK_Cooldown(Player _player, double _second, double _hp)
		{
			player = _player;
			hp = _hp;
			player.sendMessage("§e§l[ 기사는 쓰러지지 않는다...!! ]");
			player.setMetadata("malammo.skill.dk", new FixedMetadataValue(MalaMMO_Skill.plugin, _second));
			player.getWorld().playSound(player.getEyeLocation(), Sound.ITEM_TOTEM_USE, 1f, 2f);
			player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 1f);
			Location loc = player.getLocation().add(0, 0.5, 0);
			player.setInvulnerable(true);
			Particle_Drawer.Draw_Circle(loc, Particle.CLOUD, 1.5);
			Particle_Drawer.Draw_Circle(loc, Particle.CLOUD, 1.75);
			Particle_Drawer.Draw_Circle(loc, Particle.CLOUD, 2.0);
		}
		
		public void run()
		{
			if (!player.hasMetadata("malammo.skill.dk"))
				return;
			
			if (!first)
			{
				first = true;
				player.setHealth(Math.min(hp, player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
			double sec = player.getMetadata("malammo.skill.dk").get(0).asDouble();

			if (ticks > 100 && player.isInvulnerable())
				player.setInvulnerable(false);
			
			if (sec <= 0.0)
			{
				player.removeMetadata("malammo.skill.dk", MalaMMO_Skill.plugin);
				player.sendMessage("§e§l[ 불굴의 기사 준비 ]");
				return;
			}
			else
				player.setMetadata("malammo.skill.dk", new FixedMetadataValue(MalaMMO_Skill.plugin, sec - 0.1));
			
			ticks += 2;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
		}
	}
}
