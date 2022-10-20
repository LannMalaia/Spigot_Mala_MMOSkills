package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Bash extends RegisteredSkill
{
	public Bash()
	{	
		super(new Bash_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("dam_per", new LinearValue(43, 3, 0, 300));
		addModifier("stamina", new LinearValue(10, 2));
		addModifier("cooldown", new LinearValue(17.8, -0.2, 10.0, 20.0));
	}
}

class Bash_Handler extends MalaSkill implements Listener
{
	public Bash_Handler()
	{
		super(	"BASH",
				"강격",
				Material.NETHER_BRICK,
				"&7다음 기본 공격이 {dam_per}%의 추가 피해를 줍니다.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
		;
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void bash_attack(PlayerAttackEvent event)
	{
		if(!event.getPlayer().hasMetadata("malammo.skill.bash"))
			return;

		// 마법, 스킬은 취소
		if(event.getAttack().getDamage().hasType(DamageType.MAGIC) || event.getAttack().getDamage().hasType(DamageType.SKILL))
			return;

		event.getPlayer().sendMessage("§c§l[ 강격 발동 ]");
		double per = event.getPlayer().getMetadata("malammo.skill.bash").get(0).asDouble() * 0.01d;
		event.getAttack().getDamage().multiplicativeModifier(1.0 + per, DamageType.PHYSICAL);
		event.getPlayer().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
		event.getPlayer().removeMetadata("malammo.skill.bash", MalaMMO_Skill.plugin);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double dam_per = cast.getModifier("dam_per"); // 피해 증가치

		data.getPlayer().sendMessage("§b§l[ 강격 준비 ]");
		// 효과
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
		data.getPlayer().getWorld().spawnParticle(Particle.LAVA, data.getPlayer().getLocation().add(0, data.getPlayer().getHeight() / 2, 0), 20, 0, 0, 0, 0);
		
		data.getPlayer().setMetadata("malammo.skill.bash", new FixedMetadataValue(MalaMMO_Skill.plugin, dam_per));
	}
}
