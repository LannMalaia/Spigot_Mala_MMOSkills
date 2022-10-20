package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;

public class Venomous extends RegisteredSkill
{
	public Venomous()
	{	
		super(new Venomous_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(2, 2, 0, 40));
	}
}

class Venomous_Handler extends MalaPassiveSkill implements Listener
{
	public Venomous_Handler()
	{
		super(	"VENOMOUS",
				"맹독통",
				Material.GREEN_DYE,
				"&7독에 걸린 상대에게 {per}%의 추가 피해를 줍니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void passive_venom(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("VENOMOUS");
		
		// 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill(skill))
			return;

		if(!event.getEntity().hasPotionEffect(PotionEffectType.POISON))
			return;

		int level = data.getSkillLevel(skill);
		
		double per = skill.getModifier("per", level) * 0.01d;
		event.getDamage().multiplicativeModifier(1.0 + per);
	}
}
