package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Parry_Surge extends RegisteredSkill
{
	public Parry_Surge()
	{	
		super(new Parry_Surge_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("second", new LinearValue(0.04, 0.04));
	}
}

class Parry_Surge_Handler extends MalaPassiveSkill implements Listener
{
	public Parry_Surge_Handler()
	{
		super(	"PARRY_SURGE",
				"휘몰아치기",
				Material.NETHERITE_INGOT,
				MsgTBL.NeedSkills,
				"&e 쳐내기 - lv.20",
				"",
				"&7피해를 가할 때마다",
				"&7쳐내기의 재사용 대기시간이 &e{second}&7초 감소합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_parry_charger(EntityDamageByEntityEvent event)
	{
		if(!(event.getDamager() instanceof Player))
			return;
		if(!(event.getEntity() instanceof LivingEntity))
			return;
		
		if(!event.getDamager().hasMetadata("malammo.skill.parry"))
			return;

		if(event.isCancelled())
			return;
		
		Player player = (Player)event.getDamager();
		PlayerData data = PlayerData.get(player);

		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("PARRY_SURGE");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill) || !Skill_Util.Has_Skill(data, "PARRY", 20))
			return;

		double sub = skill.getModifier("second", level);
		double sec = player.getMetadata("malammo.skill.parry").get(0).asDouble();
		player.setMetadata("malammo.skill.parry", new FixedMetadataValue(MalaMMO_Skill.plugin, sec - sub));
	}
}
