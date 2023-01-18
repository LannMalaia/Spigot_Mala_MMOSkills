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

public class Cast_Glacia extends RegisteredSkill
{
	private static Cast_Glacia instance;
	
	public Cast_Glacia()
	{	
		super(new Cast_Glacia_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(0.0, 0));
		addModifier("mana", new LinearValue(2.2, 2.2));
		
		instance = this;
	}
	public static Cast_Glacia getInstance() {
		return instance;
	}
	public static int getLevel(PlayerData playerData) {
		return playerData.getSkillLevel(instance);
	}
}

class Cast_Glacia_Handler extends MalaSkill implements Listener
{
	public Cast_Glacia_Handler()
	{
		super(	"CAST_GLACIA",
				"글래시아",
				Material.SOUL_LANTERN,
				MsgTBL.SpellChainSkill,
				"",
				"&7냉기의 마법을 준비합니다.",
				"&7스킬 레벨에 따라 관련 영창 마법의 위력도 증가합니다.",
				"",
				MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Player player = data.getPlayer();
		
		// 효과
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 2);
		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
		CastSpellSkill_Manager.Get_Instance().PutSpellChain(player, SpellChainType.ICE);
	}
}
