package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Danger_Pay extends RegisteredSkill
{
	public Danger_Pay()
	{	
		super(new Danger_Pay_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("percent", new LinearValue(1, 1));
	}
}

class Danger_Pay_Handler extends MalaPassiveSkill implements Listener
{
	public Danger_Pay_Handler()
	{
		super(	"DANGER_PAY",
				"�������",
				Material.CROSSBOW,
				"&7����ü �⺻ ������ ������ ��, ������ �Ÿ���",
				"&e10m&7���� �����ٸ� &e{percent}&7%�� �߰� ���ظ� �ݴϴ�.",
				"&e40m&7���� �ִٸ� &c{percent}&7%��ŭ ���ط��� �����մϴ�.");
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_danger_pay(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Arrow)
			|| !event.getDamager().hasMetadata("malammo.skill.crossbow"))
			return;
		
		Arrow arrow = (Arrow)event.getDamager();
		Player player = (Player)arrow.getShooter();
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("DANGER_PAY");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;
		
		// �Ÿ� üũ
		double distance = event.getEntity().getLocation().distance(player.getLocation());
		double per = 0.0;
		if (distance <= 10.0)
			per = skill.getModifier("percent", level);
		if (distance >= 40.0)
			per = -skill.getModifier("percent", level);
		event.setDamage(event.getDamage() * (1.0 + per * 0.01));
	}
}

