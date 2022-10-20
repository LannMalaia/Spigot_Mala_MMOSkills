package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.MalaPassiveSkill;

public class Burn_Attack extends RegisteredSkill
{
	public Burn_Attack()
	{	
		super(new Burn_Attack_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(0, 0.5, 0, 20));
	}
}

class Burn_Attack_Handler extends MalaPassiveSkill implements Listener
{
	public Burn_Attack_Handler()
	{
		super(	"BURN_ATTACK",
				"화상통",
				Material.CAMPFIRE,
				"&7불타는 상대에게 {per}%의 추가 피해를 줍니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void passive_burn(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("BURN_ATTACK");
		
		// 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill(skill))
			return;

		if(event.getEntity().getFireTicks() <= 0)
			return;
		
		int level = data.getSkillLevel(skill);
		
		double per = skill.getModifier("per", level) * 0.01d;
		event.getDamage().multiplicativeModifier(1.0 + per);
	}
}
