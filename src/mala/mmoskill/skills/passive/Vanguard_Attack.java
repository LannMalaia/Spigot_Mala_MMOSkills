package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Vanguard_Attack extends RegisteredSkill
{
	public Vanguard_Attack()
	{	
		super(new Vanguard_Attack_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(0.75, 0.75));
	}
}

class Vanguard_Attack_Handler extends MalaPassiveSkill implements Listener
{
	public Vanguard_Attack_Handler()
	{
		super(	"VANGUARD_ATTACK",
				"����� �ϰ�",
				Material.GRAY_BANNER,
				"&7�ڽ��� �� ���̶� ������ ����",
				"&7�޴� ���ط��� ���������� &e{percent}&7% �����մϴ�.",
				"&7�÷��̾�Դ� �ش���� �ʽ��ϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_vanguard(PlayerAttackEvent event)
	{
		if (event.getEntity().hasMetadata("malammo.skill.vanguard_attack"))
		{
			double mult = event.getEntity().getMetadata("malammo.skill.vanguard_attack").get(0).asDouble();
			event.getDamage().multiplicativeModifier(mult);
			return;
		}
		
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("VANGUARD_ATTACK");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		if (event.getEntity() instanceof Player)
			return;
		
		double per = 1.0 + skill.getModifier("percent", level) * 0.01;
		event.getEntity().setMetadata("malammo.skill.vanguard_attack", new FixedMetadataValue(MalaMMO_Skill.plugin, per));
	}
}
