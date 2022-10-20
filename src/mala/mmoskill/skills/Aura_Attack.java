package mala.mmoskill.skills;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Aura_Flag;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Aura_Attack extends RegisteredSkill
{
	public Aura_Attack()
	{	
		super(new Aura_Attack_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("percent", new LinearValue(1, 0.5));
		addModifier("cooldown", new LinearValue(60, -1.5));
		addModifier("stamina", new LinearValue(20, 3));
	}
}

class Aura_Attack_Handler extends MalaSkill implements Listener
{
	public Aura_Attack_Handler()
	{
		super(	"AURA_ATTACK",
				"공격의 진",
				Material.MUSIC_DISC_BLOCKS,
				MsgTBL.NeedSkills,
				"&e 지휘술 - lv.5",
				"",
				"&7가하는 피해량이 증가하는 지휘 깃발을 꽂습니다.",
				"&7깃발의 영향을 받는 플레이어는,",
				"&7가하는 모든 피해량이 &8{percent}&7% 증가합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "MASTERY_FLAG", 5))
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
		
		double per = cast.getModifier("percent");
		
		Attack_Flag flag = Spawn_Flag(data, data.getPlayer().getLocation().add(0, 0.5, 0), per);
		if (flag == null)
			data.getPlayer().sendMessage("§c깃발을 더 이상 생성할 수 없습니다.");
	}

	public Attack_Flag Spawn_Flag(PlayerData _data, Location _loc, double _per)
	{
		EntityType type = EntityType.ARMOR_STAND;

		double second = 5;
		double temp_radius = 3.0;
		int max = 1;
		RegisteredSkill mastery = MMOCore.plugin.skillManager.getSkill("MASTERY_FLAG");
		int level = _data.getSkillLevel(mastery);
		if(_data.getProfess().hasSkill(mastery))
		{
			second = mastery.getModifier("second", level);
			temp_radius = mastery.getModifier("radius", level);
			max = (int)mastery.getModifier("max", level);
		}

		if (!Summon_Manager.Get_Instance().Check_Summon(_data.getPlayer(), "Aura_Flag", max))
			return null;
		
		Attack_Flag flag = new Attack_Flag(_data.getPlayer(), _loc, (int)(second * 20), type, temp_radius, _per);

		Entity flag_en = Summon_Manager.Get_Instance().Summon(flag);
		ArmorStand as = (ArmorStand)flag_en;
		as.setCustomName("§c§l공격 깃발");
		as.setCustomNameVisible(true);
		as.setInvisible(true);
		ItemStack item = new ItemStack(Material.RED_BANNER);
		as.getEquipment().setHelmet(item);
		as.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
		
		return flag;
	}

	class Attack_Flag extends Aura_Flag
	{
		double per = 0.0;
		
		public Attack_Flag(Player _player, Location _loc, int _tick, EntityType _type, double _radius, double _per)
		{
			super(_player, _loc, _tick, _type, _radius);
			per = _per;
		}

		@Override
		protected void Particle_Setup()
		{
			super.Particle_Setup();
			m_Particle = Particle.REDSTONE;
			m_Dust = new DustOptions(Color.RED, 1.0f);
		}

		public double Mult_Damage(double _damage)
		{
			return _damage = Math.max(0.0, _damage + (_damage * per * 0.01));
		}
		
		@EventHandler
		public void shield_aura_damaged(EntityDamageByEntityEvent event)
		{
			if (!entity.isValid())
			{
				EntityDamageByEntityEvent.getHandlerList().unregister(this);
				return;
			}
			if (event.isCancelled())
				return;
			if (players.contains(event.getEntity()))
			{
				double damage = Mult_Damage(event.getDamage());
				event.setDamage(damage);
			}
		}
	}
}


