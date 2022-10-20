package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Self_Heal extends RegisteredSkill
{
	public Self_Heal()
	{	
		super(new Self_Heal_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("damage", new LinearValue(4, 0.5));
		addModifier("cooldown", new LinearValue(60, 0));
		addModifier("stamina", new LinearValue(10, 0));
	}
}

class Self_Heal_Handler extends MalaSkill implements Listener
{
	public Self_Heal_Handler()
	{
		super(	"SELF_HEAL",
				"응급치료",
				Material.PAPER,
				"&8{damage}&7만큼 회복합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		LivingEntity target = data.getPlayer();
		double damage = cast.getModifier("damage");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Heal_Task(data.getPlayer(), (Player)target, damage));
	}
}
