package mala.mmoskill.skills.passive;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

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
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;

public class Whip_Poison extends RegisteredSkill
{
	public Whip_Poison()
	{	
		super(new Whip_Poison_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("sec", new LinearValue(1.3, 0.3));
	}
}

class Whip_Poison_Handler extends MalaPassiveSkill implements Listener
{
	public Whip_Poison_Handler()
	{
		super(	"WHIP_POISON",
				"����� ���",
				Material.AMETHYST_SHARD,
				"&7ä������ ��ų, ���� ������ �ƴ� ������ ���� ��",
				"&7��ȭ ������ &e{sec}&7�ʰ� �ο��մϴ�.",
				"&eLv.10&7���� �ߵ��մϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_whip_poison(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);

		if (event.getDamage().hasType(DamageType.MAGIC) || event.getDamage().hasType(DamageType.SKILL))
			return;
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("WHIP_POISON");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill) || level < 10)
			return;

		// ������ üũ
		if (!Weapon_Identify.Hold_MMO_Whip(player))
			return;
		
		int ticks = (int)(skill.getModifier("sec", level) * 20.0);
		Buff_Manager.Increase_Buff(event.getEntity(), PotionEffectType.WEAKNESS,
				0, ticks, PotionEffectType.INCREASE_DAMAGE, 0);
	}
}
