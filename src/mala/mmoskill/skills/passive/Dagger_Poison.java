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
				"실명독 주머니",
				Material.POISONOUS_POTATO,
				"&7표창 공격이 실명을 겁니다.",
				"&7실명은 &8{second}&7초간 지속됩니다.",
				"&eLv.10&7에서 발동합니다.");
	}

}
