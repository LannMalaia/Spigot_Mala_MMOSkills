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

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;

public class Sword_Trick extends RegisteredSkill
{
	public static Sword_Trick skill;
	
	public Sword_Trick()
	{	
		super(new Sword_Trick_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("sec", new LinearValue(0.03, 0.03));
		skill = this;
	}
	
	public static void Reduce_Cooldown_ALL(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (data.getSkillLevel(skill) < 1)
			return;
		double reduceSec = skill.getModifier("sec", data.getSkillLevel(skill));
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			ci.reduceFlat(reduceSec);
		}
	}
}

class Sword_Trick_Handler extends MalaPassiveSkill implements Listener
{
	public Sword_Trick_Handler()
	{
		super(	"SWORD_TRICK",
				"�����",
				Material.GOLDEN_SWORD,
				"&7�ܰ�, ������ ����ü, ��ų, ���� ������ �ƴ� ������ ���� ��",
				"&7��ü ��ų�� ���� ���ð��� &e{sec}&7�� �����մϴ�.",
				"&c�⺻ ���⿡�� ������� �ʽ��ϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_swordtrick(PlayerAttackEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof Player)
			return;
		
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("SWORD_TRICK");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// ���� Ÿ�� üũ
		if (event.getDamage().hasType(DamageType.PROJECTILE) || event.getDamage().hasType(DamageType.MAGIC)
			|| event.getDamage().hasType(DamageType.SKILL))
			return;
		
		// ������ üũ
		if (!Weapon_Identify.Hold_MMO_Sword(player))
			return;
		
		Sword_Trick.Reduce_Cooldown_ALL(player);
	}
}
