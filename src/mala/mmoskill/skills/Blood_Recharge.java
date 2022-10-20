package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.Stance_Change.Stance_Type;
import mala.mmoskill.skills.passive.Gathering_Strike;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Blood_Recharge extends RegisteredSkill
{
	public static Blood_Recharge skill;
	
	public Blood_Recharge()
	{	
		super(new Blood_Recharge_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("hp_deal", new LinearValue(12, 2));
		addModifier("count", new LinearValue(14, 4));
		addModifier("hp_heal", new LinearValue(0.3, 0.075));
		addModifier("cooldown", new LinearValue(14.75, -0.25, 5.0, 20.0));
		
		skill = this;
	}
}

class Blood_Recharge_Handler extends MalaSkill implements Listener
{
	public Blood_Recharge_Handler()
	{
		super(	"BLOOD_RECHARGE",
				"원기충전",
				Material.BAKED_POTATO,
				MsgTBL.NeedSkills,
				"&e 스탠스 마스터리 - lv.10",
				"",
				"&c[ 버서크 스탠스 ]",
				"&cHP&7를 &e{hp_deal}&7 소모해 &e{count}&7의 혈흔을 얻습니다.",
				"&f[ &b이베이드 스탠스 &f& &7스탠스 해제 &f]",
				"&7혈흔을 전부 소모해 &e혈흔의 수 * {hp_heal}&7의 &cHP&7를 회복합니다.",
				MsgTBL.Cooldown);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "MASTERY_STANCE", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		
		int level = data.getSkillLevel(Blood_Recharge.skill);
		Stance_Type type = !data.getPlayer().hasMetadata(Stance_Change.meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(data.getPlayer().getMetadata(Stance_Change.meta_name).get(0).asString());
		if (type == Stance_Type.BERSERK)
		{
			double hp_deal = Blood_Recharge.skill.getModifier("hp_deal", level);
			if (data.getPlayer().getHealth() > hp_deal)
			{
				data.getPlayer().setHealth(Math.max(0.0, data.getPlayer().getHealth() - hp_deal));
				return new SimpleSkillResult(true);
			}
		}
		else
		{
			int stack = Stance_Change.Get_BloodStack(data.getPlayer());
			return new SimpleSkillResult(stack > 0);
		}
		return new SimpleSkillResult(false);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		Location loc = data.getPlayer().getLocation();
		int level = data.getSkillLevel(Blood_Recharge.skill);
		Stance_Type type = !data.getPlayer().hasMetadata(Stance_Change.meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(data.getPlayer().getMetadata(Stance_Change.meta_name).get(0).asString());
		if (type == Stance_Type.BERSERK)
		{
			int blood = (int)Blood_Recharge.skill.getModifier("count", level);
			Stance_Change.Add_BloodStack(data.getPlayer(), blood);
			loc.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_AMBIENT, 1, 0.5f);
			loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 2);
			loc.getWorld().spawnParticle(Particle.FLAME, loc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0);
		}
		else
		{
			int stack = Stance_Change.Get_BloodStack(data.getPlayer());
			Stance_Change.Set_BloodStack(data.getPlayer(), 0);
			double hp_heal = Blood_Recharge.skill.getModifier("hp_heal", level) * stack;
			data.getPlayer().setHealth(Math.min(data.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), data.getPlayer().getHealth() + hp_heal));
			loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
			loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
			loc.getWorld().spawnParticle(Particle.HEART, loc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0);
		}
		
		
	}
}
