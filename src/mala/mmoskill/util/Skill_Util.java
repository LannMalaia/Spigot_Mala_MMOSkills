package mala.mmoskill.util;

import org.bukkit.Bukkit;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Skill_Util
{
	public static boolean Has_Skill(PlayerData data, String skillName, int level)
	{
		if (data == null)
			return false;
		
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill(skillName);
		if (skill == null)
		{
			Bukkit.getConsoleSender().sendMessage(skillName + "이란 스킬은 없는데요?");
			return false;
		}
		
		int lv = data.getSkillLevel(skill);
		
		if (!data.getProfess().hasSkill(skill))
			return false;
		if (data.getLevel() < data.getProfess().getSkill(skill).getUnlockLevel())
			return false;
		if (lv < level)
			return false;
		
		return true;
	}
}
