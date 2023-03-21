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
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;

public class Mastery_Whip extends RegisteredSkill
{
	public Mastery_Whip()
	{	
		super(new Mastery_Whip_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(8, 8));
	}
}

class Mastery_Whip_Handler extends MalaPassiveSkill implements Listener
{
	public Mastery_Whip_Handler()
	{
		super(	"MASTERY_WHIP",
				"�� �����͸�",
				Material.KNOWLEDGE_BOOK,
				"&7ä������ ��ų, ���� ������ �ƴ� ������ ���� ��",
				"&7�߰��� &e{percent}&7%�� ���ظ� �ݴϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_whipmastery(PlayerAttackEvent event)
	{
		Player player = event.getAttacker().getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// Ÿ�� üũ
		if (event.getDamage().hasType(DamageType.SKILL)
			|| event.getDamage().hasType(DamageType.MAGIC))
			return;
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MASTERY_WHIP");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;
		// ������ üũ
		if (!Weapon_Identify.Hold_MMO_Whip(player))
			return;
		
		double per = skill.getModifier("percent", level);
		event.getDamage().multiplicativeModifier(1.0 + per * 0.01);
	}
}
