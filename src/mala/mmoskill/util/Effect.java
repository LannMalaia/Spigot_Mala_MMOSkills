package mala.mmoskill.util;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import mala_mmoskill.main.MalaMMO_Skill;

public class Effect
{
	protected Location location;
	protected Particle particle;
	protected float pitch = 1.0f, volume = 1.0f;
	protected DustOptions dustOptions = null;
	protected DustTransition dustTransition = null;
	protected ArrayList<Vector> points = new ArrayList<Vector>(); // 위치
	protected ArrayList<Vector> velocities = new ArrayList<Vector>(); // 방향
	protected ArrayList<SoundData> sounds = new ArrayList<SoundData>(); // 소리
	
	public Effect(Location location, Particle particle)
	{
		this.location = location;
		this.particle = particle;
	}
	
	/**
	 * 이펙트를 한 번에 표현합니다.
	 */
	public void playEffect()
	{
		// 소리가 있으면 소리 출력
		if (sounds.size() > 0)
		{
			for (SoundData soundData : sounds)
				location.getWorld().playSound(location, soundData.sound, soundData.volume, soundData.pitch);
		}
		
		// 파티클 출력
		for (int i = 0; i < points.size(); i++)
		{
			Location loc = location.clone().add(points.get(i));
			Vector vel = velocities.get(i);
			double strength = vel.length();
			if (dustOptions != null)
				loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, dustOptions);
			else if (dustTransition != null)
				loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, dustTransition);
			else
				loc.getWorld().spawnParticle(particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), strength, null);
		}
	}
	/**
	 * 이펙트를 시간차별로 표현합니다.
	 */
	public void playAnimation(int particlesPerTick)
	{
		// 소리가 있으면 소리 출력
		if (sounds.size() > 0)
		{
			for (SoundData soundData : sounds)
				location.getWorld().playSound(location, soundData.sound, soundData.volume, soundData.pitch);
		}
		
		// 파티클 출력
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Runnable() {
			int count = 0;
			public void run()
			{
				if (count < points.size())
				{
					for (int i = 0; i < particlesPerTick && count < points.size(); i++, count++)
					{
						Location loc = location.clone().add(points.get(count));
						Vector vel = velocities.get(count);
						double strength = vel.length();
						if (dustOptions != null)
							loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, dustOptions);
						else if (dustTransition != null)
							loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0, dustTransition);
						else
							loc.getWorld().spawnParticle(particle, loc, 0, vel.getX(), vel.getY(), vel.getZ(), strength, null);
					}
				}
				else return;
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			}			
		});
	}
	
	// 소리 설정
	public Effect addSound(Sound sound)
	{ return addSound(sound, 1.0, 1.0); }
	public Effect addSound(Sound sound, double volume, double pitch)
	{
		sounds.add(new SoundData(sound, pitch, volume));
		return this;
	}
	public Effect clearSound()
	{
		sounds.clear();
		return this;
	}
	
	// 파티클 (레드스톤, 컬러 더스트) 설정
	public Effect setParticle(Particle particle)
	{
		this.particle = particle;
		this.dustOptions = null;
		this.dustTransition = null;
		return this;
	}
	public Effect setDustOptions(DustOptions dustOptions)
	{
		this.particle = Particle.REDSTONE;
		this.dustOptions = dustOptions;
		return this;
	}
	public Effect setDustTransition(DustTransition dustTransition)
	{
		this.particle = Particle.DUST_COLOR_TRANSITION;
		this.dustTransition = dustTransition;
		return this;
	}
	
	/**
	 * 파티클이 그려질 원점을 설정합니다.
	 */
	public Effect setLocation(Location location)
	{
		this.location = location;
		return this;
	}
	
	// append -> 파티클 위치값을 추가
	/**
	 * 평면 이미지를 그립니다. MalaMMO_Skill/images 폴더 내의 이미지 파일을 불러옵니다.
	 * 이미지 파일은 무조건 0.1배로 축소됩니다. 해상도가 400x400인 그림의 경우 40x40블록 크기가 됩니다.
	 * 투명도만 체크하여 반영하기 때문에 png 파일을 사용해야 합니다.
	 * velocity는 중심을 기점으로 퍼지듯이 형성됩니다.
	 * @param fileName 불러올 이미지의 이름입니다. (ex: circle.png)
	 * @param skipCount (1 이상으로 설정하세요) 이미지의 해상도가 너무 높은 경우를 대비해, 일정 부분을 건너 뛰면서 그립니다. 숫자가 높을수록 대충 그려집니다.
	 */
	public Effect append2DImage(String fileName, int skipCount)
	{
		boolean[][] img = ImageReader.readImage(fileName);
		int height = img.length, width = img[0].length;
		Vector start = new Vector(-width * 0.1 * 0.5, 0.0, -height * 0.1 * 0.5);
		for (int y = 0; y < height; y += skipCount) {
			for (int x = 0; x < width; x += skipCount) {
				if (!img[y][x])
					continue;
				Vector v = start.clone().add(new Vector(x * 0.1, 0.0, y * 0.1));
				points.add(v);
				velocities.add(v.clone());
			}
		}
		
		return this;
	}
	/**
	 * 평면 원을 그립니다.
	 * density가 낮을수록 그리는 밀도가 줄어듭니다.
	 * @param radius 반지름
	 */
	public Effect append2DCircle(double radius)
	{ return append2DCircle(radius, 1.0); }
	public Effect append2DCircle(double radius, double density)
	{
		double radianGap = (Math.PI * 2.0) / (25.0 * radius * density);
		for (double r = 0.0; r <= Math.PI * 2.0; r += radianGap)
		{
			Vector v = new Vector(Math.cos(r) * radius, 0.0, Math.sin(r) * radius);
			points.add(v);
			velocities.add(v.clone());
		}
		return this;
	}
	/**
	 * 평면 십자 별을 그립니다.
	 * density가 낮을수록 그리는 밀도가 줄어듭니다.
	 * @param radius 반지름
	 */
	public Effect append2DCrossedStar(double radius)
	{ return append2DCrossedStar(radius, 1.0); }
	public Effect append2DCrossedStar(double radius, double density)
	{
		double radianGap = (Math.PI * 2.0) / (25.0 * radius * density);
		Vector[] starPoints = { 
				new Vector(-radius, 0, -radius),
				new Vector(radius, 0, -radius),
				new Vector(radius, 0, radius),
				new Vector(-radius, 0, radius) };
		for (int i = 0; i < 4; i++) {
			for (double r = 0.0; r <= Math.PI * 0.5; r += radianGap)
			{
				double correction = Math.PI * 0.5 * (double)i;
				Vector v = new Vector(Math.cos(correction + r) * radius, 0.0, Math.sin(correction + r) * radius);
				v.add(starPoints[i]);
				points.add(v);
				velocities.add(v.clone());
			}
		}
		return this;
	}
	/**
	 * 평면 호를 그립니다.
	 * density가 낮을수록 그리는 밀도가 줄어듭니다.
	 * @param radius 반지름
	 */
	public Effect append2DArc(double angle, double radius)
	{ return append2DArc(angle, radius, 1.0); }
	public Effect append2DArc(double angle, double radius, double density)
	{
		double radianGap = (Math.PI * 2.0) / (25.0 * radius * density);
		for (double r = 0.0; r <= Math.toRadians(angle); r += radianGap)
		{
			Vector v = new Vector(Math.cos(r) * radius, 0.0, Math.sin(r) * radius);
			points.add(v);
			velocities.add(v.clone());
		}
		return this;
	}
	/**
	 * 평면 도형을 그립니다.
	 * density가 높을수록 그리는 밀도가 줄어듭니다.
	 * @param corners 각의 개수
	 * @param radius 반지름
	 */
	public Effect append2DShape(int corners, double radius)
	{ return append2DShape(corners, radius, 1.0); }
	public Effect append2DShape(int corners, double radius, double density)
	{
		ArrayList<Vector> cornerList = new ArrayList<>();
		// 정점 설정 (사각형이면 1->2->3->4->1로 5개의 정점을 설정)
		for (int i = 0; i <= corners; i++)
		{
			double rad = Math.toRadians(360.0 / corners);
			cornerList.add(new Vector(Math.cos(rad * i) * radius, 0.0, Math.sin(rad * i) * radius));
		}
		// 정점에 따른 n각형 선
		for (int i = 0; i < cornerList.size() - 1; i++)
		{
			Vector start = cornerList.get(i), end = cornerList.get(i + 1);
			Vector dir = end.clone().subtract(start).normalize();
			double distance = start.distance(end);
			for (double d = 0; d < distance; d += 0.07 * density)
			{
				Vector v = start.clone().add(dir.clone().multiply(d));
				points.add(v);
				velocities.add(v.clone());
			}
		}
		return this;
	}
	/**
	 * 평면 직선을 그립니다. 직선은 z축으로 나아갑니다.
	 * @param length 길이
	 * @return
	 */
	public Effect append2DLine(double length)
	{ return append2DLine(length, 1.0); }
	public Effect append2DLine(double length, double density)
	{
		for (double d = 0; d < length; d += 0.07 * density)
		{
			Vector v = new Vector(0, 0, d);
			points.add(v);
			velocities.add(v.clone());
		}
		return this;
	}
	public Effect append2DLine(Vector dir, double length)
	{ return append2DLine(dir, length, 1.0); }
	public Effect append2DLine(Vector dir, double length, double density)
	{
		for (double d = 0; d < length; d += 0.07 * density)
		{
			Vector v = dir.clone().multiply(d);
			points.add(v);
			velocities.add(v.clone());
		}
		return this;
	}

	// 별 그리기, 크리스탈 등 몇 개 빠진 거 있음
	
	/**
	 * 입체 전격 선을 그립니다. 전기가 흐르는듯한 느낌을 줄 수 있습니다.
	 * @param length 길이
	 * @param width 너비&높이
	 * @param count 각의 개수, 이게 많을수록 그리는 선도 많아집니다.
	 */
	public Effect append3DLightningLine(double length, double width, int count)
	{ return append3DLightningLine(length, width, count, 1.0); }
	public Effect append3DLightningLine(double length, double width, int count, double density)
	{
		ArrayList<Vector> corners = new ArrayList<Vector>();
		ArrayList<Vector> vec = new ArrayList<Vector>();
		
		// 정점 설정
		corners.add(new Vector(0, 0, 0));
		for (int i = 1; i < count; i++)
		{
			corners.add(new Vector(
					-width + Math.random() * width * 2.0,
					-width + Math.random() * width * 2.0,
					-width + Math.random() * width * 2.0 + (length / (double)count * (double)i)));
		}
		corners.add(new Vector(0, 0, length));
		
		// 그릴 라인 설정
		for (int i = 0; i < corners.size() - 1; i++)
		{
			Vector start = corners.get(i), end = corners.get(i + 1);
			Vector dir = end.clone().subtract(start).normalize();
			double distance = start.distance(end);
			for (double d = 0; d < distance; d += 0.07 * density)
			{
				Vector v = start.clone().add(dir.clone().multiply(d));
				points.add(v);
				velocities.add(v.clone());
			}
		}
		return this;
	}
	/**
	 * 입체 구를 그립니다.
	 * @param radius 반지름
	 */
	public Effect append3DSphere(double radius)
	{ return append3DSphere(radius, 1.0); }
	public Effect append3DSphere(double radius, double density)
	{
		for(int i = -90; i <= 90; i += 60.0 / density / radius)
		{
			double altitude = Math.toRadians(i);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);
			
			for(double angle = 0.0; angle <= 360.0; angle += 60.0 / density / radius)
			{
				double rad = Math.toRadians(angle);
				double rad_cos = Math.cos(rad);
				double rad_sin = Math.sin(rad);
				
				Vector v = new Vector(rad_cos * alt_cos, alt_sin, rad_sin * alt_cos);
				v.multiply(radius);
				points.add(v);
				velocities.add(v.clone());
			}
		}
		return this;
	}
	
	// TRS -> 이동, 회전, 크기
	/**
	 * 파티클 위치를 고정합니다. 원점을 기준으로 팽창시키거나 하고 싶을 때 좋습니다.
	 */
	public Effect setPoint(double x, double y, double z)
	{
		for (Vector vector : points)
			vector.setX(x).setY(y).setZ(z);
		return this;
	}
	/**
	 * 파티클 속력을 고정합니다. 파티클을 특정 방향으로 일제히 이동시킬 수 있습니다.
	 */
	public Effect setVelocity(double x, double y, double z)
	{
		for (Vector vector : velocities)
			vector.setX(x).setY(y).setZ(z);
		return this;
	}
	/**
	 * 특정 방향으로 이동합니다. 위치와 속력 모두 이동합니다.
	 */
	public Effect translate(double x, double y, double z)
	{
		return translatePoint(x, y, z).translateVelocity(x, y, z);
	}
	public Effect translatePoint(double x, double y, double z)
	{
		for (Vector point : points)
			point.add(new Vector(x, y, z));
		// points = TRS.Translate(points, x, y, z);
		return this;
	}
	public Effect translateVelocity(double x, double y, double z)
	{
		for (Vector velocity : velocities)
			velocity.add(new Vector(x, y, z));
		return this;
	}
	/**
	 * 특정 방향으로 회전합니다. 위치와 속력 모두 이동합니다.
	 * 각도는 육십분법을 따릅니다.
	 * @param x pitch, 고개를 위아래로 끄덕이기
	 * @param y yaw, 고개를 옆으로 돌리기
	 * @param z roll, 고개를 비틀기
	 */
	public Effect rotate(double x, double y, double z)
	{
		return rotatePoint(x, y, z).rotateVelocity(x, y, z);
	}
	public Effect rotatePoint(double x, double y, double z)
	{
		points = TRS.Rotate_Z(points, z);
		points = TRS.Rotate_X(points, x);
		points = TRS.Rotate_Y(points, y);
		return this;
	}
	public Effect rotateVelocity(double x, double y, double z)
	{
		velocities = TRS.Rotate_Z(velocities, z);
		velocities = TRS.Rotate_X(velocities, x);
		velocities = TRS.Rotate_Y(velocities, y);
		return this;
	}
	/**
	 * 특정 크기만큼 비율을 키우거나 줄입니다. 위치와 속력 모두 영향을 받습니다.
	 * 2.0 으로 설정하면 2배로 커집니다.
	 */
	public Effect scale(double size)
	{
		return scale(size, size, size);
	}
	public Effect scale(double x, double y, double z)
	{
		return scalePoint(x, y, z).scaleVelocity(x, y, z);
	}
	public Effect scalePoint(double size)
	{
		return scalePoint(size, size, size);
	}
	public Effect scalePoint(double x, double y, double z)
	{
		points = TRS.Scale(points, x, y, z);
		return this;
	}
	public Effect scaleVelocity(double size)
	{
		return scaleVelocity(size, size, size);
	}
	public Effect scaleVelocity(double x, double y, double z)
	{
		velocities = TRS.Scale(velocities, x, y, z);
		return this;
	}

	/**
	 * 파티클 배열 순서를 뒤집습니다.
	 */
	public Effect reverse()
	{
		Collections.reverse(points);
		Collections.reverse(velocities);
		return this;
	}
	
	/**
	 * 특정 수치만큼 뒤섞습니다. 위치와 속력 모두 영향을 받습니다.
	 * @param randomizeType add: min~max만큼 더합니다. multiply: min~max만큼 곱합니다. multiply_linear: multiply와 같으나 같은 랜덤값을 사용합니다.
	 */
	public static enum RANDOMIZE_TYPE { ADD, MULTIPLY, MULTIPLY_LINEAR }
	public Effect randomize(RANDOMIZE_TYPE randomizeType, double min, double max)
	{ return randomize(randomizeType, min, min, min, max, max, max); }
	public Effect randomize(RANDOMIZE_TYPE randomizeType, double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
	{
		return randomizePoint(randomizeType, minX, minY, minZ, maxX, maxY, maxZ).randomizeVelocity(randomizeType, minX, minY, minZ, maxX, maxY, maxZ);
	}
	public Effect randomizePoint(RANDOMIZE_TYPE randomizeType, double min, double max)
	{ return randomizePoint(randomizeType, min, min, min, max, max, max); }
	public Effect randomizePoint(RANDOMIZE_TYPE randomizeType, double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
	{
		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		double rangeZ = maxZ - minZ;
		double linearValue = Math.random();
		for (Vector vector : points)
		{
			switch (randomizeType)
			{
			case ADD:
				vector.add(new Vector(minX + Math.random() * rangeX,
						minY + Math.random() * rangeY,
						minZ + Math.random() * rangeZ));
				break;
			case MULTIPLY:
				vector.multiply(new Vector(
						minX + Math.random() * rangeX,
						minY + Math.random() * rangeY,
						minZ + Math.random() * rangeZ));
				break;
			case MULTIPLY_LINEAR:
				vector.multiply(new Vector(
						minX + linearValue * rangeX,
						minY + linearValue * rangeY,
						minZ + linearValue * rangeZ));
				break;
			}
		}
		return this;
	}
	public Effect randomizeVelocity(RANDOMIZE_TYPE randomizeType, double min, double max)
	{ return randomizeVelocity(randomizeType, min, min, min, max, max, max); }
	public Effect randomizeVelocity(RANDOMIZE_TYPE randomizeType, double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
	{
		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		double rangeZ = maxZ - minZ;
		double linearValue = Math.random();
		for (Vector vector : velocities)
		{
			switch (randomizeType)
			{
			case ADD:
				vector.add(new Vector(minX + Math.random() * rangeX,
						minY + Math.random() * rangeY,
						minZ + Math.random() * rangeZ));
				break;
			case MULTIPLY:
				vector.multiply(new Vector(
						minX + Math.random() * rangeX,
						minY + Math.random() * rangeY,
						minZ + Math.random() * rangeZ));
				break;
			case MULTIPLY_LINEAR:
				vector.multiply(new Vector(
						minX + linearValue * rangeX,
						minY + linearValue * rangeY,
						minZ + linearValue * rangeZ));
				break;
			}
		}
		return this;
	}
	
	/**
	 * 각 파티클이 바로 다음 파티클의 위치로 이동할 수 있도록 속력값을 설정합니다.
	 * 마지막 파티클은 첫번째 파티클의 위치값을 참조합니다.
	 */
	public Effect velocityToAfterPoint()
	{
		for (int i = 0; i < points.size(); i++) {
			Vector gap = points.get((i + 1) % points.size())
					.clone().subtract(points.get(i));
			Vector vel = velocities.get(i);
			vel.setX(gap.getX()).setY(gap.getY()).setZ(gap.getZ()).normalize();
		}
		return this;
	}

}
