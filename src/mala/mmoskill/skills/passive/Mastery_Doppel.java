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
				"분신 마스터리",
				Material.KNOWLEDGE_BOOK,
				"&7분신이 &e{second}&7초간 지속됩니다.",
				"&7최대 &e{max}&7개의 분신을 유지할 수 있습니다.",
				"&eLv.20 - 분신의 원본이 갑옷 거치대에서 늑대로 바뀝니다.",
				"",
				"&f[ &e분신 늑대 능력치 &f]",
				"&f&l[ &a생명 &f{hp} &f&l][ &e방어 &f{def} &f&l]",
				"&f&l[ &c공격 &f{atk} &f&l][ &b이동 &f{speed} &f&l]",
				"&e※ 분신의 공격력은 스킬 피해량 증가치에 따라 더 증가합니다.");
	}
}
