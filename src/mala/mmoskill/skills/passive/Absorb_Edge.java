package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Absorb_Edge extends RegisteredSkill
{
	public Absorb_Edge()
	{	
		super(new Absorb_Edge_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("heal", new LinearValue(0.2, 0.2, 0, 10.0));
	}
}

class Absorb_Edge_Handler extends MalaPassiveSkill implements Listener
{
	public Absorb_Edge_Handler()
	{
		super(	"ABSORB_EDGE",
				"흡수의 일격",
				Material.RED_DYE,
				"&7적을 해치웠을 때, &cHP&7의 &e{heal}&7%를 회복합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_absord_edge(PlayerKillEntityEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("ABSORB_EDGE");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		double max_health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		double heal = max_health * skill.getModifier("heal", level) * 0.01;
		player.setHealth(Math.min(max_health, player.getHealth() + heal));
	}
}

