package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Canon extends RegisteredSkill
{
	public static Canon skill;
	
	public Canon()
	{	
		super(new Canon_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("radius", new LinearValue(1.65, 0.15));
		addModifier("damage", new LinearValue(33.5, 3.5));
		addModifier("count", new LinearValue(0.15, 0.15));
		addModifier("cooldown", new LinearValue(30.0, 0));
		addModifier("stamina", new LinearValue(25.5, .5));
		skill = this;
	}

	public static void Attack(SkillMetadata cast, Player player, Location loc, double radius, double damage, boolean finalAttack)
	{
		double rad = radius * (finalAttack ? 1.5 : 1.0);
		double dmg = damage * (finalAttack ? 1.5 : 1.0);
		
		player.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
		if (finalAttack)
		{
			player.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 1.5f);
			player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.5f);
		}
		for (int i = 0; i < (finalAttack ? 5 : 3); i++)
		{
			Particle_Drawer.Draw_Circle(loc.clone().add(0, 0.75, 0), finalAttack ? Particle.SCRAPE : Particle.CRIT,
					rad, -40 + Math.random() * 80.0, Math.random() * 360.0);
		}
		if (finalAttack)
		{
			loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 64, radius, radius, radius, 0);
		}
		
		for(Entity en : loc.getWorld().getNearbyEntities(loc, rad, rad, rad))
		{
			if (!(en instanceof LivingEntity))
				continue;
			if (en == player)
				continue;

			LivingEntity le = (LivingEntity)en;
			if (Damage.Is_Possible(player, le))
			{
				Damage.SkillAttack(cast, le, dmg, DamageType.WEAPON, DamageType.PHYSICAL, DamageType.SKILL);
				le.setNoDamageTicks(0);
			}
		}
	}
	
}

class Canon_Handler extends MalaSkill implements Listener
{
	public HashMap<Player, LivingEntity> final_attacked;
	
	public Canon_Handler()
	{
		super(	"CANON",
				"카논",
				Material.LEAD,
				MsgTBL.NeedSkills,
				"&e 난도질 - lv.20",
				"&e 후방 습격 - lv.20",
				"",
				MsgTBL.WEAPON + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&7마지막으로 공격한 적에게 빠르게 접근합니다.",
				"&7접근중에는 주변 &e{radius}&7m에 &e{damage}&7의 피해를 줍니다.",
				"&7접근이 끝나면 시작 지점에서 잔상이 나타나며,",
				"&7잔상은 접근한 경로를 똑같이 따라오며 같은 피해를 줍니다.",
				"&7대상이 죽거나 3초동안 다가가지 못하면 접근이 중지됩니다.",
				"&7잔상은 접근하는 데 걸린 시간마다 하나씩, 총 &e{count}&7개 생성됩니다.",
				"",
				"&eLv.20 - 마지막 공격은 피해량, 범위가 50% 증가합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		final_attacked = new HashMap<Player, LivingEntity>();
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	
	@EventHandler
	public void canon_attack(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		// 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill("CANON"))
			return;
		final_attacked.put(data.getPlayer(), event.getEntity());
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if (!Skill_Util.Has_Skill(data, "HACKSLASH", 20)
			|| !Skill_Util.Has_Skill(data, "BACK_ATTACK", 20))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		if (!final_attacked.containsKey(player))
		{
			data.getPlayer().sendMessage("§c마지막으로 공격한 적이 없거나 죽었습니다.");
			return new SimpleSkillResult(false);
		}
		LivingEntity le = final_attacked.get(player);
		if (!le.isValid() || le.getWorld() != player.getWorld())
		{
			data.getPlayer().sendMessage("§c마지막으로 공격한 적이 없거나 죽었습니다.");
			return new SimpleSkillResult(false);
		}
		
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double rad = cast.getModifier("radius");
		double damage = cast.getModifier("damage");
		int count = (int)cast.getModifier("count");
		int level = data.getSkillLevel(Canon.skill);
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Canon_Skill(cast, data.getPlayer(), final_attacked.get(data.getPlayer()), rad, damage, count));
	}

	class Canon_Skill implements Runnable
	{
		SkillMetadata cast;
		Player player;
		PlayerData data;
		LivingEntity target;
		double radius, damage;
		int count;
		
		double sec = 3.0;
		double velocity = 0.8;
		
		List<Location> recordedLoc = new ArrayList<>();
		
		public Canon_Skill(SkillMetadata cast, Player _player, LivingEntity _target, double _radius, double _damage, int _count)
		{
			this.cast = cast;
			player = _player; target = _target; radius = _radius;
			damage = _damage; count = _count;
			
			data = PlayerData.get(player);

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 2.0f);
			
		}
		
		public void run()
		{
			sec -= 0.05;
			
			recordedLoc.add(player.getLocation());
			
			Vector dir = target.getLocation().subtract(player.getLocation()).toVector().normalize();
			Vector vc = dir.multiply(velocity);
			player.setVelocity(vc);
			player.getWorld().spawnParticle(Particle.SCRAPE, player.getLocation().add(0, 0.2, 0), 10, 0.4, 0.2, 0.4, 0.0);

			
			if (player.getWorld() != target.getWorld() || sec <= 0.0
				|| target.getLocation().distance(player.getLocation()) < 1.0)
			{
				player.setVelocity(new Vector());
				Canon.Attack(cast, player, player.getLocation(), radius, damage, data.getSkillLevel(Canon.skill) >= 20);
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
						new Canon_Recorded_Skill(cast, player, player.getLocation(), new ArrayList<Location>(recordedLoc), radius, damage, count - 1));
				return;
			}
			else
				Canon.Attack(cast, player, player.getLocation(), radius, damage, false);
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

	class Canon_Recorded_Skill implements Runnable
	{
		SkillMetadata cast;
		Player player;
		PlayerData data;
		double radius, damage;
		double sec = 3.0;
		double velocity = 1.3;
		int count = 0;
		
		Location loc, startLoc;
		List<Location> recordedLoc = new ArrayList<>();
		
		public Canon_Recorded_Skill(SkillMetadata cast, Player _player, Location _startLoc, List<Location> _recordedLoc, double _radius, double _damage, int _count)
		{
			this.cast = cast;
			player = _player; radius = _radius; damage = _damage;
			startLoc = loc = _startLoc; recordedLoc = _recordedLoc; count = _count;
			
			data = PlayerData.get(player);
			
			loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 2.0f);
		}
		
		int index = 0;
		public void run()
		{
			loc = recordedLoc.get(index++).clone();
			// recordedLoc.add(player.getLocation());
			
			loc.getWorld().spawnParticle(Particle.SCRAPE, loc.add(0, 0.2, 0), 10, 0.4, 0.2, 0.4, 0.0);
			
			if (index < recordedLoc.size())
			{
				Canon.Attack(cast, player, loc, radius, damage, false);
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			}
			else
				Canon.Attack(cast, player, loc, radius, damage, data.getSkillLevel(Canon.skill) >= 20);
			
			if (index == recordedLoc.size() && count > 0)
			{
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
						new Canon_Recorded_Skill(cast, player, startLoc, new ArrayList<Location>(recordedLoc), radius, damage, count - 1));
			}
		}
	}
}