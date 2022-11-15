package mala.mmoskill.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import mala_mmoskill.main.MalaMMO_Skill;

public class ImageReader
{
	private static HashMap<String, boolean[][]> cacheImage = new HashMap<>();
	private static HashMap<String, int[][]> cacheImageRGB = new HashMap<>();
	
	public static boolean[][] readImage(String fileName)
	{
		if (cacheImage.containsKey(fileName))
			return cacheImage.get(fileName);
		
		File dir = new File(MalaMMO_Skill.plugin.getDataFolder(), "images");
		try {
			BufferedImage image = ImageIO.read(new File(dir, fileName));
			boolean[][] imageArray = new boolean[image.getHeight()][image.getWidth()];
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					Color color = new Color(image.getRGB(x, y), true);
					imageArray[y][x] = color.getAlpha() > 0;
				}
			}
			cacheImage.put(fileName, imageArray);
			return imageArray;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static int[][] readImageRGB(String fileName)
	{
		if (cacheImageRGB.containsKey(fileName))
			return cacheImageRGB.get(fileName);
		
		File dir = new File(MalaMMO_Skill.plugin.getDataFolder(), "images");
		try {
			BufferedImage image = ImageIO.read(new File(dir, fileName));
			int[][] imageArray = new int[image.getHeight()][image.getWidth()];
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					Color color = new Color(image.getRGB(x, y), true);
					imageArray[y][x] = color.getAlpha() > 0 ? color.getRGB() : 0;
				}
			}
			cacheImageRGB.put(fileName, imageArray);
			return imageArray;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
