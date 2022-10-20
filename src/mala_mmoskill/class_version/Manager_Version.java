package mala_mmoskill.class_version;

import java.io.File;
import java.util.HashMap;

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
public class Manager_Version implements Listener
{
	HashMap<String, Integer> versions = new HashMap<String, Integer>();
	class Player_Version
	{
		public String name;
		public Integer version;
		
		public Player_Version(String _name, Integer _version)
		{
			name = _name; version = _version;
		}
	}
	
	public Manager_Version()
	{
		if (!Read_Version_File())
		{
			Save_Sample_File();
			Read_Version_File();
		}
	}
	
	void Save_Sample_File()
	{
		File saveto;
		FileConfiguration file;
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			saveto = new File(directory, "Class_Version.yml");
			if (!saveto.exists())
				saveto.createNewFile();

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			
			for (PlayerClass pc : MMOCore.plugin.classManager.getAll())
				file.set(pc.getName(), 1);
			
			file.save(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	boolean Read_Version_File()
	{
		File saveto;
		FileConfiguration file;
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			saveto = new File(directory, "Class_Version.yml");
			if (!saveto.exists())
				return false;

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			
			versions = new HashMap<String, Integer>();
			for (PlayerClass pc : MMOCore.plugin.classManager.getAll())
				versions.put(pc.getName(), file.getInt(pc.getName()));
			
			file.save(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	void Save_Version_File()
	{
		File saveto;
		FileConfiguration file;
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			saveto = new File(directory, "Class_Version.yml");
			if (!saveto.exists())
				saveto.createNewFile();

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			
			for (String name : versions.keySet())
				file.set(name, versions.get(name));
			
			file.save(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	Player_Version Read_Player_File(Player player)
	{
		File saveto;
		FileConfiguration file;
		String name = "";
		Integer version = 0;
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();

			directory = new File(directory, "PlayerClassVersions");
			if (!directory.exists())
				directory.mkdir();
			
			saveto = new File(directory, player.getUniqueId().toString() + ".yml");
			if (!saveto.exists())
				return null;

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);
			
			name = file.getString("class_name");
			version = file.getInt("class_version");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new Player_Version(name, version);
	}
	
	void Save_Player_File(Player player, Player_Version version)
	{
		File saveto;
		FileConfiguration file;
		try
		{
			// 폴더 설정
			File directory = MalaMMO_Skill.plugin.getDataFolder();
			if (!directory.exists())
				directory.mkdir();
			
			directory = new File(directory, "PlayerClassVersions");
			if (!directory.exists())
				directory.mkdir();
			
			saveto = new File(directory, player.getUniqueId().toString() + ".yml");
			if (!saveto.exists())
				saveto.createNewFile();

			file = YamlConfiguration.loadConfiguration(saveto);
			file.load(saveto);

			file.set("class_name", version.name);
			file.set("class_version", version.version);
			
			file.save(saveto);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@EventHandler
	public void When_Player_Joined(PlayerJoinEvent event)
	{
		/*
		// 데이터 취득
		PlayerData pd = PlayerData.get(event.getPlayer());
		// 클래스를 체크
		String class_name = pd.getRPG().getClassName();
		// 목록에 클래스가 없다면 클래스 등록
		if (!versions.containsKey(class_name))
		{
			versions.put(class_name, 1);
			Save_Version_File();
		}
		Player_Version pv = Read_Player_File(event.getPlayer());
		if (pv == null)
		{
			pv = new Player_Version(class_name, versions.get(class_name));
		}
		// 클래스를 비교
		if (!pv.name.equals(class_name))
		{
			pv.name = class_name;
			pv.version = versions.get(class_name);
		}
		else
		{
			// 버전을 비교
			if (pv.version != versions.get(class_name))
			{
				MalaMMO_Skill.plugin.Reset_Player_Skills(event.getPlayer());
				event.getPlayer().sendMessage("§b[§c!§b] 클래스 스킬 업데이트로 인해 투자한 스킬이 초기화되었습니다.");
				pv.version = versions.get(class_name);
			}
		}
		Save_Player_File(event.getPlayer(), pv);
		*/
	}

	@EventHandler
	public void When_Player_Joined2(PlayerJoinEvent event)
	{
		// 데이터 취득
		PlayerData pd = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		int level = pd.getLevel(); // 현재 레벨
		int skillpoints = pd.getSkillPoints(); // 잔여 포인트
		int skill_level_sum = 0; // 투자한 포인트
		for (ClassSkill si : pd.getProfess().getSkills())
			skill_level_sum += pd.getSkillLevel(si.getSkill()) - 1;
		int target_skillpoints = level - 1 - skillpoints; // 스킬 포인트 총량
		int final_give_point = target_skillpoints - skill_level_sum;
		if (final_give_point > 0)
		{
			event.getPlayer().sendMessage("§b[§c!§b] 스킬 패치로 잃어버린 스킬에 대한 포인트를 돌려받았습니다.");
			pd.giveSkillPoints(final_give_point);
		}
	}
}
