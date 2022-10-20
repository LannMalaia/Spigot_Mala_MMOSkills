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
				"스며들기",
				Material.FEATHER,
				"&7실명에 걸린 적의 공격을 &8{per}&7%의 확률로 회피합니다.",
				"&7재사용 대기시간이 없습니다.");
		
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

		 // 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill(skill))
			return;
		
		int level = data.getSkillLevel(skill);
		double per = skill.getModifier("per", level);
		
		if (Math.random() * 100.0 < per)
		{
			data.getPlayer().sendMessage("§e§l[ 스며들기 발동 ]");
			event.setCancelled(true);
		}
	}
}
