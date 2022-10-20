package mala.mmoskill.skills;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Leap extends RegisteredSkill
{
	public Leap()
	{	
		super(new Leap_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(1, 0.05, 0.5, 5));
		addModifier("cooldown", new LinearValue(10, -0.25));
		addModifier("stamina", new LinearValue(6, -0.1));
	}
}

class Leap_Handler extends MalaSkill implements Listener
{
	public Leap_Handler()
	{
		super(	"LEAP",
				"도약",
				Material.LEATHER_BOOTS,
				"&8{power}&7의 힘으로 앞을 향해 뜁니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		float power = (float) cast.getModifier("power"); // 앞으로 뛰는 힘
		
		// 효과
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.ENTITY_HORSE_JUMP, 1, 1);
		data.getPlayer().getWorld().spawnParticle(Particle.SMOKE_NORMAL, data.getPlayer().getLocation().add(0, data.getPlayer().getHeight() / 2, 0), 24, 0, 0, 0, .7);
		
		Vector jump = data.getPlayer().getLocation().getDirection().normalize();
		jump.setX(jump.getX() * power);
		jump.setZ(jump.getZ() * power);
		jump.setY(0.6d);
		jump.multiply(1.5d);
		data.getPlayer().setVelocity(jump);
	}

}
