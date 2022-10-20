package mala.mmoskill.skills.passive;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.PlayerKillEntityEvent;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;

public class Axe_Surge extends RegisteredSkill
{
	public Axe_Surge()
	{	
		super(new Axe_Surge_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("sta_heal", new LinearValue(1, 1));
	}
}

class Axe_Surge_Handler extends MalaPassiveSkill implements Listener
{
	public Axe_Surge_Handler()
	{
		super(	"AXE_SURGE",
				"���� ����ȭ",
				Material.GOLDEN_AXE,
				"&7����, �б⸦ ����� ����&��ų��",
				"&7���� ��ġ���� ��, &e{sta_heal}&7�� ���¹̳��� ȸ���մϴ�.",
				"&c�⺻ ���⿡�� ������� �ʽ��ϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_axesurge(PlayerKillEntityEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("AXE_SURGE");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// ������ üũ
		if (!Weapon_Identify.Hold_Axe(player))
			return;
		
		double heal = skill.getModifier("sta_heal", level);
		data.giveStamina(heal, UpdateReason.SKILL_REGENERATION);
	}
}
