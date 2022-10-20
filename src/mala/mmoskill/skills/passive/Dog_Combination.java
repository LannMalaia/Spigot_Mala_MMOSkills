package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Dog_Combination extends RegisteredSkill
{
	public static Dog_Combination skill;
	
	public Dog_Combination()
	{	
		super(new Dog_Combination_Handler(), MalaMMO_Skill.plugin.getConfig());
		skill = this;
	}
	
	public static boolean Can_Init(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!Skill_Util.Has_Skill(data, "MASTERY_DOG", 10)
			|| !Skill_Util.Has_Skill(data, "DOMINATE", 10))
			return false;
		if (!data.getProfess().hasSkill(skill))
			return false;
		return data.getSkillLevel(skill) >= 20;
	}
}

class Dog_Combination_Handler extends MalaPassiveSkill implements Listener
{
	public Dog_Combination_Handler()
	{
		super(	"DOG_COMBINATION",
				"���� ���",
				Material.GOLDEN_HOE,
				MsgTBL.NeedSkills,
				"&e ���ü� - lv.10",
				"&e ������ �ϰ� - lv.10",
				"",
				"&7Ž����, ��ɰ�, ������ ���� ������ ���,",
				"&7������ �ϰ��� �ʱ�ȭ�˴ϴ�.",
				"&7�ٸ� �÷��̾���� ������ �ϰݵ� �ʱ�ȭ�˴ϴ�.",
				"&eLv.20&7���� �ߵ��մϴ�.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler
	public void When_Dog_Attack(EntityDamageByEntityEvent event)
	{
		if (!event.getEntity().hasMetadata("malammo.skill.dominate"))
			return;
		if (event.getDamager() instanceof Wolf)
		{
			Wolf wolf = (Wolf)event.getDamager();
			if (!wolf.isTamed())
				return;
			if (!wolf.hasMetadata("summoned"))
				return;
			if (!(wolf.getOwner() instanceof Player))
				return;
			Player player = (Player)wolf.getOwner();
			if (Dog_Combination.Can_Init(player))
			{
				event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 2, 1.5f);
				event.getEntity().removeMetadata("malammo.skill.dominate", MalaMMO_Skill.plugin);
				player.removeMetadata("malammo.skill.dominate_use", MalaMMO_Skill.plugin);
			}
		}
	}
}
