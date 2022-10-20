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

public class Mastery_Heal extends RegisteredSkill
{
	public Mastery_Heal()
	{	
		super(new Mastery_Heal_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(7.5, 7.5));
	}
	
	public static double Get_Mult(Player player)
	{
		double result = 1.0;
		
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTERY_HEAL");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return result;
		
		return result + skill.getModifier("percent", level) * 0.01;
	}
}

class Mastery_Heal_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Heal_Handler()
	{
		super(	"MASTERY_HEAL",
				"���� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&7ȸ�� ������ �߰��� &e{percent}&7%��ŭ �� ȸ���˴ϴ�.");

	}
}
