package mala.mmoskill.skills.passive;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;

public class Sword_Chain extends RegisteredSkill
{
	public Sword_Chain()
	{	
		super(new Sword_Chain_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage", new LinearValue(0.0, 0.1));
	}
}

class Sword_Chain_Handler extends MalaPassiveSkill implements Listener
{
	public Sword_Chain_Handler()
	{
		super(	"SWORD_CHAIN",
				"���� Į��",
				Material.GOLDEN_SWORD,
				"&7�ܰ�, ������ ���� ������ ������",
				"&e{damage}&7�� �߰� ���� ���ظ� �ݴϴ�.",
				"&7�� ���ش� ������ ������ ����ؼ� �����մϴ�.",
				"&c�÷��̾�Դ� ������� �ʽ��ϴ�.",
				"&c�⺻ ���⿡�� ������� �ʽ��ϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_swordchain(PlayerAttackEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof Player)
			return;
		
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("SWORD_CHAIN");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// ������ üũ
		if (!Weapon_Identify.Hold_MMO_Sword(player))
			return;
		
		String uuid = "mala.mmoskill.swordchain." + player.getUniqueId().toString();
		double damage = skill.getModifier("damage", level);
		if (event.getEntity().hasMetadata(uuid))
			damage += event.getEntity().getMetadata(uuid).get(0).asDouble();
		else
			event.getEntity().setMetadata(uuid, new FixedMetadataValue(MalaMMO_Skill.plugin, damage));
		
		event.getDamage().add(damage);
	}
}
