package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Nimble_Body extends RegisteredSkill
{
	public Nimble_Body()
	{	
		super(new Nimble_Body_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(1.5, 1.5));
	}
}

class Nimble_Body_Handler extends MalaPassiveSkill implements Listener
{
	public Nimble_Body_Handler()
	{
		super(	"NIMBLE_BODY",
				"날렵함",
				Material.RABBIT_FOOT,
				"&e{percent}&7% 확률로 받는 피해를 반감합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void nimble_body(EntityDamageByEntityEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;
		
		if(event.isCancelled())
			return;
		
		Player player = (Player)event.getEntity();
		PlayerData data = PlayerData.get(player);

		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("NIMBLE_BODY");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		double per = (skill.getModifier("percent", level) + Nimble_Body_Evade.Get_Evade_Percent(player)) * 0.01d;
		if (Math.random() > per)
			return;
		
		event.getEntity().sendMessage("§b§l[ 날렵하게 움직여 적은 피해를 받았습니다. ]");
		event.setDamage(event.getDamage() * 0.5);
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 2f);
	}
}
