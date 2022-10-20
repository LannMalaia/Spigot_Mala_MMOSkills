package mala.mmoskill.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.util.Vector;

import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Doppel extends Summoned_OBJ implements Runnable
{
	private Doppel(Player _player, Location _loc, int _tick, EntityType _type)
	{
		super(_player, "Doppelganger", _type, _loc, _tick);
		_loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, _loc, 120, 0.5, 1.0, 0.5, 0);
		_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 10);
	}
	
	public static Doppel Spawn_Doppel(PlayerData _data, Location _loc)
	{
		
		EntityType type = EntityType.ARMOR_STAND;

		double second = 5;
		int max = 1;
		RegisteredSkill mastery = MMOCore.plugin.skillManager.getSkill("MASTERY_DOPPEL");
		int level = _data.getSkillLevel(mastery);
		if (level >= 20)
			type = EntityType.WOLF;
		if(_data.getProfess().hasSkill(mastery))
		{
			second = mastery.getModifier("second", level);
			max = (int)mastery.getModifier("max", level);
		}

		if (!Summon_Manager.Get_Instance().Check_Summon(_data.getPlayer(), "Doppelganger", max))
			return null;
		
		Doppel doppel = new Doppel(_data.getPlayer(), _loc, (int)(second * 20), type);


		Entity doppel_en = Summon_Manager.Get_Instance().Summon(doppel);
		if (doppel_en instanceof ArmorStand)
		{
			ArmorStand as = (ArmorStand)doppel_en;
			as.setCustomName(_data.getPlayer().getPlayerListName());
			as.setCustomNameVisible(true);
		}
		else if (doppel_en instanceof Wolf)
		{
			Wolf wolf = (Wolf)doppel_en;
			wolf.setCustomName(_data.getPlayer().getPlayerListName());
			wolf.setCustomNameVisible(true);
			wolf.setTamed(true);
			wolf.setOwner(_data.getPlayer());
			wolf.setAge(0);
			wolf.setAgeLock(true);

			double hp = mastery.getModifier("hp", level);
			double def = mastery.getModifier("def", level);
			double atk = mastery.getModifier("atk", level);
			double speed = mastery.getModifier("speed", level);
			double skill_per = 1.0 + _data.getStats().getStat(StatType.SKILL_DAMAGE) * 0.01;
			
			// hp *= skill_per;
			atk *= skill_per;
			
			wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
			wolf.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(def);
			wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(atk);
			wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
			
			wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		}
		PlayerDisguise pd = new PlayerDisguise(_data.getPlayer().getName());
		pd.setNameVisible(true);
		pd.setEntity(doppel_en);
		// pd.setDeadmau5Ears(true);
		pd.setName(_data.getPlayer().getName());
		pd.startDisguise();
		
		
		return doppel;
	}
	
	public void run()
	{
		if (!entity.isValid())
			return;
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 10);
	}
	
	@Override
	public void Remove()
	{
		entity.getWorld().spawnParticle(Particle.SMOKE_LARGE, entity.getLocation(), 120, 0.5, 1.0, 0.5, 0);
		super.Remove();
	}

	@EventHandler
	public void When_ItemDrop(EntityDropItemEvent event)
	{
		if (!entity.isValid())
		{
			EntityDropItemEvent.getHandlerList().unregister(this);
			return;
		}
		if (event.getEntity() == entity)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void When_Enemy_Damaged(EntityDamageByEntityEvent event)
	{
		if (!entity.isValid())
		{
			EntityDamageByEntityEvent.getHandlerList().unregister(this);
			return;
		}
		if (event.getDamager() != player)
			return;
		if (event.getEntity() == entity)
			return;
		
		Vector dir = event.getEntity().getLocation().subtract(entity.getLocation()).toVector().normalize();
		double yaw = TRS.Get_Yaw_Degree(dir);
		double pitch = TRS.Get_Pitch_Degree(dir);
		try
		{
			entity.setRotation((float)yaw, Float.parseFloat(pitch + ""));
		}
		catch (Exception e)
		{
			Bukkit.getConsoleSender().sendMessage("[Mala_MMOSkills]§c분신 생성 오류 발생"
					+ "yaw = " + yaw + " // pitch = " + pitch);
		}
	}
}
