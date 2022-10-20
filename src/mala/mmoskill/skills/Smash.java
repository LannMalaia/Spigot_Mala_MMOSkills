package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Smash extends RegisteredSkill
{
	public Smash()
	{	
		super(new Smash_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(15, 0));
		addModifier("power", new LinearValue(2, 0.4, 2, 5));
		addModifier("stamina", new LinearValue(15, 0));
	}
}

class Smash_Handler extends MalaSkill implements Listener
{
	public Smash_Handler()
	{
		super(	"SMASH",
				"밀치기",
				Material.BONE_MEAL,
				"&7다음 기본 공격이 {power}의 강도로 상대를 밀어냅니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void smash_attack(EntityDamageByEntityEvent event)
	{
		if(!(event.getDamager() instanceof Player))
			return;
		if(!(event.getEntity() instanceof LivingEntity))
			return;
		
		Player player = (Player)event.getDamager();
		if(!player.hasMetadata("malammo.skill.smash"))
			return;
		
		
		player.sendMessage("§c§l[ 밀치기 발동 ]");
		double per = player.getMetadata("malammo.skill.smash").get(0).asDouble();
		player.removeMetadata("malammo.skill.smash", MalaMMO_Skill.plugin);
		player.getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
		
		if (event.isCancelled())
			return;
		
		event.getEntity().setVelocity(player.getLocation().getDirection().multiply(per));
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double power = (int) cast.getModifier("power"); // 피해 증가치
		
		// 효과
		data.getPlayer().sendMessage("§b§l[ 밀치기 준비 ]");
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
		data.getPlayer().getWorld().spawnParticle(Particle.LAVA, data.getPlayer().getLocation().add(0, data.getPlayer().getHeight() / 2, 0), 20, 0, 0, 0, 0);
		
		data.getPlayer().setMetadata("malammo.skill.smash", new FixedMetadataValue(MalaMMO_Skill.plugin, power));
	}
}
