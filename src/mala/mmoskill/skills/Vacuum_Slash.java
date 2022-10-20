package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.RayTraceResult;
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

public class Vacuum_Slash extends RegisteredSkill
{
	public Vacuum_Slash()
	{	
		super(new Vacuum_Slash_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(20, 5));
		addModifier("cooldown", new LinearValue(3, 0));
		addModifier("stamina", new LinearValue(15, 0.85, 20, 35));
	}
}

class Vacuum_Slash_Handler extends MalaSkill implements Listener
{
	public Vacuum_Slash_Handler()
	{
		super(	"VACUUM_SLASH",
				"진공칼날",
				Material.FEATHER,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7바닥을 긁어 날카로운 검기를 발사합니다.",
				"&7검기에 닿은 적들은 &8{damage}&7의 피해를 받고 위로 떠오릅니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double damage = cast.getModifier("damage"); // 공격력
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Vacuum_Slash_Skill(data.getPlayer(), 20.0, damage));
	}
	
	class Vacuum_Slash_Skill implements Runnable
	{
		ArrayList<LivingEntity> damaged_entities = new ArrayList<LivingEntity>();
		
		Player player;
		double damage;
		double range = 20.0;

		double speed = 1.0;

		Location before_loc, cur_loc;
		Vector dir;
		
		public Vacuum_Slash_Skill(Player _player, double _range, double _dmg)
		{
			player = _player;
			range = _range;
			damage = _dmg;
			cur_loc = player.getLocation().add(0, 1.0, 0);
			dir = player.getEyeLocation().getDirection();
			dir.setY(0);
			dir.normalize();
			
			cur_loc.getWorld().playSound(cur_loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.5f);
		}
		
		
		public void run()
		{
			before_loc = cur_loc.clone();
			range -= speed;

			cur_loc.getWorld().playSound(cur_loc, Sound.ITEM_TRIDENT_THROW, 2.0f, 1.2f);
			// Particle_Drawer.Draw_Line(loc, loc.clone().add(0, 10, 0), Particle.BARRIER, 0.1);
			
			double additive = 0.1;
			double max_height = 2.5;
			double height = 2.5;
			cur_loc.add(dir.clone().multiply(speed));
			for (double point = speed; point >= -speed; point -= additive)
			{
				
				// 플로어 찾기
				Location temp_loc = cur_loc.clone().add(dir.clone().multiply((speed - point) * -1.0)).add(0.0, 0.5, 0.0);
				RayTraceResult rtr = cur_loc.getWorld().rayTraceBlocks(temp_loc, new Vector(0.0, -1.0, 0.0), 8.0, FluidCollisionMode.NEVER, true);
				if (rtr == null || rtr.getHitPosition() == null)
					continue;
				
				Location floor_loc = new Location(cur_loc.getWorld(), rtr.getHitPosition().getX(), rtr.getHitPosition().getY(), rtr.getHitPosition().getZ());
				Location top_loc = floor_loc.clone().add(0, height, 0);
				
				Particle_Drawer.Draw_Line(floor_loc, top_loc, Particle.CRIT, 0.1);

				// 엔티티 찾기
				rtr = cur_loc.getWorld().rayTraceEntities(floor_loc, new Vector(0.0, 1.0, 0.0), max_height);
				if (rtr != null && rtr.getHitEntity() != null)
				{
					Entity e = rtr.getHitEntity();
					if (!(e instanceof LivingEntity))
						continue;
					if (e == player)
						continue;
					LivingEntity le = (LivingEntity)e;
					if (!le.isOnGround() || damaged_entities.contains(le))
						continue;
					
					damaged_entities.add(le);
					le.getWorld().playSound(floor_loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.5f);
					
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
				height -= height * 0.05;
			}
			
			if (range > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
