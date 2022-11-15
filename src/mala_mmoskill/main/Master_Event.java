package mala_mmoskill.main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.Weapon_Identify;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;

public class Master_Event implements Listener
{
	@EventHandler
	public void whenReload(MythicReloadedEvent event)
	{
		Bukkit.getConsoleSender().sendMessage("��b�̍� ���ε��");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Runnable() {
			
			@Override
			public void run() {
				Bukkit.getConsoleSender().sendMessage("Ȱ��ȭ ���� - " + event.getInstance().isEnabled());
				if (event.getInstance().isEnabled())
					MalaMMO_Skill.plugin.Add_Skills();
				else
					Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			}
		});
	}
	@EventHandler
	public void whenReload(MythicMechanicLoadEvent event)
	{
//		Bukkit.getConsoleSender().sendMessage("��b��ī�� ����");
	}
	@EventHandler
	public void whenReload(MythicConditionLoadEvent event)
	{
//		Bukkit.getConsoleSender().sendMessage("��b����� ����");
	}
	@EventHandler
	public void whenReload(MythicDropLoadEvent event)
	{
//		Bukkit.getConsoleSender().sendMessage("��b����� ����");
	}
	
	@EventHandler
	public void when_join(PlayerJoinEvent event)
	{
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, () ->
		{
			PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
			PermissionAttachment attachment = event.getPlayer().addAttachment(MalaMMO_Skill.plugin);
			attachment.setPermission(data.getProfess().getName(), true);
			Bukkit.getConsoleSender().sendMessage(event.getPlayer().getName() + "���� " + data.getProfess().getName() + " ������ �ӽ÷� �ο��߾��." );

			int remained_point = data.getLevel() - 1;
			for (ClassSkill si : data.getProfess().getSkills())
			{
				// event.getPlayer().sendMessage(si.getSkill().getName() + "::" + si.getUnlockLevel());
				
				remained_point -= data.getSkillLevel(si.getSkill()) - 1;
			}
			if (remained_point < 0 && !event.getPlayer().hasPermission("*"))
			{
				event.getPlayer().sendMessage("��b������ ���� ����ġ�� ���� ��ų�� ������׽��ϴ�.");
				MalaMMO_Skill.Reset_Player_Skills(event.getPlayer());
			}
		}, 40);
		
	}
	
	@EventHandler
	public void when_exit_vehicle(VehicleExitEvent event)
	{
		if (event.getExited() instanceof Player)
		{
			if (event.getExited().hasMetadata("malaskill.charging"))
				event.setCancelled(true);
		}
	}

	@EventHandler
	public void when_attack(PlayerAttackEvent event)
	{
		if (Weapon_Identify.Weapon_Restrict(event.getPlayer()))
		{
			event.getPlayer().sendMessage("��c[ ���ظ� �� �� �����ϴ�. �� ���԰� ���� ������ Ȯ���ϼ���. ]");
			event.setCancelled(true);
		}
		
		if (event.getPlayer().hasPermission("*"))
			return;
		
		if (event.getPlayer().isFlying())
			event.setCancelled(true);
	}

	@EventHandler
	public void when_shoot(EntityShootBowEvent event)
	{
		if (event.getEntity().hasPermission("*"))
			return;
		
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			if (player.isFlying())
				event.setCancelled(true);

			if (Weapon_Identify.Hold_Crossbow(player))
				event.getProjectile().setMetadata("malammo.skill.crossbow", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			if (Weapon_Identify.Hold_Bow(player))
				event.getProjectile().setMetadata("malammo.skill.bow", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			
		}
	}

	@EventHandler (priority = EventPriority.MONITOR)
	public void when_damaged_by_arrow(EntityDamageByEntityEvent event)
	{
		Player attacker = null;
		Player target = null;
		if (event.getDamager() instanceof Projectile)
		{
			Projectile arrow = (Projectile)event.getDamager();
			if (arrow.getShooter() instanceof Player)
			{
				Player player = (Player)arrow.getShooter();
				attacker = player;
				
				if (!event.getEntity().hasPermission("*"))
				{
					if(player.isFlying())
					{
						event.setCancelled(true);
						return;
					}
				}
			}
		}
		if (event.getDamager() instanceof Player)
			attacker = (Player)event.getDamager();
		if (event.getEntity() instanceof Player)
			target = (Player)event.getEntity();
		
		// pvp ������ �ݰ�
		if (attacker != null && target != null)
		{
			if (event.getDamage() > 10)
			{
				// Bukkit.broadcastMessage("�ݰ� �� - " + event.getDamage());
				event.setDamage(Math.max(10, event.getDamage() * 0.3));
				// Bukkit.broadcastMessage("�ݰ� �� - " + event.getDamage());
			}
		}
		
	}
}
