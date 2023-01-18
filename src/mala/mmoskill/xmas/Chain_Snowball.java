package mala.mmoskill.xmas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Lightning;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import laylia_core.main.Damage;

public class Chain_Snowball
{
	public static void Draw_Lightning_Line(Location _start, Location _end)
	{
		Draw_Lightning_Line(_start, _end, Particle.SNOWBALL);
	}
	public static void Draw_Lightning_Line(Location _start, Location _end, Particle _particle)
	{
		List<Vector> vecs = new ArrayList<Vector>();
		
		double random_range = 6;
		double random_range_y = 4;
		
		Vector nor = _end.clone().subtract(_start).toVector().normalize();
		vecs.add(_start.toVector());
		for(int i = 3; i < _start.distance(_end) - 3; i += 3)
		{
			Vector new_vec = new Vector(
					random_range * -0.5 + Math.random() * random_range,
					random_range_y * -0.5 + Math.random() * random_range_y,
					random_range * -0.5 + Math.random() * random_range);
			new_vec.add(nor.clone().multiply(i).add(_start.toVector()));
			vecs.add(new_vec);
		}
		vecs.add(_end.toVector());
		

		Location loc = new Location(_start.getWorld(), 0, 0, 0);
		for(int i = 0; i < vecs.size() - 1; i++)
		{
			Vector dist = vecs.get(i + 1).clone().subtract(vecs.get(i));

			for(double j = 0; j < dist.length(); j += 0.1)
			{
				_start.getWorld().spawnParticle(_particle, loc.clone().add(vecs.get(i).clone().add(dist.clone().normalize().multiply(j)))
						, 1, 0, 0, 0, 0);
			}
		}
	}
}