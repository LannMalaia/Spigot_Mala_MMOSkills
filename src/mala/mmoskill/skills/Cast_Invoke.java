package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.ArrowSkill_Manager;
import mala.mmoskill.manager.CastChain;
import mala.mmoskill.manager.CastSpellSkill_Manager;
import mala.mmoskill.manager.SpellChainType;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Cast_Invoke extends RegisteredSkill
{
	private static Cast_Invoke instance;
	public Cast_Invoke()
	{	
		super(new Cast_Invoke_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("level", new LinearValue(0.1, 0.1, 1, 4));
		addModifier("cooldown", new LinearValue(0.0, 0));
		addModifier("mana", new LinearValue(0, 0));
		
		instance = this;
	}
	public static Cast_Invoke getInstance() {
		return instance;
	}
}

class Cast_Invoke_Handler extends MalaSkill implements Listener
{
	public Cast_Invoke_Handler()
	{
		super(	"CAST_INVOKE",
				"인보크",
				Material.NETHER_STAR,
				MsgTBL.SpellChainSkill,
				"",
				"&7마술식을 조합해 특정한 마법을 영창합니다.",
				"&7최대 &e{level}&7단계의 마법을 영창할 수 있습니다.",
				"&7마술식의 조합은 순서에 상관 없이 이루어집니다.",
				"",
				MsgTBL.Cooldown);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Player player = data.getPlayer();
		
		CastChain cc = CastSpellSkill_Manager.Get_Instance().getSpellChain(player);
		if (cc == null)
		{
			player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
			player.sendMessage("§c술식이 비었습니다.");
			return;
		}
		
		// 효과
		MalaSpellEffect spell = cc.findSpell();
		CastSpellSkill_Manager.Get_Instance().removeSpellChain(player);
		if (spell == null)
		{
			// 술식 실패
			player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1, 2f);
			player.sendMessage("§6§l[ §c§l마술식 실패 §6§l]");
		}
		else
		{
			// 술식 성공
			player.getWorld().playSound(player.getEyeLocation(), "mala_sound:skill.reinforce1", 1.5f, 1.2f);
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, spell);
		}
		
	}
}
