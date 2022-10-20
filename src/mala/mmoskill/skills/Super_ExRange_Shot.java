package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Super_ExRange_Shot extends RegisteredSkill
{
	public Super_ExRange_Shot()
	{	
		super(new Super_ExRange_Shot_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(95, 25));
		addModifier("cooldown", new LinearValue(29.75, -0.25, 10, 30));
		addModifier("stamina", new LinearValue(22, 2, 0, 50));
	}
}

class Super_ExRange_Shot_Handler extends MalaSkill implements Listener
{
	public Super_ExRange_Shot_Handler()
	{
		super(	"SUPER_EXRANGE_SHOT",
				"초광폭 화살",
				Material.COMPARATOR,
				MsgTBL.NeedSkills,
				"&e 광폭 화살 - lv.15",
				"",
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7초광폭 화살을 장전합니다.",
				"&7초광폭 화살은 전방 3방향으로 나아가며 {damage}의 피해를 줍니다.", 
				"&c다른 화살과 함께 사용할 수 없습니다.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "EXRANGE_SHOT", 15))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double damage = cast.getModifier("damage"); // 피해량
		Player player = data.getPlayer();
		
		// 효과
		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);

		player.sendMessage("§c§l[ 초광폭 화살 장전 ]");
		player.setMetadata("malammo.skill.super_edge_arrow_count", new FixedMetadataValue(MalaMMO_Skill.plugin, 1));
		player.setMetadata("malammo.skill.super_edge_arrow", new FixedMetadataValue(MalaMMO_Skill.plugin, damage));
		player.setMetadata("malammo.skill.current_arrow", new FixedMetadataValue(MalaMMO_Skill.plugin, "super_edge"));
	}

	@EventHandler
	public void explode_arrow(EntityShootBowEvent event)
	{
		if(!(event.getEntity() instanceof Player))
			return;
		if(!(event.getProjectile() instanceof Arrow))
			return;
		if(!(event.getBow().getType() == Material.BOW || event.getBow().getType() == Material.CROSSBOW))
			return;
		if(!event.getEntity().hasMetadata("malammo.skill.current_arrow"))
			return;
		if(!event.getEntity().getMetadata("malammo.skill.current_arrow").get(0).asString().equals("super_edge"))
			return;
			
		Player player = (Player)event.getEntity();
		int damage = player.getMetadata("malammo.skill.super_edge_arrow").get(0).asInt();
		
		player.removeMetadata("malammo.skill.super_edge_arrow", MalaMMO_Skill.plugin);
		player.removeMetadata("malammo.skill.current_arrow", MalaMMO_Skill.plugin);
		
		Arrow arrow = (Arrow)event.getProjectile();
		arrow.remove();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Edge_Arrow_Skill(player, 40, damage, -45.0, true));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Edge_Arrow_Skill(player, 40, damage, 0.0, true));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Edge_Arrow_Skill(player, 40, damage, 45.0, true));
	}
}