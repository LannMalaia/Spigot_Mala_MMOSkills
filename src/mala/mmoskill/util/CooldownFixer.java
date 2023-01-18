package mala.mmoskill.util;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import mala.mmoskill.skills.Ancient;
import mala.mmoskill.skills.Recovery;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class CooldownFixer
{
	public static void Fix_Cooldown(PlayerData data, RegisteredSkill skill)
	{
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			if (cs.getSkill() != skill)
				continue;
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			double gap = (ci.getInitialCooldown() - ci.getRemaining()) * -0.001;
			ci.reduceFlat(gap);
		}
	}
	public static void Add_Cooldown(PlayerData data, RegisteredSkill skill, double sec)
	{
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			if (cs.getSkill() != skill)
				continue;
			data.getCooldownMap().applyCooldown(cs, sec);
//			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
//			if (ci == null) {
//				
//			}
//				continue;
//			double gap = (ci.getInitialCooldown() - ci.getRemaining()) * -0.001;
//			ci.reduceFlat(gap);
		}
	}
	public static void Initialize_Cooldown(PlayerData data, RegisteredSkill skill)
	{
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			if (cs.getSkill() != skill)
				continue;
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			ci.reduceFlat(999.0);
		}
	}
}
