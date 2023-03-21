package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Blooded_Axe extends RegisteredSkill
{
	public Blooded_Axe()
	{	
		super(new Blooded_Axe_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(0, 0.5, 0, 20));
	}
}

class Blooded_Axe_Handler extends MalaPassiveSkill implements Listener
{
	public Blooded_Axe_Handler()
	{
		super(	"BLOODED_AXE",
				"피의 도끼날",
				Material.GOLDEN_AXE,
				"&7도끼로 적을 쓰러트렸을 때,",
				"&7적 최대 생명력의 &e{per}%&7만큼 회복합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void passive_blooded_axe(EntityDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();
		if(killer == null)
			return;

		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(killer);
		ItemStack item = data.getPlayer().getInventory().getItemInMainHand();
		if(item == null)
			return;
		if(!Weapon_Identify.Hold_Axe(killer))
			return;
		
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("BLOODED_AXE");
		int level = data.getSkillLevel(skill);
		
		double per = skill.getModifier("per", level) * 0.01d;
		double heal = event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * per;
		double player_health = killer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		killer.setHealth(Math.min(player_health, killer.getHealth() + heal));
	}
}