package mala.mmoskill.skills.passive;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;

public class Mastery_Axe extends RegisteredSkill
{
	public Mastery_Axe()
	{	
		super(new Mastery_Axe_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(1.25, 1.25));
	}
}

class Mastery_Axe_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Axe_Handler()
	{
		super(	"MASTERY_AXE",
				"액스 마스터리",
				Material.KNOWLEDGE_BOOK,
				"&7도끼, 둔기를 사용한 공격&스킬이",
				"&7추가로 &e{percent}&7%의 피해를 줍니다.",
				"&c기본 무기에는 적용되지 않습니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_swordmastery(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTERY_AXE");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// 아이템 체크
		if (!Weapon_Identify.Hold_Axe(player))
			return;
		
		double per = skill.getModifier("percent", level);
		event.getDamage().multiplicativeModifier(1.0 + per * 0.01);
	}
}
