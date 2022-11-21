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
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Rapid_Fire extends RegisteredSkill
{
	public static Rapid_Fire skill;
	public Rapid_Fire()
	{	
		super(new Rapid_Fire_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("stamina_cost", new LinearValue(2.4, -0.1, 0.5, 5.0));
		addModifier("cooldown", new LinearValue(1.0, 0.0));
		addModifier("stamina", new LinearValue(0, 0));
		
		skill = this;
	}
}

class Rapid_Fire_Handler extends MalaSkill implements Listener
{
	public Rapid_Fire_Handler()
	{
		super(	"RAPID_FIRE",
				"�ӻ�",
				Material.SPECTRAL_ARROW,
				MsgTBL.NeedSkills,
				"&e �⵵�ϴ� ȭ�� - lv.20",
				"",
				"&7�Ͻ������� ���� ������ ����� �� �ֽ��ϴ�.",
				"&7�� ���� ���¹̳��� �ڿ������� ȸ������ �ʽ��ϴ�.",
				"&7�ʴ� &e{stamina_cost}&7�� ���¹̳��� �Ҹ��ϸ�,",
				"&7���¹̳��� ���� �Ҹ��ϰų� ��ų�� �ٽ� ����ؼ� ����� �� �ֽ��ϴ�.",
				"&7��� �ִ� ��� ���� ȿ���� �޶����ϴ�.",
				"&7Ȱ - ������ �ð��� ª�����ϴ�.",
				"&7���� - ����� ������ �ڵ����� �����˴ϴ�.",
				"",
				MsgTBL.Cooldown_Fixed);
		
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

		if (data.getPlayer().hasMetadata("malammo.skill.rapid_fire")) {
			data.getPlayer().removeMetadata("malammo.skill.rapid_fire", MalaMMO_Skill.plugin);
			data.getPlayer().sendMessage("��c[ �ӻ� ���� ]");
			return;
		}
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Rapid_Fire_Task(data.getPlayer(), cast.getModifier("stamina_cost")));
	}

	@EventHandler
	public void staminaHeal(PlayerResourceUpdateEvent event)
	{
		if (event.getPlayer().hasMetadata("malammo.skill.rapid_fire")) {
			if (event.getResource() == PlayerResource.STAMINA) {
				if (event.getAmount() > 0)
					event.setAmount(0);
			}
		}
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
	PlayerData playerData;
	Player player;
	Location loc;
	double staminaCost;
	
	public Rapid_Fire_Task(Player _player, double _stamina_cost)
	{
		player = _player;
		playerData = PlayerData.get(_player);
		staminaCost = _stamina_cost;

		player.sendMessage("��b[ �ӻ� �ߵ� ]");
		player.setMetadata("malammo.skill.rapid_fire", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
	}
	
	public void run()
	{
		// �¶���?
		if (!player.isOnline())
			return;
		
		// �ð� �� �� ���
		if (playerData.getStamina() <= staminaCost)
		{
			player.removeMetadata("malammo.skill.rapid_fire", MalaMMO_Skill.plugin);
			player.sendMessage("��c[ �ӻ� ���� ]");
			return;
		}
		if (!player.hasMetadata("malammo.skill.rapid_fire"))
			return;

		playerData.giveStamina(-staminaCost * 0.05, UpdateReason.SKILL_COST);
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
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}




