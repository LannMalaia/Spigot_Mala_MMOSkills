package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Dog_BackAttack extends RegisteredSkill
{
	public static Dog_BackAttack skill;
	
	public Dog_BackAttack()
	{	
		super(new Dog_BackAttack_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("hp_heal", new LinearValue(5, 5));
		skill = this;
	}
	
	public static void Play_Heal(Player _player, LivingEntity _wolf)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!Skill_Util.Has_Skill(data, "MASTERY_DOG", 10)
			|| !Skill_Util.Has_Skill(data, "BACK_ATTACK", 10))
			return;
		if (!data.getProfess().hasSkill(skill))
			return;
		
		if (_wolf instanceof Wolf)
		{
			Wolf wolf = (Wolf)_wolf;
			if (!wolf.isTamed())
				return;
			if (!wolf.hasMetadata("summoned"))
				return;
			if (!(wolf.getOwner() instanceof Player))
				return;
			double heal = skill.getModifier("hp_heal", data.getSkillLevel(skill));
			wolf.setHealth(Math.min(wolf.getHealth() + heal, wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			wolf.getWorld().spawnParticle(Particle.HEART, wolf.getEyeLocation(), 20, 0.5, 0.5, 0.5, 0);
			
			Reduce_Cooldown(_player);
			Reduce_Cooldown_ALL(_player);
		}
	}
	public static void Reduce_Cooldown(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (data.getSkillLevel(skill) < 10)
			return;
		CooldownInfo ci = data.getCooldownMap().getInfo(data.getProfess().getSkill(skill));
		if (ci == null)
			return;
		ci.reduceFlat(2.0);
	}
	public static void Reduce_Cooldown_ALL(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (data.getSkillLevel(skill) < 20)
			return;
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			if (cs.getSkill().getHandler().getId().equals("BACK_ATTACK"))
				continue;
			ci.reduceFlat(1.0);
		}
	}
}

class Dog_BackAttack_Handler extends MalaPassiveSkill implements Listener
{
	public Dog_BackAttack_Handler()
	{
		super(	"DOG_BACKATTACK",
				"도움닫기",
				Material.GHAST_TEAR,
				MsgTBL.NeedSkills,
				"&e 조련술 - lv.10",
				"&e 후방 습격 - lv.10",
				"",
				"&7탐색견, 사냥견, 투견에게 후방 습격을 사용했다면,",
				"&7해당 소환수의 &cHP&7를 &e{hp_heal}&7 회복시킵니다.",
				"&eLv.10 - 추가로 후방 습격의 쿨타임이 2초 감소합니다.",
				"&eLv.20 - 모든 스킬의 쿨타임이 1초 감소합니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
}
