package mala.mmoskill.util;

import java.util.ArrayList;

import org.bukkit.util.Vector;

public class TRS
{
	static Vector Mult(double[][] mat, Vector vec)
	{
		double[] vec_mat = new double[4];
		vec_mat[0] = vec.getX();
		vec_mat[1] = vec.getY();
		vec_mat[2] = vec.getZ();
		vec_mat[3] = 1;
		
		double[] new_mat = new double[4];
		for(int j = 0; j < 4; j++)
		{
			for(int k = 0; k < 4; k++)
			{
				new_mat[j] += mat[k][j] * vec_mat[k];
			}
		}
		return new Vector(new_mat[0], new_mat[1], new_mat[2]);
	}

	public static Vector[] Translate(Vector[] origin_vecs, double x, double y, double z)
	{
		/*
		 * [ 1  0  0  x]
		 * [ 0  1  0  y]
		 * [ 0  0  1  z]
		 * [ 0  0  0  1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = 1;
		mat[0][3] = x;
		mat[1][1] = 1;
		mat[1][3] = y;
		mat[2][2] = 1;
		mat[2][3] = z;
		mat[3][3] = 1;
		
		Vector[] new_vec = new Vector[origin_vecs.length];
		for(int i = 0; i < origin_vecs.length; i++)
			new_vec[i] = Mult(mat, origin_vecs[i]);
		return new_vec;
	}
	public static ArrayList<Vector> Translate(ArrayList<Vector> origin_vecs, double x, double y, double z)
	{
		/*
		 * [ 1  0  0  x]
		 * [ 0  1  0  y]
		 * [ 0  0  1  z]
		 * [ 0  0  0  1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = 1;
		mat[0][3] = x;
		mat[1][1] = 1;
		mat[1][3] = y;
		mat[2][2] = 1;
		mat[2][3] = z;
		mat[3][3] = 1;
		
		ArrayList<Vector> new_vec = new ArrayList<Vector>();
		for(int i = 0; i < origin_vecs.size(); i++)
			new_vec.add(Mult(mat, origin_vecs.get(i)));
		return new_vec;
	}

	public static Vector[] Rotate_X(Vector[] origin_vecs, double angle)
	{
		/*
		 * [ 1  0    0   0]
		 * [ 0 cos -sin  0]
		 * [ 0 sin  cos  0]
		 * [ 0  0    0   1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = 1;
		mat[1][1] = Math.cos(Math.toRadians(angle));
		mat[2][1] = -Math.sin(Math.toRadians(angle));
		mat[1][2] = Math.sin(Math.toRadians(angle));
		mat[2][2] = Math.cos(Math.toRadians(angle));
		mat[3][3] = 1;
		
		Vector[] new_vec = new Vector[origin_vecs.length];
		for(int i = 0; i < origin_vecs.length; i++)
			new_vec[i] = Mult(mat, origin_vecs[i]);
		return new_vec;
	}
	public static ArrayList<Vector> Rotate_X(ArrayList<Vector> origin_vecs, double angle)
	{
		/*
		 * [ 1  0    0   0]
		 * [ 0 cos -sin  0]
		 * [ 0 sin  cos  0]
		 * [ 0  0    0   1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = 1;
		mat[1][1] = Math.cos(Math.toRadians(angle));
		mat[2][1] = -Math.sin(Math.toRadians(angle));
		mat[1][2] = Math.sin(Math.toRadians(angle));
		mat[2][2] = Math.cos(Math.toRadians(angle));
		mat[3][3] = 1;
		
		ArrayList<Vector> new_vec = new ArrayList<>();
		for(int i = 0; i < origin_vecs.size(); i++)
			new_vec.add(Mult(mat, origin_vecs.get(i)));
		return new_vec;
	}

	public static Vector[] Rotate_Y(Vector[] origin_vecs, double angle)
	{
		/*
		 * [ cos 0 sin 0]
		 * [  0  1  0  0]
		 * [-sin 0 cos 0]
		 * [  0  0  0  1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = Math.cos(Math.toRadians(angle));
		mat[2][0] = -Math.sin(Math.toRadians(angle));
		mat[1][1] = 1;
		mat[0][2] = Math.sin(Math.toRadians(angle));
		mat[2][2] = Math.cos(Math.toRadians(angle));
		mat[3][3] = 1;
		
		Vector[] new_vec = new Vector[origin_vecs.length];
		for(int i = 0; i < origin_vecs.length; i++)
			new_vec[i] = Mult(mat, origin_vecs[i]);
		return new_vec;
	}
	public static ArrayList<Vector> Rotate_Y(ArrayList<Vector> origin_vecs, double angle)
	{
		/*
		 * [ cos 0 sin 0]
		 * [  0  1  0  0]
		 * [-sin 0 cos 0]
		 * [  0  0  0  1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = Math.cos(Math.toRadians(angle));
		mat[2][0] = -Math.sin(Math.toRadians(angle));
		mat[1][1] = 1;
		mat[0][2] = Math.sin(Math.toRadians(angle));
		mat[2][2] = Math.cos(Math.toRadians(angle));
		mat[3][3] = 1;
		
		ArrayList<Vector> new_vec = new ArrayList<Vector>();
		for(int i = 0; i < origin_vecs.size(); i++)
			new_vec.add(Mult(mat, origin_vecs.get(i)));
		return new_vec;
	}

	public static Vector[] Rotate_Z(Vector[] origin_vecs, double angle)
	{
		/*
		 * [cos -sin 0 0]
		 * [sin  cos 0 0]
		 * [ 0    0  1 0]
		 * [ 0    0  0 1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = Math.cos(Math.toRadians(angle));
		mat[1][0] = -Math.sin(Math.toRadians(angle));
		mat[0][1] = Math.sin(Math.toRadians(angle));
		mat[1][1] = Math.cos(Math.toRadians(angle));
		mat[2][2] = 1;
		mat[3][3] = 1;
		
		Vector[] new_vec = new Vector[origin_vecs.length];
		for(int i = 0; i < origin_vecs.length; i++)
			new_vec[i] = Mult(mat, origin_vecs[i]);
		return new_vec;
	}
	public static ArrayList<Vector> Rotate_Z(ArrayList<Vector> origin_vecs, double angle)
	{
		/*
		 * [cos -sin 0 0]
		 * [sin  cos 0 0]
		 * [ 0    0  1 0]
		 * [ 0    0  0 1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = Math.cos(Math.toRadians(angle));
		mat[1][0] = -Math.sin(Math.toRadians(angle));
		mat[0][1] = Math.sin(Math.toRadians(angle));
		mat[1][1] = Math.cos(Math.toRadians(angle));
		mat[2][2] = 1;
		mat[3][3] = 1;
		
		ArrayList<Vector> new_vec = new ArrayList<>();
		for(int i = 0; i < origin_vecs.size(); i++)
			new_vec.add(Mult(mat, origin_vecs.get(i)));
		return new_vec;
	}

	public static Vector[] Scale(Vector[] origin_vecs, double x, double y, double z)
	{
		/*
		 * [ x  0  0  0]
		 * [ 0  y  0  0]
		 * [ 0  0  z  0]
		 * [ 0  0  0  1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = x;
		mat[1][1] = y;
		mat[2][2] = z;
		mat[3][3] = 1;
		
		Vector[] new_vec = new Vector[origin_vecs.length];
		for(int i = 0; i < origin_vecs.length; i++)
			new_vec[i] = Mult(mat, origin_vecs[i]);
		return new_vec;
	}
	public static ArrayList<Vector> Scale(ArrayList<Vector> origin_vecs, double x, double y, double z)
	{
		/*
		 * [ x  0  0  0]
		 * [ 0  y  0  0]
		 * [ 0  0  z  0]
		 * [ 0  0  0  1]
		 */
		double[][] mat = new double[4][4];
		mat[0][0] = x;
		mat[1][1] = y;
		mat[2][2] = z;
		mat[3][3] = 1;
		
		ArrayList<Vector> new_vec = new ArrayList<Vector>();
		for(int i = 0; i < origin_vecs.size(); i++)
			new_vec.add(Mult(mat, origin_vecs.get(i)));
		return new_vec;
	}

	public static Vector Reflect(Vector _dir, Vector _normal)
	{
		Vector temp_normal = _normal.clone().multiply(2.0);
		temp_normal.multiply(_dir.dot(_normal));
		return _dir.clone().add(temp_normal.multiply(-1));
	}
	public static double Get_Yaw_Degree(Vector _dir)
	{
		double x = _dir.getX();
		double z = _dir.getZ();
		
		double yaw = 0;
		if (x != 0)
		{
			if (x < 0)
				yaw = 1.5 * Math.PI;
			else
				yaw = 0.5 * Math.PI;
			yaw -= Math.atan(z / x);
		}
		else if (z < 0)
		{
			yaw = Math.PI;
		}
		return -yaw * 180 / Math.PI;// - 90;
		
		// return Math.toDegrees(Math.atan2(z, x));

	}
	public static double Get_Pitch_Degree(Vector _dir)
	{
		//double x = _dir.getX();
		//double y = _dir.getY();
		//double z = _dir.getZ();
		//return Math.toDegrees(Math.atan2(Math.sqrt(z * z + x * x), y) + Math.PI);
		return -Math.toDegrees(Math.asin(_dir.getY() / _dir.distance(new Vector())));
	}
}
