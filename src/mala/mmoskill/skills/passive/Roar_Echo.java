package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Roar_Echo extends RegisteredSkill
{
	public Roar_Echo()
	{	
		super(new Roar_Echo_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(2, 2));
		addModifier("power", new LinearValue(3.25, 0.25, 2, 6));
	}
}

class Roar_Echo_Handler extends MalaPassiveSkill implements Listener
{
	public Roar_Echo_Handler()
	{
		super(	"ROAR_ECHO",
				"�Լ��� �޾Ƹ�",
				Material.GHAST_TEAR,
				MsgTBL.NeedSkills,
				"&e ��ħ - lv.10",
				"",
				"&7��ħ�� ������� ��,",
				"&7���͵鿡�� ��ȭ ������ &e{second}&7�ʰ� �ο��մϴ�.",
				"&7�Ʊ��� ���, �� ������ ������ �ִٸ� �� �ܰ� ��½�ŵ�ϴ�.",
				"&7�� ������ �ִ� &e{power}&7�ܰ���� ��µ˴ϴ�.");
	}
}
