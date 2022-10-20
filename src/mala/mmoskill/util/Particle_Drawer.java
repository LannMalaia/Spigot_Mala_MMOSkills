package mala.mmoskill.util;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.util.Vector;

public class Particle_Drawer
{
	public static void Draw_Sphere(Location _loc, Particle _particle, double _radius, double _angle_correct, double _density)
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		
		for(int i = -90; i <= 90; i += 60.0 / _density / _radius)
		{
			double altitude = Math.toRadians(i + _angle_correct);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);
			
			for(double angle = 0.0; angle <= 360.0; angle += 60.0 / _density / _radius)
			{
				double rad = Math.toRadians(angle);
				double rad_cos = Math.cos(rad);
				double rad_sin = Math.sin(rad);
				
				Vector pos = new Vector(rad_cos * alt_cos, alt_sin, rad_sin * alt_cos);
				temp_vecs.add(pos.clone());
			}
		}
		
		// 옮겨심기
		Vector[] vecs = new Vector[temp_vecs.size()];
		for(int i = 0; i < temp_vecs.size(); i++)
			vecs[i] = temp_vecs.get(i);

		//angle_correct += 3;
		vecs = TRS.Scale(vecs, _radius, _radius, _radius);
		
		for (int i = 0; i < vecs.length; i++)
		{
			Location loc = _loc.clone().add(vecs[i]);
			_loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	
	public static void Draw_Circle(Location _loc, Particle _particle, double _radius, double _angle)
	{
		for (double angle = 0.0; angle <= 360.0; angle += 30.0 / _radius)
		{
			Location loc = _loc.clone().add(Math.cos(Math.toRadians(angle + _angle)) * _radius,
					0.0, Math.sin(Math.toRadians(angle + _angle)) * _radius);
			loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	public static void Draw_Circle(Location _loc, Particle _particle, double _radius)
	{
		for (double angle = 0.0; angle <= 360.0; angle += 30.0 / _radius)
		{
			Location loc = _loc.clone().add(Math.cos(Math.toRadians(angle)) * _radius,
					0.0, Math.sin(Math.toRadians(angle)) * _radius);
			loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	public static void Draw_Circle(Location _loc, Particle _particle, double _radius, double _pitch, double _yaw)
	{
		Vector[] vec = new Vector[(int)(360.0 / (15.0 / _radius))];
		for (int i = 0; i < vec.length; i++)
		{
			vec[i] = new Vector(Math.cos(Math.toRadians((15.0 / _radius) * i)) * _radius,
					0.0, Math.sin(Math.toRadians((15.0 / _radius) * i)) * _radius);
		}
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		
		for (int i = 0; i < vec.length; i++)
		{
			Location loc = _loc.clone().add(vec[i]);
			loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	public static void Draw_Circle(Location _loc, DustOptions _dop, double _radius)
	{
		for (double angle = 0.0; angle <= 360.0; angle += 30.0 / _radius)
		{
			Location loc = _loc.clone().add(Math.cos(Math.toRadians(angle)) * _radius,
					0.0, Math.sin(Math.toRadians(angle)) * _radius);
			loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, _dop);
		}
	}
	public static void Draw_Line(Location _start, Location _end, Particle _particle, double _point)
	{
		Vector dir = _end.clone().subtract(_start).toVector().normalize();
		for(double range = 0; range < _start.distance(_end); range += _point)
		{
			Location loc = new Location(_start.getWorld(), _start.getX(), _start.getY(), _start.getZ());
			loc.add(dir.clone().multiply(range));

			_start.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	public static void Draw_Line(Location _start, Location _end, DustOptions _dop, double _point)
	{
		Vector dir = _end.clone().subtract(_start).toVector().normalize();
		for(double range = 0; range < _start.distance(_end); range += _point)
		{
			Location loc = new Location(_start.getWorld(), _start.getX(), _start.getY(), _start.getZ());
			loc.add(dir.clone().multiply(range));

			_start.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, _dop);
		}
	}
	public static void Draw_Line(Location _start, Location _end, DustTransition _dtr, double _point)
	{
		Vector dir = _end.clone().subtract(_start).toVector().normalize();
		for(double range = 0; range < _start.distance(_end); range += _point)
		{
			Location loc = new Location(_start.getWorld(), _start.getX(), _start.getY(), _start.getZ());
			loc.add(dir.clone().multiply(range));

			_start.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, _dtr);
		}
	}
}
