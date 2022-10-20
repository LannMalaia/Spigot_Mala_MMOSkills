package mala.mmoskill.skills;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Make_Doppel;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Back_Step extends RegisteredSkill
{
	public Back_Step()
	{	
		super(new Back_Step_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(10, -0.25, 5, 10));
		addModifier("power", new LinearValue(0.82, 0.02));
		addModifier("stamina", new LinearValue(10, 0));
	}
}

class Back_Step_Handler extends MalaSkill implements Listener
{
	public Back_Step_Handler()
	{
		super(	"BACK_STEP",
				"ÈÄÅð",
				Material.FEATHER,
				"&8{power}&7ÀÇ ÈûÀ¸·Î µÚ¸¦ ÇâÇØ ¶Ý´Ï´Ù.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		float power = (float) cast.getModifier("power"); // µÚ·Î ¶Ù´Â Èû

		// È¿°ú
		if (!Make_Doppel.Try_Doppel_Make(data, this))
		{
			data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.ENTITY_HORSE_JUMP, 1, 1);
			data.getPlayer().getWorld().spawnParticle(Particle.SMOKE_NORMAL, data.getPlayer().getLocation().add(0, data.getPlayer().getHeight() / 2, 0), 24, 0, 0, 0, .7);
		}
		
		Vector jump = data.getPlayer().getLocation().getDirection().normalize();
		jump.setX(jump.getX() * -1.0d * power);
		jump.setZ(jump.getZ() * -1.0d * power);
		jump.setY(0.15d);
		jump.multiply(1.5d);
		data.getPlayer().setVelocity(jump);
	}

}
