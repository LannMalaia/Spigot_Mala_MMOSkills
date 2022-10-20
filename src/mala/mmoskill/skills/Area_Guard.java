package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Area_Guard extends RegisteredSkill
{
	public Area_Guard()
	{	
		super(new Area_Guard_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("per", new LinearValue(125, -5, 30, 200));
		addModifier("second", new LinearValue(10, 1));
		addModifier("cooldown", new LinearValue(60, -1.5));
		addModifier("stamina", new LinearValue(25, 0));
	}
}

class Area_Guard_Handler extends MalaSkill implements Listener
{
	public Area_Guard_Handler()
	{
		super(	"AREA_GUARD",
				"에리어 가드",
				Material.CHAINMAIL_CHESTPLATE,
				MsgTBL.NeedSkills,
				"&e 방어태세 - lv.20",
				"",
				"&715m내에 있는 플레이어가 피해를 입을 경우,",
				"&7해당 플레이어가 받는 피해를 자신이 대신 받습니다.",
				"&7이렇게 감쌀 경우, &e{per}&7%의 피해를 받습니다.",
				"&7보호장은 &e{second}&7초간 유지됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		registerModifiers("per", "second");
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "DEFENCEMODE", 20))
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

		double second = cast.getModifier("second");
		double per = cast.getModifier("per") * 0.01;
		
		// 효과
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Area_Guarding(data.getPlayer(), per, second));
	}
	
	class Area_Guarding implements Runnable, Listener
	{
		Player player;
		double per;
		double time;
		
		double radius = 15.0;
		double cur_angle = 0.0;
		World world;
		
		public Area_Guarding(Player _player, double _per, double _time)
		{
			player = _player;
			per = _per;
			time = _time;
			world = player.getWorld();
			world.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 2f, 1.5f);
			Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		}


		@EventHandler (priority = EventPriority.LOWEST)
		public void When_Attack(EntityDamageByEntityEvent event)
		{
			if (event.isCancelled())
				return;
			
			if (event.getEntity() instanceof Player // 피해입은자 -> 플레이어
				&& !(event.getDamager() instanceof Player) // 피해준자 -> 플레이어 아님
				&& event.getEntity() != player) // 피해입은자 -> 스스로 아님
			{
				Player target = (Player)event.getEntity();
				if (player.getWorld() != target.getWorld())
					return;
				if (player.getLocation().distance(target.getLocation()) <= radius)
				{
					player.damage(event.getDamage() * per, event.getDamager());
					event.setCancelled(true);
				}
			}
		}
		
		public void run()
		{
			time -= 0.05;
			cur_angle += 5.0;
			
			for (double angle = 0.0; angle <= 360.0; angle += 90.0)
			{
				double x = Math.cos(Math.toRadians(angle + cur_angle)) * radius;
				double z = Math.sin(Math.toRadians(angle + cur_angle)) * radius;
				double x2 = Math.cos(Math.toRadians(angle + cur_angle + 90)) * radius;
				double z2 = Math.sin(Math.toRadians(angle + cur_angle + 90)) * radius;
				Location start = player.getLocation().add(x, 0.1, z);
				Location end = player.getLocation().add(x2, 0.1, z2);
				Particle_Drawer.Draw_Line(start, end, Particle.ELECTRIC_SPARK, 0.3);
				x = Math.cos(Math.toRadians(angle - cur_angle)) * radius;
				z = Math.sin(Math.toRadians(angle - cur_angle)) * radius;
				x2 = Math.cos(Math.toRadians(angle - cur_angle + 90)) * radius;
				z2 = Math.sin(Math.toRadians(angle - cur_angle + 90)) * radius;
				start = player.getLocation().add(x, 0.1, z);
				end = player.getLocation().add(x2, 0.1, z2);
				Particle_Drawer.Draw_Line(start, end, Particle.ELECTRIC_SPARK, 0.3);
			}
			
			if (time > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			else
				EntityDamageByEntityEvent.getHandlerList().unregister(this);
		}
	}
}
