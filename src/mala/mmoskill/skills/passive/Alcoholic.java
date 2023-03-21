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
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;

public class Alcoholic extends RegisteredSkill
{
	public Alcoholic()
	{	
		super(new Alcoholic_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(3.5, 3.5));
	}
}

class Alcoholic_Handler extends MalaPassiveSkill implements Listener
{
	public Alcoholic_Handler()
	{
		super(	"ALCOHOLIC",
				"취권",
				Material.POTION,
				"&7멀미 상태일 때,",
				"&7맨손 피해량이 &e{percent}&7% 증가합니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void attack_fistmastery(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		if (!player.hasPotionEffect(PotionEffectType.CONFUSION))
			return;
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("ALCOHOLIC");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;
		
		if (event.getDamage().hasType(DamageType.UNARMED))
		{
			double per = skill.getModifier("percent", level);
			event.getDamage().multiplicativeModifier(1.0 + per * 0.01);
		}
	}
}
