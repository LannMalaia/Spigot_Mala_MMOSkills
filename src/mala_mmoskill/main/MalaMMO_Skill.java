package mala_mmoskill.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.ArrowSkill_Manager;
import mala.mmoskill.manager.ArrowTip;
import mala.mmoskill.manager.CastSpellSkill_Manager;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.util.AntiCheat_Ignore;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.ImageReader;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Animal_Ignore;
import mala_mmoskill.class_version.Manager_April;
import mala_mmoskill.class_version.Manager_Version;
import me.blackvein.quests.libs.mysql.cj.x.protobuf.MysqlxCrud.Collection;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.ConfigFile;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.MMOCoreUtils;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.milkbowl.vault.economy.Economy;

public class MalaMMO_Skill extends JavaPlugin
{
	public static MalaMMO_Skill plugin;
	public static Economy econ = null;
	
	@Override
	public void onEnable()
	{
		int build = 4;
		String msg = ChatColor.AQUA + "������ mmocore ��ų Ȯ�� Ȱ��ȭ!\n���� - " + build;
		Bukkit.getConsoleSender().sendMessage(msg);
		plugin = this;
		
		Add_Skills();

		MMOCore.plugin.classManager.initialize(true);

		if (!setupEconomy())
		{
			getServer().getConsoleSender().sendMessage("���ڳ�� �ñ� ������? �ȵǴµ�...");
		}
		// Bukkit.getConsoleSender().sendMessage("��b���� ��ų ���");
		// for(Skill s : MMOCore.plugin.skillManager.getActive())
		//	Bukkit.getConsoleSender().sendMessage(s.getId());

		Buff_Manager.Run();
		Summon_Manager.Get_Instance();
		ArrowSkill_Manager.Get_Instance();
		CastSpellSkill_Manager.Get_Instance();
		
		Bukkit.getPluginManager().registerEvents(new Master_Event(), MalaMMO_Skill.plugin);
		Bukkit.getPluginManager().registerEvents(new AntiCheat_Ignore(), MalaMMO_Skill.plugin);
		Bukkit.getPluginManager().registerEvents(new Skill_Animal_Ignore(), MalaMMO_Skill.plugin);
		Bukkit.getPluginManager().registerEvents(new Manager_Version(), MalaMMO_Skill.plugin);
		
		/*
		Bukkit.getConsoleSender().sendMessage("��b���� Ŭ���� ���");		
		for(PlayerClass c : MMOCore.plugin.classManager.getAll())
		{
			Bukkit.getConsoleSender().sendMessage(c.getId());
		}
		
		Bukkit.getConsoleSender().sendMessage("��b�α� ��ų");
		for(SkillInfo s : MMOCore.plugin.classManager.get("ROGUE").getSkills())
			Bukkit.getConsoleSender().sendMessage(s.getSkill().getName());
		*/
	}
	
	@Override
	public void onDisable()
	{
		Bukkit.getConsoleSender().sendMessage("���� mmocore ��ų Ȯ�� bȰ��ȭ!");
	}

