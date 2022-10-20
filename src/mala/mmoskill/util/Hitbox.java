package mala.mmoskill.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;


public class Hitbox
{
	public static Vector[] Random_Location_In_the_Box(Vector _box_pos, Vector _box_rot, Vector _box_size, int _count)
	{
		Vector[] pos = new Vector[_count];
		for(int i = 0; i < pos.length; i++)
			pos[i] = new Vector(
					_box_size.getX() * -0.5 + Math.random() * _box_size.getX(),
					_box_size.getY() * -0.5 + Math.random() * _box_size.getY(),
					_box_size.getZ() * -0.5 + Math.random() * _box_size.getZ());
		
		// 포지션 역회전
		// 상자는 이미 역회전이 되어 있는 상태이므로 타겟의 포지션만 고려한다
		pos = TRS.Rotate_Z(pos, -_box_rot.getZ());
		pos = TRS.Rotate_Y(pos, -_box_rot.getY());
		pos = TRS.Rotate_X(pos, -_box_rot.getX());

		for(int i = 0; i < pos.length; i++)
			pos[i].add(_box_pos);
		return pos;
	}
	public static List<Entity> Targets_In_the_Box(Vector _box_pos, Vector _box_rot, Vector _box_size, List<Entity> _targets)
	{
		BOX box = new BOX(_box_size);
		Vector[] pos = new Vector[_targets.size()];
		for(int i = 0; i < _targets.size(); i++)
			pos[i] = _targets.get(i).getLocation().toVector().subtract(_box_pos);
		
		// 포지션 역회전
		// 상자는 이미 역회전이 되어 있는 상태이므로 타겟의 포지션만 고려한다
		pos = TRS.Rotate_Z(pos, -_box_rot.getZ());
		pos = TRS.Rotate_Y(pos, -_box_rot.getY());
		pos = TRS.Rotate_X(pos, -_box_rot.getX());

		List<Entity> result = new ArrayList<Entity>();
		for(int i = 0; i < _targets.size(); i++)
		{
			if (box.Position_Check(pos[i]))
				result.add(_targets.get(i));
		}
		
		return result;
	}
	public static List<Entity> Targets_In_the_BoundingBox(Vector _box_pos, Vector _box_rot, Vector _box_size, List<Entity> _targets)
	{
		BOX box = new BOX(_box_size);
		Vector[] pos = new Vector[_targets.size()];
		for(int i = 0; i < _targets.size(); i++)
			pos[i] = _targets.get(i).getLocation().toVector().subtract(_box_pos); // 포지션의 원점축을 박스로
		
		// 포지션 역회전
		// 상자는 이미 역회전이 되어 있는 상태이므로 타겟의 포지션만 고려한다
		pos = TRS.Rotate_Z(pos, -_box_rot.getZ());
		pos = TRS.Rotate_Y(pos, -_box_rot.getY());
		pos = TRS.Rotate_X(pos, -_box_rot.getX());

		List<Entity> result = new ArrayList<Entity>();
		for(int i = 0; i < _targets.size(); i++)
		{
			BoundingBox origin = _targets.get(i).getBoundingBox();
			Vector half_size = origin.getMax().subtract(origin.getMin()).multiply(0.5);
			Vector min = pos[i].clone().subtract(half_size);
			Vector max = pos[i].clone().add(half_size);
			origin = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
			
			if (box.Position_Check(origin))
				result.add(_targets.get(i));
		}
		
		return result;
	}
	public static boolean Is_BoundingBox_Collide_Other_BoundingBox(BoundingBox _a, BoundingBox _b)
	{
		try
		{
			BoundingBox bb = _a.intersection(_b);
			return bb.getVolume() > 0.0;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public static void Draw_hitbox(World world, Vector _box_pos, Vector _box_rot, Vector _box_size)
	{
		BOX box = new BOX(_box_size);
		
		Vector[] vecs = new Vector[8];
		vecs[0] = new Vector(box.left, box.up, box.front);
		vecs[1] = new Vector(box.right, box.up, box.front);
		vecs[2] = new Vector(box.left, box.down, box.front);
		vecs[3] = new Vector(box.right, box.down, box.front);
		vecs[4] = new Vector(box.left, box.up, box.back);
		vecs[5] = new Vector(box.right, box.up, box.back);
		vecs[6] = new Vector(box.left, box.down, box.back);
		vecs[7] = new Vector(box.right, box.down, box.back);
		
		vecs = TRS.Rotate_Z(vecs, _box_rot.getZ());
		vecs = TRS.Rotate_Y(vecs, _box_rot.getY());
		vecs = TRS.Rotate_X(vecs, _box_rot.getX());
		vecs = TRS.Translate(vecs, _box_pos.getX(), _box_pos.getY(), _box_pos.getZ());

		Location[] pos = new Location[8];
		for (int i = 0; i < pos.length; i++)
		{
			pos[i] = _box_pos.clone().add(new Vector(vecs[i].getX(), vecs[i].getY(), vecs[i].getZ())).toLocation(world);
		}
		Particle_Drawer.Draw_Line(pos[0], pos[1], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[1], pos[3], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[3], pos[2], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[2], pos[0], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);

		Particle_Drawer.Draw_Line(pos[0], pos[4], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[1], pos[5], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[2], pos[6], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[3], pos[7], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);

		Particle_Drawer.Draw_Line(pos[4], pos[5], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[5], pos[7], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[7], pos[6], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
		Particle_Drawer.Draw_Line(pos[6], pos[4], new DustOptions(Color.FUCHSIA, 1.0f), 0.25);
	}

}
