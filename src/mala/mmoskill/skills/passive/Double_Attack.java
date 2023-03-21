package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Double_Attack extends RegisteredSkill
{
	public Double_Attack()
	{	
		super(new Double_Attack_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(2, 2));
	}
}

class Double_Attack_Handler extends MalaPassiveSkill implements Listener
{
	public Double_Attack_Handler()
	{
		super(	"DOUBLE_ATTACK",
				"치명타 공격",
				Material.GOLDEN_SWORD,
				"&7무기 공격이 &7{per}%의 확률로 &e1.3&7배의 피해를 줍니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void passive_double_attack(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getAttacker().getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("DOUBLE_ATTACK");
		
		// 무기 공격이 아니거나 스킬을 알고 있지 않으면 취소
		if(!event.getDamage().hasType(DamageType.WEAPON) || !data.getProfess().hasSkill(skill))
			return;

		int level = data.getSkillLevel(skill);
		if(!data.getProfess().hasSkill(skill))
			return;
		
		double per = skill.getModifier("per", level) * 0.01d;
		if(Math.random() > per)
			return;

		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2, 2);
		data.getPlayer().getWorld().spawnParticle(Particle.SWEEP_ATTACK, data.getPlayer().getEyeLocation(), 1);
		event.getDamage().multiplicativeModifier(1.3, DamageType.WEAPON);
	}
}
