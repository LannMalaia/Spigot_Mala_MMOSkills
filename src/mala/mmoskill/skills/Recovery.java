package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Recovery extends RegisteredSkill
{
	public Recovery()
	{	
		super(new Recovery_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("cooldown", new LinearValue(7, 0));
		addModifier("mana", new LinearValue(11, 1));
	}
}

class Recovery_Handler extends MalaTargetSkill implements Listener
{
	public Recovery_Handler()
	{
		super(	"RECOVERY",
				"리커버리",
				Material.EMERALD,
				"&720 거리 내 아군 한 명의 멀미, 실명, 허기를 치료합니다.",
				"&7스킬 레벨 5에서 나약함을 치료합니다.",
				"&7스킬 레벨 10에서 독을 치료합니다.",
				"&7스킬 레벨 15에서 구속을 치료합니다.",
				"&7스킬 레벨 20에서 시듦을 치료합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public TargetSkillResult getResult(SkillMetadata cast)
	{
		TargetSkillResult tsr = new TargetSkillResult(cast, range, InteractionType.SUPPORT_SKILL);
		
		if (tsr.isSuccessful(cast) && tsr.getTarget() instanceof Player)
			return tsr;
		return new TargetSkillResult(cast, 0.0, InteractionType.SUPPORT_SKILL);
	}

	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("RECOVERY");
				
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Recovery_Task(data.getPlayer(), (Player)_data.getTarget(), data.getSkillLevel(skill)));
	}
	
	public static List<PotionEffectType> Get_TypeList(int level)
	{
		List<PotionEffectType> list = new ArrayList<PotionEffectType>();

		list.add(PotionEffectType.CONFUSION);
		list.add(PotionEffectType.BLINDNESS);
		list.add(PotionEffectType.HUNGER);

		if(level >= 5)
			list.add(PotionEffectType.WEAKNESS);

		if(level >= 10)
			list.add(PotionEffectType.POISON);

		if(level >= 15)
			list.add(PotionEffectType.SLOW);

		if(level >= 20)
			list.add(PotionEffectType.WITHER);
		
		return list;
	}
}

class Recovery_Task implements Runnable
{
	Player target;
	double y = 0;
	int counter = 2;
	
	public Recovery_Task(Player _player, Player _target, int _level)
	{
		target = _target;
		
		target.sendMessage("§b§l[" + _player.getDisplayName() + "§b§l님의 도움으로 디버프를 회복했습니다. ]");
		for(PotionEffectType type : Recovery_Handler.Get_TypeList(_level))
			Buff_Manager.Remove_Buff(_target, type);
	}
	
	public void run()
	{
		Location origin_loc = target.getLocation().clone();
		Location loc = target.getLocation().clone();
		for(double angle = 0; angle < 360.0; angle += 12)
		{
			loc.setX(origin_loc.getX() + Math.cos(Math.toRadians(angle)) * 1.5);
			loc.setY(origin_loc.getY() + y);
			loc.setZ(origin_loc.getZ() + Math.sin(Math.toRadians(angle)) * 1.5);
			loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
		}
		loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
		
		y += 0.4;
		
		if(counter-- > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
	}
}
