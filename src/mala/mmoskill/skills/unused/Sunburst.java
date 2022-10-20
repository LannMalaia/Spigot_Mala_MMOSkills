package mala.mmoskill.skills.unused;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Sunburst extends Skill
{
	public Sunburst()
	{
		super();
		
		setName("선버스트");
		setLore("&7거대한 화염구를 폭발시켜 주변에 피해를 줍니다.", "", MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.SUNFLOWER);

		addModifier("damage", new LinearValue(40, 8));
		addModifier("cooldown", new LinearValue(2, -0.5, 0, 30));
		addModifier("mana", new LinearValue(1, -.3, 3, 5));
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		SkillMetadata cast = new SkillMetadata(data, skill);
		
		if (!cast.isSuccessful())
			return cast;
		
		double damage = cast.getModifier("damage");
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		Vector dir = data.getPlayer().getLocation().getDirection();
		
		
		double radius = 40.0;
		Location axis = data.getPlayer().getLocation().add(0d, 3.0d, 0d);
		
		// Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, meteor);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Sunburst_Pulse(axis, radius));
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new Sunburst_Skill(axis, data.getPlayer(), damage, radius), 60);
		return cast;
	}
}

class Sunburst_Pulse implements Runnable
{
	Location pos;
	double max_radius;
	double cur_radius = 1.0;
	
	public Sunburst_Pulse(Location _pos, double _max_radius)
	{
		pos = _pos;
		max_radius = _max_radius;
		
		pos.getWorld().playSound(pos, Sound.ITEM_TOTEM_USE, 2, 1.5f);
	}
	
	public void run()
	{
		World world = pos.getWorld();
		// DustOptions dop = new Particle.DustOptions(Color.fromRGB(255, 200, 80), 2f);
		DustOptions dop = new Particle.DustOptions(Color.fromRGB(255, 255, 170), 2f);
		for(double angle = 0.0; angle <= 360.0; angle += 30.0 / cur_radius)
		{
			Location temp_pos = pos.clone().add(Math.cos(Math.toRadians(angle)) * cur_radius, 0, Math.sin(Math.toRadians(angle)) * cur_radius);
			world.spawnParticle(Particle.REDSTONE, temp_pos, 1, 0, 0, 0, 0, dop);
		}
		
		cur_radius = cur_radius + (max_radius - cur_radius) * 0.2;
		if(cur_radius < max_radius - 0.01)
		{
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
		}
	}
}

class Sunburst_Skill implements Runnable
{
	Location pos;
	Player player;
	double damage;
	
	double max_radius;
	double cur_radius = 1.0;

	double timer = 10.0;
	
	double angle_correct = 0.0;
	
	Vector[] vecs;
	
	public Sunburst_Skill(Location _pos, Player _player, double _damage, double _max_radius)
	{
		pos = _pos;
		player = _player;
		damage = _damage;
		max_radius = _max_radius;
		
		Make_Ball();
	}
	
	void Make_Ball()
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		angle_correct += 0.02;
		
		for(int i = -90; i <= 90; i += 60.0 / cur_radius)
		{
			double altitude = Math.toRadians(i + angle_correct);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);
			
			for(double angle = 0.0; angle <= 360.0; angle += 60.0 / cur_radius)
			{
				double rad = Math.toRadians(angle);
				double rad_cos = Math.cos(rad);
				double rad_sin = Math.sin(rad);
				
				Vector pos = new Vector(rad_cos * alt_cos, alt_sin, rad_sin * alt_cos);
				temp_vecs.add(pos.clone());
			}
		}
		
		// 옮겨심기
		vecs = new Vector[temp_vecs.size()];
		for(int i = 0; i < temp_vecs.size(); i++)
			vecs[i] = temp_vecs.get(i);

		//angle_correct += 3;
		vecs = TRS.Scale(vecs, cur_radius, cur_radius, cur_radius);
		// vecs = TRS.Rotate_X(vecs, 45);
		// vecs = TRS.Rotate_Y(vecs, angle_correct);
	}
	
	public void run()
	{
		Make_Ball();
		
		pos.getWorld().playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 2, 0.5f);
		
		// 파티클
		DustOptions dop = new Particle.DustOptions(Color.YELLOW, 20f);
		for (int i = 0; i < vecs.length; i++)
		{
			Location loc = pos.clone().add(vecs[i]);
			pos.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dop);
			// pos.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
		}
		
		
		// 적 찾고 피해주기
		
		
		
		// 마무리
		cur_radius = cur_radius + (max_radius - cur_radius) * 0.1;
		if(cur_radius < max_radius - 0.01) //timer >= 0)
		{
			timer -= 0.1;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
