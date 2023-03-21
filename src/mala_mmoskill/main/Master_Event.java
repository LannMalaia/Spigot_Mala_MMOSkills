package mala_mmoskill.main;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitRunnable;

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.damage.MeleeAttackMetadata;
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

	@EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void when_attack(PlayerAttackEvent event)
	{
		Player player = event.getAttacker().getPlayer();
		if (event.getAttack() instanceof MeleeAttackMetadata)
			event.setCancelled(false);
		
		if (Weapon_Identify.Weapon_Restrict(player))
		{
			player.sendMessage("��c[ ���ظ� �� �� �����ϴ�. �� ���԰� ���� ������ Ȯ���ϼ���. ]");
			event.setCancelled(true);
		}
		
		
		if (player.isOp())
			return;
		
		if (player.isFlying())
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
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void whenDamage(EntityDamageEvent event)
	{
		if (!event.getEntity().getWorld().getName().equals("ArenaWorld"))
			return;
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();

		if (event.getDamage() > 500 && event.getCause() == DamageCause.ENTITY_ATTACK) {
			event.setDamage(50);
			
			long time = 0;
			if (DamagePreventer.playerDamagedTimes.containsKey(player)) {
				time = System.currentTimeMillis() - DamagePreventer.playerDamagedTimes.get(player);
				time = time / 100;
				if (time > 150.0)
					time = 0;
			}
			DamagePreventer.playerDamagedTimes.put(player, System.currentTimeMillis());
			if (time == 0)
				DamagePreventer.addLog(player, "[ ��� " + Calendar.getInstance().getTime().toString() + " ]");
			String log = "���ؽ���-" + (time == 0 ? "����" : String.format("%.1f��", (time * 0.1))) + "--"
					+ "����Ÿ��-" + event.getCause().toString() + "--"
					+ "��ҿ���-" + event.isCancelled() + "--"
					+ "�ܿ�HP-" + String.format("%.1f", player.getHealth()) + "--"
					+ "�����ð�-" + player.getNoDamageTicks() + "--"
					+ "���ط�-" + String.format("%.1f", event.getFinalDamage()) + "(" + String.format("%.1f", event.getDamage()) + ")";
			DamagePreventer.addLog(player, log);
		}
	}
}
