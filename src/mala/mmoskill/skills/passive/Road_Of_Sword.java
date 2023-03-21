package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Road_Of_Sword extends RegisteredSkill
{
	public Road_Of_Sword()
	{	
		super(new Road_Of_Sword_Handler(), MalaMMO_Skill.plugin.getConfig());
	}
}

class Road_Of_Sword_Handler extends MalaPassiveSkill implements Listener
{
	public Road_Of_Sword_Handler()
	{
		super(	"ROAD_OF_SWORD",
				"검의 길",
				Material.KNOWLEDGE_BOOK,
				"&7단검, 검으로 공격했을 때,",
				"&7자신이 가진 좋은 버프의 시간이 2초 증가합니다.",
				"&7최대 10분까지 증가할 수 있습니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_Road_Of_Sword(PlayerAttackEvent event)
	{
		Player player = event.getAttacker().getPlayer();
		PlayerData data = PlayerData.get(player);
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("ROAD_OF_SWORD");
		if (!data.getProfess().hasSkill(skill))
			return;

		// 아이템 체크
		if (!Weapon_Identify.Hold_MMO_Sword(player))
			return;

		Buff_Manager.Increase_All_Good_Buff(player, 40, 1200 * 10);
	}
}
