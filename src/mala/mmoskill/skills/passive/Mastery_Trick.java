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

public class Mastery_Trick extends RegisteredSkill
{
	public static Mastery_Trick skill;
	
	public Mastery_Trick()
	{	
		super(new Mastery_Trick_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("stack", new LinearValue(11.5, 1.5));
		addModifier("additive", new LinearValue(0.2, 0.2));
		addModifier("reduce", new LinearValue(0.2, 0.2, 0, 4));
		skill = this;
	}
	public static int Get_Max_Stack(Player player)
	{
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 50;
		
		return (int)skill.getModifier("stack", level);
	}
	public static int Get_Stack_Additive(Player player)
	{
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0;
		
		return (int)skill.getModifier("additive", level);
	}
	public static int Get_Reduce(Player player)
	{
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0;
		
		return (int)skill.getModifier("reduce", level);
	}
}

class Mastery_Trick_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Trick_Handler()
	{
		super(	"MASTERY_TRICK",
				"트릭 마스터리",
				Material.KNOWLEDGE_BOOK,
				"&7스킬 시전시 추가로 &e{additive}&7개의 연계를 획득합니다.",
				"&7최대 연계량이 &e{stack}&7개로 증가합니다",
				"&7피격시 연계량 감소치가 &e{reduce}&7 감소합니다.");
	}
	
}
