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
				"연계검",
				Material.GOLDEN_SWORD,
				"&7단검, 검으로 투사체, 스킬, 마법 공격이 아닌 공격을 했을 때",
				"&7전체 스킬의 재사용 대기시간이 &e{sec}&7초 감소합니다.",
				"&c기본 무기에는 적용되지 않습니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_swordtrick(PlayerAttackEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity) || event.getEntity() instanceof Player)
			return;
		
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("SWORD_TRICK");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// 공격 타입 체크
		if (event.getDamage().hasType(DamageType.PROJECTILE) || event.getDamage().hasType(DamageType.MAGIC)
			|| event.getDamage().hasType(DamageType.SKILL))
			return;
		
		// 아이템 체크
		if (!Weapon_Identify.Hold_MMO_Sword(player))
			return;
		
		Sword_Trick.Reduce_Cooldown_ALL(player);
	}
}
