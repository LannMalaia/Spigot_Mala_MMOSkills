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
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.Stance_Change.Stance_Type;
import mala.mmoskill.skills.passive.Gathering_Strike;
import mala.mmoskill.skills.passive.Trick;
import mala.mmoskill.util.Buff_Remover;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Lucky_Star extends RegisteredSkill
{
	public static Lucky_Star skill;
	
	public Lucky_Star()
	{	
		super(new Lucky_Star_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(20.0, 0.0));
		
		skill = this;
	}
}

class Lucky_Star_Handler extends MalaSkill implements Listener
{
	public Lucky_Star_Handler()
	{
		super(	"LUCKY_STAR",
				"럭키 스타",
				Material.NETHER_STAR,
				MsgTBL.NeedSkills,
				"&e 트릭 - lv.20",
				"",
				"&7모든 스킬의 재사용 대기시간을 초기화합니다.",
				"&eLv.5 - 디버프를 전부 제거합니다.",
				"&eLv.10 - 연계 수치를 최대로 상승시킵니다.",
				MsgTBL.Cooldown_Fixed);
	}


	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "TRICK", 20))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return super.getResult(cast);
	}	
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		Location loc = data.getPlayer().getLocation();
		draw(data.getPlayer());
		loc.getWorld().playSound(loc, Sound.BLOCK_GRASS_BREAK, 2.0f, 1.5f);
		loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 1.5f);
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			if (cs.getSkill() == Lucky_Star.skill)
				continue;
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			ci.reduceFlat(999.0);
		}
		CooldownFixer.Fix_Cooldown(data, Lucky_Star.skill);
		if (data.getSkillLevel(Lucky_Star.skill) >= 5)
		{
			Buff_Remover.Remove_Player_Bad_Buff(data.getPlayer());
		}
		if (data.getSkillLevel(Lucky_Star.skill) >= 10)
			Trick.Add_Stack(data.getPlayer(), 999);
	}
	
	public void draw(Player player)
	{
		Vector[] vecs = new Vector[8];
		vecs[0] = new Vector(0, 0, 1);
		vecs[1] = new Vector(0.25, 0, 0.25);
		vecs[2] = new Vector(1, 0, 0);
		vecs[3] = new Vector(0.25, 0, -0.25);
		vecs[4] = new Vector(0, 0, -1);
		vecs[5] = new Vector(-0.25, 0, -0.25);
		vecs[6] = new Vector(-1, 0, 0);
		vecs[7] = new Vector(-0.25, 0, 0.25);
		
		Location drawLoc = player.getLocation().add(0, player.getHeight() * 0.5, 0);
		
		for (int i = 0; i < 3; i++)
		{
			int m = i;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
				() -> {
					drawLoc.getWorld().playSound(drawLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 1.5f);
					Vector[] tempvecs = TRS.Rotate_X(vecs, Math.random() * 40.0);
					tempvecs = TRS.Rotate_Y(tempvecs, Math.random() * 360.0);
					tempvecs = TRS.Scale(tempvecs, 2.0 + 1 * m, 2.0 + 1 * m, 2.0 + 1 * m);
					for (int j = 0; j < 8; j++)
					{
						Location start = drawLoc.clone().add(tempvecs[j]);
						Location end = drawLoc.clone().add(tempvecs[j == 7 ? 0 : j + 1]);
						Particle_Drawer.Draw_Line(start, end, Particle.WAX_ON, 0.1);
					}
				}, i * 2);
		}
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Lucky_Star_Effect(drawLoc));
	}
	
	class Lucky_Star_Effect implements Runnable
	{
		Location loc;
		double pitch, yaw;
		public Lucky_Star_Effect(Location _loc)
		{
			loc = _loc;
			pitch = Math.random() * 40.0;
			yaw = Math.random() * 360.0;
		}
		
		double duration = 0.6;
		double radius = 1.0;
		double targetRadius = 6.0;
		public void run()
		{
			radius = radius + (targetRadius - radius) * 0.2;
			Particle_Drawer.Draw_Circle(loc, Particle.WAX_ON, radius, pitch, yaw);
			
			duration -= 0.05;
			if (duration > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
