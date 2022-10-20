package mala.mmoskill.skills;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Animals;
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

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Arrow_Bomb_Rain extends RegisteredSkill
{
	public static Arrow_Bomb_Rain skill;
	public Arrow_Bomb_Rain()
	{	
		super(new Arrow_Bomb_Rain_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("sec", new LinearValue(3.1, 0.1));
		addModifier("count", new LinearValue(40, 4));
		addModifier("damage", new LinearValue(30, 3));
		addModifier("cooldown", new LinearValue(40, 0));
		addModifier("stamina", new LinearValue(50, 0));
		
		skill = this;
	}
}

class Arrow_Bomb_Rain_Handler extends MalaLocationSkill implements Listener
{
	public static HashMap<Player, Location> last_location;
	
	public Arrow_Bomb_Rain_Handler()
	{
		super(	"ARROW_BOMB_RAIN",
				"폭렬화살비",
				Material.TNT,
				MsgTBL.NeedSkills,
				"&e 화살비 - lv.10",
				"&e 폭발 화살 - lv.15",
				"",
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7폭렬화살비가 내릴 구역을 선택합니다.",
				"&7이후 하늘을 향해 화살을 쏘면 폭렬화살비가 시작됩니다.",
				"&7화살비가 내리는 구역에 있는 적들은",
				"&70.3초마다 &e{damage}&7의 피해를 받습니다.",
				"&7폭렬화살비는 &e{sec}&7초간 지속됩니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_BOW + "피해량 30% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		registerModifiers("count", "damage");

		last_location = new HashMap<Player, Location>();
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler
	public void passive_arrow_rain_shoot(EntityShootBowEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;
		if(!(event.getProjectile() instanceof Arrow))
			return;
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get((Player)event.getEntity());
		
		// 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill("ARROW_BOMB_RAIN"))
			return;
		
		if(!data.getPlayer().hasMetadata("malammo.skill.arrow_bomb_rain.ready"))
			return;

		data.getPlayer().removeMetadata("malammo.skill.arrow_bomb_rain.ready", MalaMMO_Skill.plugin);
		double duration = Arrow_Bomb_Rain.skill.getModifier("sec", data.getSkillLevel(Arrow_Rain.skill));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Arrow_Bomb_Rain_Task(data.getPlayer(), (Arrow)event.getProjectile(), duration));
	}
	
	@Override
	public LocationSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "ARROW_RAIN", 10)
			|| !Skill_Util.Has_Skill(data, "EXPLODE_ARROW", 15))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new LocationSkillResult(cast, 0.0);
		}
		return new LocationSkillResult(cast, range);
	}

	@Override
	public void whenCast(LocationSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Location loc = _data.getTarget().add(0, 1.1, 0);
		last_location.put(data.getPlayer(), loc);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Arrow_Bomb_Rain_Ready_Task(data.getPlayer(), 10));
	}
}

class Arrow_Bomb_Rain_Ready_Task implements Runnable
{
	Player player;
	Location loc;
	int time;
	
	public Arrow_Bomb_Rain_Ready_Task(Player _player, int _time)
	{
		player = _player;
		loc = Arrow_Bomb_Rain_Handler.last_location.get(player);
		time = _time;

		player.sendMessage("§b[ 폭렬 화살비 준비 ]");
		player.setMetadata("malammo.skill.arrow_bomb_rain.ready", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
	}
	
	public void run()
	{
		// 시간 다 되거나 메타값이 없거나 멀리 떨어진 경우
		if(time-- <= 0)
		{
			player.removeMetadata("malammo.skill.arrow_bomb_rain.ready", MalaMMO_Skill.plugin);
			player.sendMessage("§c[ 폭렬 화살비 해제 ]");
			return;
		}
		if(!player.hasMetadata("malammo.skill.arrow_bomb_rain.ready"))
			return;
		if(loc.getWorld() != player.getWorld())
		{
			player.removeMetadata("malammo.skill.arrow_bomb_rain.ready", MalaMMO_Skill.plugin);
			player.sendMessage("§c[ 폭렬 화살비 해제 ]");
			return;
		}
		if(loc.distance(player.getLocation()) > 50)
		{
			player.removeMetadata("malammo.skill.arrow_bomb_rain.ready", MalaMMO_Skill.plugin);
			player.sendMessage("§c[ 폭렬 화살비 해제 (너무 멉니다.) ]");
			return;
		}
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
	}
}

class Arrow_Bomb_Rain_Task implements Runnable
{
	enum PHASE { FIRED, WAIT, RAINING, WAIT_2 }
	
	PHASE phase = PHASE.FIRED;
	Player player;
	Arrow arrow;
	Location start_loc, end_loc;
	double radius = 10.0;
	
	double highest_y = 0.0;
	
	int max_arrows = 0;
	int shooted_arrows = 0;
	int shooting_count = 1;
	double damage = 0;
	double duration = 4.0;
	
	int waiting_time = 30;
	
	World world;
	
	public Arrow_Bomb_Rain_Task(Player _player, Arrow _arrow, double _duration)
	{
		player = _player;
		arrow = _arrow;
		duration = _duration;
		end_loc = Arrow_Bomb_Rain_Handler.last_location.get(player);
		world = end_loc.getWorld();
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("ARROW_BOMB_RAIN");
		int level = data.getSkillLevel(skill);

		max_arrows = (int)skill.getModifier("count", level);
		damage = skill.getModifier("damage", level);
		shooting_count = Math.max(1, Math.min(3, max_arrows / 5));
	}
	
