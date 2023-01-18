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

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.skills.Lightning_Bolt;
import mala.mmoskill.skills.passive.Mastery_Lightning;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;


public class SnowCircle implements Runnable
{
	Player player;
	Location pos;
	
	List<Entity> entities;
	double damage;
	double radius;

	Vector[] vecs;
	Vector[] vecs_lightning;
	
	boolean is_attack = false;
	RegisteredSkill skill_chain_lightning;
	int count;
	double reduce;
	
	public SnowCircle(Player p, double _damage, double _radius)
	{
		player = p;
		damage = _damage;
		radius = _radius;
		pos = player.getEyeLocation();

		vecs = new Vector[(int) (radius * 40)];
		vecs_lightning = new Vector[9];

		double additive = 360.0 / (radius * 40);
		double lightning_angle = 45.0;
		for(int i = 0; i < vecs.length; i++)
		{
			double _angle = -180.0 + additive * i;
			vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
		}
		for (int i = 0; i < vecs_lightning.length; i++)
		{
			lightning_angle += 45.0 * i;
			vecs_lightning[i] = new Vector(Math.cos(Math.toRadians(lightning_angle)), 0, Math.sin(Math.toRadians(lightning_angle)));
		}
		vecs = TRS.Scale(vecs, radius, radius, radius);
		vecs_lightning = TRS.Scale(vecs_lightning, radius, radius, radius);
		double y_rand = Math.random() * 360.0;
		double x_rand = Math.random() * 15.0;
		vecs = TRS.Rotate_Y(vecs, y_rand);
		vecs_lightning = TRS.Rotate_Y(vecs_lightning, y_rand);
		vecs = TRS.Rotate_X(vecs, x_rand);
		vecs_lightning = TRS.Rotate_X(vecs_lightning, x_rand);
		
		player.getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
	}
	
	public void run()
	{
		if (is_attack == false)
		{
			// 검기
			player.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
			
			// 파티클 그리기
			for(int i = 0; i < vecs.length; i++)
			{
				Location loc = pos.clone().add(vecs[i]);
				player.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 1, 0d, 0d, 0d, 0d);
			}

			is_attack = true;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20L);
		}
		else
		{
			pos.getWorld().playSound(pos, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 1.1f);
			pos.getWorld().playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
			// 파티클 그리기
			for(int i = 0; i < vecs_lightning.length - 1; i++)
			{
				Location start = pos.clone().add(vecs_lightning[i]);
				Location end = pos.clone().add(vecs_lightning[i + 1]);
				Chain_Snowball.Draw_Lightning_Line(start, end);
			}
			// 범위 판정
			List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, radius * 3, radius * 3, radius * 3));
			for(Entity temp : abc)
			{
				if (!(temp instanceof LivingEntity))
					continue;
				if (temp == player)
					continue;
				if (pos.distance(temp.getLocation()) > radius)
					continue;
				
				LivingEntity temp2 = (LivingEntity)temp;
				temp2.damage(damage);
			}
		}
	}
}
