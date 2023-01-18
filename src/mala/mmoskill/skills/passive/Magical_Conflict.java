package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.events.SpellCastEvent;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Magical_Conflict extends RegisteredSkill
{
	public Magical_Conflict()
	{	
		super(new Magical_Conflict_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(96, -3));
	}
}

class Magical_Conflict_Handler extends MalaPassiveSkill implements Listener
{
	public Magical_Conflict_Handler()
	{
		super(	"MAGICAL_CONFLICT",
				"���� �� �浹",
				Material.KNOWLEDGE_BOOK,
				"&7�ֺ� 50m�� �ش� ��ų�� ���� �÷��̾ ���� ���,",
				"&7������ ������ &c{percent}&7% �����մϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void castSpell(SpellCastEvent event)
	{
		if (event.getCaster() instanceof Player) {
			Player player = (Player)event.getCaster();
			for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
				if (!(entity instanceof Player))
					continue;
				Player target = (Player)entity;
				if (!PlayerData.has(target))
					continue;
				PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(target);
				if (data == null)
					continue;
				if (Skill_Util.Has_Skill(data, "MAGICAL_CONFLICT", 1)) {
					player.sendMessage("��c������ ������ �浹�� ������ ������ ��������.");
					target.sendMessage("��d�������� ���� �� �浹�� ����Ű�� �ִ�.");
					event.getSpell().spellPower = 0.1;
					break;
				}
			}
		}
	}
	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack(PlayerAttackEvent event)
	{
		if (!Skill_Util.Has_Skill(PlayerData.get(event.getPlayer()), "MAGICAL_CONFLICT", 1))
			return;
		if (!event.getAttack().getDamage().hasType(DamageType.MAGIC))
			return;
		Player player = event.getPlayer();
		for (Entity entity : player.getNearbyEntities(50, 50, 50)) {
			if (!(entity instanceof Player))
				continue;
			Player target = (Player)entity;
			if (!PlayerData.has(target))
				continue;
			PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(target);
			if (Skill_Util.Has_Skill(data, "MAGICAL_CONFLICT", 1)) {
				player.sendMessage("��c������ ������ �浹�� ������ ������ ��������.");
				event.getAttack().getDamage().multiplicativeModifier(0.1);
				break;
			}
		}
	}
}
