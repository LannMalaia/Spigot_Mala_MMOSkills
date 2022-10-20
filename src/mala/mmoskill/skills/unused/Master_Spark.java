package mala.mmoskill.skills.unused;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import mala.mmoskill.skills.passive.Mastery_Lightning;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;

public class Master_Spark extends Skill
{
	public Master_Spark()
	{
		super();
		
		setName("볼티지 버스트");
		setLore(MsgTBL.SKILL + MsgTBL.MAGIC,
				"",
				"&7바라보고 있는 장소로 계속해서 전격을 발사합니다.",
				"대상은 0.25초마다 &8{damage}&7의 피해를 받습니다.",
				"&72초동안 준비해 5초동안 발사합니다.",
				"&c방향 전환이 불가능하며, 움직이면 취소됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.NETHER_STAR);

		addModifier("damage", new LinearValue(10, 1.25));
		addModifier("radius", new LinearValue(5, 0.2));
		addModifier("cooldown", new LinearValue(2, 0));
		//addModifier("cooldown", new LinearValue(120, -2));
		addModifier("mana", new LinearValue(100, 2.5));
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		SkillMetadata cast = new SkillMetadata(data, skill);
		
		if (!cast.isSuccessful())
			return cast;
				
		double damage = cast.getModifier("damage");
		damage *= Mastery_Lightning.Get_Mult(data.getPlayer());
		double radius = cast.getModifier("radius");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Master_Spark_Skill(data.getPlayer(), damage, radius, 60.0));
		
		return cast;
	}
}
class Master_Spark_Skill implements Runnable
{
	Player player;
	double damage;
	double max_distance;
	Location fixed_loc, memorized_loc;
	Vector dir;
	
	double timer = 7.0;
	int count = 0;
	double ready_time = 2.0;
	double roll = 0.0;
	double radius = 5.0;
	Vector[] vecs_beam;
	Vector[] vecs;
	Vector[] ring_vecs;
	
	public Master_Spark_Skill(Player _player, double _damage, double _radius, double _max_distance)
	{
		player = _player;
		memorized_loc = _player.getLocation();
		fixed_loc = _player.getLocation().add(0.0, 1.0, 0.0).add(_player.getLocation().getDirection().multiply(2.0));
		dir = _player.getLocation().getDirection();
		damage = _damage;
		radius = _radius;
		max_distance = _max_distance;
		
		Make_Spark();
		Make_Spark2();
		Make_Ring();
	}
	
	void Make_Spark()
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		
		double angle = 0.0;
		double alt_angle = 90.0;
		for(double distance = 0.0; distance < max_distance; distance += 0.5)
		{
			double altitude = Math.toRadians(alt_angle);
			double alt_cos = Math.cos(altitude);
			for(double additive_angle = 0.0; additive_angle <= 360.0; additive_angle += 360.0 / 4.0)
			{
				double rad = Math.toRadians(angle + additive_angle);
				temp_vecs.add(new Vector(Math.cos(rad) * radius * alt_cos * 0.6,
						Math.sin(rad) * radius * alt_cos * 0.6,
						distance));
			}
			angle += 3.0;
			alt_angle = Math.max(0.0, alt_angle - 2);
		}
		
