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

public class Mastery_Stance extends RegisteredSkill
{
	public static Mastery_Stance skill;
	
	public Mastery_Stance()
	{	
		super(new Mastery_Stance_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("bloodstack", new LinearValue(110, 10));
		addModifier("hp_max", new LinearValue(1.75, 1.75, 0, 30.0));
		addModifier("sta_sub", new LinearValue(2.5, 2.5, 0, 50.0));
		skill = this;
	}
	public static int Get_Max_Bloodstack(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 50;
		
		return (int)skill.getModifier("bloodstack", level);
	}
	public static double Get_HP_Max_Percentage(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0.0;
		
		return skill.getModifier("hp_max", level) * 0.01;
	}
	public static double Get_Sta_Percentage(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 100.0;
		
		return (1.0 - skill.getModifier("sta_sub", level) * 0.01);
	}
}

class Mastery_Stance_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Stance_Handler()
	{
		super(	"MASTERY_STANCE",
				"���Ľ� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&c[ ����ũ ���Ľ� ]",
				"&7������ &e{bloodstack}&7������ ���� �� �ֽ��ϴ�.",
				"&7HP�� &e{hp_max}&7% ���Ϸ� �������� HP �Ҹ���� �����˴ϴ�.",
				"&b[ �̺��̵� ���Ľ� ]",
				"&7���¹̳� �Ҹ��� &e{sta_sub}&7% �����մϴ�.");
	}
	
}
