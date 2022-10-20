package mala.mmoskill.util;

import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class BOX
{
	public double up, down, left, right, front, back;
	public Vector min, max;	
	public BoundingBox origin_box;
	
	public BOX(Vector _size)
	{
		up = _size.getY() * 0.5;
		down = _size.getY() * -0.5;
		right = _size.getX() * 0.5;
		left = _size.getX() * -0.5;
		front = _size.getZ() * 0.5;
		back = _size.getZ() * -0.5;

		min = new Vector(left, down, back);
		max = new Vector(right, up, front);
		origin_box = new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
	}
	
	public boolean Position_Check(Vector _target_pos)
	{
		return _target_pos.getY() <= up && _target_pos.getY() >= down
				&& _target_pos.getX() <= right && _target_pos.getX() >= left
				&& _target_pos.getZ() <= front && _target_pos.getZ() >= back;
	}
	public boolean Position_Check(BoundingBox _target_box)
	{
		try
		{
			origin_box.clone().intersection(_target_box);
			/*
			Bukkit.getConsoleSender().sendMessage("original_box_min = " + min.toString());
			Bukkit.getConsoleSender().sendMessage("original_box_max = " + max.toString());
			Bukkit.getConsoleSender().sendMessage("target_box_min = " + _target_box.getMin().toString());
			Bukkit.getConsoleSender().sendMessage("target_box_max = " + _target_box.getMax().toString());
			*/
		}
		catch(Exception e)
		{
			return false;
		}
		
		return true;
	}
}