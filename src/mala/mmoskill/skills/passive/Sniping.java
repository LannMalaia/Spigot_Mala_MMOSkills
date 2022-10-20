package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Sniping extends RegisteredSkill
{
	public Sniping()
	{	
		super(new Sniping_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(0.8, 0.8));
	}
}

class Sniping_Handler extends MalaPassiveSkill implements Listener
{
	public Sniping_Handler()
	{
		super(	"SNIPING",
				"곡사술",
				Material.BOW,
				"&7투사체 기본 공격을 맞췄을 때, 대상과의 거리가",
				"&e20m&7보다 멀다면 &e{percent}&7%의 추가 피해를 줍니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_danger_pay(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Arrow)
			|| !event.getDamager().hasMetadata("malammo.skill.bow"))
			return;
		
		Arrow arrow = (Arrow)event.getDamager();
		Player player = (Player)arrow.getShooter();
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("SNIPING");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;
		
		// 거리 체크
		double distance = event.getEntity().getLocation().distance(player.getLocation());
		double per = 0.0;
		if (distance >= 20.0)
			per = skill.getModifier("percent", level);
		event.setDamage(event.getDamage() * (1.0 + per * 0.01));
	}
}

