package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Mastery_Lightning extends RegisteredSkill
{
	public Mastery_Lightning()
	{	
		super(new Mastery_Lightning_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(3, 3));
	}
	
	public static double Get_Mult(Player player)
	{
		double result = 1.0;
		
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTERY_LIGHTNING");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return result;
		
		return result + skill.getModifier("percent", level) * 0.01;
	}
}

class Mastery_Lightning_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Lightning_Handler()
	{
		super(	"MASTERY_LIGHTNING",
				"라이트닝 마스터리",
				Material.KNOWLEDGE_BOOK,
				"&7전격 마법이 추가로 &e{percent}&7%의 피해를 줍니다.");
	}
}

