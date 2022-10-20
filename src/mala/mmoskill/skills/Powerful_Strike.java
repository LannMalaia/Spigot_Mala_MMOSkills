package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.skills.passive.Gathering_Strike;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Powerful_Strike extends RegisteredSkill
{
	public Powerful_Strike()
	{	
		super(new Powerful_Strike_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(29, -1, 5, 30));
		addModifier("stamina", new LinearValue(25, -0.25, 10, 30));
	}
}

class Powerful_Strike_Handler extends MalaSkill implements Listener
{
	public Powerful_Strike_Handler()
	{
		super(	"POWERFUL_STRIKE",
				"충격타",
				Material.NETHERITE_AXE,
				MsgTBL.NeedSkills,
				"&e 강격 - lv.10",
				"",
				MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7다음 기본 공격이 주변의 적들에게도 피해를 줍니다.",
				"&7주변에 입히는 피해는 스킬 피해로 적용됩니다.",
				"&c도끼를 들고 있어야 합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		// 무기 확인
		if (!Gathering_Strike.Can_AnyWeapon(data.getPlayer()))
		{
			if (!Weapon_Identify.Hold_Axe(data.getPlayer()))
			{
				data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
				return new SimpleSkillResult(false);
			}
		}
		if(!Skill_Util.Has_Skill(data, "BASH", 10))
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
		data.getPlayer().sendMessage("§b§l[ 충격타 준비 ]");
		
		// 효과
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
		data.getPlayer().getWorld().spawnParticle(Particle.LAVA, data.getPlayer().getLocation().add(0, data.getPlayer().getHeight() / 2, 0), 20, 0, 0, 0, 0);
		
		data.getPlayer().setMetadata("malammo.skill.powerful_strike", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void powerful_strike_attack(PlayerAttackEvent event)
	{
		if(!event.getPlayer().hasMetadata("malammo.skill.powerful_strike"))
			return;

		event.getPlayer().removeMetadata("malammo.skill.powerful_strike", MalaMMO_Skill.plugin);
		if (!Gathering_Strike.Can_AnyWeapon(event.getPlayer()))
		{
			if (!Weapon_Identify.Hold_Axe(event.getPlayer()))
				return;
		}

		event.getPlayer().sendMessage("§c§l[ 충격타 발동 ]");
		event.getPlayer().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
		double radius = Gathering_Strike.Get_Radius(event.getPlayer());
		Damage_Others(event.getPlayer(), event.getEntity(), radius, event.getAttack().getDamage().getDamage());
	}
	
	void Damage_Others(Player _player, LivingEntity _target, double _range, double _damage)
	{
		Location pos = _target.getLocation();
		List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, _range * 2, _range * 2, _range * 2));
		Vector rot = new Vector(_player.getLocation().getPitch(), _player.getLocation().getYaw(), 0);
		List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(), rot, new Vector(_range, _range, _range), abc);
		
		for(Entity en : entities)
		{
			if (!(en instanceof LivingEntity))
				continue;
			if (en == _player)
				continue;

			if (Gathering_Strike.Is_WeaponType(_player))
				Damage.Attack(_player, (LivingEntity)en, _damage, DamageType.WEAPON, DamageType.PHYSICAL);
			else
				Damage.Attack(_player, (LivingEntity)en, _damage, DamageType.SKILL, DamageType.PHYSICAL);
			
			if (Gathering_Strike.Can_Gathering(_player))
			{
				Location enpos = en.getLocation();
				Vector gap = pos.clone().subtract(enpos).toVector();
				en.setVelocity(gap.add(new Vector(0.0, 0.4, 0.0)).multiply(0.25));
			}
		}
	}
}













