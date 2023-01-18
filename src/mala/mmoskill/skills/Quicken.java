package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Quicken extends RegisteredSkill
{
	public static Quicken skill;
	
	public Quicken()
	{	
		super(new Quicken_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(15.0, 0.0));
		addModifier("hp", new LinearValue(17, 2.0));
		
		skill = this;
	}
}

class Quicken_Handler extends MalaSkill implements Listener
{
	public Quicken_Handler()
	{
		super(	"QUICKEN",
				"퀴큰",
				Material.WITHER_ROSE,
				MsgTBL.NeedSkills,
				"",
				"&7수명을 앞당기는 대가로 스킬의 재사용 대기시간을 초기화합니다.",
				"&7레벨이 오를수록 적용되는 스킬의 종류가 증가합니다.",
				"&eLv.1 - 브리즈",
				"&eLv.10 - 이그니스, 글래시아, 벤투스",
				"&eLv.20 - 퀴큰",
				MsgTBL.Cooldown_Fixed, MsgTBL.HPCost);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Location loc = data.getPlayer().getLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_SHOOT, 2.0f, 1.5f);
		loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 1.5f);
		
		int level = data.getSkillLevel(Quicken.skill);
		double damage = cast.getModifier("hp");
		data.getPlayer().setHealth(Math.max(1.0, data.getPlayer().getHealth() - damage));

		if (level < 20)
			CooldownFixer.Fix_Cooldown(data, Quicken.skill);
		
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;

			if (cs.getSkill() == Breeze.skill)
				ci.reduceFlat(999.0);
			
			if (level >= 10) {
				if (cs.getSkill() == Cast_Ignis.getInstance()
					|| cs.getSkill() == Cast_Glacia.getInstance()
					|| cs.getSkill() == Cast_Ventus.getInstance()) {
					ci.reduceFlat(999.0);
				}
			}
			if (level >= 20) {
				if (cs.getSkill() == Quicken.skill)
					ci.reduceFlat(999.0);
			}
		}
		
		for (int i = 0; i < 4; i++) {
			new Effect(data.getPlayer().getLocation().add(0, 1.0, 0), Particle.REDSTONE)
				.setDustOptions(new DustOptions(Color.RED, 1.0f))
				.append2DArc(240, 2.0 + i)
				.rotatePoint(-20, Math.random() * 360.0, 0)
				.playAnimation(6);
			new Effect(data.getPlayer().getLocation().add(0, 1.0, 0), Particle.REDSTONE)
				.setDustOptions(new DustOptions(Color.BLACK, 1.0f))
				.append2DLine(4.0)
				.rotate(0, 180, 0)
				.append2DLine(4.0)
				.rotatePoint(Math.random() * 180.0, Math.random() * 360.0, 0)
				.playEffect();
		}
	}
}
