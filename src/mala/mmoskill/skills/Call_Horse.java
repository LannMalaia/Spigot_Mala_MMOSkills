package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.Listener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Money_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Call_Horse extends RegisteredSkill
{
	public Call_Horse()
	{	
		super(new Call_Horse_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(240, 20));
		addModifier("max", new LinearValue(1, 0));
		addModifier("hp", new LinearValue(125, 25));
		addModifier("def", new LinearValue(5, 1.25));
		addModifier("speed", new LinearValue(0.25, 0.01, 0.25, 0.35));
		addModifier("cooldown", new LinearValue(40, 0));
		addModifier("stamina", new LinearValue(30, 1));
		addModifier("gold", new LinearValue(5500, 500));
	}
}

class Call_Horse_Handler extends MalaSkill implements Listener
{
	public Call_Horse_Handler()
	{
		super(	"CALL_HORSE",
				"군마 호출",
				Material.LEATHER_HORSE_ARMOR,
				MsgTBL.SKILL,
				"&7전투를 도와줄 말을 부릅니다.",
				"&7말은 &8{second}&7초간 유지됩니다.",
				"&7최대 &8{max}&7 마리까지 동시에 유지할 수 있습니다.",
				"",
				"&f[ &e말 능력치 &f]",
				"&f&l[ &a생명 &f{hp} &f&l][ &e방어 &f{def} &f&l]",
				"&f&l[ &b이동 &f{speed} &f&l]",
				"&e※ 말의 생명력은 스킬 피해량 증가치에 따라 더 증가합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost, "&9비용: &7{gold} &6골드");
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Summon_Manager sm = Summon_Manager.Get_Instance();

		double second = cast.getModifier("second");
		int max = (int)cast.getModifier("max");
		
		if (!sm.Check_Summon(data.getPlayer(), "S_Horse", max))
		{
			data.getPlayer().sendMessage("§c군마를 더 이상 부를 수 없습니다.");
			return;
		}
		if (!Money_Util.Check_Enough_Money(data.getPlayer(), cast.getModifier("gold")))
		{
			Money_Util.Send_Msg_NotEnoughMoney(data.getPlayer());
			return;
		}
		Money_Util.Withdraw_Money(data.getPlayer(), cast.getModifier("gold"));
		
		
		S_Horse s_horse = new S_Horse(data.getPlayer(),
				data.getPlayer().getLocation(), (int)(second * 20));
		Horse horse = (Horse)sm.Summon(s_horse);
		horse.setCustomName(data.getPlayer().getName() + "의 군마");
		horse.setCustomNameVisible(true);
		horse.setTamed(true);
		horse.setOwner(data.getPlayer());
		horse.setAge(0);
		horse.setAgeLock(true);
		horse.setColor(Color.WHITE);
		horse.setStyle(Style.NONE);
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.setBreed(false);
		
		double hp = cast.getModifier("hp");
		double def = cast.getModifier("def");
		double speed = cast.getModifier("speed");
		double skill_per = 1.0 + data.getStats().getStat(StatType.SKILL_DAMAGE) * 0.01;
		
		// data.getPlayer().sendMessage("skill per = " + skill_per);
		// data.getPlayer().sendMessage("speed = " + wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

		hp *= skill_per;
		// speed = 0.3 + skill_per;
		
		horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
		horse.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(def);
		horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
		
		horse.setHealth(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
		horse.teleport(data.getPlayer().getLocation().add(0, 0.5, 0));
	}
	
	class S_Horse extends Summoned_OBJ implements Runnable
	{
		public S_Horse(Player _player, Location _loc, int _tick)
		{
			super(_player, "S_Horse", EntityType.HORSE, _loc, _tick);
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
