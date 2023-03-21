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
				// 스킬 체크
				if (!Skill_Util.Has_Skill(data, "SUPER_SPEED", 1))
					continue;

				// 이속 증가 버프 체크
				PotionEffect pe = player.getPotionEffect(PotionEffectType.SPEED);
				if (pe == null)
					continue;
				if (pe.getAmplifier() < 3)
					continue;

				// 플레이어가 투명화 가졌는지 체크
				player.removePotionEffect(PotionEffectType.GLOWING);
				pe = player.getPotionEffect(PotionEffectType.INVISIBILITY);
				if (pe != null) {
					if (pe.getDuration() > 60) // 지속시간이 3초 이상 남은 경우 스킵
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
				"초속화",
				Material.FEATHER,
				"&7속도 증가 버프의 수준이 4 이상일 때,",
				"&7영구적으로 투명화가 적용됩니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
}
