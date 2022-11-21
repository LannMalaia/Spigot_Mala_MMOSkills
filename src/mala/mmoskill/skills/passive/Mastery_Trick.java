package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Mastery_Trick extends RegisteredSkill
{
	public static Mastery_Trick skill;
	
	public Mastery_Trick()
	{	
		super(new Mastery_Trick_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("stack", new LinearValue(11.5, 1.5));
		addModifier("additive", new LinearValue(0.2, 0.2));
		addModifier("reduce", new LinearValue(0.2, 0.2, 0, 4));
		skill = this;
	}
	public static int Get_Max_Stack(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 50;
		
		return (int)skill.getModifier("stack", level);
	}
	public static int Get_Stack_Additive(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0;
		
		return (int)skill.getModifier("additive", level);
	}
	public static int Get_Reduce(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0;
		
		return (int)skill.getModifier("reduce", level);
	}
}

class Mastery_Trick_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Trick_Handler()
	{
		super(	"MASTERY_TRICK",
				"Ʈ�� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&7��ų ������ �߰��� &e{additive}&7���� ���踦 ȹ���մϴ�.",
				"&7�ִ� ���跮�� &e{stack}&7���� �����մϴ�",
				"&7�ǰݽ� ���跮 ����ġ�� &e{reduce}&7 �����մϴ�.");
	}
	
}
