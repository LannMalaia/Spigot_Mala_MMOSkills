package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.api.event.skill.PlayerCastSkillEvent;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;

public class Trick extends RegisteredSkill
{
	public static Trick skill;
	public static final String trickstack_name = "mala.mmoskill.trick.trickstack";
	public static final String speed_modifier_name = "mala.mmoskill.trick.speedmod";
	
	public Trick()
	{	
		super(new Trick_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage", new LinearValue(0.15, 0.15));
		addModifier("speed", new LinearValue(0.15, 0.15));
		addModifier("reduce", new LinearValue(10, 0));
		skill = this;
		Bukkit.getScheduler().runTaskTimer(MalaMMO_Skill.plugin, new Trick_Manager(), 0, 40);
	}

	public static int Get_Stack(Player player)
	{
		return !player.hasMetadata(trickstack_name) ? 0 : player.getMetadata(trickstack_name).get(0).asInt();
	}
	public static void Add_Stack(Player player, int additive)
	{
		int stack = !player.hasMetadata(trickstack_name) ? 0 : player.getMetadata(trickstack_name).get(0).asInt();
		stack = Math.min(Mastery_Trick.Get_Max_Stack(player), stack + additive);
		player.setMetadata(trickstack_name, new FixedMetadataValue(MalaMMO_Skill.plugin,
						Math.min(Mastery_Trick.Get_Max_Stack(player), stack)));
		
	}
	public static void Sub_Stack(Player player, int sub)
	{
		int stack = !player.hasMetadata(trickstack_name) ? 0 : player.getMetadata(trickstack_name).get(0).asInt();
		if (stack - sub <= 0)
			player.removeMetadata(trickstack_name, MalaMMO_Skill.plugin);
		else
			player.setMetadata(trickstack_name, new FixedMetadataValue(MalaMMO_Skill.plugin,
						Math.max(0, stack - sub)));
		
	}
	public static void Set_Stack(Player player, int num)
	{
		player.setMetadata(trickstack_name,
				new FixedMetadataValue(MalaMMO_Skill.plugin,
				Math.min(Mastery_Trick.Get_Max_Stack(player), num)));
		
	}
	
	public class Trick_Manager implements Runnable
	{
		public Trick_Manager()
		{
		}
		
		public void run()
		{
			for (Player player : Bukkit.getServer().getOnlinePlayers())
			{
				// 연계량 관리
				if (player.hasMetadata(trickstack_name))
					Trick.Sub_Stack(player, 1);

				// 연계 이속 관리
				if (player.hasMetadata(trickstack_name))
				{
					PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
					if (!Skill_Util.Has_Skill(data, "TRICK", 1))
						continue;
					
					int level = data.getSkillLevel(skill);
					int stack = player.getMetadata(trickstack_name).get(0).asInt();
					double speed = skill.getModifier("speed", level) * 0.01 * stack;
					AttributeModifier speed_mod = new AttributeModifier(speed_modifier_name, speed, Operation.MULTIPLY_SCALAR_1);
					
					for (AttributeModifier mod : player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers())
					{
						if (mod.getName().equals(speed_modifier_name))
							player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(mod);
					}
					//if (att_already_enabled)
					//	player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(speed_mod);
					player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(speed_mod);
				}
				else
				{
					for (AttributeModifier mod : player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers())
					{
						if (mod.getName().equals(speed_modifier_name))
						{
							player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(mod);
							break;
						}
					}
				}
			}

		}
	}
}

class Trick_Handler extends MalaPassiveSkill implements Listener
{
	public Trick_Handler()
	{
		super(	"TRICK",
				"트릭",
				Material.LARGE_AMETHYST_BUD,
				"&7스킬을 사용할 때마다 &a연계&7를 획득할 수 있습니다.",
				"&a연계&7량에 따라 모든 공격력이 &e{damage}&7%, 이동속도가 &e{speed}&7% 상승합니다.",
				"시간이 지남에 따라 &a연계&7량이 1씩 감소하며",
				"피해를 받을 때마다 &a연계&7량이 &e{reduce}&7 감소합니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void cast_Trick(PlayerCastSkillEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		if (!data.getProfess().hasSkill(Trick.skill))
			return;
		if (!(event.getCast().getHandler() instanceof MalaSkill))
			return;
		if (event.isCancelled())
			return;
		
		Trick.Add_Stack(player, 1 + Mastery_Trick.Get_Stack_Additive(player));
	}
	@EventHandler
	public void attack_Trick(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		if (!Skill_Util.Has_Skill(data, "TRICK", 1))
			return;
		
		int level = data.getSkillLevel(Trick.skill);
		double damage = Trick.skill.getModifier("damage", level) * Trick.Get_Stack(player);
		event.getDamage().multiplicativeModifier(1.0 + damage * 0.01);
	}
	@EventHandler
	public void damaged_Trick(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player)event.getEntity();
		if (!MMOCore.plugin.dataProvider.getDataManager().isLoaded(player.getUniqueId()))
			return;
		PlayerData data = PlayerData.get(player);
		
		// 스킬 체크
		int level = data.getSkillLevel(Trick.skill);
		if (!data.getProfess().hasSkill(Trick.skill))
			return;
		if (event.isCancelled())
			return;
		
		int reduce = (int)Trick.skill.getModifier("reduce", level);
		Trick.Sub_Stack(player, Math.max(0, reduce - Mastery_Trick.Get_Reduce(player)));
	}
}
