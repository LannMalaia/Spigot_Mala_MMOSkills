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
				"�ż�ȭ",
				Material.GLOWSTONE_DUST,
				"&7�ڽſ��� �ӵ� ���� {tier} ������ �ο��մϴ�.",
				"&7������ {second}�� �� ���ӵ˴ϴ�.",
				"&7���ӿ� �ɷ� �ִ� ���, �ش� ������� ������ ����ϴ�.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		LivingEntity target = null;
		int tier = 0; // ���� ����ġ
		int second = 0;
		target = data.getPlayer();
		tier = (int) cast.getModifier("tier") - 1; // ���� ����ġ
		second = (int) cast.getModifier("second"); // ���� ����ġ
		
		Haste_Handler.Haste_Target(target, tier, second * 20);
	}
}
