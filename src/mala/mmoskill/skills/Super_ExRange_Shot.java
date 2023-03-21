package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Super_ExRange_Shot extends RegisteredSkill
{
	public Super_ExRange_Shot()
	{	
		super(new Super_ExRange_Shot_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(95, 25));
		addModifier("cooldown", new LinearValue(29.75, -0.25, 10, 30));
		addModifier("stamina", new LinearValue(22, 2, 0, 50));
	}
}

class Super_ExRange_Shot_Handler extends MalaSkill implements Listener
{
	public Super_ExRange_Shot_Handler()
	{
		super(	"SUPER_EXRANGE_SHOT",
				"�ʱ��� ȭ��",
				Material.COMPARATOR,
				MsgTBL.NeedSkills,
				"&e ���� ȭ�� - lv.15",
				"",
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7�ʱ��� ȭ���� �����մϴ�.",
				"&7�ʱ��� ȭ���� ���� 3�������� ���ư��� {damage}�� ���ظ� �ݴϴ�.", 
				"&cȰ�̳� ������ ��� �־�� �մϴ�.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if (!Skill_Util.Has_Skill(data, "EXRANGE_SHOT", 15))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		if (!(Weapon_Identify.Hold_Bow(data.getPlayer()) || Weapon_Identify.Hold_Crossbow(data.getPlayer()))) {
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double damage = cast.getModifier("damage"); // ���ط�
		Player player = data.getPlayer();
		
		// ȿ��
		player.getWorld().playSound(player.getEyeLocation(), "mala_sound:skill.bow1", 1, 1);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Edge_Arrow_Skill(cast, player, 40, damage, -45.0, true));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Edge_Arrow_Skill(cast, player, 40, damage, 0.0, true));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Edge_Arrow_Skill(cast, player, 40, damage, 45.0, true));
	}
}