package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.skills.Stance_Change;
import mala.mmoskill.skills.Stance_Change.Stance_Type;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Gathering_Strike extends RegisteredSkill
{
	public static Gathering_Strike skill;
	public Gathering_Strike()
	{	
		super(new Gathering_Strike_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("radius", new LinearValue(4.3, 0.3));
		skill = this;
	}

	public static boolean Can_Gathering(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return false;
		
		if (!Skill_Util.Has_Skill(data, "POWERFUL_STRIKE", 10))
			return false;
		
		return level >= 5;
	}
	public static double Get_Radius(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 4.0;
		
		if (!Skill_Util.Has_Skill(data, "POWERFUL_STRIKE", 10))
			return 4.0;
		
		return skill.getModifier("radius", level);
	}
	public static boolean Can_AnyWeapon(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return false;
		
		if (!Skill_Util.Has_Skill(data, "POWERFUL_STRIKE", 10))
			return false;
		
		return level >= 15;
	}
	public static boolean Is_WeaponType(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return false;
		
		if (!Skill_Util.Has_Skill(data, "POWERFUL_STRIKE", 10))
			return false;
		
		return level >= 20;
	}
}

class Gathering_Strike_Handler extends MalaPassiveSkill implements Listener
{
	public Gathering_Strike_Handler()
	{
		super(	"GATHERING_STRIKE",
				"���� ���",
				Material.TARGET,
				MsgTBL.NeedSkills,
				"&e ���Ÿ - lv.10",
				"",
				"&7���Ÿ�� ������ ����ŵ�ϴ�.",
				"&eLv.5 - ���Ÿ�� ������ ���� ������ �� ������ �����ϴ�.",
				"&eLv.10 - ���Ÿ�� ������ &b{radius}&em�� �����մϴ�.",
				"&eLv.15 - ���Ÿ�� ����� �� �� �̻� ���� ������ ���� �ʽ��ϴ�.",
				"&eLv.20 - ���Ÿ�� ���� ��ų ���ذ� �ƴ� ���� ���� ���ظ� �ݴϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
}
