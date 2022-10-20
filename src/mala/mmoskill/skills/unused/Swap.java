package mala.mmoskill.skills.unused;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;

import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.skill.metadata.TargetSkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Swap extends Skill
{
	public Swap()
	{
		super();
		
		setName("스왑");
		setLore("&7대상과 자신의 위치를 맞바꿉니다.", "", MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.SEA_PICKLE);

		addModifier("cooldown", new LinearValue(30, -0.5));
		addModifier("mana", new LinearValue(40, -1));
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		TargetSkillMetadata cast = new TargetSkillMetadata(data, skill, 50);
		
		if (!cast.isSuccessful())
			return cast;
		
		World world = data.getPlayer().getWorld();
		Location player_loc = data.getPlayer().getLocation().clone();
		Location target_loc = cast.getTarget().getLocation().clone();
		
		data.getPlayer().teleport(target_loc);
		cast.getTarget().teleport(player_loc);

		world.spawnParticle(Particle.END_ROD, target_loc, 30, 0.3, 1, 0.3, 0);
		world.spawnParticle(Particle.END_ROD, player_loc, 30, 0.3, 1, 0.3, 0);
		world.playSound(target_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
		world.playSound(player_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
		
		return cast;
	}

}
