package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Dagger_Poison extends RegisteredSkill
{
	public Dagger_Poison()
	{	
		super(new Dagger_Poison_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(2, 0.5));
	}
}

class Dagger_Poison_Handler extends MalaPassiveSkill implements Listener
{
	public Dagger_Poison_Handler()
	{
		super(	"DAGGER_POISON",
				"�Ǹ� �ָӴ�",
				Material.POISONOUS_POTATO,
				"&7ǥâ ������ �Ǹ��� �̴ϴ�.",
				"&7�Ǹ��� &8{second}&7�ʰ� ���ӵ˴ϴ�.",
				"&eLv.10&7���� �ߵ��մϴ�.");
	}

}
