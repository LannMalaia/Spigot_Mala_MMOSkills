package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Rapid_Fire extends RegisteredSkill
{
	public static Rapid_Fire skill;
	public Rapid_Fire()
	{	
		super(new Rapid_Fire_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(5.5, 0.5, 5, 20));
		addModifier("cooldown", new LinearValue(20.5, 0.5));
		addModifier("stamina", new LinearValue(25.5, 0.5));
		
		skill = this;
	}
}

class Rapid_Fire_Handler extends MalaSkill implements Listener
{
	public Rapid_Fire_Handler()
	{
		super(	"RAPID_FIRE",
				"속사",
				Material.SPECTRAL_ARROW,
				MsgTBL.NeedSkills,
				"&e 쇄도하는 화살 - lv.20",
				"",
				"&e{second}&7초 동안 아주 빠르게 사격할 수 있습니다.",
				"&7들고 있는 장비에 따라 효과가 달라집니다.",
				"&7활 - 집중의 시간이 짧아집니다.",
				"&7석궁 - 사격할 때마다 자동으로 장전됩니다.",
				"",
				MsgTBL.Cooldown_Fixed, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "RUSHING_ARROW", 20))
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

		CooldownFixer.Fix_Cooldown(data, Rapid_Fire.skill);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Rapid_Fire_Task(data.getPlayer(), cast.getModifier("second")));
	}
	
	@EventHandler
	public void passive_rapid_fire(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		if (!(event.getProjectile() instanceof Arrow))
			return;
				
		Player player = (Player)event.getEntity();
		
		if (!player.hasMetadata("malammo.skill.rapid_fire"))
			return;
		
		if (!Weapon_Identify.Hold_Crossbow(player))
			return;
		
		Arrow arrow = (Arrow)event.getProjectile();
		arrow.setCritical(true);
		arrow.setPickupStatus(PickupStatus.DISALLOWED);
		arrow.setMetadata("arrow_no_time", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
		arrow.setMetadata("arrow_remove", new FixedMetadataValue(MalaMMO_Skill.plugin, true));

	}
}

class Rapid_Fire_Task implements Runnable
{
	Player player;
	Location loc;
	double time;
	
	public Rapid_Fire_Task(Player _player, double _time)
	{
		player = _player;
		time = _time;

		player.sendMessage("§b[ 속사 발동 ]");
		player.setMetadata("malammo.skill.rapid_fire", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
	}
	
	public void run()
	{
		// 시간 다 된 경우
		if(time <= 0.0)
		{
			player.removeMetadata("malammo.skill.rapid_fire", MalaMMO_Skill.plugin);
			player.sendMessage("§c[ 속사 해제 ]");
			return;
		}
		if(!player.hasMetadata("malammo.skill.rapid_fire"))
			return;

		if (Weapon_Identify.Hold_Crossbow(player))
		{
			ItemStack crossbow = player.getInventory().getItemInMainHand();
			CrossbowMeta cbmeta = (CrossbowMeta)crossbow.getItemMeta();
			ArrayList<ItemStack> list = new ArrayList<ItemStack>();
			list.add(new ItemStack(Material.ARROW));
			cbmeta.setChargedProjectiles(list);
			crossbow.setItemMeta(cbmeta);
			player.updateInventory();
		}
		
		time -= 0.05;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}




