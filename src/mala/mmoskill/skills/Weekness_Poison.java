package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;

public class Weekness_Poison extends RegisteredSkill
{
	public Weekness_Poison()
	{	
		super(new Weekness_Poison_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(45, 0));
		addModifier("dam_per", new LinearValue(2, 2, 0, 40));
		addModifier("stamina", new LinearValue(10, 1.5));
	}
}

class Weekness_Poison_Handler extends MalaTargetSkill implements Listener
{
	public Weekness_Poison_Handler()
	{
		super(	"WEEKNESS_POISON",
				"약화의 독",
				Material.BROWN_DYE,
				"&85&7m 거리내에 있는 상대에게 10초간 유지되는 독을 심습니다.",
				"대상은 기본 공격에 {dam_per}% 만큼의 추가 피해를 받습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		range = 5.0;
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void weekness_poison_attack(PlayerAttackEvent event)
	{
		if(!event.getEntity().hasMetadata("malammo.skill.weakness_poison"))
			return;
		
		// 마법, 스킬은 취소
		if(event.getDamage().hasType(DamageType.MAGIC) || event.getDamage().hasType(DamageType.SKILL))
			return;
		
		double per = event.getEntity().getMetadata("malammo.skill.weakness_poison").get(0).asInt() * 0.01d;
		Location loc = event.getEntity().getEyeLocation();
		loc.getWorld().playSound(loc, "mala_sound:skill.weak1", 1, 1);
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EAT, 1, 2);
		event.getDamage().multiplicativeModifier(1.0 + per);
	}
	
	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		final int dam_per = (int) cast.getModifier("dam_per"); // 피해 증가치

		// 타겟 취득
		LivingEntity target = _data.getTarget();

		// 효과
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
		target.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, target.getLocation().add(0, target.getHeight(), 0), 20, 0.3, 0.3, 0.3, 0);
		
		target.setMetadata("malammo.skill.weakness_poison", new FixedMetadataValue(MalaMMO_Skill.plugin, dam_per));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Poison(target, 10));
	}
	
	class Poison implements Runnable
	{
		LivingEntity m_Entity;
		int sec;
		
		public Poison(LivingEntity _entity, int _sec)
		{
			m_Entity = _entity;
			sec = _sec;
		}
		
		@Override
		public void run()
		{
			if(--sec == 0)
				m_Entity.removeMetadata("malammo.skill.weakness_poison", MalaMMO_Skill.plugin);
			else
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
		}		
	}
}
