package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Law_of_the_Jungle extends RegisteredSkill
{
	public Law_of_the_Jungle()
	{	
		super(new Law_of_the_Jungle_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(0.2, 0.2));
	}
}

class Law_of_the_Jungle_Handler extends MalaPassiveSkill implements Listener
{
	public Law_of_the_Jungle_Handler()
	{
		super(	"LAW_OF_THE_JUNGLE",
				"약육강식",
				Material.CARROT,
				"&7해로운 버프에 걸린 적에게",
				"&e버프의 수 * {percent}&7%의 추가 피해를 줍니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_swordmastery(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("LAW_OF_THE_JUNGLE");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		
		double per = skill.getModifier("percent", level) * Buff_Manager.Get_Debuff_Count(event.getEntity()) * 0.01;
		event.getDamage().multiplicativeModifier(1.0 + per * 0.01);
	}
}
