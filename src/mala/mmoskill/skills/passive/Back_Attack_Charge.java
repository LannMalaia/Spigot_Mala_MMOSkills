package mala.mmoskill.skills.passive;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import mala.mmoskill.skills.Back_Attack;
import mala.mmoskill.skills.Lucky_Star;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
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

public class Back_Attack_Charge extends RegisteredSkill
{
	public static Back_Attack_Charge skill;
	
	public Back_Attack_Charge()
	{	
		super(new Back_Attack_Charge_Handler(), MalaMMO_Skill.plugin.getConfig());
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

class Back_Attack_Charge_Handler extends MalaPassiveSkill implements Listener
{
	public Back_Attack_Charge_Handler()
	{
		super(	"BACK_ATTACK_CHARGE",
				"동에 번쩍 서에 번쩍",
				Material.CHORUS_FRUIT,
				"&7적을 쓰러트릴 때마다",
				"&7후방 습격의 재사용 대기시간이 초기화됩니다.",
				"&7Lv.10에서 발동합니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void death_backattackcharge(EntityDeathEvent event)
	{
		if (event.getEntity().getKiller() == null)
			return;
		
		Player player = event.getEntity().getKiller();
		if (!player.isOnline() || !PlayerData.has(player))
			return;
		
		PlayerData data = PlayerData.get(player);
		if (Skill_Util.Has_Skill(data, "BACK_ATTACK_CHARGE", 10))
			return;
		
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			if (cs.getSkill() != Back_Attack.skill)
				continue;
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			ci.reduceFlat(999.0);
		}
	}
}
