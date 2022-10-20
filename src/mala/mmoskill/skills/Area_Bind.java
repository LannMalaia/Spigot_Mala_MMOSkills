package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Area_Bind  extends RegisteredSkill
{
	public Area_Bind()
	{	
		super(new Area_Bind_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("sec", new LinearValue(3.0, 0.3));
		addModifier("cooldown", new LinearValue(13, 0.3));
		addModifier("stamina", new LinearValue(20, 1));
	}
}

class Area_Bind_Handler extends MalaSkill implements Listener
{
	public Area_Bind_Handler()
	{
		super(	"AREA_BIND",
				"올가미 감옥",
				Material.LEAD,
				"&8{sec}&7초간 주변 3m의 적들을 붙잡아둡니다.",
				"&7붙잡힌 적들은 자신의 전방으로 모입니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		registerModifiers("sec");
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double sec = (int)cast.getModifier("sec");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Area_Bind_Skill(data.getPlayer(), sec));
	}

	class Area_Bind_Skill implements Runnable
	{
		Player player;
		double sec;
		
		ArrayList<LivingEntity> enemies = new ArrayList<LivingEntity>();
		
		public Area_Bind_Skill(Player _player, double _sec)
		{
			player = _player;
			sec = _sec;

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1.5f, 1.5f);
		}
		
		double sound_sec = 0.0;
		double radius = 3.0;
		double cur_radius = 0.0;
		public void run()
		{
			sec -= 0.05;
			sound_sec += 0.05;
			cur_radius += (radius - cur_radius) * 0.1;
			
			if (sound_sec >= 1.0)
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.5f, 1.5f);
				sound_sec = 0.0;
				cur_radius = 0.0;
			}
			
			Particle_Drawer.Draw_Circle(player.getLocation().add(0, 0.4, 0), Particle.CRIT_MAGIC, radius - cur_radius);
			
			for (Entity en : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius))
			{
				if (!(en instanceof Monster) || (en instanceof Player))
					continue;
				if (en == player)
					continue;
				if (enemies.contains(en))
					continue;
				
				Location loc = en.getLocation().add(0.0, en.getHeight() * 0.5, 0.0);
				player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.2, 0.2, 0.2, 0.0);
				player.getWorld().playSound(en.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.5f);
				EntityDamageEvent ede = new EntityDamageByEntityEvent(player, en, DamageCause.ENTITY_ATTACK, 0);
				Bukkit.getPluginManager().callEvent(ede);
				if (!ede.isCancelled())
					enemies.add((LivingEntity)en);
			}
			
			Location tp_loc = player.getLocation().add(player.getLocation().getDirection().multiply(radius));
			for (LivingEntity le : enemies)
			{
				le.teleport(tp_loc, TeleportCause.PLUGIN);
			}
			
			if (sec > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