	private boolean setupEconomy()
	{
		if (getServer().getPluginManager().getPlugin("Vault") == null)
		{
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null)
			return false;
		econ = rsp.getProvider();
		return rsp.getProvider() != null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (cmd.getName().equalsIgnoreCase("MM_ChangeClass"))
		{
			if (!sender.hasPermission("*"))
				return true;
			
			if (args.length == 2)
			{
				Player player = Bukkit.getPlayer(args[0]);
				String changed_class_id = args[1];
				if (player != null)
				{
					Change_Player_Class(player, changed_class_id);
					Reset_Player_Skills(player);
					Reset_Player_Attributes(player);
				}
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("MM_SkillReAlloc"))
		{
			if (!sender.hasPermission("*"))
				return true;
				
			if (args.length == 0 && sender instanceof Player)
				Reset_Player_Skills((Player)sender);
			else if (args.length == 1)
			{
				Player player = Bukkit.getPlayer(args[0]);
				if (player != null)
				{
					sender.sendMessage("��b" + player.getName() + "���� ��ų�� �ʱ�ȭ�߽��ϴ�.");
					Reset_Player_Skills(player);
				}
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("MM_StatReAlloc"))
		{
			if (!sender.hasPermission("*"))
				return true;
			
			if (args.length == 0 && sender instanceof Player)
				Reset_Player_Attributes((Player)sender);
			else if (args.length == 1)
			{
				Player player = Bukkit.getPlayer(args[0]);
				if (player != null)
				{
					sender.sendMessage("��b" + player.getName() + "���� ������ �ʱ�ȭ�߽��ϴ�.");
					Reset_Player_Attributes(player);
				}
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("MM_ImageTest"))
		{
			if (!sender.hasPermission("*"))
				return true;
			
			if (args.length == 1 && sender instanceof Player)
			{
				Player player = (Player)sender;
				player.sendMessage("��b[ �̹��� �׽�Ʈ ���� ]");
				boolean[][] img = ImageReader.readImage(args[0]);
				double scale = 0.1;
				Location loc = player.getLocation().clone().subtract(img.length * 0.5 * scale, 0, img.length * 0.5 * scale);
				DustTransition dts = new DustTransition(Color.ORANGE, Color.YELLOW, 0.5f);
				Bukkit.getScheduler().runTask(this, new Runnable() {
					int count = 0;
					public void run() {
						for (int y = 0; y < img.length; y += 2) {
							for (int x = 0; x < img[y].length; x += 2) {
								if (img[y][x] == true)
								{
									Location ptLoc = loc.clone().add(x * scale, 0, y * scale);
									ptLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, ptLoc, 1, 0, 0, 0, 0);
								}
							}
						}
						if (count++ < 20)
							Bukkit.getScheduler().runTaskLater(plugin, this, 5);
					}
				});
			}
			return true;
		}
//		if (cmd.getName().equalsIgnoreCase("MM_AprilReset"))
//		{
//			if (args.length == 0 && sender instanceof Player)
//			{
//				Player player = (Player)sender;
//				int count = Manager_April.loadPlayerReset(player);
//				if (count < 2)
//				{
//					Manager_April.savePlayerReset(player, ++count);
//					Reset_Player_Attributes((Player)sender);
//					Reset_Player_Skills((Player)sender);
//					sender.sendMessage("��b��ų�� ������ �ʱ�ȭ�߽��ϴ�.");
//					sender.sendMessage("��b������ " + (2 - count) + "ȸ �ʱ�ȭ�� �����մϴ�.");
//				}
//				else
//					sender.sendMessage("��c�� �̻� �ʱ�ȭ �� �� �����ϴ�.");
//			}
//			return true;
//		}
		return false;
	}
	  
	void Change_Player_Class(Player player, String class_id)
	{
		PlayerData p_data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		int level = p_data.getLevel();
		p_data.setClass(MMOCore.plugin.classManager.get(class_id));
		p_data.setLevel(level);
		
		player.sendMessage("��bŬ������ ��c��l" + p_data.getProfess().getName() + "��b(��)�� ����Ǿ����ϴ�.");
		
	}
	
	void Reset_Player_Attributes(Player player)
	{
		PlayerData p_data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		int point_count = Math.max(0, p_data.getLevel() - 1);
		p_data.getAttributes().getInstances().forEach(ins -> ins.setBase(0));
		p_data.setAttributePoints(point_count);
		player.sendMessage("��b������ �ʱ�ȭ�Ǿ����ϴ�.");
		player.sendMessage("��b" + point_count + "���� ���� ����Ʈ�� �����޾ҽ��ϴ�.");
		// for (PlayerAttribute pa : MMOCore.plugin.attributeManager.getAll());
	}
	  
	public static void Reset_Player_Skills(Player player)
	{
		PlayerData p_data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		int point_count = Math.max(0, p_data.getLevel() - 1);
		for (ClassSkill si : p_data.getProfess().getSkills())
		{
			// player.sendMessage("si = " + si.getSkill().getName());
			p_data.setSkillLevel(si.getSkill(), 1);
		}
		p_data.setSkillPoints(point_count);
		player.sendMessage("��b��ų�� �ʱ�ȭ�Ǿ����ϴ�.");
		player.sendMessage("��b" + point_count + "���� ��ų ����Ʈ�� �����޾ҽ��ϴ�.");
		// for (PlayerAttribute pa : MMOCore.plugin.attributeManager.getAll());
	}
	
	
	private ArrayList<RegisteredSkill> registeredSkills = new ArrayList<RegisteredSkill>();
	
	@SuppressWarnings({ "resource" })
	void Add_Skills()
	{
		
		if (registeredSkills.size() != 0) // �̹� �� �� �ҷ��� ���
		{
			Bukkit.getConsoleSender().sendMessage("��b��ų ���ε� - �� " + registeredSkills.size() + "���� ��ų");
			// MMOCore.plugin.skillManager.initialize(true);
		}
		else
		{
			Bukkit.getConsoleSender().sendMessage("��b��ų �߰�");
			File skillFolder = new File("" + MMOCore.plugin.getDataFolder() + "/skills");
			if (!skillFolder.exists())
				skillFolder.mkdir();
			
			try
			{
				for (Enumeration<JarEntry> en = new JarFile(plugin.getFile()).entries(); en.hasMoreElements();)
				{
					JarEntry entry = en.nextElement();
					
					if (entry.getName().startsWith("mala/mmoskill/skills/") && !entry.isDirectory() && !entry.getName().contains("$"))
					{
						boolean not_include = false;
						Class<?> skillclass = Class.forName(entry.getName().replace("/", ".").replace(".class", ""));
						
						if (skillclass.getModifiers() < 1) // public�� �ƴ� Ŭ������ �ҷ����� �ʴ´�
							not_include = true;
						if (not_include)
							continue;
						
						Bukkit.getConsoleSender().sendMessage(skillclass.getSimpleName() + " ����");
						ConfigurationSection cs = getConfig();
						RegisteredSkill handler = (RegisteredSkill)Class.forName(
								entry.getName().replace("/", ".").replace(".class", "")).getDeclaredConstructor().newInstance();
						registeredSkills.add(handler);
					}
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
		
		// ��ų���� �ҷ��´�
		for (RegisteredSkill skill : registeredSkills)
			MMOCore.plugin.skillManager.registerSkill(skill);

		// Ŭ������ �ٽ� �ҷ��´�
		MMOCore.plugin.classManager.initialize(true);
	}
}
