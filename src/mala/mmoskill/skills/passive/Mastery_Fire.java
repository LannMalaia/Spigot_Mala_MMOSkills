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


public class Mastery_Fire extends RegisteredSkill
{
	public Mastery_Fire()
	{	
		super(new Mastery_Fire_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(3, 3));
	}
	
	public static double Get_Mult(Player player)
	{
		double result = 1.0;
		
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTERY_FIRE");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return result;
		
		return result + skill.getModifier("percent", level) * 0.01;
	}
}

class Mastery_Fire_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Fire_Handler()
	{
		super(	"MASTERY_FIRE",
				"���̾� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&7ȭ�� ������ �߰��� &e{percent}&7%�� ���ظ� �ݴϴ�.");
	}
}