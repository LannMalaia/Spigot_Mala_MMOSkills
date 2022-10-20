package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.skills.passive.Mastery_Dog;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Call_Fighting_Dog  extends RegisteredSkill
{
	public Call_Fighting_Dog()
	{	
		super(new Call_Fighting_Dog_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(65, 5));
		addModifier("max", new LinearValue(1, 0));
		addModifier("hp", new LinearValue(80, 25));
		addModifier("def", new LinearValue(6, 1.2));
		addModifier("atk", new LinearValue(22, 5));
		addModifier("speed", new LinearValue(0.2, 0.0));
		addModifier("cooldown", new LinearValue(25, 0));
		addModifier("stamina", new LinearValue(25, 2));
	}
}

class Call_Fighting_Dog_Handler extends MalaSkill implements Listener
{
	public Call_Fighting_Dog_Handler()
	{
		super(	"CALL_FIGHTING_DOG",
				"투견 호출",
				Material.BEEF,
				MsgTBL.SKILL,
				"&7싸움에 특화된 투견을 부릅니다.",
				"&7투견은 &8{second}&7초간 유지됩니다.",
				"&7최대 &8{max}&7 마리까지 동시에 유지할 수 있습니다.",
				"",
				"&f[ &e투견 능력치 &f]",
				"&f&l[ &a생명 &f{hp} &f&l][ &e방어 &f{def} &f&l]",
				"&f&l[ &c공격 &f{atk} &f&l][ &b이동 &f{speed} &f&l]",
				"",
				"&f[ &e투견 스킬 &f]",
				"&fLv.10 - 10초마다 5m 내의 적들을 한 번에 공격합니다.",
				"&fLv.15 - 매 공격시 10의 HP를 회복합니다.",
				"&fLv.20 - 10초마다 5m 내의 적들이 자신을 노리게 합니다.",
				"",
				"&e※ 투견의 생명력과 공격력은 스킬 피해량 증가치에 따라 더 증가합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Summon_Manager sm = Summon_Manager.Get_Instance();
		
		double second = cast.getModifier("second");
		int max = (int)cast.getModifier("max");
		int level = data.getSkillLevel(MMOCore.plugin.skillManager.getSkill("CALL_FIGHTING_DOG"));
		
		if (!sm.Check_Summon(data.getPlayer(), "S_Fight_Wolf", max))
		{
			data.getPlayer().sendMessage("§c투견을 더 이상 부를 수 없습니다.");
			return;
		}
		
		S_Fighting_Dog s_wolf = new S_Fighting_Dog(data.getPlayer(), data.getPlayer().getLocation(), (int)(second * 20), level);
		Wolf wolf = (Wolf)sm.Summon(s_wolf);
		wolf.setCustomName(data.getPlayer().getName() + "의 투견");
		wolf.setCustomNameVisible(true);
		wolf.setTamed(true);
		wolf.setOwner(data.getPlayer());
		wolf.setAge(0);
		wolf.setCollarColor(DyeColor.BLACK);
		wolf.setAgeLock(true);

		double hp = cast.getModifier("hp");
		double def = cast.getModifier("def");
		double atk = cast.getModifier("atk");
		double speed = cast.getModifier("speed");
		double skill_per = 1.0 + data.getStats().getStat(StatType.SKILL_DAMAGE) * 0.01;
		
		// data.getPlayer().sendMessage("skill per = " + skill_per);
		// data.getPlayer().sendMessage("speed = " + wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

		hp = hp * skill_per * Mastery_Dog.Get_Percentage(data.getPlayer());
		atk = atk * skill_per * Mastery_Dog.Get_Percentage(data.getPlayer());
		def = def * Mastery_Dog.Get_Percentage(data.getPlayer());
		// speed = 0.3 + skill_per;
		
		wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
		wolf.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(def);
		wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(atk);
		wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
		
		wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		wolf.teleport(data.getPlayer().getLocation().add(0, 0.5, 0));
	}
	
	class S_Fighting_Dog extends Summoned_OBJ implements Runnable
	{
		int time_counter = 0;
		boolean skill_whirlwind = false;
		boolean skill_attack_heal = false;
		boolean skill_bark_aggro = false;
		
		public S_Fighting_Dog(Player _player, Location _loc, int _tick, int _level)
		{
			super(_player, "S_Fight_Wolf", EntityType.WOLF, _loc, _tick);
			_loc.getWorld().spawnParticle(Particle.HEART, _loc, 40, 0.5, 0.5, 0.5, 0);
			_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

			skill_whirlwind = _level >= 10;
			skill_attack_heal = _level >= 15;
			skill_bark_aggro = _level >= 20;
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
		}
		
		public void run()
		{
			time_counter += 1;
			
			if (!entity.isValid())
				return;

			if (time_counter % 5 == 0 && skill_bark_aggro)
			{
				Aggro.Taunt_Area((LivingEntity)entity, 5.0);
				entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WOLF_GROWL, 0.6f, 0.8f);
				Particle_Drawer.Draw_Circle(entity.getLocation().add(0, 0.5, 0), Particle.WAX_ON, 5.0);
			}
			if (time_counter % 10 == 0 && skill_whirlwind)
			{
				entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
				Particle_Drawer.Draw_Circle(entity.getLocation(), Particle.CRIT, 5.0);
				for (Entity e : entity.getNearbyEntities(5.0, 5.0, 5.0))
				{
					if (e instanceof Damageable && !(e instanceof Animals))
					{
						double damage = ((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
						((Damageable)e).damage(damage, entity);
					}
				}
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
		}
		
		@Override
		public void Remove()
		{
			entity.getWorld().spawnParticle(Particle.SMOKE_LARGE, entity.getLocation(), 40, 0.5, 0.5, 0.5, 0);
			super.Remove();
		}

		@EventHandler
		public void Hunting_Dog_Attack(EntityDamageByEntityEvent event)
		{
			if (!entity.isValid())
			{
				EntityDamageByEntityEvent.getHandlerList().unregister(this);
				return;
			}
			if (event.isCancelled())
				return;
			if (!(event.getEntity() instanceof LivingEntity))
				return;
			if (event.getDamager() == entity && skill_attack_heal)
			{
				LivingEntity le = (LivingEntity)entity;
				le.setHealth(Math.min(le.getHealth() + 10, le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
			}
		}
	}
}
