package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Full_Armored extends RegisteredSkill
{
	public Full_Armored()
	{	
		super(new Full_Armored_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(0.35, 0.35));
	}
}

class Full_Armored_Handler extends MalaPassiveSkill implements Listener
{
	public Full_Armored_Handler()
	{
		super(	"FULL_ARMORED",
				"완전무장",
				Material.DIAMOND_CHESTPLATE,
				"&7다이아몬드나 네더라이트 소재의 갑옷을",
				"&7모든 슬롯에 착용했을 경우,",
				"&7받는 모든 피해가 {percent}% 감소합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	boolean Check_Armored(Player player)
	{
		for (ItemStack item : player.getInventory().getArmorContents())
		{
			if (item == null)
				return false;
			// player.sendMessage("debug = " + item.getType());
			switch (item.getType())
			{
			case DIAMOND_HELMET:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:
			case NETHERITE_HELMET:
			case NETHERITE_CHESTPLATE:
			case NETHERITE_LEGGINGS:
			case NETHERITE_BOOTS:
				break;
			default:
				return false;
			}
		}
		return true;
	}
	
	@EventHandler
	public void Full_Armored_Event(EntityDamageByEntityEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;
		
		if(event.isCancelled())
			return;
		
		Player player = (Player)event.getEntity();
		PlayerData data = PlayerData.get(player);

		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("Full_Armored");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;
		
		if (!Check_Armored(player))
			return;
		
		double reduce = 1.0 - skill.getModifier("percent", level) * 0.01d;
		
		event.setDamage(event.getDamage() * reduce);
	}
}
