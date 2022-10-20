package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Stomp extends RegisteredSkill
{
	public Stomp()
	{	
		super(new Stomp_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(20, 2));
		addModifier("distance", new LinearValue(3, 0.3));
		addModifier("cooldown", new LinearValue(25, -0.2));
		addModifier("stamina", new LinearValue(12, 0.5, 20, 35));
	}
}

class Stomp_Handler extends MalaSkill implements Listener
{
	public Stomp_Handler()
	{
		super(	"STOMP",
				"발 구르기",
				Material.RABBIT_FOOT,
				MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7바닥을 내리쳐 주변 &8{distance}&7m구간에 지진을 일으킵니다.",
				"&7주변에 있는 적들은 &8{damage}&7의 피해를 받고 위로 떠오릅니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double damage = cast.getModifier("damage"); // 공격력
		double distance = cast.getModifier("distance"); // 거리

		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Stomp_Skill(data.getPlayer(), distance, damage));
	}
	
	class Stomp_Skill implements Runnable
	{
		ArrayList<LivingEntity> damaged_entities = new ArrayList<LivingEntity>();
		
		Player player;
		double damage;
		double range = 20.0;

		Location loc;
		double cur_range = 0.0;
		double range_speed = 0.3;
		
		public Stomp_Skill(Player _player, double _range, double _dmg)
		{
			player = _player;
			range = _range;
			damage = _dmg;
			range_speed = range * 0.1;
			loc = player.getLocation();
			loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 1.5f);
		}
		
		
		public void run()
		{
			cur_range += range_speed;
			
			Particle_Drawer.Draw_Circle(loc.clone().add(0.0, 0.1, 0.0), Particle.CLOUD, cur_range);
			
			for (Entity e : loc.getWorld().getNearbyEntities(loc, cur_range, cur_range, cur_range))
			{
				// 엔티티 찾기
				if (!(e instanceof LivingEntity))
					continue;
				if (e == player)
					continue;
				LivingEntity le = (LivingEntity)e;
				if (!le.isOnGround() || damaged_entities.contains(le))
					continue;
				
				damaged_entities.add(le);
				le.getWorld().playSound(e.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.5f);
				
				le.setNoDamageTicks(0);
				double hp = le.getHealth();

				Damage.Attack(player, le, damage, DamageType.SKILL, DamageType.PHYSICAL);
				
				if (hp != le.getHealth())
				{
					Vector vel = le.getVelocity();
					vel.setY(1.2);
					le.setVelocity(vel);
					return;
				}
			}
			
			if (cur_range < range)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
