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
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Call_Knights  extends RegisteredSkill
{
	public Call_Knights()
	{	
		super(new Call_Knights_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(65, 5));
		addModifier("max", new LinearValue(2.2, 0.2, 2.0, 7.0));
		addModifier("hp", new LinearValue(100, 40));
		addModifier("def", new LinearValue(6, 1.2));
		addModifier("atk", new LinearValue(12, 2));
		addModifier("speed", new LinearValue(0.2, 0.0));
		addModifier("cooldown", new LinearValue(40, 0));
		addModifier("stamina", new LinearValue(40, 0));
	}
}

class Call_Knights_Handler extends MalaSkill implements Listener
{
	public Call_Knights_Handler()
	{
		super(	"CALL_KNIGHTS",
				"��ö�� ����",
				Material.IRON_SWORD,
				MsgTBL.SKILL,
				"&7�ڽ��� ������ ��縦 �θ��ϴ�.",
				"&7���� &8{second}&7�ʰ� �����˴ϴ�.",
				"&7�ִ� &8{max}&7 ����� ���ÿ� ������ �� �ֽ��ϴ�.",
				"&7��ȯ�� �ִ�ġ�� �°� ��ȯ�մϴ�.",
				"",
				"&f[ &e��� �ɷ�ġ &f]",
				"&f&l[ &a���� &f{hp} &f&l][ &e��� &f{def} &f&l]",
				"&f&l[ &c���� &f{atk} &f&l][ &b�̵� &f{speed} &f&l]",
				"",
				"&f[ &e��� ��ų &f]",
				"&fLv.1 - 10�ʸ��� 5m ���� ������ �ڽ��� �븮�� �մϴ�.",
				"&fLv.10 - ������ 10m�� �����մϴ�.",
				"&fLv.15 - ���� �󵵰� 5�ʷ� �پ��ϴ�.",
				"&fLv.20 - ������ 15m�� �����մϴ�.",
				"",
				"&e�� ������ ����°� ������ ��ų ���ط� ����ġ�� ���� �� �����մϴ�.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost,
				"",
				"&8idea by Hero1112");
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Summon_Manager sm = Summon_Manager.Get_Instance();
		
		double second = cast.getModifier("second");
		int max = (int)cast.getModifier("max");
		int level = data.getSkillLevel(MMOCore.plugin.skillManager.getSkill("CALL_KNIGHTS"));

		if (sm.Check_Summon(data.getPlayer(), "S_Knight", max)) {
			data.getPlayer().sendMessage("��c��縦 �� �̻� �θ� �� �����ϴ�.");
			return;			
		}
		
		while (sm.Check_Summon(data.getPlayer(), "S_Knight", max))
		{
			S_Knight s_knight = new S_Knight(data.getPlayer(), data.getPlayer().getLocation(), (int)(second * 20), level);
			Wolf wolf = (Wolf)sm.Summon(s_knight);
			wolf.setCustomName(data.getPlayer().getName() + "�� ���");
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
			double skill_per = 1.0 + data.getStats().getStat("SKILL_DAMAGE") * 0.01;
			
			hp = hp * skill_per;
			def = def * skill_per;
			
			wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
			wolf.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(def);
			wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(atk);
			wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
			
			wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

			PlayerDisguise pd = new PlayerDisguise("NopeNath");
			pd.setNameVisible(true);
			pd.setEntity(wolf);
			pd.setName(wolf.getCustomName());
			pd.startDisguise();
			
			wolf.teleport(data.getPlayer().getLocation().add(0, 0.5, 0));
		}
	}
	
	class S_Knight extends Summoned_OBJ implements Runnable
	{
		int time_counter = 0;
		int aggroLatency = 10;
		double aggroRange = 5.0;
		
		public S_Knight(Player _player, Location _loc, int _tick, int _level)
		{
			super(_player, "S_Knight", EntityType.WOLF, _loc, _tick);
			_loc.getWorld().spawnParticle(Particle.HEART, _loc, 40, 0.5, 0.5, 0.5, 0);
			_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

			if (_level >= 10)
				aggroRange = 10.0;
			if (_level >= 15)
				aggroLatency = 5;
			if (_level >= 20)
				aggroRange = 15.0;
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
		}
		
		public void run()
		{
			time_counter += 1;
			
			if (!entity.isValid())
				return;

			if (time_counter % aggroLatency == 0)
			{
				Aggro.Taunt_Area((LivingEntity)entity, aggroRange);
				entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_WOLF_GROWL, 0.6f, 0.8f);
				Particle_Drawer.Draw_Circle(entity.getLocation().add(0, 0.5, 0), Particle.WAX_ON, aggroRange);
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
		}
		
		@Override
		public void Remove()
		{
			entity.getWorld().spawnParticle(Particle.SMOKE_LARGE, entity.getLocation(), 40, 0.5, 0.5, 0.5, 0);
			super.Remove();
		}
	}
}
