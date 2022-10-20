package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Mastery_Flag extends RegisteredSkill
{
	public Mastery_Flag()
	{	
		super(new Mastery_Flag_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(10.5, 0.5));
		addModifier("radius", new LinearValue(3.35, 0.35));
		addModifier("max", new LinearValue(1.05, 0.05, 1, 2));
	}
}

class Mastery_Flag_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Flag_Handler()
	{
		super(	"MASTERY_FLAG",
				"지휘술",
				Material.WHITE_BANNER,
				"&7지휘 깃발의 성능을 향상시킵니다.",
				"",
				"&f[ &e지휘 깃발 능력치 &f]",
				"&f&l[ &b지속 시간 &f{second} &f&l]",
				"&f&l[ &e범위 &f{radius} &f&l]",
				"&f&l[ &9최대 소환 가능 &f{max} &f&l]");
	}
}
