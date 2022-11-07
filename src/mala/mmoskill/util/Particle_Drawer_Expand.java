package mala.mmoskill.util;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.util.Vector;

import mala_mmoskill.main.MalaMMO_Skill;
import me.vagdedes.spartan.b.a.v;

public class Particle_Drawer_Expand
{
	private static ArrayList<Vector> makeCircle(double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle)
	{
		double radianGap = (Math.PI * 2.0) / (25.0 * _radius); // 점간 거리
		double corrRadian = Math.toRadians(_corrAngle);
		
		// 원 생성
		ArrayList<Vector> vec = new ArrayList<>();
		for (double r = 0.0; r <= Math.PI * 2.0; r += radianGap)
			vec.add(new Vector(
					Math.cos(r + corrRadian),
					0.0,
					Math.sin(r + corrRadian)));
		
		// holeCount에 맞춰 원에 구멍 뚫기
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
	public static void drawCircle(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle, double _strength)
	{
		// 원 생성
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Vector vel = vec.get(i).clone();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), _strength, null);
		}
	}
	public static void drawCircleRandomize(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			int _holeCount, double _corrAngle, double _strength)
	{
		// 원 생성
		ArrayList<Vector> vec = makeCircle(_radius, _pitch, _yaw, _holeCount, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Vector vel = vec.get(i).clone();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), Math.random() * _strength, null);
		}
	}

	private static ArrayList<Vector> makeSquare(double _size,
			double _pitch, double _yaw, double _corrAngle)
	{
		
		// 사각형 생성
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
		for (Vector v : vec)
			v.multiply(1.0 / _size); // 형태를 유지하면서 정규화
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		return vec;
	}
	public static void drawSquare(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw, double _corrAngle, double _strength)
	{
		// 원 생성
		ArrayList<Vector> vec = makeSquare(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Vector vel = vec.get(i).clone();
			double newStrength = _strength * vel.distance(new Vector());
			vel.normalize();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), newStrength, null);
		}
	}

	private static ArrayList<Vector> makeTriangle(double _size,
			double _pitch, double _yaw, double _corrAngle)
	{
		
		// 사각형 생성
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
		for (Vector v : vec)
			v.multiply(1.0 / _size); // 형태를 유지하면서 정규화
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		return vec;
	}
	public static void drawTriangle(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle, double _strength)
	{
		// 원 생성
		ArrayList<Vector> vec = makeTriangle(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Vector vel = vec.get(i).clone();
			double newStrength = _strength * vel.distance(new Vector());
			vel.normalize();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), newStrength, null);
		}
	}

	private static ArrayList<Vector> makeStar(double _size,
			double _pitch, double _yaw, double _corrAngle)
	{
		
		// 사각형 생성
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
		for (Vector v : vec)
			v.multiply(1.0 / _size); // 형태를 유지하면서 정규화
		vec = TRS.Rotate_Y(vec, _corrAngle);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		return vec;
	}
	public static void drawStar(Location _loc, Particle _particle, double _radius,
			double _pitch, double _yaw,
			double _corrAngle, double _strength)
	{
		// 원 생성
		ArrayList<Vector> vec = makeStar(_radius, _pitch, _yaw, _corrAngle);
		
		for (int i = 0; i < vec.size(); i++)
		{
			Location loc = _loc.clone().add(vec.get(i));
			Vector vel = vec.get(i).clone();
			double newStrength = _strength * vel.distance(new Vector());
			vel.normalize();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), newStrength, null);
		}
	}

	public static void drawArc(Location _loc, Particle _particle,
			double _density, double _angle,
			double _pitch, double _yaw,
			double _corrAngle, double _strength, int _speed, boolean _reverse)
	{
		// 호 생성
		double radianGap = (Math.PI * 2.0) / (25.0 * _density); // 점간 거리
		double corrRadian = Math.toRadians(_corrAngle);
		
		// 호 생성
		ArrayList<Vector> vec = new ArrayList<>();
		for (double r = 0.0; r <= Math.toRadians(_angle); r += radianGap)
			vec.add(new Vector(
					Math.cos(r + corrRadian),
					0.0,
					Math.sin(r + corrRadian)));
		
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new EffectDrawer(_loc, _particle, vec, _strength, _speed));
	}
	

	public static void drawLine(Location _loc, Particle _particle, double _length,
			double _pitch, double _yaw, double _strength)
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
			double strength = _strength * v.distance(new Vector());
			Vector vel = v.clone().normalize();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), strength, null);
		}
	}

	// 원점 -> 수정 모양으로 뻗어나감
	public static void drawCrystal(Location _loc, Particle _particle,
			int _point, double _width, double _length,
			double _pitch, double _yaw, double _roll,
			double _strength)
	{
		ArrayList<Vector> points = new ArrayList<>();
		ArrayList<Vector> vec = new ArrayList<>();
		// 정점 설정 (사각형이면 1->2->3->4->1로 5개의 정점을 설정)
		for (int i = 0; i <= _point; i++)
		{
			double rad = Math.toRadians(360.0 / _point);
			points.add(new Vector(Math.cos(rad * i) * _width, 0.0, Math.sin(rad * i) * _width));
		}
		// 정점에 따른 n각형 선
//		for (int i = 0; i < points.size() - 1; i++)
//		{
//			Vector start = points.get(i), end = points.get(i + 1);
//			Vector dir = end.clone().subtract(start).normalize();
//			double distance = start.distance(end);
//			for (double d = 0; d < distance; d += 0.07)
//				vec.add(start.clone().add(dir.clone().multiply(d)));
//		}
		// 정점과 기둥 윗 끝을 연결하는 선
		for (int i = 0; i < points.size() - 1; i++)
		{
			Vector start = points.get(i), end = new Vector(0.0, -_length, 0.0);
			Vector dir = end.clone().subtract(start).normalize();
			double distance = start.distance(end);
			for (double d = 0; d < distance; d += 0.07)
				vec.add(start.clone().add(dir.clone().multiply(d)));
		}
		// 정점과 기둥 밑 끝을 연결하는 선
		for (int i = 0; i < points.size() - 1; i++)
		{
			Vector start = points.get(i), end = new Vector(0.0, _length, 0.0);
			Vector dir = end.clone().subtract(start).normalize();
			double distance = start.distance(end);
			for (double d = 0; d < distance; d += 0.07)
				vec.add(start.clone().add(dir.clone().multiply(d)));
		}
		
		// 굴리기
		vec = TRS.Rotate_Z(vec, _roll);
		vec = TRS.Rotate_X(vec, _pitch);
		vec = TRS.Rotate_Y(vec, _yaw);
		
		for (Vector v : vec)
		{
			double strength = _strength * v.distance(new Vector());
			Vector vel = v.clone().normalize();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), strength, null);
		}
	}

	// 원점 -> 랜덤한 구 모양으로 뻗어나감
	public static void drawRandomSphere(Location _loc, Particle _particle,
			int _count, double _radius, double _strength, double _randomStrength)
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
		
		// 굴리기
		vec = TRS.Scale(vec, _radius, _radius, _radius);
		
		for (Vector v : vec)
		{
			double strength = (_strength + Math.random() * _randomStrength) * v.distance(new Vector());
			Vector vel = v.clone().normalize();
			_loc.getWorld().spawnParticle(_particle, _loc, 0, vel.getX(), vel.getY(), vel.getZ(), strength, null);
		}
	}
}
