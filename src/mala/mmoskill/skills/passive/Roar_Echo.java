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
				"함성의 메아리",
				Material.GHAST_TEAR,
				MsgTBL.NeedSkills,
				"&e 외침 - lv.10",
				"",
				"&7외침을 사용했을 때,",
				"&7몬스터들에게 약화 버프를 &e{second}&7초간 부여합니다.",
				"&7아군의 경우, 힘 버프를 가지고 있다면 한 단계 상승시킵니다.",
				"&7힘 버프는 최대 &e{power}&7단계까지 상승됩니다.");
	}
}
