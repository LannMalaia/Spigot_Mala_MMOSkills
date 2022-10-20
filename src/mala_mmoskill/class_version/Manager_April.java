package mala_mmoskill.class_version;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.PlayerClass;
import net.Indyuce.mmocore.skill.ClassSkill;

/**
 * @author jimja
 * @version 2021. 7. 3.
 * @apiNote 클래스 버전 관리자
 */
public class Manager_April implements Listener
{
	
	public static void savePlayerReset(Player player, int count)
	{
		File saveto;
		FileConfiguration file;
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			File dir2 = new File(directory, "AprilReset");
			if (!dir2.exists())
				dir2.mkdir();
			
			saveto = new File(dir2, player.getUniqueId().toString() + ".yml");
			if (!saveto.exists())
				saveto.createNewFile();

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			file.set("resetcount", count);
			file.save(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static int loadPlayerReset(Player player)
	{
		Calendar calendar = Calendar.getInstance();
		if (calendar.get(Calendar.YEAR) > 2022 || 
			(calendar.get(Calendar.MONTH) + 1) > 4)
			return 3;
		
		File saveto;
		FileConfiguration file;
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			File dir2 = new File(directory, "AprilReset");
			if (!dir2.exists())
				dir2.mkdir();
			
			saveto = new File(dir2, player.getUniqueId().toString() + ".yml");
			if (!saveto.exists())
			{
				return 0;
			}

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			return file.getInt("resetcount");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return 3;
	}
	
}
