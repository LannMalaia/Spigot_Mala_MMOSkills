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
				"가시의 일격",
				Material.ROSE_BUSH,
				MsgTBL.NeedSkills,
				"&e 제압의 일격 - lv.20",
				"",
				"&7제압의 일격 적중시 적에게 약화와 시듦을 부여합니다.",
				"&7버프는 &8{second}&7초간 지속됩니다.");
	}
}
