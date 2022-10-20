package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Mastery_Buff;
import mala.mmoskill.skills.passive.Mastery_Heal;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Sanctuary extends RegisteredSkill
{
	public static Sanctuary skill;
	
	public Sanctuary()
	{	
		super(new Sanctuary_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(5, 2));
		addModifier("radius", new LinearValue(6, 1.5));
		addModifier("cooldown", new LinearValue(240, 0));
		addModifier("mana", new LinearValue(200, 20));
		skill = this;
	}
}

class Sanctuary_Handler extends MalaLocationSkill implements Listener
{
	public Sanctuary_Handler()
	{
		super(	"SANCTUARY",
				"생츄어리",
				Material.MUSIC_DISC_CAT,
				MsgTBL.NeedSkills,
				"&e 힐 - lv.30",
				"",
				"&7바라보고 있는 곳의 반경 &8{radius}&7m를 성소로 만듭니다.",
				"&7성소는 20초간 유지되며,",
				"&7내부의 플레이어들을 &8{damage}&7 만큼 지속적으로 회복시킵니다.",
				"&7지속 시간은 버프 마스터리의 영향을,",
				"&7회복량은 힐링 마스터리의 영향을 받습니다.",
				"",
				MsgTBL.Cooldown_Fixed, MsgTBL.ManaCost);
	}

	@Override
	public LocationSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "HEAL", 30))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new LocationSkillResult(cast, 0.0);
		}
		return new LocationSkillResult(cast, range);
	}

	@Override
	public void whenCast(LocationSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Location loc = _data.getTarget().add(0, 1.1, 0);

		double duration = 20.0;
		double damage = cast.getModifier("damage");
		double radius = cast.getModifier("radius");

		duration *= Mastery_Buff.Get_Mult(data.getPlayer());
		damage *= Mastery_Heal.Get_Mult(data.getPlayer());

		CooldownFixer.Fix_Cooldown(data, Sanctuary.skill); 
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Sanctuary_Task(data.getPlayer(), loc.add(0, 1.1, 0), duration, damage, radius));
	}

}

class Sanctuary_Task implements Runnable
{
	Player player;
	double damage;
	Location loc;
	double radius = 6.0;
	
	double timer = 30.0;
	long interval = 5;
	int counter = 0;
	World world;
	Vector[] vecs;
	
	public Sanctuary_Task(Player _player, Location _loc, double _duration, double _damage, double _radius)
	{
		player = _player;
		loc = _loc;
		damage = _damage;
		radius = _radius;
		world = loc.getWorld();

		Make_Vec();
		world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.5f, 1.5f);
	}
	void Make_Vec()
	{
		vecs = new Vector[24];
		int i = 0;
		vecs[i++] = new Vector(-radius, 0, -radius);
		vecs[i++] = new Vector(radius, 0, -radius);
		
		vecs[i++] = new Vector(radius, 0, -radius);
		vecs[i++] = new Vector(radius, 0, radius);
		
		vecs[i++] = new Vector(radius, 0, radius);
		vecs[i++] = new Vector(-radius, 0, radius);
		
		vecs[i++] = new Vector(-radius, 0, radius);
		vecs[i++] = new Vector(-radius, 0, -radius);

		vecs[i++] = new Vector(radius * 0.4, 0, radius * 0.4);
		vecs[i++] = new Vector(radius, 0, radius * 0.4);
		vecs[i++] = new Vector(radius * 0.4, 0, radius * 0.4);
		vecs[i++] = new Vector(radius * 0.4, 0, radius);

		vecs[i++] = new Vector(radius * -0.4, 0, radius * 0.4);
		vecs[i++] = new Vector(-radius, 0, radius * 0.4);
		vecs[i++] = new Vector(radius * -0.4, 0, radius * 0.4);
		vecs[i++] = new Vector(radius * -0.4, 0, radius);

		vecs[i++] = new Vector(radius * 0.4, 0, radius * -0.4);
		vecs[i++] = new Vector(radius, 0, radius * -0.4);
		vecs[i++] = new Vector(radius * 0.4, 0, radius * -0.4);
		vecs[i++] = new Vector(radius * 0.4, 0, -radius);

		vecs[i++] = new Vector(radius * -0.4, 0, radius * -0.4);
		vecs[i++] = new Vector(-radius, 0, radius * -0.4);
		vecs[i++] = new Vector(radius * -0.4, 0, radius * -0.4);
		vecs[i++] = new Vector(radius * -0.4, 0, -radius);
	}
	
	public void run()
	{
		for(int i = 0; i < vecs.length; i += 2)
		{
			Location start_loc = loc.clone().add(vecs[i]);
			Location end_loc = loc.clone().add(vecs[i + 1]);
			Location lerped = start_loc.clone();
			double add = 0.1d / start_loc.distance(end_loc);
			for(double j = 0; j < 1.0; j += add)
			{
				lerped.setX(start_loc.getX() + (end_loc.getX() - start_loc.getX()) * j);
				lerped.setY(start_loc.getY() + (end_loc.getY() - start_loc.getY()) * j);
				lerped.setZ(start_loc.getZ() + (end_loc.getZ() - start_loc.getZ()) * j);
				world.spawnParticle(Particle.END_ROD, lerped, 1, 0, 0, 0, 0);
			}
		}
		world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 20, radius, 0, radius, 0);
		
		for(Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius))
		{
			if (!(e instanceof Player))
				continue;

			if (e.isDead())
				continue;
			
			Player target = (Player)e;
			double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			target.setHealth(Math.min(max, target.getHealth() + damage));
		}
		
		
		timer -= interval / 20.0;
		if(timer > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, interval);
	}
}
