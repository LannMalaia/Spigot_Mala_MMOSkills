package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import mala.mmoskill.manager.Doppel;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Make_Doppel extends RegisteredSkill
{
	public Make_Doppel()
	{	
		super(new Make_Doppel_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(0, 0.5, 0, 20));
	}
	
	// �н� ���� ������ true
	public static boolean Try_Doppel_Make(PlayerData _data, SkillHandler<?> _skill)
	{
		if (!_data.getPlayer().isSneaking())
			return false;
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MAKE_DOPPEL");
		if (!_data.getProfess().hasSkill(skill))
			return false;
		int level = _data.getSkillLevel(skill);
		
		RegisteredSkill doppel_active = MMOCore.plugin.skillManager.getSkill("DOPPELGANGER");
		RegisteredSkill doppel_passive = MMOCore.plugin.skillManager.getSkill("MASTERY_DOPPEL");
		int active_level = _data.getSkillLevel(doppel_active);
		int passive_level = _data.getSkillLevel(doppel_passive);
		if(!_data.getProfess().hasSkill(doppel_active) && active_level < 10)
			return false;
		if(!_data.getProfess().hasSkill(doppel_passive) && passive_level < 10)
			return false;
		//_data.getPlayer().sendMessage("skillname = " + _skill.getLowerCaseId());
		
		if (level >= 10 && _skill.getLowerCaseId().equals("back-step"))
		{
			Buff_Manager.Add_Buff(_data.getPlayer(), PotionEffectType.INVISIBILITY, 0, 100, null);
			Doppel.Spawn_Doppel(_data, _data.getPlayer().getLocation());
			return true;
		}
		else if (level >= 15 && _skill.getLowerCaseId().equals("back-attack"))
		{
			Buff_Manager.Add_Buff(_data.getPlayer(), PotionEffectType.INVISIBILITY, 0, 100, null);
			Doppel.Spawn_Doppel(_data, _data.getPlayer().getLocation());
			return true;
		}
		else if (level >= 20 && _skill.getLowerCaseId().equals("dagger-throw-mult"))
		{
			Buff_Manager.Add_Buff(_data.getPlayer(), PotionEffectType.INVISIBILITY, 0, 100, null);
			Doppel.Spawn_Doppel(_data, _data.getPlayer().getLocation());
			return true;
		}
		else if (level >= 25 && _skill.getLowerCaseId().equals("dagger-throw"))
		{
			Buff_Manager.Add_Buff(_data.getPlayer(), PotionEffectType.INVISIBILITY, 0, 100, null);
			Doppel.Spawn_Doppel(_data, _data.getPlayer().getLocation());
			return true;
		}
		
		return false;
	}
}

class Make_Doppel_Handler extends MalaPassiveSkill implements Listener
{
	public Make_Doppel_Handler()
	{
		super(	"MAKE_DOPPEL",
				"�ܻ� �����",
				Material.KNOWLEDGE_BOOK,
				MsgTBL.NeedSkills,
				"&e �н� �����͸� - lv.10",
				"&e �׸��� �нż� - lv.10",
				"",
				"&7��ũ�� ä ��ų�� ����� ������ �н��� ����ϴ�.",
				"&7�н��� ������� ������ �ڽ��� 5�ʰ� ����ȭ ���°� �˴ϴ�.",
				"&eLv.10 - ���� (����ƮX)",
				"&eLv.15 - �Ĺ���� (����ƮX)",
				"&eLv.20 - ǥâ ����",
				"&eLv.25 - ǥâ ��ô",
				"&eLv.30 - ���� �Ϲ� ����(5%)",
				"&c�ؿ�ũ���� �ʰ� ��ų�� ����� ��� �нú갡 �ߵ����� �ʽ��ϴ�.");
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void normal_attack_doppel(PlayerAttackEvent event)
	{
		if (event.isCancelled())
			return;
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("MAKE_DOPPEL");
		
		if (event.getDamage().hasType(DamageType.PHYSICAL)
				&& event.getDamage().hasType(DamageType.WEAPON)
				&& !event.getDamage().hasType(DamageType.SKILL))
		{
			PlayerData data = PlayerData.get(event.getPlayer());
			if (!data.getPlayer().isSneaking())
				return;

			RegisteredSkill doppel_active = MMOCore.plugin.skillManager.getSkill("DOPPELGANGER");
			RegisteredSkill doppel_passive = MMOCore.plugin.skillManager.getSkill("MASTERY_DOPPEL");
			int active_level = data.getSkillLevel(doppel_active);
			int passive_level = data.getSkillLevel(doppel_passive);
			if(!data.getProfess().hasSkill(doppel_active) && active_level < 10)
				return;
			if(!data.getProfess().hasSkill(doppel_passive) && passive_level < 10)
				return;
			
			if (data.getProfess().hasSkill(skill))
			{
				int level = data.getSkillLevel(skill);
				if (level >= 30 && Math.random() * 100.0 < 5.0)
				{
					Buff_Manager.Add_Buff(data.getPlayer(), PotionEffectType.INVISIBILITY, 0, 100, null);
					Doppel.Spawn_Doppel(data, data.getPlayer().getLocation());
				}
			}
		}
	}
}
