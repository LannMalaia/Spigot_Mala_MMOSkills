package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Mastery_Dog extends RegisteredSkill
{
	public static Mastery_Dog skill;
	
	public Mastery_Dog()
	{	
		super(new Mastery_Dog_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(5, 5));
		skill = this;
	}
	
	public static double Get_Percentage(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!data.getProfess().hasSkill(skill))
			return 1.0;
		return 1.0 + skill.getModifier("per", data.getSkillLevel(skill)) * 0.01;
	}
}

class Mastery_Dog_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Dog_Handler()
	{
		super(	"MASTERY_DOG",
				"조련술",
				Material.BONE,
				"&7탐색견, 사냥견, 투견의 HP, 공격력, 방어력이 &e{per}&7% 상승합니다.");
	}
}
