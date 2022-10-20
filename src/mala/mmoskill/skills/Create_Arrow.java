package mala.mmoskill.skills;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Create_Arrow extends RegisteredSkill
{
	public Create_Arrow()
	{	
		super(new Create_Arrow_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(120, 0));
		addModifier("count", new LinearValue(64, 16));
		addModifier("mana", new LinearValue(15, 0));
	}
}

class Create_Arrow_Handler extends MalaSkill implements Listener
{
	public Create_Arrow_Handler()
	{
		super(	"CREATE_ARROW",
				"ȭ�� ����",
				Material.ARROW,
				"&e������ ����&7�� ����Ͽ� ȭ���� ����ϴ�.", 
				"&7�� ���� &8{count}&7���� ���� �� �ֽ��ϴ�.",
				"&c������ �տ� ��� �־�� �մϴ�.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		ItemStack item = data.getPlayer().getInventory().getItemInMainHand();
		
		if (item.getType() != Material.OAK_LOG)
		{
			data.getPlayer().sendMessage("��cȭ���� ����� ���ؼ��� ������ ������ ��� �־�� �մϴ�.");
			return;
		}
		
		if (item.getAmount() == 1)
			item = null;
		else
			item.setAmount(item.getAmount() - 1);
		data.getPlayer().getInventory().setItemInMainHand(item);
		data.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, (int)cast.getModifier("count")));

		data.getPlayer().sendMessage("��eȭ���� ��������ϴ�.");
		data.getPlayer().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
	}
}
