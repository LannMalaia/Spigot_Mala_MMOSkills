package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
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
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Call_Hunting_Dog  extends RegisteredSkill
{
	public Call_Hunting_Dog()
	{	
		super(new Call_Hunting_Dog_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(65, 5));
		addModifier("max", new LinearValue(1, 0));
		addModifier("hp", new LinearValue(34, 9));
		addModifier("def", new LinearValue(4, 0.75));
		addModifier("atk", new LinearValue(15, 3.5));
		addModifier("speed", new LinearValue(0.26, 0.01, 0.25, 0.3));
		addModifier("cooldown", new LinearValue(25, 0));
		addModifier("stamina", new LinearValue(25, 2));
	}
}

class Call_Hunting_Dog_Handler extends MalaSkill implements Listener
{
	public Call_Hunting_Dog_Handler()
	{
		super(	"CALL_HUNTING_DOG",
				"��ɰ� ȣ��",
				Material.FEATHER,
				MsgTBL.SKILL,
				"&7���������� �Ʒõ� ��ɰ��� �θ��ϴ�.",
				"&7��ɰ��� &8{second}&7�ʰ� �����˴ϴ�.",
				"&7�ִ� &8{max}&7 �������� ���ÿ� ������ �� �ֽ��ϴ�.",
				"",
				"&f[ &e��ɰ� �ɷ�ġ &f]",
				"&f&l[ &a���� &f{hp} &f&l][ &e��� &f{def} &f&l]",
				"&f&l[ &c���� &f{atk} &f&l][ &b�̵� &f{speed} &f&l]",
				"",
				"&f[ &e��ɰ� ��ų &f]",
				"&fLv.10 - ������ ������ �ӵ� ���Ҹ� �ο��մϴ�.",
				"&fLv.15 - �̵��ӵ��� 30% �����մϴ�.",
				"&fLv.20 - �ӵ� ���Ҹ� �ִ� 4���� �ο��մϴ�.",
				"",
				"&e�� ��ɰ��� ����°� ���ݷ��� ��ų ���ط� ����ġ�� ���� �� �����մϴ�.",
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
		int level = data.getSkillLevel(MMOCore.plugin.skillManager.getSkill("CALL_HUNTING_DOG"));
		
		if (!sm.Check_Summon(data.getPlayer(), "S_Hunt_Wolf", max))
		{
			data.getPlayer().sendMessage("��c��ɰ��� �� �̻� �θ� �� �����ϴ�.");
			return;
		}
		
		S_Hunting_Dog s_wolf = new S_Hunting_Dog(data.getPlayer(), data.getPlayer().getLocation(), (int)(second * 20), level);
		Wolf wolf = (Wolf)sm.Summon(s_wolf);
		wolf.setCustomName(data.getPlayer().getName() + "�� ��ɰ�");
		wolf.setCustomNameVisible(true);
		wolf.setTamed(true);
		wolf.setOwner(data.getPlayer());
		wolf.setAge(0);
		wolf.setCollarColor(DyeColor.GREEN);
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
		speed = speed * level >= 15 ? 1.3 : 1.0;
		
		wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
		wolf.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(def);
		wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(atk);
		wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
		
		wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		wolf.teleport(data.getPlayer().getLocation().add(0, 0.5, 0));
	}
	
	class S_Hunting_Dog extends Summoned_OBJ
	{
		int time_counter = 0;
		boolean skill_attack_slow = false;
		boolean skill_slow_stack = false;
		
		public S_Hunting_Dog(Player _player, Location _loc, int _tick, int _level)
		{
			super(_player, "S_Hunt_Wolf", EntityType.WOLF, _loc, _tick);
			_loc.getWorld().spawnParticle(Particle.HEART, _loc, 40, 0.5, 0.5, 0.5, 0);
			_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

			skill_attack_slow = _level >= 10;
			skill_slow_stack = _level >= 20;
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
			if (event.getDamager() == entity && skill_attack_slow)
			{
				Buff_Manager.Increase_Buff((LivingEntity)event.getEntity(),
						PotionEffectType.SLOW, 0, 100,
						PotionEffectType.SPEED, skill_slow_stack ? 3 : 0);
			}
		}
	}
}
