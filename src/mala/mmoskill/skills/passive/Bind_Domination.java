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
				"구속의 일격",
				Material.STRING,
				MsgTBL.NeedSkills,
				"&e 제압의 일격 - lv.20",
				"",
				"&7제압의 일격 적중시 적에게 속도 감소 2를 부여합니다.",
				"&7디버프는 &e{second}&7초간 지속됩니다.");
	}
}
