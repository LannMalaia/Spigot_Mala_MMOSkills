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
				"체인 라이트닝",
				Material.HORN_CORAL,
				"&7라이트닝 볼트가 주변 5m 내 적에게 총 &e{move_count}&7번 옮겨갑니다.",
				"&7옮겨붙을 때마다 피해량이 &e{dam_reduce}&7% 감소합니다.",
				"&7한 번 맞은 적에게는 옮겨붙지 않습니다.");
	}
}
