package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.Stance_Change.Stance_Type;
import mala.mmoskill.skills.passive.Mastery_Stance;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Stance_Change extends RegisteredSkill
{
	public static final String meta_name = "mala.mmoskill.stance.type";
	public static final String bloodstack_name = "mala.mmoskill.stance.bloodstack";
	public static final String bloodstack_sub_name = "mala.mmoskill.stance.bloodstack.sub";
	public static final String speed_modifier_name = "mala.mmoskill.stance.speedmod";
	public static final int sub_init = -4;
	public enum Stance_Type { NORMAL, BERSERK, EVADE }
	static RegisteredSkill skill;
	
	public Stance_Change()
	{	
		super(new Stance_Change_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("hp_deal", new LinearValue(0.55, 0.1));
		addModifier("sta_deal", new LinearValue(1.0, 0.75));
		addModifier("damage", new LinearValue(0.03, 0.03, 0.0, 2.0));
		addModifier("count", new LinearValue(2.2, 0.2, 0, 20));
		addModifier("defense", new LinearValue(1.5, 1.5, 10.0, 30.0));
		addModifier("speed", new LinearValue(21.5, 1.5, 10.0, 50.0));
		addModifier("cooldown", new LinearValue(1, 0));
		addModifier("stamina", new LinearValue(0, 0));
		Bukkit.getScheduler().runTaskTimer(MalaMMO_Skill.plugin, new Stance_Manager(), 0, 20);
		
		skill = this;
	}

	public static int Get_BloodStack(Player player)
	{
		return !player.hasMetadata(Stance_Change.bloodstack_name) ? 0 : player.getMetadata(Stance_Change.bloodstack_name).get(0).asInt();
	}
	public static void Add_BloodStack(Player player, int additive)
	{
		int bloodstack = !player.hasMetadata(Stance_Change.bloodstack_name) ? 0 : player.getMetadata(Stance_Change.bloodstack_name).get(0).asInt();
		
		player.setMetadata(Stance_Change.bloodstack_name,
				new FixedMetadataValue(MalaMMO_Skill.plugin,
						Math.min(Mastery_Stance.Get_Max_Bloodstack(player), bloodstack + additive)));
		
	}
	public static void Set_BloodStack(Player player, int num)
	{
		player.setMetadata(Stance_Change.bloodstack_name,
				new FixedMetadataValue(MalaMMO_Skill.plugin,
						Math.min(Mastery_Stance.Get_Max_Bloodstack(player), num)));
		
	}
	
	public class Stance_Manager implements Runnable
	{
		public Stance_Manager()
		{
		}
		
		public void run()
		{
			for (Player player : Bukkit.getServer().getOnlinePlayers())
			{
				// 혈흔 수준 관리
				if (player.hasMetadata(bloodstack_name))
				{
					int bloodstack = player.getMetadata(bloodstack_name).get(0).asInt();
					if (bloodstack <= 0)
					{
						player.removeMetadata(bloodstack_name, MalaMMO_Skill.plugin);
						if (player.hasMetadata(bloodstack_sub_name))
							player.removeMetadata(bloodstack_sub_name, MalaMMO_Skill.plugin);
					}
					else
					{
						int sub = !player.hasMetadata(bloodstack_sub_name) ? sub_init : player.getMetadata(bloodstack_sub_name).get(0).asInt();
	
						Stance_Type type = !player.hasMetadata(meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(player.getMetadata(Stance_Change.meta_name).get(0).asString());
						if (sub > 0)
							bloodstack = Math.max(bloodstack - sub, 0);
						if (bloodstack <= 0)
							player.removeMetadata(bloodstack_name, MalaMMO_Skill.plugin);
						else
							player.setMetadata(Stance_Change.bloodstack_name, new FixedMetadataValue(MalaMMO_Skill.plugin, bloodstack));
						
						player.setMetadata(Stance_Change.bloodstack_sub_name, new FixedMetadataValue(MalaMMO_Skill.plugin, Math.min(type == Stance_Type.EVADE ? 4 : 20, sub + 1)));
					}
				}
				
				// 스탠스 관리
				if (player.hasMetadata(meta_name))
				{
					Stance_Type type = Stance_Type.valueOf(player.getMetadata(Stance_Change.meta_name).get(0).asString());
					PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
					if (!Skill_Util.Has_Skill(data, "STANCE_CHANGE", 1))
						continue;
					
					int level = data.getSkillLevel(skill);
					double hp_deal = skill.getModifier("hp_deal", level);
					double sta_deal = skill.getModifier("sta_deal", level) * Mastery_Stance.Get_Sta_Percentage(player);
					switch (type)
					{
					case BERSERK:
						// 고정 피해
						if (player.getHealth() > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * Mastery_Stance.Get_HP_Max_Percentage(player))
							player.setHealth(player.getHealth() - hp_deal);
						// hp 체크후 부족하면 강제 취소
						if (player.getHealth() < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 0.1)
						{
							player.sendMessage("§7§l[ 스탠스 유지를 위한 HP가 부족합니다. ]");
							Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Stance_Effect(data.getPlayer(), Stance_Change.Stance_Type.NORMAL));
						}
						break;
					case EVADE:
						// 고정 피해
						data.giveStamina(-sta_deal, UpdateReason.SKILL_COST);
						// 스태미나 체크후 부족하면 강제 취소
						if (data.getStamina() < data.getStats().getStat("MAX_STAMINA") * 0.1)
						{
							player.sendMessage("§7§l[ 스탠스 유지를 위한 스태미나가 부족합니다. ]");
							Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Stance_Effect(data.getPlayer(), Stance_Change.Stance_Type.NORMAL));
						}
						break;
					default:
						break;
					}
					
					double speed = skill.getModifier("speed", level) * 0.01;
					AttributeModifier speed_mod = new AttributeModifier(speed_modifier_name, speed, Operation.MULTIPLY_SCALAR_1);
					boolean att_already_enabled = false;
					for (AttributeModifier mod : player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers())
					{
						if (mod.getName().equals(speed_modifier_name))
						{
							att_already_enabled = true;
							speed_mod = mod;
						}
					}
					if (type == Stance_Type.EVADE)
					{
						if (!att_already_enabled)
							player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(speed_mod);
					}
					else
						player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(speed_mod);
				}
				else
				{
					for (AttributeModifier mod : player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers())
					{
						if (mod.getName().equals(speed_modifier_name))
						{
							player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(mod);
							break;
						}
					}
				}
			}
		}
	}
}

