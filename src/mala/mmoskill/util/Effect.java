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
	protected ArrayList<Vector> points = new ArrayList<Vector>(); // ��ġ
	protected ArrayList<Vector> velocities = new ArrayList<Vector>(); // ����
	protected ArrayList<SoundData> sounds = new ArrayList<SoundData>(); // �Ҹ�
	
	public Effect(Location location, Particle particle)
	{
		this.location = location;
		this.particle = particle;
	}
	
	/**
	 * ����Ʈ�� �� ���� ǥ���մϴ�.
	 */
	public void playEffect()
	{
		// �Ҹ��� ������ �Ҹ� ���
		if (sounds.size() > 0)
		{
			for (SoundData soundData : sounds)
				location.getWorld().playSound(location, soundData.sound, soundData.volume, soundData.pitch);
		}
		
		// ��ƼŬ ���
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
	 * ����Ʈ�� �ð������� ǥ���մϴ�.
	 */
	public void playAnimation(int particlesPerTick)
	{
		// �Ҹ��� ������ �Ҹ� ���
		if (sounds.size() > 0)
		{
			for (SoundData soundData : sounds)
				location.getWorld().playSound(location, soundData.sound, soundData.volume, soundData.pitch);
		}
		
		// ��ƼŬ ���
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
	
	// �Ҹ� ����
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
	
	// ��ƼŬ (���彺��, �÷� ����Ʈ) ����
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
	 * ��ƼŬ�� �׷��� ������ �����մϴ�.
	 */
	public Effect setLocation(Location location)
	{
		this.location = location;
		return this;
	}
	
	// append -> ��ƼŬ ��ġ���� �߰�
	/**
	 * ��� �̹����� �׸��ϴ�. MalaMMO_Skill/images ���� ���� �̹��� ������ �ҷ��ɴϴ�.
	 * �̹��� ������ ������ 0.1��� ��ҵ˴ϴ�. �ػ󵵰� 400x400�� �׸��� ��� 40x40��� ũ�Ⱑ �˴ϴ�.
	 * ������ üũ�Ͽ� �ݿ��ϱ� ������ png ������ ����ؾ� �մϴ�.
	 * velocity�� �߽��� �������� �������� �����˴ϴ�.
	 * @param fileName �ҷ��� �̹����� �̸��Դϴ�. (ex: circle.png)
	 * @param skipCount (1 �̻����� �����ϼ���) �̹����� �ػ󵵰� �ʹ� ���� ��츦 �����, ���� �κ��� �ǳ� �ٸ鼭 �׸��ϴ�. ���ڰ� �������� ���� �׷����ϴ�.
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
	 * ��� ���� �׸��ϴ�.
	 * density�� �������� �׸��� �е��� �پ��ϴ�.
	 * @param radius ������
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
	 * ��� ���� ���� �׸��ϴ�.
	 * density�� �������� �׸��� �е��� �پ��ϴ�.
	 * @param radius ������
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
	 * ��� ȣ�� �׸��ϴ�.
	 * density�� �������� �׸��� �е��� �پ��ϴ�.
	 * @param radius ������
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
	 * ��� ������ �׸��ϴ�.
	 * density�� �������� �׸��� �е��� �پ��ϴ�.
	 * @param corners ���� ����
	 * @param radius ������
	 */
	public Effect append2DShape(int corners, double radius)
	{ return append2DShape(corners, radius, 1.0); }
	public Effect append2DShape(int corners, double radius, double density)
	{
		ArrayList<Vector> cornerList = new ArrayList<>();
		// ���� ���� (�簢���̸� 1->2->3->4->1�� 5���� ������ ����)
		for (int i = 0; i <= corners; i++)
		{
			double rad = Math.toRadians(360.0 / corners);
			cornerList.add(new Vector(Math.cos(rad * i) * radius, 0.0, Math.sin(rad * i) * radius));
		}
		// ������ ���� n���� ��
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
	 * ��� ������ �׸��ϴ�. ������ z������ ���ư��ϴ�.
	 * @param length ����
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

	// �� �׸���, ũ����Ż �� �� �� ���� �� ����
	
	/**
	 * ��ü ���� ���� �׸��ϴ�. ���Ⱑ �帣�µ��� ������ �� �� �ֽ��ϴ�.
	 * @param length ����
	 * @param width �ʺ�&����
	 * @param count ���� ����, �̰� �������� �׸��� ���� �������ϴ�.
	 */
	public Effect append3DLightningLine(double length, double width, int count)
	{ return append3DLightningLine(length, width, count, 1.0); }
	public Effect append3DLightningLine(double length, double width, int count, double density)
	{
		ArrayList<Vector> corners = new ArrayList<Vector>();
		ArrayList<Vector> vec = new ArrayList<Vector>();
		
		// ���� ����
		corners.add(new Vector(0, 0, 0));
		for (int i = 1; i < count; i++)
		{
			corners.add(new Vector(
					-width + Math.random() * width * 2.0,
					-width + Math.random() * width * 2.0,
					-width + Math.random() * width * 2.0 + (length / (double)count * (double)i)));
		}
		corners.add(new Vector(0, 0, length));
		
		// �׸� ���� ����
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
	 * ��ü ���� �׸��ϴ�.
	 * @param radius ������
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
	
	// TRS -> �̵�, ȸ��, ũ��
	/**
	 * ��ƼŬ ��ġ�� �����մϴ�. ������ �������� ��â��Ű�ų� �ϰ� ���� �� �����ϴ�.
	 */
	public Effect setPoint(double x, double y, double z)
	{
		for (Vector vector : points)
			vector.setX(x).setY(y).setZ(z);
		return this;
	}
	/**
	 * ��ƼŬ �ӷ��� �����մϴ�. ��ƼŬ�� Ư�� �������� ������ �̵���ų �� �ֽ��ϴ�.
	 */
	public Effect setVelocity(double x, double y, double z)
	{
		for (Vector vector : velocities)
			vector.setX(x).setY(y).setZ(z);
		return this;
	}
	/**
	 * Ư�� �������� �̵��մϴ�. ��ġ�� �ӷ� ��� �̵��մϴ�.
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
	 * Ư�� �������� ȸ���մϴ�. ��ġ�� �ӷ� ��� �̵��մϴ�.
	 * ������ ���ʺй��� �����ϴ�.
	 * @param x pitch, ���� ���Ʒ��� �����̱�
	 * @param y yaw, ���� ������ ������
	 * @param z roll, ���� ��Ʋ��
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
	 * Ư�� ũ�⸸ŭ ������ Ű��ų� ���Դϴ�. ��ġ�� �ӷ� ��� ������ �޽��ϴ�.
	 * 2.0 ���� �����ϸ� 2��� Ŀ���ϴ�.
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
	 * ��ƼŬ �迭 ������ �������ϴ�.
	 */
	public Effect reverse()
	{
		Collections.reverse(points);
		Collections.reverse(velocities);
		return this;
	}
	
	/**
	 * Ư�� ��ġ��ŭ �ڼ����ϴ�. ��ġ�� �ӷ� ��� ������ �޽��ϴ�.
	 * @param randomizeType add: min~max��ŭ ���մϴ�. multiply: min~max��ŭ ���մϴ�. multiply_linear: multiply�� ������ ���� �������� ����մϴ�.
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
	 * �� ��ƼŬ�� �ٷ� ���� ��ƼŬ�� ��ġ�� �̵��� �� �ֵ��� �ӷ°��� �����մϴ�.
	 * ������ ��ƼŬ�� ù��° ��ƼŬ�� ��ġ���� �����մϴ�.
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
