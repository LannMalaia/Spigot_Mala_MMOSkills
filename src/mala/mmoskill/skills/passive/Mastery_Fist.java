package mala.mmoskill.skills.passive;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;

public class Mastery_Fist extends RegisteredSkill
{
	public Mastery_Fist()
	{	
		super(new Mastery_Fist_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage", new LinearValue(0.5, 0.5));
		addModifier("percent", new LinearValue(2.0, 2.0));
	}
}

class Mastery_Fist_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Fist_Handler()
	{
		super(	"MASTERY_FIST",
				"���� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&7���� �� ���� ��� �������� �ʾ��� ��,",
				"&7�⺻ ������ �߰��� &e{damage}&7�� ���ظ� �ݴϴ�.",
				"&7��ų ���ط��� &e{percent}&7% �����մϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_fistmastery(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTERY_FIST");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// ������ üũ
		if (!Weapon_Identify.Has_No_Item(player))
			return;

		
		if (event.getDamage().hasType(DamageType.SKILL))
		{
			double per = skill.getModifier("percent", level);
			event.getDamage().multiplicativeModifier(1.0 + per * 0.01);
		}
		else
		{
			double dam = skill.getModifier("damage", level);
			event.getDamage().add(dam, DamageType.UNARMED);
		}
	}
}
