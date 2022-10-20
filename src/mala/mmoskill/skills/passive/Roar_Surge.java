package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Roar_Surge extends RegisteredSkill
{
	public Roar_Surge()
	{	
		super(new Roar_Surge_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(2, 2));
		addModifier("power", new LinearValue(3.25, 0.25, 2, 6));
	}
}

class Roar_Surge_Handler extends MalaPassiveSkill implements Listener
{
	public Roar_Surge_Handler()
	{
		super(	"ROAR_SURGE",
				"�Լ��� ��簨",
				Material.SUGAR,
				MsgTBL.NeedSkills,
				"&e ��ħ - lv.10",
				"",
				"&7��ħ�� ������� ��,",
				"&7���͵鿡�� �ӵ� ���� ������ &e{second}&7�ʰ� �ο��մϴ�.",
				"&7�Ʊ��� ���, �̵� �ӵ� ���� ������ ������ �ִٸ� �� �ܰ� ��½�ŵ�ϴ�.",
				"&7�̵� �ӵ� ���� ������ �ִ� &e{power}&7�ܰ���� ��µ˴ϴ�.");
	}
}
