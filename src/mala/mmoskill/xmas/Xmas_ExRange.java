package mala.mmoskill.xmas;

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
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.ArrowSkill_Manager;
import mala.mmoskill.manager.ArrowTip;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;


// 광폭 화살 효과
public class Xmas_ExRange implements Runnable
{
	Player player;
	double damage;
	double distance;
	List<Entity> entities;
	
	Location pos;
	Vector dir;
	
	double speed = 1.5;
	double angle = 180;
	
	Vector[] vecs;

	public Xmas_ExRange(Player _p, double _distance, double _damage)
	{
		this(_p, _distance, _damage, 0.0);
	}
	public Xmas_ExRange(Player _p, double _distance, double _damage, double _angle_correct)
	{
		player = _p;
		distance = _distance;
		damage = _damage;
		
		pos = _p.getEyeLocation();
		dir = _p.getLocation().getDirection();
		Vector[] origin_dir = new Vector[1];
		origin_dir[0] = dir.clone();
		dir = TRS.Rotate_Y(origin_dir, _angle_correct)[0];
		
		vecs = new Vector[36];
		for(int i = 0; i < vecs.length; i++)
		{
			double _angle = 90.0 + (angle * -0.5) + i * angle / 36.0;
			vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
		}
		vecs = TRS.Scale(vecs, 5.0, 1.0, 1.0);
		vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch());
		vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw() + _angle_correct);
	}
	
	public void run()
	{
		Location before_pos = pos.clone();
		pos.add(dir.clone().multiply(speed));
		double len = before_pos.distance(pos);
		
		// 파티클 그리기
		pos.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
		for(double i = 0.0; i < len; i += 0.4)
		{
			for(int j = 0; j < vecs.length; j++)
			{
				Location particle_pos = before_pos.clone().add(dir.clone().multiply(i)).add(vecs[j]);
				particle_pos.getWorld().spawnParticle(
						Particle.SNOWFLAKE,
						particle_pos, 1, 0, 0, 0, 0);
			}
		}
		
		// 판정 하기
		Location hitbox_axis = before_pos.clone().add(dir.clone().multiply(len * 0.5));
		List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, 10.0, 10.0, 10.0));
		entities = Hitbox.Targets_In_the_BoundingBox(hitbox_axis.toVector(),
				new Vector(pos.getPitch(), pos.getYaw(), 0),
				new Vector(10.0, 4.0, len),
				abc);
		for(Entity en : entities)
		{
			if(!(en instanceof LivingEntity))
				continue;
			if (en == player)
				continue;
			((LivingEntity)en).damage(damage);
		}
		
		distance -= speed;
		if(distance > 0.0)
		{
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
