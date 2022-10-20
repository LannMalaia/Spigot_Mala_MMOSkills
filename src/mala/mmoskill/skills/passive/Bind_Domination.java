package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Bind_Domination extends RegisteredSkill
{
	public Bind_Domination()
	{	
		super(new Bind_Domination_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(0.25, 0.25));
	}
}

class Bind_Domination_Handler extends MalaPassiveSkill implements Listener
{
	public Bind_Domination_Handler()
	{
		super(	"BIND_DOMINATION",
				"������ �ϰ�",
				Material.STRING,
				MsgTBL.NeedSkills,
				"&e ������ �ϰ� - lv.20",
				"",
				"&7������ �ϰ� ���߽� ������ �ӵ� ���� 2�� �ο��մϴ�.",
				"&7������� &e{second}&7�ʰ� ���ӵ˴ϴ�.");
	}
}
