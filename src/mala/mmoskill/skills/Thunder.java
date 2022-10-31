package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.RayTraceResult;

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Lightning;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Thunder extends RegisteredSkill
{
	public Thunder()
	{	
		super(new Thunder_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(70, 17));
		addModifier("cooldown", new LinearValue(25, 0));
		addModifier("mana", new LinearValue(48, 8));
	}
}

class Thunder_Handler extends MalaLocationSkill implements Listener
{
	public Thunder_Handler()
	{
		super(	"THUNDER",
				"썬더",
				Material.YELLOW_DYE,
				MsgTBL.SKILL + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC,
				"",
				"&7바라보고 있는 장소에 번개를 내리칩니다.",
				"&7번개에 휘말린 대상들은 &8{damage}&7 만큼의 피해를 받습니다.",
				"&7번개가 내리치기까지는 약간의 시간이 필요합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		range = 20.0;
	}
	
	@Override
	public void whenCast(LocationSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
//		RayTraceResult hit = data.getPlayer().rayTraceBlocks(20.0, FluidCollisionMode.NEVER);
		Location target_loc = _data.getTarget().add(0.5, 0.2, 0.5);

//		if (hit == null)
//		{
//			data.getPlayer().sendMessage("§c너무 먼 거리를 지정했습니다.");
//			return;
//		}
//		else if (hit.getHitBlock() != null)
//		{
//			target_loc = hit.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
//			target_loc.add(hit.getHitBlockFace().getModX(), hit.getHitBlockFace().getModY(), hit.getHitBlockFace().getModZ());
//			if (hit.getHitBlockFace() == BlockFace.DOWN)
//				target_loc.add(0.0, -1.0, 0.0);
//		}
//		
//		if (target_loc == null)
//		{
//			data.getPlayer().sendMessage("§c너무 먼 거리를 지정했습니다.");
//			return;
//		}
				
		double damage = cast.getModifier("damage");
		damage *= Mastery_Lightning.Get_Mult(data.getPlayer());

		DamageMetadata ar = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new LightningMagicEvent(data.getPlayer(), ar));
		damage = ar.getDamage();
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Thunder_Task(data.getPlayer(), target_loc, damage));
	}

}

class Thunder_Task implements Runnable
{
	Player player;
	double damage;
	Location loc;
	
	double radius = 6;
	double timer = 4.0;
	long interval = 20;
	World world;
	
	public Thunder_Task(Player _player, Location _loc, double _damage)
	{
		player = _player;
		loc = _loc;
		damage = _damage;
		world = loc.getWorld();
	}
	
	public void run()
	{
		timer -= interval / 20.0;
		// interval = Math.max(1, interval / 2);
		
		if(timer > 0.0) // 타이머가 남았으면 대기
		{
			Location temp_loc = loc.clone();
			for(double angle = 0; angle < 360.0; angle += 360.0 / 64.0)
			{
				temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
				temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
				world.spawnParticle(Particle.CRIT, temp_loc, 1, 0, 0, 0, 0);
			}
			world.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 1.5f);
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, interval);
		}
		else // 다 됐으면 폭발
		{
			Location temp_loc = loc.clone();
			for(double angle = 0; angle < 360.0; angle += 360.0 / 64.0)
			{
				temp_loc.setX(loc.getX() + Math.cos(Math.toRadians(angle)) * radius);
				temp_loc.setZ(loc.getZ() + Math.sin(Math.toRadians(angle)) * radius);
				world.spawnParticle(Particle.LAVA, temp_loc, 1, 0, 0, 0, 0);
			}
			double temp_rad = radius * 20.0;
			for(int i = 0; i < 4; i++)
			{
				temp_loc.setX(loc.getX() + (-temp_rad + Math.random() * temp_rad * 2.0));
				temp_loc.setY(256);
				temp_loc.setZ(loc.getZ() + (-temp_rad + Math.random() * temp_rad * 2.0));
				Lightning_Bolt.Draw_Lightning_Line(temp_loc, loc, Particle.END_ROD);
			}

			for(Entity e : world.getNearbyEntities(loc, radius, radius, radius))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e == player)
					continue;
				
				LivingEntity target = (LivingEntity)e;
				Damage.Attack(player, target,damage, DamageType.MAGIC, DamageType.SKILL);
			}

			world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
			world.playSound(loc, Sound.ITEM_TOTEM_USE, 2f, 1.1f);
			world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 1.1f);
			
			// world.spawnParticle(Particle.EXPLOSION_HUGE, loc, 50, 3, 3, 3, 0);
			world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 30, 3, 3, 3, 0);
			world.spawnParticle(Particle.LAVA, loc, 60, 0.3, 0.3, 0.3, 0);
		}
	}
}










