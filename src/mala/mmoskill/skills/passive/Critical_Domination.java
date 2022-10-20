package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Critical_Domination extends RegisteredSkill
{
	public static Critical_Domination skill;
	
	public Critical_Domination()
	{	
		super(new Critical_Domination_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(0.25, 0.25));
		skill = this;
	}
	
	public static boolean Can_Critical(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!Skill_Util.Has_Skill(data, "DOUBLE_SHOT", 10)
			|| !Skill_Util.Has_Skill(data, "DOMINATE", 10))
			return false;
		if (!data.getProfess().hasSkill(skill))
			return false;
		return data.getSkillLevel(skill) >= 10;
	}
	public static boolean Can_Init_Dominate(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!Skill_Util.Has_Skill(data, "DOUBLE_SHOT", 10)
			|| !Skill_Util.Has_Skill(data, "DOMINATE", 10))
			return false;
		if (!data.getProfess().hasSkill(skill))
			return false;
		return data.getSkillLevel(skill) >= 20;
	}
}

class Critical_Domination_Handler extends MalaPassiveSkill implements Listener
{
	public Critical_Domination_Handler()
	{
		super(	"CRITICAL_DOMINATION",
				"사각에서의 저격",
				Material.DRIED_KELP,
				MsgTBL.NeedSkills,
				"&e 치명적인 한 발 - lv.10",
				"&e 제압의 일격 - lv.10",
				"",
				"&eLv.10 - 투사체 공격으로 제압의 일격 적중시,",
				"&e        1.5배의 피해를 줍니다.",
				"&eLv.20 - 치명적인 한 발 적중시 제압의 일격이 초기화됩니다.",
				"&e        다른 플레이어들의 제압의 일격도 초기화됩니다.");
	}
}
