package mala.mmoskill.skills.passive;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.player.inventory.EquippedItem;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Weapon_Identify;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;

public class Crossbow_Recharge extends RegisteredSkill
{
	public static RegisteredSkill skill;
	
	public Crossbow_Recharge()
	{	
		super(new Crossbow_Recharge_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("percent", new LinearValue(2, 2));
		skill = this;
	}
}

class Crossbow_Recharge_Handler extends MalaPassiveSkill implements Listener
{
	public Crossbow_Recharge_Handler()
	{
		super(	"CROSSBOW_RECHARGE",
				"빨리 뽑기",
				Material.KNOWLEDGE_BOOK,
				"&7석궁으로 사격시 &e{percent}&7%의 확률로 일반 화살을 자동 장전합니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		
	}

	@EventHandler
	public void passive_Crossbow_Recharge(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();
		PlayerData data = PlayerData.get(player);

		// 스킬 체크
		if (!data.getProfess().hasSkill(Crossbow_Recharge.skill))
			return;

		int level = data.getSkillLevel(Crossbow_Recharge.skill);
		double percent = Crossbow_Recharge.skill.getModifier("percent", level) * 0.01;
		
		if (Weapon_Identify.Hold_Crossbow(player) && Math.random() <= percent)
		{
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, () ->
			{
				ItemStack crossbow = player.getInventory().getItemInMainHand();
				CrossbowMeta cbmeta = (CrossbowMeta)crossbow.getItemMeta();
				ArrayList<ItemStack> list = new ArrayList<ItemStack>();
				list.add(new ItemStack(Material.ARROW));
				cbmeta.setChargedProjectiles(list);
				crossbow.setItemMeta(cbmeta);
				player.updateInventory();
				player.playSound(player, Sound.ITEM_CROSSBOW_LOADING_END, 1f, 2f);
			}, 1);
		}
	}

}
