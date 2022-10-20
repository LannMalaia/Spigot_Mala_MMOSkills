package mala.mmoskill.skills;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class DummySpawn extends RegisteredSkill
{
	public DummySpawn()
	{	
		super(new DummySpawn_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(1, 0));
	}
}

class DummySpawn_Handler extends MalaSkill implements Listener
{
	public DummySpawn_Handler()
	{
		super(	"DUMMYSPAWN",
				"더미 소환",
				Material.STONE_HOE,
				"&7주변에 더미를 소환");
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		Player player = data.getPlayer();
		World world = player.getWorld();
		world.spawnEntity(player.getLocation(), EntityType.ARMOR_STAND);
		for (int radius = 1; radius < 10; radius += 1)
		{
			for (double i = 0; i <= 360.0; i += 360.0 / (radius * 12))
			{
				Location loc = player.getLocation().add(Math.cos(Math.toRadians(i)) * radius, 0, Math.sin(Math.toRadians(i)) * radius);
				world.spawnEntity(loc, EntityType.ARMOR_STAND);
			}
		}
	}
}
