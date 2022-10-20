package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Mastery_Flag extends RegisteredSkill
{
	public Mastery_Flag()
	{	
		super(new Mastery_Flag_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(10.5, 0.5));
		addModifier("radius", new LinearValue(3.35, 0.35));
		addModifier("max", new LinearValue(1.05, 0.05, 1, 2));
	}
}

class Mastery_Flag_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Flag_Handler()
	{
		super(	"MASTERY_FLAG",
				"���ּ�",
				Material.WHITE_BANNER,
				"&7���� ����� ������ ����ŵ�ϴ�.",
				"",
				"&f[ &e���� ��� �ɷ�ġ &f]",
				"&f&l[ &b���� �ð� &f{second} &f&l]",
				"&f&l[ &e���� &f{radius} &f&l]",
				"&f&l[ &9�ִ� ��ȯ ���� &f{max} &f&l]");
	}
}
