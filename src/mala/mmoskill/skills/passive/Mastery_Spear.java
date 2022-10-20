package mala.mmoskill.skills.passive;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;

public class Mastery_Spear extends RegisteredSkill
{
	public Mastery_Spear()
	{	
		super(new Mastery_Spear_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(4, 4));
		addModifier("percent_2", new LinearValue(2, 2));
	}
}

class Mastery_Spear_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Spear_Handler()
	{
		super(	"MASTERY_SPEAR",
				"���Ǿ� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&7â�� ����� ������",
				"&7�߰��� &e{percent}&7%�� ���ظ� �ݴϴ�.",
				"&7â�� ����� ��ų��",
				"&7�߰��� &e{percent_2}&7%�� ���ظ� �ݴϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_spearmastery(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTERY_SPEAR");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// ������ üũ
		if (!Weapon_Identify.Hold_Spear(player))
			return;
		
		double per = skill.getModifier("percent", level);

		if (event.getDamage().hasType(DamageType.SKILL))
		{
			per = skill.getModifier("percent_2", level);
		}
		
		event.getDamage().multiplicativeModifier(1.0 + per * 0.01);
	}
}
