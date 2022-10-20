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
				"연쇄 칼날",
				Material.GOLDEN_SWORD,
				"&7단검, 검으로 적을 공격할 때마다",
				"&e{damage}&7의 추가 물리 피해를 줍니다.",
				"&7이 피해는 공격할 때마다 계속해서 증가합니다.",
				"&c플레이어에게는 적용되지 않습니다.",
				"&c기본 무기에는 적용되지 않습니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_swordchain(PlayerAttackEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof Player)
			return;
		
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("SWORD_CHAIN");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// 아이템 체크
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
