package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.skills.passive.Mastery_Dog;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Call_Wolf extends RegisteredSkill
{
	public Call_Wolf()
	{	
		super(new Call_Wolf_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(65, 5));
		addModifier("max", new LinearValue(2, 0));
		addModifier("hp", new LinearValue(40, 10));
		addModifier("def", new LinearValue(2, 0.5));
		addModifier("atk", new LinearValue(10, 2.5));
		addModifier("speed", new LinearValue(0.207, 0.007, 0.2, 0.24));
		addModifier("cooldown", new LinearValue(25, 0));
		addModifier("stamina", new LinearValue(15, 1));
	}
}

class Call_Wolf_Handler extends MalaSkill implements Listener
{
	public Call_Wolf_Handler()
	{
		super(	"CALL_WOLF",
				"탐색견 호출",
				Material.BONE,
				MsgTBL.SKILL,
				"&7전투를 도와줄 탐색견을 부릅니다.",
				"&7탐색견은 &8{second}&7초간 유지됩니다.",
				"&7최대 &8{max}&7 마리까지 동시에 유지할 수 있습니다.",
				"",
				"&f[ &e탐색견 능력치 &f]",
				"&f&l[ &a생명 &f{hp} &f&l][ &e방어 &f{def} &f&l]",
				"&f&l[ &c공격 &f{atk} &f&l][ &b이동 &f{speed} &f&l]",
				"&e※ 탐색견의 생명력과 공격력은 스킬 피해량 증가치에 따라 더 증가합니다.",
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

		if (!sm.Check_Summon(data.getPlayer(), "S_Wolf", max))
		{
			data.getPlayer().sendMessage("§c탐색견을 더 이상 부를 수 없습니다.");
			return;
		}
		
		S_Wolf s_wolf = new S_Wolf(data.getPlayer(), data.getPlayer().getLocation(), (int)(second * 20));
		Wolf wolf = (Wolf)sm.Summon(s_wolf);
		wolf.setCustomName(data.getPlayer().getName() + "의 탐색견");
		wolf.setCustomNameVisible(true);
		wolf.setTamed(true);
		wolf.setOwner(data.getPlayer());
		wolf.setAge(0);
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
		// speed = speed * Mastery_Dog.Get_Percentage(data.getPlayer());
		// speed = 0.3 + skill_per;
		
		wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
		wolf.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(def);
		wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(atk);
		wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
		
		wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		
		wolf.teleport(data.getPlayer().getLocation().add(0, 0.5, 0));
	}
	
	class S_Wolf extends Summoned_OBJ implements Runnable
	{
		public S_Wolf(Player _player, Location _loc, int _tick)
		{
			super(_player, "S_Wolf", EntityType.WOLF, _loc, _tick);
			_loc.getWorld().spawnParticle(Particle.HEART, _loc, 40, 0.5, 0.5, 0.5, 0);
			_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 10);
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
			entity.getWorld().spawnParticle(Particle.SMOKE_LARGE, entity.getLocation(), 40, 0.5, 0.5, 0.5, 0);
			super.Remove();
		}
		
	}
}
