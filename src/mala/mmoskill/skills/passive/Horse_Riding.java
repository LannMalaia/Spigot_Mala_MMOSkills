package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Horse_Riding extends RegisteredSkill
{
	public Horse_Riding()
	{	
		super(new Horse_Riding_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(1, 1));
	}
}

class Horse_Riding_Handler extends MalaPassiveSkill implements Listener
{
	public Horse_Riding_Handler()
	{
		super(	"HORSE_RIDING",
				"기마술",
				Material.IRON_HORSE_ARMOR,
				"&7말에 탑승하고 있을 때,",
				"&7가하는 모든 피해가 {percent}% 증가합니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	boolean Check_Riding(Player player)
	{
		if (player.getVehicle() == null)
			return false;
		if (player.getVehicle() instanceof Horse)
			return true;
		return false;
	}
	
	@EventHandler
	public void Horse_Riding_Event(PlayerAttackEvent event)
	{
		if (event.isCancelled())
			return;
		
		PlayerData data = PlayerData.get(event.getPlayer());

		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("Horse_Riding");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;
		
		if (!Check_Riding(event.getPlayer()))
			return;
		
		double mult = 1.0 + skill.getModifier("percent", level) * 0.01d;
		
		event.getDamage().multiplicativeModifier(mult);
	}

}