class Stance_Change_Handler extends MalaSkill implements Listener
{
	public Stance_Change_Handler()
	{
		super(	"STANCE_CHANGE",
				"스탠스",
				Material.ARMOR_STAND,
				"&7전투 자세를 변경해 특별한 효과를 얻습니다.",
				"",
				"&c[ 버서크 스탠스 ]",
				"&7매 초 &e{hp_deal}&7만큼의 &cHP&7를 소모합니다.",
				"&cHP&7가 10% 미만이 되면 해제됩니다.",
				"&7기본 공격으로 &e{count}&7의 혈흔을 획득할 수 있으며,",
				"&7스킬 공격으로는 &e1&7의 혈흔을 획득합니다.",
				"&7피해량을 &e혈흔의 수 * {damage}&7% 강화하며,",
				"&7스킬은 그 절반만큼 강화합니다.",
				"",
				"&b[ 이베이드 스탠스 ]",
				"&7매 초 &e{sta_deal}&7만큼의 &6스태미나&7를 소모합니다.",
				"&6스태미나&7가 10% 미만이 되면 해제됩니다.",
				"&7이동속도가 &e{speed}&7% 증가합니다.",
				"&7혈흔이 천천히 줄어듭니다(최대 -4).",
				"&7혈흔을 가지고 있는 동안 받는 피해량이 &e{defense}&7% 감소합니다.",
				"",
				MsgTBL.Cooldown);
		registerModifiers("hp_deal", "count", "damage", "sta_deal", "defense", "speed");
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void when_damaged(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player)event.getEntity();
		
		if (!MMOCore.plugin.dataProvider.getDataManager().isLoaded(player.getUniqueId()))
			return;
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		
		if (!Skill_Util.Has_Skill(data, "STANCE_CHANGE", 1))
			return;

		Stance_Type type = !player.hasMetadata(Stance_Change.meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(player.getMetadata(Stance_Change.meta_name).get(0).asString());
		if (type != Stance_Type.EVADE)
			return;

		if (player.hasMetadata(Stance_Change.bloodstack_name))
		{
			int level = data.getSkillLevel(Stance_Change.skill);
			double defence = 100.0d - Stance_Change.skill.getModifier("defense", level);
			event.setDamage(event.getDamage() * (defence * 0.01));
		}
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void when_attack(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		if (!Skill_Util.Has_Skill(data, "STANCE_CHANGE", 1))
			return;

		Stance_Type type = !player.hasMetadata(Stance_Change.meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(player.getMetadata(Stance_Change.meta_name).get(0).asString());
		if (type != Stance_Type.BERSERK)
			return;

		int level = data.getSkillLevel(Stance_Change.skill);
		// 스킬, 마법, 투사체 공격이 아니라면 혈흔 스택 추가
		if (!(event.getDamage().hasType(DamageType.SKILL)
				|| event.getDamage().hasType(DamageType.MAGIC)
				|| event.getDamage().hasType(DamageType.PROJECTILE)))
		{
			Stance_Change.Add_BloodStack(player, (int)Stance_Change.skill.getModifier("count", level));
		}
		else
		{
			Stance_Change.Add_BloodStack(player, 1);
		}
		player.setMetadata(Stance_Change.bloodstack_sub_name, new FixedMetadataValue(MalaMMO_Skill.plugin, Stance_Change.sub_init));
		
		int bloodstack = !player.hasMetadata(Stance_Change.bloodstack_name) ? 0 : player.getMetadata(Stance_Change.bloodstack_name).get(0).asInt();
		double damage = Stance_Change.skill.getModifier("damage", level);
		double mult = (bloodstack * damage * 0.01);
		if (event.getDamage().hasType(DamageType.SKILL))
			mult *= 0.5;
		event.getDamage().multiplicativeModifier(1.0 + mult, DamageType.PHYSICAL);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		if (!data.getPlayer().hasMetadata(Stance_Change.meta_name))
		{
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Stance_Effect(data.getPlayer(), Stance_Change.Stance_Type.BERSERK));
		}
		else
		{
			switch (Stance_Change.Stance_Type.valueOf(data.getPlayer().getMetadata(Stance_Change.meta_name).get(0).asString()))
			{
			case BERSERK:
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Stance_Effect(data.getPlayer(), Stance_Change.Stance_Type.EVADE));
				break;
			case EVADE:
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Stance_Effect(data.getPlayer(), Stance_Change.Stance_Type.NORMAL));
				break;
			case NORMAL:
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Stance_Effect(data.getPlayer(), Stance_Change.Stance_Type.BERSERK));
				break;
			}
		}
		
	}
}

