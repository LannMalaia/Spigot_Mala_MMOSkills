package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Chain_Lightning extends RegisteredSkill
{
	public Chain_Lightning()
	{	
		super(new Chain_Lightning_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("dam_reduce", new LinearValue(59, -1, 20, 60));
		addModifier("move_count", new LinearValue(2.2, 0.2, 2, 10));
	}
}

class Chain_Lightning_Handler extends MalaPassiveSkill implements Listener
{
	public Chain_Lightning_Handler()
	{
		super(	"CHAIN_LIGHTNING",
				"ü�� ����Ʈ��",
				Material.HORN_CORAL,
				"&7����Ʈ�� ��Ʈ�� �ֺ� 5m �� ������ �� &e{move_count}&7�� �Űܰ��ϴ�.",
				"&7�Űܺ��� ������ ���ط��� &e{dam_reduce}&7% �����մϴ�.",
				"&7�� �� ���� �����Դ� �Űܺ��� �ʽ��ϴ�.");
	}
}
