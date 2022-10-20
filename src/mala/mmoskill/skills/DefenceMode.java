package mala.mmoskill.skills;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class DefenceMode extends RegisteredSkill
{
	public DefenceMode()
	{	
		super(new DefenceMode_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("protect_tier", new LinearValue(1.1, 0.1, 1, 2));
		addModifier("slow_tier", new LinearValue(3.8, 0.2, 1, 4));
		addModifier("protect_second", new LinearValue(20, 1.5));
		addModifier("slow_second", new LinearValue(40, -2, 0, 100));
		addModifier("cooldown", new LinearValue(60, 0));
		addModifier("mana", new LinearValue(15, 0));
	}
}

class DefenceMode_Handler extends MalaSkill implements Listener
{
	public DefenceMode_Handler()
	{
		super(	"DEFENCEMODE",
				"방어태세",
				Material.SHIELD,
				"&7자신에게 저항과 함께 속도 감소 버프를 부여합니다.",
				"&7저항은 &8{protect_tier}&7 등급이 &8{protect_second}&7초,",
				"&7속도 감소는 &8{slow_tier}&7 등급이 &8{slow_second}&7초 지속됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		LivingEntity target = null;
		int protect_tier = 0; // 피해 증가치
		int protect_second = 0;
		int slow_tier = 0; // 피해 증가치
		int slow_second = 0;
		target = data.getPlayer();
		protect_tier = (int) cast.getModifier("protect_tier") - 1; // 피해 증가치
		protect_second = (int) cast.getModifier("protect_second") * 20; // 피해 증가치
		slow_tier = (int) cast.getModifier("slow_tier") - 1; // 피해 증가치
		slow_second = (int) cast.getModifier("slow_second") * 20; // 피해 증가치

		Buff_Manager.Add_Buff(target, PotionEffectType.DAMAGE_RESISTANCE, protect_tier, protect_second, PotionEffectType.WEAKNESS);
		Buff_Manager.Add_Buff(target, PotionEffectType.SLOW, slow_tier, slow_second, PotionEffectType.SPEED);
	
		target.getWorld().playSound(target.getEyeLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
	}
}
