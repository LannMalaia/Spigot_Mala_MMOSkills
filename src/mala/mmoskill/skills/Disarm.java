package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;

public class Disarm extends RegisteredSkill
{
	public Disarm()
	{	
		super(new Disarm_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("count", new LinearValue(5, 1));
		addModifier("cooldown", new LinearValue(60, -1.5));
		addModifier("stamina", new LinearValue(25, 0));
	}
}

class Disarm_Handler extends MalaTargetSkill implements Listener
{
	public Disarm_Handler()
	{
		super(	"DISARM",
				"무장 해제",
				Material.FLINT,
				"&85&7블럭 거리내에 있는 대상의 무적 시간을 없앱니다.",
				"&8{count}&7번 피해를 받으면 해제됩니다.",
				"&7플레이어에게는 사용할 수 없습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void Disarm_attack(PlayerAttackEvent event)
	{
		if(!event.getEntity().hasMetadata("malammo.skill.disarm"))
			return;
				
		int count = event.getEntity().getMetadata("malammo.skill.disarm").get(0).asInt();
		event.getEntity().setNoDamageTicks(0);
		if(count > 0)
			event.getEntity().setMetadata("malammo.skill.disarm", new FixedMetadataValue(MalaMMO_Skill.plugin, count - 1));
		else
			event.getEntity().removeMetadata("malammo.skill.disarm", MalaMMO_Skill.plugin);
	}
	
	@Override
	public TargetSkillResult getResult(SkillMetadata cast)
	{
		TargetSkillResult tsr = new TargetSkillResult(cast, cast.getModifier("distance"), InteractionType.OFFENSE_ACTION);
		if (tsr.isSuccessful(cast) && !(tsr.getTarget() instanceof Player))
			return tsr;
		return new TargetSkillResult(cast, 0.0, InteractionType.OFFENSE_SKILL);
	}

	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		// 타겟 취득
		LivingEntity target = _data.getTarget();
		int count = (int)cast.getModifier("count");
		
		// 효과
		target.getWorld().playSound(target.getLocation(), Sound.ITEM_SHIELD_BREAK, 2, 0.5f);
		target.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, target.getLocation().add(0, target.getHeight(), 0), 20, 0.3, 0.3, 0.3, 0);
		
		target.setMetadata("malammo.skill.disarm", new FixedMetadataValue(MalaMMO_Skill.plugin, count));
	}
}
