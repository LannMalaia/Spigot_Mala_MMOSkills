package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Dagger_Throw_Repeat extends RegisteredSkill
{
	public Dagger_Throw_Repeat()
	{	
		super(new Dagger_Throw_Repeat_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("count", new LinearValue(3.5, 0.5));
		addModifier("cooldown", new LinearValue(29.5, -0.5));
		addModifier("stamina", new LinearValue(12.4, 0.4));
	}
}

class Dagger_Throw_Repeat_Handler extends MalaSkill implements Listener
{
	public Dagger_Throw_Repeat_Handler()
	{
		super(	"DAGGER_THROW_REPEAT",
				"표창 연사",
				Material.MELON_SEEDS,
				MsgTBL.NeedSkills,
				"&e 표창 투척 - lv.10",
				"",
				MsgTBL.PHYSICAL + MsgTBL.PROJECTILE,
				"",
				"&8{count}&7개의 표창을 연달아 던집니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}
	
	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "DAGGER_THROW", 10))
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
		
		RegisteredSkill dagger_skill = MMOCore.plugin.skillManager.getSkill("DAGGER_THROW");
		
		int lv = data.getSkillLevel(dagger_skill);
		double distance = dagger_skill.getModifier("distance", lv);
		double damage = dagger_skill.getModifier("damage", lv);
		double additive = dagger_skill.getModifier("additive", lv);

		for (int i = 0; i < cast.getModifier("count"); i++)
		{
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, () ->
			{
				Vector dir = data.getPlayer().getEyeLocation().getDirection();
				Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
					new Dagger_Throw_Skill(data.getPlayer().getEyeLocation(), data.getPlayer(),
							dir, damage, additive, distance));
			}, i * 3);
		}
	}
	
}
