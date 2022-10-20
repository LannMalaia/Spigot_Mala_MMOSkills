package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Terminate_Blast extends RegisteredSkill
{
	public Terminate_Blast()
	{	
		super(new Terminate_Blast_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(30, 8));
		addModifier("cooldown", new LinearValue(30, -1, 10, 30));
		addModifier("stamina", new LinearValue(20, 2.5, 20, 50));
	}
}

class Terminate_Blast_Handler extends MalaSkill implements Listener
{
	public Terminate_Blast_Handler()
	{
		super(	"TERMINATE_BLAST",
				"몸통박치기",
				Material.RED_DYE,
				MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&73초간 전방으로 빠르게 돌진합니다.",
				"&7가장 먼저 닿은 적에게 &8{power}&7의 &f고정 피해&7를 줍니다.",
				"&7자신 또한 절반의 &f고정 피해&7를 입습니다.",
				"&c피해는 장비의 스킬/물리 피해량 보너스를 받지 않습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("power"); // 공격력
		
		double sec = 3.0;
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Terminate_Blast_Skill(data.getPlayer(), sec, damage));
	}

	class Terminate_Blast_Skill implements Runnable
	{
		Player player;
		double sec;
		double damage;
		
		int count = 0;
		double velocity = 0.4;
		double angle = 0.0;
		
		
		public Terminate_Blast_Skill(Player _player, double _sec, double _damage)
		{
			player = _player;
			sec = _sec;
			damage = _damage;
			
			angle = player.getLocation().getYaw();

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
		}
		
		public Location Calculate_Y_Pos(Location _loc)
		{
			Location loc = _loc.clone().add(0.0, 0.5, 0.0);
			Block b = loc.clone().add(loc.getDirection().multiply(0.1)).getBlock();
			if (b.getType().isSolid()) // 바로 앞에 블럭 있음
			{
				b = b.getLocation().add(0.0, 1.0, 0.0).getBlock();
				if (!b.getType().isSolid())
					loc.add(0.0, 0.5, 0.0);
			}
			else // 비어있는 경우
			{
				b = b.getLocation().add(0.0, -1.0, 0.0).getBlock();
				if (b.getType().isSolid()) // 바로 앞 바닥에 블럭 있음
				{
					return _loc;
				}
				else // 바로 앞에 블럭 없음
				{
					b = b.getLocation().add(0.0, -1.0, 0.0).getBlock();
					if (!b.getType().isSolid())
						return null;
					loc.add(0.0, -1.5, 0.0);
				}
			}
			
			return loc;
		}
		
		void Slerp()
		{
			double new_angle = player.getLocation().getYaw();
			
			double gap = new_angle - angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			
			gap = Math.min(2.0, Math.max(-2.0, gap));
			angle += gap;
			
			// Bukkit.broadcastMessage("gap = " + gap);
		}
		
		public void run()
		{
			sec -= 0.05;
			count += 1;
			Slerp();
			if (player.isSneaking())
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
				player.sendMessage("§c§l[ 몸통박치기 취소 ]");
				return;
			}
			if (sec <= 0.0)
				return;
			
			if (count % 2 == 0)
			{
				// player.getWorld().playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.5f, 0.5f);			
			}
			
			velocity = Math.min(1.4, velocity + 0.05);
			
			
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)), 0.0, Math.sin(Math.toRadians(angle + 90)));
			Vector vc = dir.multiply(velocity);
			vc.setY(player.getVelocity().getY());
			// player.teleport(new_loc);
			player.setVelocity(vc);

			player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
			
			for (Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 4.0, 4.0, 4.0))
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;
				if (!Hitbox.Is_BoundingBox_Collide_Other_BoundingBox(en.getBoundingBox(), player.getBoundingBox()))
					continue;
				
				
				LivingEntity le = (LivingEntity)en;
				EntityDamageEvent ede = new EntityDamageByEntityEvent(player, le, DamageCause.ENTITY_ATTACK, 1);
				Bukkit.getPluginManager().callEvent(ede);
				if (!ede.isCancelled())
				{
					le.setHealth(Math.max(0.0, le.getHealth() - damage));
					Location loc = en.getLocation().add(0.0, en.getHeight() * 0.5, 0.0);
					player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 40, 0.4, 0.4, 0.4, 0.0);
					player.getWorld().playSound(en.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
					player.getWorld().playSound(en.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.5f);
					Vector recoil = vc.clone().add(new Vector(0.0, 0.6, 0.0));
					le.setVelocity(recoil);
				}
				ede = new EntityDamageEvent(player, DamageCause.SUICIDE, 1);
				Bukkit.getPluginManager().callEvent(ede);
				if (!ede.isCancelled())
				{
					player.setHealth(Math.max(0.0, player.getHealth() - damage * 0.5));
					Vector recoil = vc.clone().multiply(-1).add(new Vector(0.0, 0.6, 0.0));
					player.setVelocity(recoil);
				}
				
				return;
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

}