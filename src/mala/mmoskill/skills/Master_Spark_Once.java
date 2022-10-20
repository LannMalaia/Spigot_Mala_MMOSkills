package mala.mmoskill.skills;

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
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Lightning;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Master_Spark_Once extends RegisteredSkill
{
	public Master_Spark_Once()
	{	
		super(new Master_Spark_Once_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(200, 20));
		addModifier("radius", new LinearValue(7, 0.5));
		addModifier("cooldown", new LinearValue(120, 0));
		//addModifier("cooldown", new LinearValue(120, -2));
		addModifier("mana", new LinearValue(165, 15));
	}
}

class Master_Spark_Once_Handler extends MalaSkill implements Listener
{
	public Master_Spark_Once_Handler()
	{
		super(	"MASTER_SPARK_ONCE",
				"플라즈마 블래스트",
				Material.NETHER_STAR,
				MsgTBL.SKILL + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC,
				"",
				"&7바라보고 있는 장소로 필살의 전격을 발사합니다.",
				"대상은 &8{damage}&7의 피해를 받습니다.",
				"&7최대 5초까지 힘을 모아 300%의 피해를 줄 수 있습니다.",
				"&c방향 전환이 불가능하며, 움직이는 순간 시전합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		registerModifiers("radius");
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("damage");
		damage *= Mastery_Lightning.Get_Mult(data.getPlayer());
		double radius = cast.getModifier("radius");

		DamageMetadata ar = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new LightningMagicEvent(data.getPlayer(), ar));
		damage = ar.getDamage();
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Master_Spark_Once_Skill(data.getPlayer(), damage, radius, 60.0));
	}
}
class Master_Spark_Once_Skill implements Runnable
{
	Player player;
	double damage;
	double max_distance;
	Location fixed_loc, memorized_loc;
	Vector dir;
	
	double timer = 4.0;
	int count = 0;
	double roll = 0.0;
	double radius = 5.0;
	Vector[] vecs_beam;
	Vector[] vecs;
	Vector[] ring_vecs;
	
	int m_Stack = -1; // 얼마나 모았나
	
	public Master_Spark_Once_Skill(Player _player, double _damage, double _radius, double _max_distance)
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
	
	// 기모으기 링 그리기
	double m_Ring_Radius = 8.0;
	void Draw_Gather_Ring()
	{
		for(double angle = 0.0; angle <= 360.0; angle += 360.0 / (m_Ring_Radius * 36.0))
		{
			double rad = Math.toRadians(angle);
			Vector vec = new Vector(Math.cos(rad) * m_Ring_Radius, 0.5, Math.sin(rad) * m_Ring_Radius);
			Location loc = player.getLocation().add(vec);
			loc.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
		}
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
		// 그릴 구역 돌리기
		Vector[] temp_vecs_beam = TRS.Rotate_Z(vecs_beam, roll);
		temp_vecs_beam = TRS.Rotate_X(temp_vecs_beam, fixed_loc.getPitch());
		temp_vecs_beam = TRS.Rotate_Y(temp_vecs_beam, fixed_loc.getYaw());
		Vector[] temp_ring_vecs = TRS.Rotate_Z(ring_vecs, roll);
		temp_ring_vecs = TRS.Rotate_X(temp_ring_vecs, fixed_loc.getPitch());
		temp_ring_vecs = TRS.Rotate_Y(temp_ring_vecs, fixed_loc.getYaw());
		

		// 기모으기 링 그리기
		if (count % 20 == 0)
		{
			m_Stack += 1;
			m_Ring_Radius = 8.0;
			// player.sendMessage("stack = " + m_Stack);
		}
		m_Ring_Radius = m_Ring_Radius - m_Ring_Radius * 0.2;
		Draw_Gather_Ring();
		
		// 빔 예상 궤적 그리기
		for(int i = 0; i < temp_vecs_beam.length; i++)
		{
			Location loc = fixed_loc.clone().add(temp_vecs_beam[i]);
			fixed_loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
		}

		if (memorized_loc.distance(player.getLocation()) > 0.1)
		{
			Fire_Spark();
			return;
		}
		if (count % 4 == 0)
			player.teleport(memorized_loc, TeleportCause.PLUGIN);
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(m_Stack == 5)
		{
			Fire_Spark();
			return;
		}
		
		// 소리
		for (double cur_dist = 0.0; cur_dist <= max_distance; cur_dist += 3.0)
		{
			Location loc = fixed_loc.clone().add(dir.clone().multiply(cur_dist));
			if(count % 20 == 0)
				loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.5f, 1.5f);
		}
					
		// 마무리
		roll += 4.0f;
		count += 1;
		timer -= 0.05;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}

	void Fire_Spark()
	{
		Vector[] temp_vecs_beam = TRS.Rotate_Z(vecs_beam, roll);
		temp_vecs_beam = TRS.Rotate_X(temp_vecs_beam, fixed_loc.getPitch());
		temp_vecs_beam = TRS.Rotate_Y(temp_vecs_beam, fixed_loc.getYaw());
		Vector[] temp_ring_vecs = TRS.Rotate_Z(ring_vecs, roll);
		temp_ring_vecs = TRS.Rotate_X(temp_ring_vecs, fixed_loc.getPitch());
		temp_ring_vecs = TRS.Rotate_Y(temp_ring_vecs, fixed_loc.getYaw());
		
		Vector recoil = player.getEyeLocation().getDirection();
		player.setVelocity(recoil.multiply(-1.0 + m_Stack * -0.5));
		
		// 빔 예상 궤적
		for(int i = 0; i < temp_vecs_beam.length; i++)
		{
			Location loc = fixed_loc.clone().add(temp_vecs_beam[i]);
			fixed_loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 1, 0, 0, 0, 0);
		}
		
		// 전기
		for (int k = 0; k < 5; k++)
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
		
		// 소리
		for (double cur_dist = 0.0; cur_dist <= max_distance; cur_dist += 3.0)
		{
			Location loc = fixed_loc.clone().add(dir.clone().multiply(cur_dist));
			fixed_loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
			loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.2f);
			loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 2f);
		}
		
		// 피격 체크
		Location hitbox_axis = fixed_loc.clone().add(dir.clone().multiply(max_distance * 0.5));
		
		List<Entity> abc = new ArrayList<Entity>(fixed_loc.getWorld().getNearbyEntities(hitbox_axis, max_distance, max_distance, max_distance));
		List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(hitbox_axis.toVector(),
				new Vector(fixed_loc.getPitch(), fixed_loc.getYaw(), 0),
				new Vector(radius, radius, max_distance),
				abc);
		// Hitbox.Draw_hitbox(player.getWorld(), hitbox_axis.toVector(), new Vector(fixed_loc.getPitch(), fixed_loc.getYaw(), 0), new Vector(radius, radius, max_distance));
		
		for(Entity en : entities)
		{
			if (!(en instanceof LivingEntity))
				continue;
			if (en == player)
				continue;

			LivingEntity le = (LivingEntity)en;
			le.setNoDamageTicks(0);
			Damage.Attack(player, le, damage * (1 + m_Stack * 0.4), DamageType.MAGIC, DamageType.SKILL);
		}
	}
}



















