package mala_mmoskill.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public class DamagePreventer {

	public static HashMap<Player, Long> playerDamagedTimes = new HashMap<Player, Long>();
	
	public static void addLog(Player player, String log)
	{
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			directory = new File(directory, "damage_logger");
			if (!directory.exists())
				directory.mkdir();
			
			File saveto = new File(directory, player.getName() + ".yml");
			if (!saveto.exists())
				saveto.createNewFile();

			// 파일 작성
			FileConfiguration file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			ArrayList<String> list = (ArrayList<String>) file.getStringList("logs");
			if (list == null)
				list = new ArrayList<String>();
			list.add(log);
			file.set("logs", list);
			file.save(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
