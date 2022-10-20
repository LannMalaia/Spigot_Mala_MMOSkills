package mala.mmoskill.util;

import org.bukkit.Bukkit;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Skill_Util
{
	public static boolean Has_Skill(PlayerData _data, String _skill_name, int _level)
	{
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill(_skill_name);
		if (skill == null)
		{
			Bukkit.getConsoleSender().sendMessage(_skill_name + "이란 스킬은 없는데요?");
			return false;
		}
		
		int lv = _data.getSkillLevel(skill);
		
		if(!_data.getProfess().hasSkill(skill))
			return false;
		if(lv < _level)
			return false;
		
		return true;
	}
}
