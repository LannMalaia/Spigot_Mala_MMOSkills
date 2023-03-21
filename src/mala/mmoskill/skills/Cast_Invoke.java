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
				"�κ�ũ",
				Material.NETHER_STAR,
				MsgTBL.SpellChainSkill,
				"",
				"&7�������� ������ Ư���� ������ ��â�մϴ�.",
				"&7�ִ� &e{level}&7�ܰ��� ������ ��â�� �� �ֽ��ϴ�.",
				"&7�������� ������ ������ ��� ���� �̷�����ϴ�.",
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
			player.sendMessage("��c������ ������ϴ�.");
			return;
		}
		
		// ȿ��
		MalaSpellEffect spell = cc.findSpell();
		CastSpellSkill_Manager.Get_Instance().removeSpellChain(player);
		if (spell == null)
		{
			// ���� ����
			player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1, 2f);
			player.sendMessage("��6��l[ ��c��l������ ���� ��6��l]");
		}
		else
		{
			// ���� ����
			player.getWorld().playSound(player.getEyeLocation(), "mala_sound:skill.reinforce1", 1.5f, 1.2f);
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, spell);
		}
		
	}
}
