package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mala.mmoskill.skills.Stance_Change;
import mala.mmoskill.skills.Stance_Change.Stance_Type;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Nimble_Body_Haste extends RegisteredSkill
{
	public static Nimble_Body_Haste skill;
	
	public Nimble_Body_Haste()
	{	
		super(new Nimble_Body_Haste_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(0.5, 0.5));
		addModifier("max_percent", new LinearValue(2.0, 2.0));
		skill = this;
	}
	public static double Get_Evade_Percent(Player player)
	{
		// 스킬 체크
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0;
		
		if (!Skill_Util.Has_Skill(data, "NIMBLE_BODY", 10))
			return 0;
		
		PotionEffect pe = player.getPotionEffect(PotionEffectType.SPEED);
		if (pe == null)
			return 0;
		
		double per = skill.getModifier("percent", level) * (pe.getAmplifier() + 1);
		return Math.min(per, skill.getModifier("max_percent", level));
	}
}

class Nimble_Body_Haste_Handler extends MalaPassiveSkill implements Listener
{
	public Nimble_Body_Haste_Handler()
	{
		super(	"NIMBLE_BODY_HASTE",
				"잽싼 몸놀림",
				Material.COD,
				MsgTBL.NeedSkills,
				"&e 날렵함 - lv.10",
				"",
				"&7날렵함이 발동할 확률이 &e속도 증가 버프 수준 * {percent}&7% 상승합니다.",
				"&7최대 &e{max_percent}&7%까지 상승할 수 있습니다.");
	}
	
}