		vecs_beam = new Vector[temp_vecs.size()];
		for(int i = 0; i < temp_vecs.size(); i++)
			vecs_beam[i] = temp_vecs.get(i);
	}

	void Make_Spark2()
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		
		Vector before_pos = null;
		for (int i = 0; i < 3; i++)
		{
			before_pos = new Vector();
			double rad = 0.5;
			for (double cur_dis = 5.0; cur_dis <= max_distance; cur_dis += 5.0)
			{
				rad = Math.min(radius, rad + 1.5);
				Vector pos = new Vector(-rad + Math.random() * rad * 2,
						-rad + Math.random() * rad * 2,
						cur_dis + (-2.0 + Math.random() * 4.0));
				if (before_pos != null)
				{
					Vector dir = pos.clone().subtract(before_pos).normalize();
					for (double dis = 0.0; dis < pos.distance(before_pos); dis += 0.2)
						temp_vecs.add(before_pos.clone().add(dir.clone().multiply(dis)));
				}
				before_pos = pos;
			}
		}
		
		
		vecs = new Vector[temp_vecs.size()];
		for(int i = 0; i < temp_vecs.size(); i++)
			vecs[i] = temp_vecs.get(i);
	}

	void Make_Ring()
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		
		double angle = 0.0;
		double alt_angle = 90.0;
		for(double distance = 2.0; distance < 15.0; distance += distance)
		{
			alt_angle = Math.max(0.0, alt_angle - 24);
			double altitude = Math.toRadians(alt_angle);
			double alt_cos = Math.cos(altitude);
			for(double additive_angle = 0.0; additive_angle <= 360.0; additive_angle += 360.0 / 72.0)
			{
				double rad = Math.toRadians(angle + additive_angle);
				temp_vecs.add(new Vector(Math.cos(rad) * radius * alt_cos * 1.25, Math.sin(rad) * radius * alt_cos * 1.25, distance));
			}
		}
		
		ring_vecs = new Vector[temp_vecs.size()];
		for(int i = 0; i < temp_vecs.size(); i++)
			ring_vecs[i] = temp_vecs.get(i);
	}
	
	public void run()
	{
		Vector[] temp_vecs_beam = TRS.Rotate_Z(vecs_beam, roll);
		temp_vecs_beam = TRS.Rotate_X(temp_vecs_beam, fixed_loc.getPitch());
		temp_vecs_beam = TRS.Rotate_Y(temp_vecs_beam, fixed_loc.getYaw());
		Vector[] temp_ring_vecs = TRS.Rotate_Z(ring_vecs, roll);
		temp_ring_vecs = TRS.Rotate_X(temp_ring_vecs, fixed_loc.getPitch());
		temp_ring_vecs = TRS.Rotate_Y(temp_ring_vecs, fixed_loc.getYaw());
		
		// 빔
		for(int i = 0; i < temp_vecs_beam.length; i++)
		{
			Location loc = fixed_loc.clone().add(temp_vecs_beam[i]);
			if (ready_time < 0.0)
				fixed_loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
			else
				fixed_loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
		}
		if (ready_time < 0.0)
		{
			// 전기
			if (count % 2 == 0)
			{
				Make_Spark2();
				Vector[] temp_vecs = TRS.Rotate_Z(vecs, roll);
				temp_vecs = TRS.Rotate_X(temp_vecs, fixed_loc.getPitch());
				temp_vecs = TRS.Rotate_Y(temp_vecs, fixed_loc.getYaw());
				for(int i = 0; i < temp_vecs.length; i++)
				{
					Location loc = fixed_loc.clone().add(temp_vecs[i]);
					fixed_loc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0, 0, 0, 0);
				}
			}
			// 링
			for(int i = 0; i < temp_ring_vecs.length; i++)
			{
				Location loc = fixed_loc.clone().add(temp_ring_vecs[i]);
				fixed_loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
			}
		}
		
		// 소리나 기타 파티클
		for (double cur_dist = 0.0; cur_dist <= max_distance; cur_dist += 3.0)
		{
			Location loc = fixed_loc.clone().add(dir.clone().multiply(cur_dist));
			if(ready_time >= 0.0 && count % 10 == 0)
				loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
			else if (ready_time < 0.0)
			{
				if (count % 5 == 0 && cur_dist >= 6.0)
				{
					fixed_loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
					// fixed_loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 1, 0, 0, 0, 0);
				}
				if (count % 5 == 0)
					loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.2f);
				if (count % 10 == 0)
				{
					loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2f);
				}
			}
		}
		
		
		// 피격 체크
		if (ready_time < 0.0 && count % 5 == 0)
		{
			Location hitbox_axis = fixed_loc.clone().add(dir.clone().multiply(max_distance * 0.5));
			
			List<Entity> abc = new ArrayList<Entity>(fixed_loc.getWorld().getNearbyEntities(hitbox_axis, max_distance, max_distance, max_distance));
			List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(hitbox_axis.toVector(),
					new Vector(fixed_loc.getPitch(), fixed_loc.getYaw(), 0),
					new Vector(radius, radius, max_distance),
					abc);
			for(Entity en : entities)
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;

				LivingEntity le = (LivingEntity)en;
				le.setNoDamageTicks(0);
				Damage.Attack(player, le, damage, DamageType.MAGIC, DamageType.SKILL);
			}
		}

		if (count % 3 == 0)
		{
			if (memorized_loc.distance(player.getLocation()) > 0.3)
			{
				player.sendMessage("§c[ 발동중이던 스킬이 취소되었습니다. ]");
				return;
			}
			player.teleport(memorized_loc, TeleportCause.CHORUS_FRUIT);
		}
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(timer <= 0.0)
			return;
					
		// 마무리
		roll += 4.0f;
		timer -= 0.05;
		ready_time -= 0.05;
		count++;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}