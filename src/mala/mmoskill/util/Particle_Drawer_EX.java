package mala.mmoskill.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.util.Vector;

import mala_mmoskill.main.MalaMMO_Skill;

public class Particle_Drawer_EX
{
	private static ArrayList<Vector> makeCircle(double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle)
	{
		double radianGap = (Math.PI * 2.0) / (25.0 * _radius); // 痢埃 芭府
		double corrRadian = Math.toRadians(_corrAngle);
		
		// 盔 积己
		ArrayList<Vector> vec = new ArrayList<>();
		for (double r = 0.0; r <= Math.PI * 2.0; r += radianGap)
			vec.add(new Vector(
					Math.cos(r + corrRadian) * _radius,
					0.0,
					Math.sin(r + corrRadian) * _radius));
		
		// holeCount俊 嘎苗 盔俊 备港 墩扁
		if (_holeCount > 0)
		{
			int start = vec.size() / _holeCount;
			int count = start / 2;
			ArrayList<Vector> removeVec = new ArrayList<>();
			for (int i = 0; i < _holeCount; i++)
			{
				int index = start * i;
				for (int j = index; j < index + count || j >= vec.size(); j++)
					removeVec.add(vec.get(j));
			}
			vec.removeAll(removeVec);
		}
		
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		return vec;
	}
	
