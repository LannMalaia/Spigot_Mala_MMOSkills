package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Darkness_Strike extends RegisteredSkill
{
	public Darkness_Strike()
	{	
		super(new Darkness_Strike_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("range", new LinearValue(10.5, 0.5));
		addModifier("damage", new LinearValue(40, 5));
		addModifier("cooldown", new LinearValue(34.25, -0.75));
		addModifier("stamina", new LinearValue(20, 0.5));
	}
}

class Darkness_Strike_Handler extends MalaSkill implements Listener
{
	public Darkness_Strike_Handler()
	{
		super(	"DARKNESS_STRIKE",
				"그림자 송곳",
				Material.ENDER_EYE,
				MsgTBL.WEAPON + MsgTBL.SKILL,
				"",
				"&7전방을 향해 &8{range}&7m를 이동하는 그림자를 내보냅니다.",
				"&7그림자를 밟은 적은",
				"&7 - 위로 떠오릅니다.",
				"&7 - 5초간 실명 상태에 걸립니다.",
				"&7 - &8{damage}&7의 피해를 받습니다.",
				"&7 - &c공중의 적에게는 효과가 없습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double range = cast.getModifier("range"); // 공격력
		double damage = cast.getModifier("damage"); // 공격력
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Darkness_Strike_Skill(data.getPlayer(), range, damage));
	}
	
	class Darkness_Strike_Skill implements Runnable
	{
		ArrayList<LivingEntity> damaged_entities = new ArrayList<LivingEntity>();
		
		Player player;
		double damage;
		double range = 10.0;

		double speed = 0.4;
		double radius = 1.5;

		DustOptions dop = new DustOptions(Color.BLACK, 2.0f);
		
		Location loc;
		Vector dir;
		
		public Darkness_Strike_Skill(Player _player, double _range, double _dmg)
		{
			player = _player;
			range = _range;
			damage = _dmg;
			loc = player.getEyeLocation();
			dir = player.getEyeLocation().getDirection();
			dir.setY(0);
			dir.normalize();
			loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.5f, 0.15f);

		}
		
		void Draw_Particle(Location _floor_loc)
		{
			double x = -radius + Math.random() * radius * 2.0;
			double z = -radius + Math.random() * radius * 2.0;
			
			Location start = _floor_loc.clone().add(x, 0, z);
			Vector temp_dir = _floor_loc.clone().add(0, radius * 2, 0).subtract(start).toVector().normalize();
			Location end = start.clone().add(temp_dir.multiply(radius * 4.0));
			Particle_Drawer.Draw_Line(start, end, dop, 0.1);
			Particle_Drawer.Draw_Line(start, end, Particle.CRIT, 0.1);
		}
		
		public void run()
		{
			loc.add(dir.clone().multiply(speed));
			range -= speed;

			// Particle_Drawer.Draw_Line(loc, loc.clone().add(0, 10, 0), Particle.BARRIER, 0.1);
			
			RayTraceResult rtr = loc.getWorld().rayTraceBlocks(loc, new Vector(0.0, -1.0, 0.0), 8.0, FluidCollisionMode.NEVER, true);
			if (rtr == null || rtr.getHitPosition() == null)
			{
				return;
			}
			
			Location floor_loc = new Location(loc.getWorld(), rtr.getHitPosition().getX(), rtr.getHitPosition().getY(), rtr.getHitPosition().getZ());
			loc.setY(floor_loc.getY() + 3.0);

			floor_loc.getWorld().spawnParticle(Particle.REDSTONE, floor_loc.clone().add(0, 0.05, 0), 100, radius, 0, radius, 0, dop);
			
			for (Entity temp : player.getWorld().getNearbyEntities(floor_loc, radius, radius, radius))
			{
				if (!(temp instanceof LivingEntity))
					continue;
				if (temp == player)
					continue;
				LivingEntity le = (LivingEntity)temp;
				if (!le.isOnGround() || damaged_entities.contains(le))
					continue;
				
				damaged_entities.add(le);
				Draw_Particle(floor_loc);
				le.getWorld().playSound(floor_loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 2.0f);
				le.getWorld().playSound(floor_loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 2.0f, 2.0f);
				
				le.setNoDamageTicks(0);
				double hp = le.getHealth();
				Damage.Attack(player, le, damage, DamageType.SKILL, DamageType.WEAPON);
				if (hp != le.getHealth())
				{
					Vector vel = le.getVelocity();
					vel.setY(0.8);
					le.setVelocity(vel);
					
					Buff_Manager.Add_Buff(le, PotionEffectType.BLINDNESS, 0, 100, null);
				}
			}
			
			if (range > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
