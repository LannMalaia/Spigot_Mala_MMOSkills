package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Mastery_Doppel extends RegisteredSkill
{
	public Mastery_Doppel()
	{	
		super(new Mastery_Doppel_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(21, 1));
		addModifier("max", new LinearValue(2.4, 0.4));
		addModifier("hp", new LinearValue(10, 10));
		addModifier("def", new LinearValue(1.3, 0.3));
		addModifier("atk", new LinearValue(7.5, 1.5));
		addModifier("speed", new LinearValue(0.35, 0));
	}
}

class Mastery_Doppel_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Doppel_Handler()
	{
		super(	"MASTERY_DOPPEL",
				"�н� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&7�н��� &e{second}&7�ʰ� ���ӵ˴ϴ�.",
				"&7�ִ� &e{max}&7���� �н��� ������ �� �ֽ��ϴ�.",
				"&eLv.20 - �н��� ������ ���� ��ġ�뿡�� ����� �ٲ�ϴ�.",
				"",
				"&f[ &e�н� ���� �ɷ�ġ &f]",
				"&f&l[ &a���� &f{hp} &f&l][ &e��� &f{def} &f&l]",
				"&f&l[ &c���� &f{atk} &f&l][ &b�̵� &f{speed} &f&l]",
				"&e�� �н��� ���ݷ��� ��ų ���ط� ����ġ�� ���� �� �����մϴ�.");
	}
}
