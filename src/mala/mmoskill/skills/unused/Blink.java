package mala.mmoskill.skills.unused;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.RayTraceResult;

import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;


public class Blink extends Skill
{
	public Blink()
	{
		super();
		
		setName("블링크");
		setLore("&7바라보고 있는 장소로 순간이동합니다.",
				"&7최대 &8{distance}&7m까지 이동할 수 있습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		setMaterial(Material.MAGENTA_DYE);

		addModifier("cooldown", new LinearValue(14.6, -0.4, 5.0, 15.0));
		addModifier("distance", new LinearValue(11, 1));
		addModifier("mana", new LinearValue(12, -.4));
		
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		SkillMetadata cast = new SkillMetadata(data, skill);
		
		if (!cast.isSuccessful())
			return cast;
		
		World world = data.getPlayer().getWorld();
		Location loc = data.getPlayer().getEyeLocation();

		RayTraceResult hit = data.getPlayer().rayTraceBlocks(cast.getModifier("distance"), FluidCollisionMode.NEVER);
		Location target_loc = null;

		if (hit == null)
		{
			target_loc = data.getPlayer().getLocation().add(loc.getDirection().multiply(cast.getModifier("distance")));
			// data.getPlayer().sendMessage("§c이동하기에는 너무 멉니다!");
			// cast.abort();
			// return cast;
		}
		else if (hit.getHitBlock() != null) // 블록 히트
		{
			target_loc = hit.getHitBlock().getLocation().add(0.5, 0.5, 0.5);
			target_loc.add(hit.getHitBlockFace().getModX(), hit.getHitBlockFace().getModY(), hit.getHitBlockFace().getModZ());
			if (hit.getHitBlockFace() == BlockFace.DOWN)
				target_loc.add(0.0, -1.0, 0.0);
		}
		else // 히트 안함
		{
			target_loc = data.getPlayer().getLocation().add(loc.getDirection().multiply(cast.getModifier("distance")));
		}
		
		if (target_loc == null)
		{
			data.getPlayer().sendMessage("§c이동하기에는 너무 멉니다!");
			cast.abort();
			return cast;
		}
		
		target_loc.setDirection(loc.getDirection());
		data.getPlayer().teleport(target_loc, TeleportCause.PLUGIN);

		world.spawnParticle(Particle.CRIT, loc, 30, 0.3, 1, 0.3, 0);
		world.spawnParticle(Particle.END_ROD, loc, 30, 0.3, 1, 0.3, 0);
		world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
		
		return cast;
	}

}
