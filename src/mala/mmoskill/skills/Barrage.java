package mala.mmoskill.skills;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Barrage extends RegisteredSkill
{
	public Barrage()
	{	
		super(new Barrage_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("angle", new LinearValue(60, 10, 0, 150));
		addModifier("count", new LinearValue(5, 2, 3, 99));
		addModifier("damage", new LinearValue(45, 15));
		addModifier("cooldown", new LinearValue(5.9, -0.1, 4, 20));
		addModifier("stamina", new LinearValue(21, 1));
	}
}

class Barrage_Handler extends MalaSkill implements Listener
{
	public Barrage_Handler()
	{
		super(	"BARRAGE",
				"일제 사격",
				Material.ARROW,
				MsgTBL.PROJECTILE,
				"",
				"&7화살을 &e{angle}&7도 각도로 &e{count}&7발 발사합니다.",
				"&7각 화살은 &e{damage}&7의 피해를 줍니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler
	public void passive_barrage_shoot(EntityShootBowEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;
		if(!(event.getProjectile() instanceof Arrow))
			return;
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get((Player)event.getEntity());
		
		// 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill("BARRAGE"))
			return;
		
		if(!data.getPlayer().hasMetadata("malammo.skill.barrage.ready"))
			return;

		data.getPlayer().removeMetadata("malammo.skill.barrage.ready", MalaMMO_Skill.plugin);
		Shoot_Barrage(data, (Arrow)event.getProjectile());
		event.setCancelled(true);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Barrage_Task(data.getPlayer(), 5));
	}

	public void Shoot_Barrage(PlayerData _data, Arrow _arrow)
	{
		Vector[] origin_dir = new Vector[1];
		origin_dir[0] = _arrow.getVelocity();

		RegisteredSkill rs = MMOCore.plugin.skillManager.getSkill("BARRAGE");
		double max_angle = rs.getModifier("angle", _data.getSkillLevel(rs));
		int count = (int)rs.getModifier("count", _data.getSkillLevel(rs));
		double damage = rs.getModifier("damage", _data.getSkillLevel(rs));
		
		int sec = 0;
		int mini_count = 0;
		for (double angle = -max_angle * 0.5; angle <= max_angle * 0.5; angle += max_angle / (count - 1))
		{
			Vector dir = TRS.Rotate_Y(origin_dir, angle)[0];
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
					new Barrage_Arrow(_data.getPlayer(), damage, dir), sec);
			if (++mini_count > count / 6)
			{
				mini_count = 0;
				sec++;
			}
		}
		Player player = _data.getPlayer();
		player.sendMessage("§b[ 일제 사격 ]");
		
	}
}

class Barrage_Arrow implements Runnable
{
	Player player;
	Vector dir;
	double damage;
	
	public Barrage_Arrow(Player _player, double _damage, Vector _dir)
	{
		player = _player; damage = _damage; dir = _dir;
	}
	
	
	public void run()
	{
		Location loc = player.getEyeLocation().add(dir.clone().multiply(0.5));
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 0.6f, 0.6f);
		Predicate<Entity> filter = (entity) -> entity instanceof LivingEntity;
		RayTraceResult rtr = loc.getWorld().rayTraceEntities(loc, dir, 15.0, filter);
		Location end_point = loc.clone().add(dir.clone().multiply(15.0));
		if (rtr != null)
		{
			end_point = rtr.getHitPosition().toLocation(loc.getWorld());
			if (rtr.getHitEntity() != null && rtr.getHitEntity() != player)
			{
				Damage.Attack(player, (LivingEntity)rtr.getHitEntity(), damage,
						DamageType.PROJECTILE);
			}
		}
		Particle_Drawer.Draw_Line(loc, end_point, Particle.CRIT, 0.5);
	}
}

class Barrage_Task implements Runnable
{
	Player player;
	int time;
	
	public Barrage_Task(Player _player, int _time)
	{
		player = _player;
		time = _time;

		player.sendMessage("§b[ 일제 사격 준비 ]");
		player.setMetadata("malammo.skill.barrage.ready", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
	}
	
	public void run()
	{
		// 시간 다 되거나 메타값이 없거나 멀리 떨어진 경우
		if(time-- <= 0)
		{
			player.removeMetadata("malammo.skill.barrage.ready", MalaMMO_Skill.plugin);
			player.sendMessage("§c[ 일제 사격 해제 ]");
			return;
		}
		if(!player.hasMetadata("malammo.skill.barrage.ready"))
			return;
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
	}
}