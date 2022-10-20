package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Critical_Domination extends RegisteredSkill
{
	public static Critical_Domination skill;
	
	public Critical_Domination()
	{	
		super(new Critical_Domination_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(0.25, 0.25));
		skill = this;
	}
	
	public static boolean Can_Critical(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!Skill_Util.Has_Skill(data, "DOUBLE_SHOT", 10)
			|| !Skill_Util.Has_Skill(data, "DOMINATE", 10))
			return false;
		if (!data.getProfess().hasSkill(skill))
			return false;
		return data.getSkillLevel(skill) >= 10;
	}
	public static boolean Can_Init_Dominate(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!Skill_Util.Has_Skill(data, "DOUBLE_SHOT", 10)
			|| !Skill_Util.Has_Skill(data, "DOMINATE", 10))
			return false;
		if (!data.getProfess().hasSkill(skill))
			return false;
		return data.getSkillLevel(skill) >= 20;
	}
}

class Critical_Domination_Handler extends MalaPassiveSkill implements Listener
{
	public Critical_Domination_Handler()
	{
		super(	"CRITICAL_DOMINATION",
				"�簢������ ����",
				Material.DRIED_KELP,
				MsgTBL.NeedSkills,
				"&e ġ������ �� �� - lv.10",
				"&e ������ �ϰ� - lv.10",
				"",
				"&eLv.10 - ����ü �������� ������ �ϰ� ���߽�,",
				"&e        1.5���� ���ظ� �ݴϴ�.",
				"&eLv.20 - ġ������ �� �� ���߽� ������ �ϰ��� �ʱ�ȭ�˴ϴ�.",
				"&e        �ٸ� �÷��̾���� ������ �ϰݵ� �ʱ�ȭ�˴ϴ�.");
	}
}
