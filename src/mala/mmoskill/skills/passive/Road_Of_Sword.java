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
				"���� ��",
				Material.KNOWLEDGE_BOOK,
				"&7�ܰ�, ������ �������� ��,",
				"&7�ڽ��� ���� ���� ������ �ð��� 2�� �����մϴ�.",
				"&7�ִ� 10�б��� ������ �� �ֽ��ϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_Road_Of_Sword(PlayerAttackEvent event)
	{
		Player player = event.getAttacker().getPlayer();
		PlayerData data = PlayerData.get(player);
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("ROAD_OF_SWORD");
		if (!data.getProfess().hasSkill(skill))
			return;

		// ������ üũ
		if (!Weapon_Identify.Hold_MMO_Sword(player))
			return;

		Buff_Manager.Increase_All_Good_Buff(player, 40, 1200 * 10);
	}
}
