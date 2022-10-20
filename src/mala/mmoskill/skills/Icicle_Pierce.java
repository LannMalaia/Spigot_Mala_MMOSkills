package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.skills.passive.Mastery_Ice;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Icicle_Pierce extends RegisteredSkill
{
	public Icicle_Pierce()
	{	
		super(new Icicle_Pierce_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("count", new LinearValue(3.3, 0.3));
		addModifier("damage", new LinearValue(7, 1.0));
		addModifier("cooldown", new LinearValue(5, -0.1, 1, 20));
		addModifier("mana", new LinearValue(21.5, 1.5));
	}
}

class Icicle_Pierce_Handler extends MalaSkill implements Listener
{
	public Icicle_Pierce_Handler()
	{
		super(	"ICICLE_PIERCE",
				"아이시클 피어스",
				Material.PRISMARINE_SHARD,
				MsgTBL.SKILL + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC,
				"",
				"&7얼음송곳을 만들어 &8{count}&7번 찌릅니다.",
				"전방에 있는 적들은 &8{damage}&7 만큼의 피해를 받습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		int count = (int)cast.getModifier("count");
		double damage = cast.getModifier("damage");
		damage *= Mastery_Ice.Get_Mult(data.getPlayer());
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Icicle_Pierce_Task(data.getPlayer(), damage, count));
	}
}

class Icicle_Pierce_Task implements Runnable
{
	Player player;
	double damage;
	int repeat = 0;

	long interval = 2;
	double width = 1.0;
	double height = 10.0;
	World world;
	Location loc;
	Vector[] vecs;
	
	double damage_angle = 60;
	
	public Icicle_Pierce_Task(Player _player, double _damage, int _repeat)
	{
		player = _player;
		loc = player.getEyeLocation().add(_player.getLocation().getDirection().multiply(1.5)).subtract(0, 0.4, 0);
		damage = _damage;
		repeat = _repeat;
		if(repeat < 4)
			interval = 3;
		else if(repeat < 8)
			interval = 2;
		else
			interval = 1;
		
		world = loc.getWorld();

		Make_Vec();
		vecs = TRS.Rotate_X(vecs, loc.getPitch());
		vecs = TRS.Rotate_Y(vecs, loc.getYaw());
	}
	void Make_Vec()
	{
		vecs = new Vector[16];
		int i = 0;
		vecs[i++] = new Vector(0, 0, height);
		vecs[i++] = new Vector(0, width, 0);
		
		vecs[i++] = new Vector(0, 0, height);
		vecs[i++] = new Vector(width, 0, 0);
		
		vecs[i++] = new Vector(0, 0, height);
		vecs[i++] = new Vector(0, -width, 0);
		
		vecs[i++] = new Vector(0, 0, height);
		vecs[i++] = new Vector(-width, 0, 0);
		
		vecs[i++] = new Vector(0, width, 0);
		vecs[i++] = new Vector(width, 0, 0);

		vecs[i++] = new Vector(width, 0, 0);
		vecs[i++] = new Vector(0, -width, 0);
		
		vecs[i++] = new Vector(0, -width, 0);
		vecs[i++] = new Vector(-width, 0, 0);
		
		vecs[i++] = new Vector(-width, 0, 0);
		vecs[i++] = new Vector(0, width, 0);
	}
	
	public void run()
	{
		double rand_range = 40d;
		double rand_pitch = -rand_range + Math.random() * rand_range * 2.0;
		double rand_yaw = -rand_range + Math.random() * rand_range * 2.0;
		Vector[] temp_vecs;
		temp_vecs = TRS.Rotate_X(vecs, rand_pitch);
		temp_vecs = TRS.Rotate_Y(temp_vecs, rand_yaw);
		
		loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5)).subtract(0, 0.4, 0);
		
		for(int i = 0; i < temp_vecs.length; i += 2)
		{
			Location start_loc = loc.clone().add(temp_vecs[i]);
			Location end_loc = loc.clone().add(temp_vecs[i + 1]);
			Location lerped = start_loc.clone();
			double add = 0.1d / start_loc.distance(end_loc);
			for(double j = 0; j < 1.0; j += add)
			{
				lerped.setX(start_loc.getX() + (end_loc.getX() - start_loc.getX()) * j);
				lerped.setY(start_loc.getY() + (end_loc.getY() - start_loc.getY()) * j);
				lerped.setZ(start_loc.getZ() + (end_loc.getZ() - start_loc.getZ()) * j);
				world.spawnParticle(Particle.WATER_SPLASH, lerped, 1, 0, 0, 0, 0);
				world.spawnParticle(Particle.CRIT, lerped, 1, 0, 0, 0, 0);
			}
		}
		world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1.5f, 1.5f);
		world.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.5f, 2f);


		for(Entity e : world.getNearbyEntities(loc, height, height, height))
		{
			if(!(e instanceof LivingEntity))
				continue;
			if(e == player)
				continue;
			if(loc.distance(e.getLocation()) > height)
				continue;

			Location target_loc = e.getLocation().subtract(loc);
			double vec = loc.getDirection().dot(target_loc.toVector().normalize());
			if(vec > Math.cos(Math.toRadians(damage_angle / 2)))
			{
				LivingEntity target = (LivingEntity)e;
				target.setNoDamageTicks(0);
				Damage.Attack(player, target, damage, DamageType.MAGIC, DamageType.SKILL);
			}
		}
		
		if(--repeat > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, interval);
	}
}