	public static void drawCircle(Location _loc, DustTransition _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, _particle);
		}
	}
	public static void drawCircle(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle, double _strength)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}
	public static void drawCircleRandomize(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle, double _strength, double _random)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			double strength = _strength + Math.random() * _random;
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), strength, null);
		}
	}
	public static void drawCircleUp(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle, double _strength)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(_particle, loc, 0, 0.0, 1.0, 0.0, _strength, null);
		}
	}
	public static void drawCircleVector(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle,
			Vector _velocity, double _strength)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(_particle, loc, 0, _velocity.getX(), _velocity.getY(), _velocity.getZ(), _strength, null);
		}
	}
	public static void drawCircleUpRandomize(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle, double _strength)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(_particle, loc, 0, 0.0, 1.0, 0.0, Math.random() * _strength, null);
		}
	}
	public static void drawEight(Location _loc, DustTransition _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius * 0.5, 0, 0, 0, _corrAngle);
		for (Vector v : vec)
			v.add(new Vector(0, 0, _radius * 0.5));
		
		ArrayList<Vector> vec2 = makeCircle(_radius * 0.5, 0, 0, 0, _corrAngle);
		for (Vector v : vec2)
			v.add(new Vector(0, 0, _radius * -0.5));
		
		vec.addAll(vec2);
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);

		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, _particle);
		}
	}
	public static void drawEight(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeCircle(_radius * 0.5, 0, 0, 0, _corrAngle);
		for (Vector v : vec)
			v.add(new Vector(0, 0, _radius * 0.5));
		
		ArrayList<Vector> vec2 = makeCircle(_radius * 0.5, 0, 0, 0, _corrAngle);
		for (Vector v : vec2)
			v.add(new Vector(0, 0, _radius * -0.5));
		
		vec.addAll(vec2);
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);

		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	
	

	private static ArrayList<Vector> makeSquare(double _size,
			double _pitch, double _yaw, double _corrAngle)
	{
		
		// 荤阿屈 积己
		ArrayList<Vector> vec = new ArrayList<>();
		Vector[] sqr_vecs = new Vector[] {
				new Vector(Math.cos(Math.toRadians(0.0)) * _size, 0, Math.sin(Math.toRadians(0.0)) * _size),
				new Vector(Math.cos(Math.toRadians(90.0)) * _size, 0, Math.sin(Math.toRadians(90.0)) * _size),
				new Vector(Math.cos(Math.toRadians(180.0)) * _size, 0, Math.sin(Math.toRadians(180.0)) * _size),
				new Vector(Math.cos(Math.toRadians(270.0)) * _size, 0, Math.sin(Math.toRadians(270.0)) * _size),
				new Vector(Math.cos(Math.toRadians(0.0)) * _size, 0, Math.sin(Math.toRadians(0.0)) * _size),
		};
		for (int i = 0; i < sqr_vecs.length - 1; i++)
		{
			double dist = sqr_vecs[i + 1].distance(sqr_vecs[i]);
			Vector dir = sqr_vecs[i + 1].clone().subtract(sqr_vecs[i]).normalize();
			for (double d = 0; d < dist; d += 0.07)
			{
				vec.add(sqr_vecs[i].clone().add(dir.clone().multiply(d)));
			}
		}	
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		return vec;
	}
	
	public static void drawSquare(Location _loc, DustTransition _particle, double _radius,
			double _pitch, double _yaw, double _corrAngle)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeSquare(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, _particle);
		}
	}
	public static void drawSquare(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw, double _corrAngle, double _strength)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeSquare(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}

	private static ArrayList<Vector> makeTriangle(double _size,
			double _pitch, double _yaw, double _corrAngle)
	{
		
		// 荤阿屈 积己
		ArrayList<Vector> vec = new ArrayList<>();
		Vector[] sqr_vecs = new Vector[] {
			new Vector(Math.cos(Math.toRadians(0.0)) * _size, 0, Math.sin(Math.toRadians(0.0)) * _size),
			new Vector(Math.cos(Math.toRadians(120.0)) * _size, 0, Math.sin(Math.toRadians(120.0)) * _size),
			new Vector(Math.cos(Math.toRadians(240.0)) * _size, 0, Math.sin(Math.toRadians(240.0)) * _size),
			new Vector(Math.cos(Math.toRadians(0.0)) * _size, 0, Math.sin(Math.toRadians(0.0)) * _size),
		};
		for (int i = 0; i < sqr_vecs.length - 1; i++)
		{
			double dist = sqr_vecs[i + 1].distance(sqr_vecs[i]);
			Vector dir = sqr_vecs[i + 1].clone().subtract(sqr_vecs[i]).normalize();
			for (double d = 0; d < dist; d += 0.07)
			{
				vec.add(sqr_vecs[i].clone().add(dir.clone().multiply(d)));
			}
		}	
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		return vec;
	}
	
	public static void drawTriangle(Location _loc, DustTransition _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeTriangle(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, _particle);
		}
	}
	public static void drawTriangle(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle, double _strength)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeTriangle(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}

	private static ArrayList<Vector> makeStar(double _size,
			double _pitch, double _yaw, double _corrAngle)
	{
		ArrayList<Vector> vec = new ArrayList<>();
		for (int i = 0; i < 5; i++)
		{
			Vector start = new Vector(Math.cos(Math.toRadians(i * 144.0)) * _size, 0, Math.sin(Math.toRadians(i * 144.0)) * _size);
			Vector end = new Vector(Math.cos(Math.toRadians((i + 1) * 144.0)) * _size, 0, Math.sin(Math.toRadians((i + 1) * 144.0)) * _size);
			double dist = end.distance(start);
			Vector dir = end.clone().subtract(start).normalize();
			for (double d = 0; d < dist; d += 0.07)
			{
				vec.add(start.clone().add(dir.clone().multiply(d)));
			}
		}	
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		return vec;
	}
	public static void drawStar(Location _loc, DustTransition _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeStar(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, _particle);
		}
	}
	public static void drawStar(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle, double _strength)
	{
		// 盔 积己
		ArrayList<Vector> vec = makeStar(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}
	
	public static void drawArc(Location _loc, Particle _particle,
			double _radius, double _angle,
			double _pitch, double _yaw,
			double _corrAngle, double _strength)
	{
		// 龋 积己
		double radianGap = (Math.PI * 2.0) / (25.0 * _radius); // 痢埃 芭府
		double corrRadian = Math.toRadians(_corrAngle);
		
		// 龋 积己
		ArrayList<Vector> vec = new ArrayList<>();
		for (double r = 0.0; r <= Math.toRadians(_angle); r += radianGap)
			vec.add(new Vector(
					Math.cos(r + corrRadian) * _radius,
					0.0,
					Math.sin(r + corrRadian) * _radius));

		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);

		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}
	public static void drawArc(Location _loc, Particle _particle,
			double _radius, double _angle,
			double _pitch, double _yaw, double _roll,
			double _corrAngle, double _strength)
	{
		// 龋 积己
		double radianGap = (Math.PI * 2.0) / (25.0 * _radius); // 痢埃 芭府
		double corrRadian = Math.toRadians(_corrAngle);
		
		// 龋 积己
		ArrayList<Vector> vec = new ArrayList<>();
		for (double r = 0.0; r <= Math.toRadians(_angle); r += radianGap)
			vec.add(new Vector(
					Math.cos(r + corrRadian) * _radius,
					0.0,
					Math.sin(r + corrRadian) * _radius));
		
		vec = TRS.Rotate_Z(vec, _roll);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);

		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}
	public static void drawArcVelocity(Location _loc, Particle _particle,
			double _radius, double _angle,
			double _pitch, double _yaw, double _roll,
			double _corrAngle, Vector _velocity, double _strength)
	{
		// 龋 积己
		double radianGap = (Math.PI * 2.0) / (25.0 * _radius); // 痢埃 芭府
		double corrRadian = Math.toRadians(_corrAngle);
		
		// 龋 积己
		ArrayList<Vector> vec = new ArrayList<>();
		for (double r = 0.0; r <= Math.toRadians(_angle); r += radianGap)
			vec.add(new Vector(
					Math.cos(r + corrRadian) * _radius,
					0.0,
					Math.sin(r + corrRadian) * _radius));
		
		vec = TRS.Rotate_Z(vec, _roll);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);

		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = _velocity;
			loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}
	
	public static void drawLine(Location _loc, Particle _particle, double _length,
			double _pitch, double _yaw)
	{
		ArrayList<Vector> vec = new ArrayList<>();
		for (double d = 0; d < _length; d += 0.07)
		{
			vec.add(new Vector(0, 0, d));
		}	
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		
		for(Vector v : vec)
		{
			Location loc = _loc.clone().add(v);
			_loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	
	public static void drawLightningLine(Location _loc, Particle _particle,
			double _length, double _width, int _count,
			double _pitch, double _yaw)
	{
		ArrayList<Vector> points = new ArrayList<Vector>();
		ArrayList<Vector> vec = new ArrayList<Vector>();
		
		// 沥痢 汲沥
		points.add(new Vector(0, 0, 0));
		for (int i = 1; i < _count; i++)
		{
			points.add(new Vector(
					-_width + Math.random() * _width * 2.0,
					-_width + Math.random() * _width * 2.0,
					-_width + Math.random() * _width * 2.0 + (_length / (double)_count * (double)i)));
		}
		points.add(new Vector(0, 0, _length));
		
		// 弊副 扼牢 汲沥
		for (int i = 0; i < points.size() - 1; i++)
		{
			Vector start = points.get(i), end = points.get(i + 1);
			Vector dir = end.clone().subtract(start).normalize();
			double distance = start.distance(end);
			for (double d = 0; d < distance; d += 0.07)
				vec.add(start.clone().add(dir.clone().multiply(d)));
		}

		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		
		for (Vector v : vec)
		{
			Location loc = _loc.clone().add(v);
			_loc.getWorld().spawnParticle(_particle, loc, 1, 0, 0, 0, 0);
		}
	}
	
	// 罚待茄 备
	public static void drawRandomSphere(Location _loc, Particle _particle,
			int _count, double _radius, double _strength)
	{
		ArrayList<Vector> vec = new ArrayList<>();
		
		for (int i = 0; i <= _count; i++)
		{
			double altitude = Math.toRadians(-90.0 + Math.random() * 180.0);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);

			double rad = Math.toRadians(Math.random() * 360.0);
			double rad_cos = Math.cos(rad);
			double rad_sin = Math.sin(rad);
			
			Vector pos = new Vector(rad_cos * alt_cos, alt_sin, rad_sin * alt_cos);
			vec.add(pos);
		}
		
		// 奔府扁
		vec = TRS.Scale(vec, _radius, _radius, _radius);
		
		for (Vector v : vec)
		{
			Location loc = _loc.clone().add(v);
			Vector vel = v.clone().normalize();
			_loc.getWorld().spawnParticle(_particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}
	public static void drawRandomSphere(Location _loc, DustTransition _particle,
			int _count, double _radius)
	{
		ArrayList<Vector> vec = new ArrayList<>();
		
		for (int i = 0; i <= _count; i++)
		{
			double altitude = Math.toRadians(-90.0 + Math.random() * 180.0);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);

			double rad = Math.toRadians(Math.random() * 360.0);
			double rad_cos = Math.cos(rad);
			double rad_sin = Math.sin(rad);
			
			Vector pos = new Vector(rad_cos * alt_cos, alt_sin, rad_sin * alt_cos);
			vec.add(pos);
		}
		
		// 奔府扁
		vec = TRS.Scale(vec, _radius, _radius, _radius);
		
		for (Vector v : vec)
		{
			Location loc = _loc.clone().add(v);
			loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, _particle);
		}
	}
}