class Stance_Effect implements Runnable
{
	Player player;
	Stance_Change.Stance_Type type;
	
	ArrayList<LivingEntity> enemies = new ArrayList<LivingEntity>();
	
	public Stance_Effect(Player _player, Stance_Change.Stance_Type _type)
	{
		player = _player;
		type = _type;

		Sound sound = Sound.ENTITY_FIREWORK_ROCKET_SHOOT;
		switch (_type)
		{
		case BERSERK:
			player.sendMessage("§c§l[ 버서크 스탠스 ]");
			sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
			break;
		case EVADE:
			player.sendMessage("§b§l[ 이베이드 스탠스 ]");
			sound = Sound.ENTITY_FIREWORK_ROCKET_LAUNCH;
			break;
		case NORMAL:
			player.sendMessage("§7§l[ 스탠스 해제 ]");
			sound = Sound.ENTITY_GENERIC_EXTINGUISH_FIRE;
			break;
		}
		
		player.getWorld().playSound(player.getLocation(), sound, 1.5f, 1.5f);
		player.setMetadata(Stance_Change.meta_name, new FixedMetadataValue(MalaMMO_Skill.plugin, _type.toString()));
	}
	
	double sec = 1.0;
	public void run()
	{
		Location loc = player.getLocation().add(0.0, 2.35 - sec * 2.0, 0.0);
		switch(type)
		{
		case BERSERK:
			Particle_Drawer.Draw_Circle(loc, Particle.ELECTRIC_SPARK, 1.2);
			Particle_Drawer.Draw_Circle(loc, new DustOptions(Color.RED, 0.5f), 0.8);
			break;
		case EVADE:
			Particle_Drawer.Draw_Circle(loc, new DustOptions(Color.AQUA, 0.5f), 0.6);
			break;
		case NORMAL:
			Particle_Drawer.Draw_Circle(loc.clone().add(0.0, 0.2, 0.0), Particle.CRIT, 0.5);
			Particle_Drawer.Draw_Circle(loc.clone().add(0.0, 0.1, 0.0), Particle.CRIT, 0.8);
			Particle_Drawer.Draw_Circle(loc, Particle.CRIT, 1.1);
			sec = 0.0;
			break;
		}
		
		if (sec > 0.0)
		{
			sec -= 0.05;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
