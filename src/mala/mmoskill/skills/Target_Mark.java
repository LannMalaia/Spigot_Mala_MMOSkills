package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;

public class Target_Mark extends RegisteredSkill
{
	public static Target_Mark skill;
	
	public Target_Mark()
	{	
		super(new Target_Mark_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(39, -1, 20, 40));
		addModifier("dam_per", new LinearValue(6.25, 1.25));
		addModifier("stamina", new LinearValue(10, 0.5));
		
		skill = this;
	}
	
	public static void Mark_Enemy(Player player, LivingEntity target)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		if (!Skill_Util.Has_Skill(data, "TARGET_MARK", 1))
			return;
		
		int level = data.getSkillLevel(skill);
		double dam_per = 1.0 + skill.getModifier("dam_per", level) * 0.01; // 피해 증가치

		// 효과
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 2);
		target.getWorld().spawnParticle(Particle.GLOW_SQUID_INK, target.getLocation().add(0, target.getHeight() + 0.5, 0), 1, 0, 0, 0, 0);
		Particle_Drawer.Draw_Circle(target.getEyeLocation(), Particle.SOUL, 1.5, Math.random() * 20.0, Math.random() * 360.0);
		target.sendMessage("§c§l[ " + data.getPlayer().getDisplayName() + "§c§l님의 표적이 되었습니다! ]");
		target.setMetadata("malammo.skill.target_mark." + data.getPlayer().getName(), new FixedMetadataValue(MalaMMO_Skill.plugin, dam_per));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Marking(target, data.getPlayer().getName(), 20));
	}
}


class Target_Mark_Handler extends MalaTargetSkill implements Listener
{
	public Target_Mark_Handler()
	{
		super(	"TARGET_MARK",
				"표식",
				Material.SWEET_BERRIES,
				"20m내 적에게 표식을 남깁니다.",
				"대상은 시전자의 공격에 &e{dam_per}&7% 만큼의 추가 피해를 받습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		range = 20.0;
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void target_mark_attack(PlayerAttackEvent event)
	{
		if(!event.getEntity().hasMetadata("malammo.skill.target_mark." + event.getPlayer().getName()))
			return;
		
		double per = event.getEntity().getMetadata("malammo.skill.target_mark." + event.getPlayer().getName()).get(0).asDouble();
		Location loc = event.getEntity().getEyeLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
		event.getAttack().getDamage().multiplicativeModifier(per);
	}

	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Target_Mark.Mark_Enemy(data.getPlayer(), _data.getTarget());
	}
}

class Marking implements Runnable
{
	LivingEntity m_Entity;
	int sec;
	String user_name;
	
	public Marking(LivingEntity _entity, String _username, int _sec)
	{
		m_Entity = _entity;
		sec = _sec;
		user_name = _username;
	}
	
	@Override
	public void run()
	{
		if(--sec == 0)
		{
			m_Entity.removeMetadata("malammo.skill.target_mark." + user_name, MalaMMO_Skill.plugin);
			m_Entity.sendMessage("§b§l[ 표적에서 벗어났습니다. ]");
		}
		else
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
	}		
}