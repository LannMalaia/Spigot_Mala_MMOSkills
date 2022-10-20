package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Rushing_Arrow extends RegisteredSkill
{
	public Rushing_Arrow()
	{	
		super(new Rushing_Arrow_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(0, 0.5, 0, 20));
	}
}

class Rushing_Arrow_Handler extends MalaPassiveSkill implements Listener
{
	public Rushing_Arrow_Handler()
	{
		super(	"RUSHING_ARROW",
				"�⵵�ϴ� ȭ��",
				Material.TARGET,
				"&7��� ����ü ������ ���� ���� �ð��� �����մϴ�.",
				"&eLv.20&7���� �ߵ��մϴ�.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void attack_rushing_arrow(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);

		// ���� üũ
		if (!event.getDamage().hasType(DamageType.PROJECTILE))
			return;
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("RUSHING_ARROW");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;
		if (level < 20)
			return;
		
		event.getEntity().setNoDamageTicks(0);
	}
	
	/*
	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_rushing_arrow_2(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Arrow))
			return;
		if (!(event.getEntity() instanceof LivingEntity))
			return;
		Arrow arrow = (Arrow)event.getDamager();
		if (!(arrow.getShooter() instanceof Player))
			return;
		
		Player player = (Player)arrow.getShooter();
		player.sendMessage("ede 1");
		PlayerData data = PlayerData.get(player);
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("RUSHING_ARROW");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		player.sendMessage("ede 2");
		// ���� �ð� üũ
		LivingEntity entity = (LivingEntity)event.getEntity();
		int tick = (int)(skill.getModifier("second", level) * 20.0);
				
		int remained = entity.getNoDamageTicks();
		entity.setNoDamageTicks(remained - tick);
		player.sendMessage("ede 3");
	}
	*/
}
