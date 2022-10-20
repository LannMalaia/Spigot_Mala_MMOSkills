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

public class Mastery_Stance extends RegisteredSkill
{
	public static Mastery_Stance skill;
	
	public Mastery_Stance()
	{	
		super(new Mastery_Stance_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("bloodstack", new LinearValue(110, 10));
		addModifier("hp_max", new LinearValue(1.75, 1.75, 0, 30.0));
		addModifier("sta_sub", new LinearValue(2.5, 2.5, 0, 50.0));
		skill = this;
	}
	public static int Get_Max_Bloodstack(Player player)
	{
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 50;
		
		return (int)skill.getModifier("bloodstack", level);
	}
	public static double Get_HP_Max_Percentage(Player player)
	{
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0.0;
		
		return skill.getModifier("hp_max", level) * 0.01;
	}
	public static double Get_Sta_Percentage(Player player)
	{
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 100.0;
		
		return (1.0 - skill.getModifier("sta_sub", level) * 0.01);
	}
}

class Mastery_Stance_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Stance_Handler()
	{
		super(	"MASTERY_STANCE",
				"스탠스 마스터리",
				Material.KNOWLEDGE_BOOK,
				"&c[ 버서크 스탠스 ]",
				"&7혈흔을 &e{bloodstack}&7개까지 모을 수 있습니다.",
				"&7HP가 &e{hp_max}&7% 이하로 떨어지면 HP 소모없이 유지됩니다.",
				"&b[ 이베이드 스탠스 ]",
				"&7스태미나 소모량이 &e{sta_sub}&7% 감소합니다.");
	}
	
}
