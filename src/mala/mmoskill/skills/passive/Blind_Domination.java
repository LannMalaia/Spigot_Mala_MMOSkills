package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Blind_Domination extends RegisteredSkill
{
	public Blind_Domination()
	{	
		super(new Blind_Domination_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(0.25, 0.25));
	}
}

class Blind_Domination_Handler extends MalaPassiveSkill implements Listener
{
	public Blind_Domination_Handler()
	{
		super(	"BLIND_DOMINATION",
				"����� �ϰ�",
				Material.DRIED_KELP,
				MsgTBL.NeedSkills,
				"&e ������ �ϰ� - lv.20",
				"",
				"&7������ �ϰ� ���߽� ������ �Ǹ��� �ο��մϴ�.",
				"&7�Ǹ��� &8{second}&7�ʰ� ���ӵ˴ϴ�.");
	}
}
