package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Blind_Critical extends RegisteredSkill
{
	public Blind_Critical()
	{	
		super(new Blind_Critical_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(6, 1, 5, 40));
	}
}

class Blind_Critical_Handler extends MalaPassiveSkill implements Listener
{
	public Blind_Critical_Handler()
	{
		super(	"BLIND_CRITICAL",
				"어둠 속의 비수",
				Material.ENDER_PEARL,
				MsgTBL.NeedSkills,
				"&e 어둠의 일격 - lv.10",
				"",
				"&7실명당한 적에게 무기 피해를 주었을 떄,",
				"&7피해량이 {per}% 상승합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void passive_blind_critical(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("BLIND_CRITICAL");

		// 무기 공격이 아니거나 스킬을 알고 있지 않으면 취소
		if (!event.getDamage().hasType(DamageType.WEAPON) || !data.getProfess().hasSkill(skill))
			return;
		
		// 실명 체크
		if (!event.getEntity().hasPotionEffect(PotionEffectType.BLINDNESS))
			return;

		int level = data.getSkillLevel(skill);
		if(!data.getProfess().hasSkill(skill) && level >= 10)
			return;
		
		double per = 1.0 + skill.getModifier("per", level) * 0.01d;
		
		event.getDamage().multiplicativeModifier(per);
	}

}
