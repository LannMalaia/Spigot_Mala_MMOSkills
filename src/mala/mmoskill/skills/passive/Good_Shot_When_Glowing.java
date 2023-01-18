package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Good_Shot_When_Glowing extends RegisteredSkill
{
	public Good_Shot_When_Glowing()
	{	
		super(new Good_Shot_When_Glowing_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(2, 2));
	}
}

class Good_Shot_When_Glowing_Handler extends MalaPassiveSkill implements Listener
{
	public Good_Shot_When_Glowing_Handler()
	{
		super(	"GOOD_SHOT_WHEN_GLOWING",
				"잘 보여야 잘 맞춘다",
				Material.GOLDEN_APPLE,
				MsgTBL.NeedSkills,
				"&e 치명적인 한 발 - lv.10",
				"",
				"&7발광 버프에 걸린 적을 공격시,",
				"&7치명적인 한 발의 발동 확률이 &e{per}&7% 추가로 상승합니다.");
	}
}
