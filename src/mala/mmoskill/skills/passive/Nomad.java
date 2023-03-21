package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.events.SpellCastEvent;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Nomad extends RegisteredSkill
{
	public static Nomad skill;
	
	public Nomad()
	{	
		super(new Nomad_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(30.0, 0.0));
		skill = this;
	}
}

class Nomad_Handler extends MalaPassiveSkill implements Listener
{
	public Nomad_Handler()
	{
		super(	"NOMAD",
				"방랑자",
				Material.DEAD_BUSH,
				"&7주변 30m에 플레이어가 없을 경우,",
				"&7가하는 모든 피해량이 &c{percent}&7% 증가합니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack(PlayerAttackEvent event)
	{
		if (!Skill_Util.Has_Skill(PlayerData.get(event.getPlayer()), "NOMAD", 1))
			return;
		Player player = event.getPlayer();
		boolean restricted = false;
		for (Entity entity : player.getNearbyEntities(30, 30, 30)) {
			if (!(entity instanceof Player))
				continue;
			if (entity == player)
				continue;
			restricted = true;
		}
		if (!restricted) {
			PlayerData playerData = MMOCore.plugin.dataProvider.getDataManager().get(player);
			if (playerData == null)
				return;
			double per = Nomad.skill.getModifier("percent", playerData.getSkillLevel(Nomad.skill)) * 0.01;
			event.getAttack().getDamage().multiplicativeModifier(1.0 + per);
		}
	}
}
