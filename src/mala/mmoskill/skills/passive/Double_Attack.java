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
import laylia_core.main.Damage;
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
		addModifier("per", new LinearValue(10, 2, 10, 40));
	}
}

class Double_Attack_Handler extends MalaPassiveSkill implements Listener
{
	public Double_Attack_Handler()
	{
		super(	"DOUBLE_ATTACK",
				"이중 공격",
				Material.GOLDEN_SWORD,
				"&7{per}%의 확률로 두 번 공격합니다.",
				"&7두번째 공격은 스킬 피해로 취급됩니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void passive_double_attack(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("DOUBLE_ATTACK");
		
		// 무기 공격이 아니거나 스킬을 알고 있지 않으면 취소
		if(!event.getDamage().hasType(DamageType.WEAPON) || !data.getProfess().hasSkill(skill))
			return;
		
		// 마법, 스킬은 취소
		if(event.getDamage().hasType(DamageType.MAGIC) || event.getDamage().hasType(DamageType.SKILL) || event.getDamage().hasType(DamageType.PROJECTILE))
			return;

		int level = data.getSkillLevel(skill);
		if(!data.getProfess().hasSkill(skill))
			return;
		
		double per = skill.getModifier("per", level) * 0.01d;
		if(Math.random() > per)
			return;
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new DA(event.getPlayer(), event.getEntity(), event.getAttack().getDamage().getDamage()), 4);
	}
	
	class DA implements Runnable
	{
		Player player;
		LivingEntity entity;
		double damage;
		
		public DA(Player _p, LivingEntity _e, double _d)
		{
			player = _p;
			entity = _e;
			damage = _d;
		}
		
		@Override
		public void run()
		{
			if(entity.isDead())
			{
				return;
			}

			player.sendTitle("", "§f§l[ 더블 어택 ]", 0, 40, 0);
			
			entity.setNoDamageTicks(0);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2, 2);
			player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getEyeLocation(), 1);
			entity.setVelocity(new Vector(0d, 0d, 0d));
			Damage.Attack(player, entity, damage, DamageType.SKILL, DamageType.PHYSICAL);
		}
		
	}

}
