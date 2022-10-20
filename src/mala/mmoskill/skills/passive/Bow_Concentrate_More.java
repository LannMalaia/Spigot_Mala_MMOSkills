package mala.mmoskill.skills.passive;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;

public class Bow_Concentrate_More extends RegisteredSkill
{
	public static Bow_Concentrate_More skill;
	
	public Bow_Concentrate_More()
	{	
		super(new Bow_Concentrate_More_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(24, 4));
		skill = this;
	}
	
	public static double Get_Percentage(Player player)
	{
		PlayerData data = PlayerData.get(player);
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return 1.0;
		return 1.0 + skill.getModifier("percent", level) * 0.01;
	}
}

class Bow_Concentrate_More_Handler extends MalaPassiveSkill implements Listener
{
	public Bow_Concentrate_More_Handler()
	{
		super(	"BOW_CONCENTRATE_MORE",
				"극집중",
				Material.BOW,
				"&7집중(mcmmo 궁술)의 피해량이 &e{percent}&7% 증가합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_bow_mastery(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity))
			return;
		if (!(event.getDamager() instanceof Projectile))
			return;
		Projectile proj = (Projectile)event.getDamager();
		if (!(proj.getShooter() instanceof Player))
			return;
		if (!proj.hasMetadata("be.archery.concent_dmg"))
			return;
		
		Player player = (Player)proj.getShooter();
		event.setDamage(event.getDamage() * Bow_Concentrate_More.Get_Percentage(player));
	}
}
