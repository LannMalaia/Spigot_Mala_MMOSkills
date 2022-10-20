package mala.mmoskill.util;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import mala.mmoskill.skills.Ancient;
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
}
