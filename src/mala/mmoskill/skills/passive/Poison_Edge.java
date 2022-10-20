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

public class Poison_Edge extends RegisteredSkill
{
	public Poison_Edge()
	{	
		super(new Poison_Edge_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(2, 0.5, 0, 20));
	}
}

class Poison_Edge_Handler extends MalaPassiveSkill implements Listener
{
	public Poison_Edge_Handler()
	{
		super(	"POISON_EDGE",
				"독살의 일격",
				Material.BAMBOO,
				"&7기본 공격 시 {per}%의 확률로 독을 겁니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void passive_poison(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("POISON_EDGE");
		
		// 무기 공격이 아니거나 스킬을 알고 있지 않으면 취소
		if(!event.getDamage().hasType(DamageType.WEAPON) || !data.getProfess().hasSkill(skill))
			return;
		
		// 마법, 스킬은 취소
		if(event.getDamage().hasType(DamageType.MAGIC) || event.getDamage().hasType(DamageType.SKILL) || event.getDamage().hasType(DamageType.PROJECTILE))
			return;

		int level = data.getSkillLevel(skill);
		
		double per = skill.getModifier("per", level) * 0.01d;
		if(Math.random() > per)
			return;

		event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_SPLASH_POTION_BREAK, 2, 2);
		event.getEntity().getWorld().spawnParticle(Particle.SPELL_WITCH, event.getEntity().getEyeLocation(), 15, 0.3, 0.3, 0.3, 0);
		Buff_Manager.Add_Buff(event.getEntity(), PotionEffectType.POISON, 4, 200, PotionEffectType.REGENERATION);
	}
	/*
	@EventHandler(priority = EventPriority.HIGHEST)
	public void passive_poison_test(EntityDamageByEntityEvent event)
	{
		for(RegisteredListener list : event.getHandlers().getRegisteredListeners())
			Bukkit.broadcastMessage("" + list.getListener().toString());			
		Bukkit.broadcastMessage("damage" + event.getFinalDamage());
	}
	*/
}
