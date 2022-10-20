package mala.mmoskill.skills;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Mana_Conversion extends RegisteredSkill
{
	public Mana_Conversion()
	{	
		super(new Mana_Conversion_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("manaheal", new LinearValue(50, 10));
		addModifier("cooldown", new LinearValue(58.5, -1.5));
		addModifier("hp", new LinearValue(30, 2));
	}
}

class Mana_Conversion_Handler extends MalaSkill implements Listener
{
	public Mana_Conversion_Handler()
	{
		super(	"MANA_CONVERSION",
				"마나 컨버전",
				Material.NETHER_WART,
				"&7마나를 &8{manaheal}&7 회복합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.HPCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("hp");
		double heal = cast.getModifier("manaheal");
		data.getPlayer().damage(damage);
		data.giveMana(heal, UpdateReason.OTHER);
		
		Location loc = data.getPlayer().getLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
		loc.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_AMBIENT, 1, 2);
		
		loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1, 0), 40, 0.5, 0.5, 0.5, 0);
	}
}
