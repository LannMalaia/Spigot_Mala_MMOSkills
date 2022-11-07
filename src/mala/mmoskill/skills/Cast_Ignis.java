package mala.mmoskill.skills;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.ArrowSkill_Manager;
import mala.mmoskill.manager.CastSpellSkill_Manager;
import mala.mmoskill.manager.SpellChainType;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Cast_Ignis extends RegisteredSkill
{
	public Cast_Ignis()
	{	
		super(new Cast_Ignis_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(0.0, 0));
		addModifier("mana", new LinearValue(0, 0));
	}
}

class Cast_Ignis_Handler extends MalaSkill implements Listener
{
	public Cast_Ignis_Handler()
	{
		super(	"CAST_IGNIS",
				"이그니스",
				Material.CAMPFIRE,
				MsgTBL.SpellChainSkill,
				"",
				"&7화염의 마법을 준비합니다.",
				"&7스킬 레벨에 따라 관련 영창 마법의 위력도 증가합니다.",
				"",
				MsgTBL.Cooldown);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Player player = data.getPlayer();
		
		// 효과
		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1);
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1);
		CastSpellSkill_Manager.Get_Instance().PutSpellChain(player, SpellChainType.FIRE);
	}
}
