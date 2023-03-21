package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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

public class Dog_Growth extends RegisteredSkill
{
	public static Dog_Growth skill;
	
	public Dog_Growth()
	{	
		super(new Dog_Growth_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage", new LinearValue(0.6, 0.1));
		skill = this;
	}
	
	public static double Get_Damage(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (!Skill_Util.Has_Skill(data, "MASTERY_DOG", 10))
			return 0.0;
		if (!data.getProfess().hasSkill(skill))
			return 0.0;
		return skill.getModifier("damage", data.getSkillLevel(skill));
	}
}

class Dog_Growth_Handler extends MalaPassiveSkill implements Listener
{
	public Dog_Growth_Handler()
	{
		super(	"DOG_GROWTH",
				"요즘 애들은 빨리 커",
				Material.GHAST_TEAR,
				MsgTBL.NeedSkills,
				"&e 조련술 - lv.10",
				"",
				"&7탐색견, 사냥견, 투견이 적을 공격한 경우,",
				"&7공격력이 &e{damage}&7 상승합니다.",
				"&7이 효과는 중첩됩니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@EventHandler
	public void When_Dog_Killed(EntityDamageByEntityEvent event)
	{
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
			AttributeModifier am = new AttributeModifier("mala.mmoskill.dog_growth", 0.0, Operation.ADD_NUMBER);
			for (AttributeModifier temp_am : wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers())
			{
				if (temp_am.getName().equals("mala.mmoskill.dog_growth"))
				{
					am = temp_am;
					wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).removeModifier(temp_am);
					break;
				}
			}
			AttributeModifier n_am = new AttributeModifier("mala.mmoskill.dog_growth",
					am.getAmount() + Dog_Growth.Get_Damage(player), Operation.ADD_NUMBER);
			wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(n_am);
		}
	}
}
