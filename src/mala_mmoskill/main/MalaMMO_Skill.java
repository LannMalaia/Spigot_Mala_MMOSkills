package mala_mmoskill.main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.util.AntiCheat_Ignore;
import mala.mmoskill.util.Buff_Manager;
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
	
	@SuppressWarnings({ "resource" })
	void Add_Skills()
	{
		Bukkit.getConsoleSender().sendMessage("��b��ų �߰�");

		File skillFolder = new File("" + MMOCore.plugin.getDataFolder() + "/skills");
		if (!skillFolder.exists())
			skillFolder.mkdir();
		
		try
		{
			// MMOCore.plugin.skillManager.initialize(true);
			for(Enumeration<JarEntry> en = new JarFile(plugin.getFile()).entries(); en.hasMoreElements();)
			{
				JarEntry entry = en.nextElement();

				if(entry.getName().startsWith("mala/mmoskill/skills/") && !entry.isDirectory() && !entry.getName().contains("$"))
				{
					boolean not_include = false;
					for(Class<?> c : Class.forName(entry.getName().replace("/", ".").replace(".class", "")).getInterfaces())
					{
						if (!c.isAssignableFrom(RegisteredSkill.class))
							not_include = true;
//						if (c.isAssignableFrom(Not_Skill.class))
//							not_include = true;
//						if (c.isAssignableFrom(Runnable.class))
//							not_include = true;
					}
					if(not_include)
						continue;

					Class<?> skillclass = Class.forName(entry.getName().replace("/", ".").replace(".class", ""));
//					for (Field field : skillclass.getDeclaredFields())
//					{
//						Class<?> fieldtype = field.getType();
//						if (Collection.class.isAssignableFrom(fieldtype))
//						{
//							ParameterizedType fieldgenerictype = (ParameterizedType)field.getGenericType();
//							Class<?> classofcollection = null;
//							if (fieldgenerictype.getActualTypeArguments().length > 0)
//							{
//								Type generictype = fieldgenerictype.getActualTypeArguments()[0];
//								if (generictype != null)
//								{
//									if (generictype instanceof Class)
//									{
//										classofcollection = (Class<?>)generictype;
//									}
//									else if (generictype instanceof ParameterizedType)
//									{
//										Type rawtype = ((ParameterizedType)generictype).getRawType();
//										classofcollection = (Class<?>)rawtype;
//									}
//								}
//								fieldtype = classofcollection;
//							}
//						}
//						Bukkit.getConsoleSender().sendMessage(fieldtype.descriptorString() + " ����");
//					}
					Bukkit.getConsoleSender().sendMessage(skillclass.getSimpleName() + " ����");
					ConfigurationSection cs = getConfig();
					RegisteredSkill handler = (RegisteredSkill)Class.forName(
							entry.getName().replace("/", ".").replace(".class", "")).getDeclaredConstructor().newInstance();

//					File skillfile = new File("" + getDataFolder() + "/skills/" + handler.getLowerCaseId() + ".yml");
//					if (!skillfile.exists())
//						skillfile.createNewFile();
//					ConfigFile config = new ConfigFile("/skills", handler.getLowerCaseId());
//					if (!config.exists())
//					{
//						config.getConfig().set("name", cs.get("name"));
//						config.getConfig().set("lore", cs.getStringList("lore"));
//						config.getConfig().set("material", cs.get("material"));
//						for (Object mod : handler.getModifiers()) 
//						{
//							config.getConfig().set("" + mod + ".base", Integer.valueOf(0));
//							config.getConfig().set("" + mod + ".per-level", Integer.valueOf(0));
//							config.getConfig().set("" + mod + ".min", Integer.valueOf(0));
//							config.getConfig().set("" + mod + ".max", Integer.valueOf(0));
//						}
//						config.save();
//					}
					
					//for (Type t : ((ParameterizedType)obj.getClass().getGenericSuperclass()).getActualTypeArguments())
					//	Bukkit.getConsoleSender().sendMessage(t.getTypeName() + " ����");
					//SkillHandler<?> handler = (SkillHandler<?>)obj;
					MMOCore.plugin.skillManager.registerSkill(handler);
					
					//		(Skill)Class.forName(entry.getName().replace("/", ".").replace(".class", "")).newInstance()
					//		);
				}
			}
			MMOCore.plugin.classManager.initialize(true);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
}
