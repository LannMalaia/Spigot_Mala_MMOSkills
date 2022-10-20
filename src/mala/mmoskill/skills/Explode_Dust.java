package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Explode_Dust extends RegisteredSkill
{
	public static RegisteredSkill skill;
	
	public Explode_Dust()
	{	
		super(new Explode_Dust_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(11.5, 1.5));
		addModifier("cooldown", new LinearValue(20, 0));
		addModifier("stamina", new LinearValue(12, 2));
		skill = this;
	}
}

class Explode_Dust_Handler extends MalaSkill implements Listener
{
	public Explode_Dust_Handler()
	{
		super(	"EXPLODE_DUST",
				"분진 폭발",
				Material.GLOWSTONE_DUST,
				MsgTBL.NeedSkills,
				"&e 폭탄 화살 - lv.10",
				"",
				MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&7자신 주변 10m 범위에 가루를 뿌립니다.",
				"&7가루는 잠시 뒤에 폭발해 주변 적들에게 &e{damage}&7의 피해를 주며,",
				"&7추가로 5초간 실명을 부여합니다.",
				"",
				"&fLv.20 - " + MsgTBL.PHYSICAL + MsgTBL.WEAPON + "&f피해로 전환됩니다.",
				"",
				MsgTBL.Cooldown);
	}
	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "EXPLODE_ARROW", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double damage = cast.getModifier("damage");
		boolean isNormal = data.getSkillLevel(Explode_Dust.skill) >= 20;
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Explode_Dust_Skill(data.getPlayer(), data.getPlayer().getEyeLocation(), damage, isNormal));
	}
}

class Explode_Dust_Skill implements Runnable
{
	Player player;
	Location skillLoc;
	double damage;
	double range = 10.0;
	boolean isNormal = false;
	
	Particle particle = Particle.ELECTRIC_SPARK;
	List<Location> curLocations;
	List<Location> targetLocations;
	
	public Explode_Dust_Skill(Player _player, Location _skillLoc, double _damage, boolean _isNormal)
	{
		player = _player; skillLoc = _skillLoc; damage = _damage; isNormal = _isNormal;

		curLocations = new ArrayList<>();
		targetLocations = new ArrayList<>();

		skillLoc.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
		skillLoc.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.5f, 1.5f);
		Random rand = new Random();
		for (int i = 0; i < 36; i++)
		{
			Location loc = skillLoc.clone();
			curLocations.add(loc.clone());
			loc.add(rand.nextDouble(range) - range * 0.5, rand.nextDouble(range) - range * 0.5, rand.nextDouble(range) - range * 0.5);
			targetLocations.add(loc);
		}
	}
	
	double duration = 1.0;
	public void run()
	{
		duration -= 0.05;
		if (duration > 0.0)
		{
			for (int i = 0; i < curLocations.size(); i++)
			{
				Location loc = curLocations.get(i);
				Vector gap = targetLocations.get(i).clone().subtract(loc).toVector().multiply(0.1);
				loc.add(gap);
				skillLoc.getWorld().spawnParticle(particle, loc, 1, 0d, 0d, 0d, 0d);
			}
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
		else
		{
			skillLoc.getWorld().playSound(skillLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.5f);
			skillLoc.getWorld().playSound(skillLoc, Sound.ITEM_TOTEM_USE, 0.5f, 2.0f);
			for (int i = 0; i < curLocations.size(); i++)
			{
				Location loc = curLocations.get(i);
				skillLoc.getWorld().spawnParticle(Particle.CRIT, loc, 64, 0d, 0d, 0d, 1.0d);
				skillLoc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0d, 0d, 0d, 0d);
			}
			
			for (Entity entity : skillLoc.getWorld().getNearbyEntities(skillLoc, range, range, range))
			{
				if (Damage.Is_Possible(player, entity) && entity instanceof LivingEntity)
				{
					if (entity.isOnGround())
					{
						if (isNormal)
							Damage.Attack(player, (LivingEntity)entity, damage, DamageType.PHYSICAL, DamageType.WEAPON);
						else
							Damage.Attack(player, (LivingEntity)entity, damage, DamageType.PHYSICAL, DamageType.SKILL);
						Buff_Manager.Increase_Buff((LivingEntity)entity, PotionEffectType.BLINDNESS, 0, 100, null, 0);
					}
				}
			}
		}
	}
}
