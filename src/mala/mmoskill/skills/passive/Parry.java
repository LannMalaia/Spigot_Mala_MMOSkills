package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Parry extends RegisteredSkill
{
	public Parry()
	{	
		super(new Parry_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("time", new LinearValue(90, -1.5));
	}
}

class Parry_Handler extends MalaPassiveSkill implements Listener
{
	public Parry_Handler()
	{
		super(	"PARRY",
				"쳐내기",
				Material.IRON_INGOT,
				"&7{time}초마다 어떤 공격이든 한 번 막습니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_parry(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		
		if (event.getEntity().hasMetadata("malammo.skill.parry"))
			return;

		if (event.isCancelled())
			return;
		
		Player player = (Player)event.getEntity();
		PlayerData data = PlayerData.get(player);

		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("PARRY");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		event.setCancelled(true);
		event.getDamager().sendMessage("§c[ 상대가 공격을 쳐냈습니다! ]");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Parry_Cooldown(data.getPlayer(), skill.getModifier("time", level)));
	}
	
	class Parry_Cooldown implements Runnable
	{
		Player player;

		public Parry_Cooldown(Player _player, double _second)
		{
			player = _player;
			player.sendMessage("§c§l[ 쳐내기 발동 ]");
			player.setMetadata("malammo.skill.parry", new FixedMetadataValue(MalaMMO_Skill.plugin, _second));
			player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 2f);
		}
		
		public void run()
		{
			if (!player.hasMetadata("malammo.skill.parry"))
				return;
			
			double sec = player.getMetadata("malammo.skill.parry").get(0).asDouble();
			
			if (sec <= 0.0)
			{
				player.removeMetadata("malammo.skill.parry", MalaMMO_Skill.plugin);
				player.sendMessage("§c§l[ 쳐내기 준비 ]");
				return;
			}
			else
				player.setMetadata("malammo.skill.parry", new FixedMetadataValue(MalaMMO_Skill.plugin, sec - 0.1));
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
		}
	}
}