	public void run()
	{
		boolean end_flag = false;
		
		switch(phase)
		{
		case FIRED: // 화살 올리는 상태
			end_flag = !Fired();
			break;
		case WAIT: // 기다리는 상태
			end_flag = !Wait();
			break;
		case RAINING: // 비내리는 상태
			end_flag = !Rain();
			break;
		case WAIT_2:
			end_flag = !Wait_2();
			break;
		}
		if(end_flag)
			return;
		
		Draw_Square();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	
	boolean Fired()
	{
		if(highest_y < arrow.getLocation().getY()) // 화살이 상승중
			highest_y = arrow.getLocation().getY();
		else // 상승을 멈춘 경우
		{
			if(highest_y < end_loc.getY() + 10) // 화살이 충분히 상승하지 않은 경우
			{
				player.sendMessage("§c[ 폭렬 화살비 해제 (너무 낮게 쐈습니다.) ]");
				return false;
			}
			
			start_loc = end_loc.clone().add(0, 20, 0);
			
			arrow.remove();
			phase = PHASE.WAIT;
		}
		return true;
	}
	
	boolean Wait()
	{
		phase = PHASE.RAINING;
		player.sendMessage("§b[ 폭렬화살비 발사 ]");

		if (arrow.hasMetadata("malammo.skill.bow"))
			damage *= 1.3;

		return true;
	}
	
	int count = 0;
	boolean Rain()
	{
		count++;
		duration -= 0.05;
		world.playSound(start_loc, Sound.ENTITY_ARROW_SHOOT, 2.0f, 1.5f);
		Vector _dir = new Vector(0, -1.0, 0);
		for(int i = 0; i < shooting_count; i++)
		{
			Location loc = start_loc.clone().add(-radius + Math.random() * radius * 2.0, 0, -radius + Math.random() * radius * 2.0);
			Location _end_loc = loc.clone().add(0, end_loc.getY() - loc.getY(), 0);
			Particle_Drawer.Draw_Line(loc, _end_loc, Particle.FLAME, 0.2);
			// world.playSound(_end_loc, Sound.ENTITY_ARROW_HIT, 2.0f, 1.5f);
			world.spawnParticle(Particle.EXPLOSION_LARGE, _end_loc, 10, 3.0, 3.0, 3.0);
			world.spawnParticle(Particle.LAVA, _end_loc, 100, 3.0, 3.0, 3.0);
			world.playSound(_end_loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);

			/*
			Location loc = start_loc.clone().add(-half_rad + Math.random() * radius, 0, -half_rad + Math.random() * radius);
			RayTraceResult rtr = world.rayTrace(loc, _dir, 100.0, FluidCollisionMode.NEVER, true, 0.5, null);
			if (rtr == null || rtr.getHitPosition() == null)
				continue;
			Location _end_loc = rtr.getHitPosition().toLocation(world);
			Particle_Drawer.Draw_Line(loc, _end_loc, Particle.FLAME, 0.2);
			world.playSound(_end_loc, Sound.ENTITY_ARROW_HIT, 2.0f, 1.5f);
			
			double temp_radius = 3;
			for (double angle = 0; angle <= 360.0; angle += 20)
			{
				Location temp_loc = _end_loc.clone().add(Math.cos(Math.toRadians(angle)) * temp_radius, 0, Math.sin(Math.toRadians(angle)) * temp_radius);
				world.spawnParticle(Particle.EXPLOSION_LARGE, temp_loc, 1, 0, 0, 0);
				world.spawnParticle(Particle.LAVA, temp_loc, 1, 0, 0, 0);
			}
			world.playSound(_end_loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
			
			for(Entity e : world.getNearbyEntities(_end_loc, temp_radius, temp_radius, temp_radius))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if((e instanceof Animals))
					continue;
				if (e == player)
					continue;
				LivingEntity c = (LivingEntity)e;
				Damage.Attack(player, c, damage,
						DamageType.PROJECTILE, DamageType.SKILL, DamageType.PHYSICAL);
			}
			*/
		}
		if (count % 6 == 0)
		{
			for (Entity e : end_loc.getWorld().getNearbyEntities(end_loc, radius, radius, radius))
			{
				if (Damage.Is_Possible(player, e) && e instanceof LivingEntity)
				{
					LivingEntity le = (LivingEntity)e;
					Damage.Attack(player, le, damage,
							DamageType.PROJECTILE, DamageType.SKILL);
					le.setNoDamageTicks(5);
				}
			}
		}
		
		shooted_arrows += shooting_count;
		if(shooted_arrows >= max_arrows)
		{
			phase = PHASE.WAIT_2;
		}
		
		return true;
	}

	boolean Wait_2()
	{
		waiting_time -= 1;
		if(waiting_time <= 0)
			return false;
		return true;
	}
	
	void Draw_Square()
	{
		/*
		Vector[] vecs = new Vector[5];
		vecs[0] = end_loc.toVector().add(new Vector(-half_rad, 0, -half_rad));
		vecs[1] = end_loc.toVector().add(new Vector(half_rad, 0, -half_rad));
		vecs[2] = end_loc.toVector().add(new Vector(half_rad, 0, half_rad));
		vecs[3] = end_loc.toVector().add(new Vector(-half_rad, 0, half_rad));
		vecs[4] = end_loc.toVector().add(new Vector(-half_rad, 0, -half_rad));
		
		for(int i = 0; i < vecs.length - 1; i++)
		{
			Vector lerp = vecs[i + 1].clone().subtract(vecs[i]);
			double length = lerp.length();
			lerp.normalize();
			
			Location loc = end_loc.clone();
			for(double j = 0; j < length; j += 0.2)
			{
				loc.setX(vecs[i].getX() + lerp.getX() * j);
				loc.setZ(vecs[i].getZ() + lerp.getZ() * j);
				world.spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
			}
		}
		*/
		Particle_Drawer.Draw_Circle(end_loc, Particle.FLAME, radius);
	}
}
