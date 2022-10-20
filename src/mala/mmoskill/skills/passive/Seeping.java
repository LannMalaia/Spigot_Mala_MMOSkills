package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Seeping extends RegisteredSkill
{
	public Seeping()
	{	
		super(new Seeping_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(3.25, 1.25));
	}
}

class Seeping_Handler extends MalaPassiveSkill implements Listener
{
	public Seeping_Handler()
	{
		super(	"SEEPING",
				"������",
				Material.FEATHER,
				"&7�Ǹ� �ɸ� ���� ������ &8{per}&7%�� Ȯ���� ȸ���մϴ�.",
				"&7���� ���ð��� �����ϴ�.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler
	public void seeping_effect(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof LivingEntity))
			return;

		LivingEntity le = (LivingEntity)event.getDamager();
		if (!le.hasPotionEffect(PotionEffectType.BLINDNESS))
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get((Player)event.getEntity());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("SEEPING");

		 // ��ų�� �˰� ���� ������ ���
		if(!data.getProfess().hasSkill(skill))
			return;
		
		int level = data.getSkillLevel(skill);
		double per = skill.getModifier("per", level);
		
		if (Math.random() * 100.0 < per)
		{
			data.getPlayer().sendMessage("��e��l[ ������ �ߵ� ]");
			event.setCancelled(true);
		}
	}
}
