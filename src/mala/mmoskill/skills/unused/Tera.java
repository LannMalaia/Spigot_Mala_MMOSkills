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

public class Tera extends Skill
{
	public Tera()
	{
		super();
		
		setName("테라");
		setLore("&7마나를 전부 소진하여 화염구를 소환합니다.", "", MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.SUNFLOWER);

		addModifier("damage", new LinearValue(8, 2));
		addModifier("mana_damage", new LinearValue(5, -0.1));
		addModifier("max_radius", new LinearValue(4, 0.5));
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
		
		
		double max_radius = cast.getModifier("max_radius");
		Location axis = data.getPlayer().getLocation().add(0d, 3.0d, 0d);
		
		// Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, meteor);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Tera_Skill(data.getPlayerData(), damage, max_radius));
		return cast;
	}
}


class Tera_Skill implements Runnable
{
	Location pos;
	Vector dir;
	PlayerData playerdata;
	Player player;
	
	double max_radius = 0;
	
	double final_damage;
	double additive_damage;
	
	double radius = 1.0;
	double timer = 1.0;
	double speed = 1.0;
	
	double angle_correct = 0.0;
	Vector[] vecs;
	
	boolean go_forward = false;
	boolean first_move = false;
	
	public Tera_Skill(PlayerData _playerdata, double _damage, double _max_radius)
	{
		playerdata = _playerdata;
		player = playerdata.getPlayer();
		max_radius = _max_radius;
		
		pos = player.getLocation();
		dir = pos.getDirection();
		final_damage = _damage;
		additive_damage = _damage;
		
		Make_Ball();
	}
	
	void Make_Ball()
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		
		for(int i = -90; i <= 90; i += 90.0 / radius)
		{
			double altitude = Math.toRadians(i);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);
			
			for(double angle = 0.0; angle <= 360.0; angle += 90.0 / radius)
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

		angle_correct += 8;
		vecs = TRS.Scale(vecs, radius, radius, radius);
		vecs = TRS.Rotate_X(vecs, 45);
		vecs = TRS.Rotate_Y(vecs, angle_correct);
	}
	
	public void run()
	{
		Make_Ball();
		
		if(!go_forward)
		{
			playerdata.setMana(Math.max(0, playerdata.getMana() - 5));
			radius += 0.4;
			
			pos = player.getLocation().add(player.getLocation().getDirection().multiply(radius * 1.1));
			
			// 파티클
			pos.getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 1, 2f);
			Draw_Particle();

			if(playerdata.getMana() < 5 || radius >= max_radius)
			{
				go_forward = true;
				timer = radius * 3;
				speed = 1.0 / radius;
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 4);
		}
		else
		{
			if(!first_move)
			{
				first_move = true;
				pos.getWorld().playSound(pos, Sound.ITEM_TOTEM_USE, 2, 2f);
				pos.getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 2, 0.5f);
			}
			
			pos.add(pos.getDirection().multiply(0.3));			
			dir = pos.getDirection();
			
			// 파티클
			Draw_Particle();
						
			// 적 찾고 피해주기
			
			
			// 마무리
			if(timer >= 0)
			{
				timer -= 0.1;
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
			}
		}
	}
	
	void Draw_Particle()
	{
		DustOptions dop = new Particle.DustOptions(Color.YELLOW, 100f);
		for (int i = 0; i < vecs.length; i++)
		{
			Location loc = pos.clone().add(vecs[i]);
			//pos.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
			//pos.getWorld().spawnParticle(Particle.LAVA, loc, 1, 0, 0, 0, 0);
			pos.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dop);
		}
	}
}
