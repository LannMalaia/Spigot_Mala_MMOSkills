package mala.mmoskill.skills.passive;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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

public class Nimble_Body_Evade extends RegisteredSkill
{
	public static Nimble_Body_Evade skill;
	
	public Nimble_Body_Evade()
	{	
		super(new Nimble_Body_Evade_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(1.5, 1.5));
		skill = this;
	}
	public static double Get_Evade_Percent(Player player)
	{
		// ��ų üũ
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 0;
		
		if (!(Skill_Util.Has_Skill(data, "MASTERY_STANCE", 10) && Skill_Util.Has_Skill(data, "NIMBLE_BODY", 10)))
			return 0;
		
		Stance_Type type = !player.hasMetadata(Stance_Change.meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(player.getMetadata(Stance_Change.meta_name).get(0).asString());
		if (type != Stance_Type.EVADE)
			return 0;
		
		return skill.getModifier("percent", level);
	}
}

class Nimble_Body_Evade_Handler extends MalaPassiveSkill implements Listener
{
	public Nimble_Body_Evade_Handler()
	{
		super(	"NIMBLE_BODY_EVADE",
				"ȸ�� ����",
				Material.DRIED_KELP,
				MsgTBL.NeedSkills,
				"&e ���Ľ� �����͸� - lv.10",
				"&e ������ - lv.10",
				"",
				"&b[ �̺��̵� ���Ľ� ]",
				"&7�������� �ߵ��� Ȯ���� �߰��� &e{percent}&7% ����մϴ�.");
	}
	
}
