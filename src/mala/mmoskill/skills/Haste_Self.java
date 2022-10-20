package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Haste_Self extends RegisteredSkill
{
	public Haste_Self()
	{	
		super(new Haste_Self_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("tier", new LinearValue(1.1, 0.1, 1, 4));
		addModifier("second", new LinearValue(20, 10, 20, 300));
		addModifier("cooldown", new LinearValue(30, 0));
		addModifier("stamina", new LinearValue(22, 2));
	}
}

class Haste_Self_Handler extends MalaSkill implements Listener
{
	public Haste_Self_Handler()
	{
		super(	"HASTE_SELF",
				"신속화",
				Material.GLOWSTONE_DUST,
				"&7자신에게 속도 증가 {tier} 버프를 부여합니다.",
				"&7버프는 {second}초 간 지속됩니다.",
				"&7구속에 걸려 있는 경우, 해당 디버프의 수준을 낮춥니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		LivingEntity target = null;
		int tier = 0; // 피해 증가치
		int second = 0;
		target = data.getPlayer();
		tier = (int) cast.getModifier("tier") - 1; // 피해 증가치
		second = (int) cast.getModifier("second"); // 피해 증가치
		
		Haste_Handler.Haste_Target(target, tier, second * 20);
	}
}
