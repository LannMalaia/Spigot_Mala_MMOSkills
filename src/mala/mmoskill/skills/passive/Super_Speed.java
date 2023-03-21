package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Super_Speed extends RegisteredSkill
{
	public static Super_Speed skill;
	public Super_Speed()
	{	
		super(new Super_Speed_Handler(), MalaMMO_Skill.plugin.getConfig());
		skill = this;

		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new SuperSpeedManager(), 100);
	}
	
	class SuperSpeedManager implements Runnable {
		public void run() {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerData data = PlayerData.get(player);
				// ��ų üũ
				if (!Skill_Util.Has_Skill(data, "SUPER_SPEED", 1))
					continue;

				// �̼� ���� ���� üũ
				PotionEffect pe = player.getPotionEffect(PotionEffectType.SPEED);
				if (pe == null)
					continue;
				if (pe.getAmplifier() < 3)
					continue;

				// �÷��̾ ����ȭ �������� üũ
				player.removePotionEffect(PotionEffectType.GLOWING);
				pe = player.getPotionEffect(PotionEffectType.INVISIBILITY);
				if (pe != null) {
					if (pe.getDuration() > 60) // ���ӽð��� 3�� �̻� ���� ��� ��ŵ
						continue;
				}

				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0));
			}
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
		}
	}
}

class Super_Speed_Handler extends MalaPassiveSkill implements Listener
{
	public Super_Speed_Handler()
	{
		super(	"SUPER_SPEED",
				"�ʼ�ȭ",
				Material.FEATHER,
				"&7�ӵ� ���� ������ ������ 4 �̻��� ��,",
				"&7���������� ����ȭ�� ����˴ϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
}
