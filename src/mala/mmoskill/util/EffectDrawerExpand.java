package mala.mmoskill.util;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import mala_mmoskill.main.MalaMMO_Skill;

public class EffectDrawerExpand implements Runnable
{
	Location location;
	Particle particle;
	ArrayList<Vector> vecs;
	double strength;
	int drawSpeed;
	
	public EffectDrawerExpand(Location location, Particle particle, ArrayList<Vector> vecs, double strength)
	{ this(location, particle, vecs, strength, 1); }
	public EffectDrawerExpand(Location location, Particle particle, ArrayList<Vector> vecs, double strength, int drawSpeed)
	{
		this.location = location.clone();
		this.particle = particle;
		this.vecs = vecs;
		this.strength = strength;
		this.drawSpeed = drawSpeed;
	}
	
	int count = 0;
	public void run()
	{
		if (count < vecs.size())
		{
			for (int i = 0; i < drawSpeed && count < vecs.size(); i++)
			{
				Vector vec = vecs.get(count).clone();
				location.getWorld().spawnParticle(particle, location, 0, vec.getX(), vec.getY(), vec.getZ(), strength, null);
				count += 1;
			}
		}
		else return;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
