package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Rose_Domination extends RegisteredSkill
{
	public Rose_Domination()
	{	
		super(new Rose_Domination_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(0.25, 0.25));
	}
}

class Rose_Domination_Handler extends MalaPassiveSkill implements Listener
{
	public Rose_Domination_Handler()
	{
		super(	"ROSE_DOMINATION",
				"������ �ϰ�",
				Material.ROSE_BUSH,
				MsgTBL.NeedSkills,
				"&e ������ �ϰ� - lv.20",
				"",
				"&7������ �ϰ� ���߽� ������ ��ȭ�� �õ��� �ο��մϴ�.",
				"&7������ &8{second}&7�ʰ� ���ӵ˴ϴ�.");
	}
}
