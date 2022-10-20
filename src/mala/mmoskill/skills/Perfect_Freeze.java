package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.IceMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Ice;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Perfect_Freeze extends RegisteredSkill
{
	public Perfect_Freeze()
	{	
		super(new Perfect_Freeze_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("area_damage", new LinearValue(5, 0.5));
		addModifier("frost_damage", new LinearValue(46, 6));
		addModifier("radius", new LinearValue(10, 0.5));

		addModifier("cooldown", new LinearValue(120, 0));
		// addModifier("cooldown", new LinearValue(120, -2));
		addModifier("mana", new LinearValue(165, 15));
	}
}

class Perfect_Freeze_Handler extends MalaSkill implements Listener
{
	public Perfect_Freeze_Handler()
	{
		super(	"PERFECT_FREEZE",
				"퍼펙트 프리즈",
				Material.SUNFLOWER,
				MsgTBL.SKILL + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC,
				"",
				"&75초간 자신의 주변을 극한의 냉기로 뒤덮습니다.",
				"&7대상들은 &8{area_damage}&7의 피해를 입으며 둔화됩니다.",
				"&7시전이 끝날 때까지 탈출하지 못한 대상은,",
				"&7추가로 둔화 수준 * &8{frost_damage}&7의 피해를 입습니다.",
				"&7이렇게 피해를 입은 경우에는 둔화가 해제됩니다.",
				"&c1m 이상 움직이면 시전이 취소됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		registerModifiers("radius");
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double area_damage = cast.getModifier("area_damage");
		double frost_damage = cast.getModifier("frost_damage");
		area_damage *= Mastery_Ice.Get_Mult(data.getPlayer());
		frost_damage *= Mastery_Ice.Get_Mult(data.getPlayer());
		
		double radius = cast.getModifier("radius");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Perfect_Freeze_Skill(data.getPlayer(), area_damage, frost_damage, radius));
	}
}

class Perfect_Freeze_Skill implements Runnable
{
	Player player;
	double area_damage, frost_damage;
	Location fixed_loc;
	
	double timer = 5.0;
	int count = 0;
	double max_radius = 0.0;
	double radius = 3.0;

	double current_height = 0;
	double additive_height = 0.2;
	double current_angle = 0;
	double additive_angle = 1.2;
	
	IceMagicEvent ime;
	
	public Perfect_Freeze_Skill(Player _player, double _area_damage, double _frost_damage, double _radius)
	{
		player = _player;
		fixed_loc = _player.getLocation();
		
		area_damage = _area_damage;
		frost_damage = _frost_damage;
		max_radius = _radius;

		DamageMetadata attack = new DamageMetadata(area_damage, DamageType.MAGIC, DamageType.SKILL);
		ime = new IceMagicEvent(player, attack);
		Bukkit.getPluginManager().callEvent(ime);
		area_damage = attack.getDamage();
		attack = new DamageMetadata(frost_damage, DamageType.MAGIC, DamageType.SKILL);
		ime = new IceMagicEvent(player, attack);
		Bukkit.getPluginManager().callEvent(ime);
		frost_damage = attack.getDamage();
		
		additive_height = 0.2 - max_radius * 0.00025;
		additive_angle = 1.5 - max_radius * 0.03;
	}
	
	boolean waved = false;
	public void run()
	{
		if (count % 5 == 0)
		{
			if (fixed_loc.distance(player.getLocation()) > 1.0)
			{
				player.sendMessage("§c[ 발동중이던 스킬이 취소되었습니다. ]");
				return;
			}
		}
		
		double temp_angle = -current_angle;
		for (double height = -current_height; height <= current_height; height += additive_height)
		{
			for (double angle = 0.0; angle <= 360.0; angle += 360.0 / 4.0)
			{
				double rad = Math.toRadians(temp_angle + angle);
				Location loc = fixed_loc.clone().add(Math.cos(rad) * radius, height, Math.sin(rad) * radius);
				// loc.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
				loc.getWorld().spawnParticle(Particle.SNOWBALL, loc, 1, 0, 0, 0, 0);
				
				if (height < additive_height * 0.5 && height > -additive_height * 0.5
						&& count % 10 == 0)
				{
					loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
				}
			}
			temp_angle -= additive_angle;
		}
		if (count % 10 == 0)
		{
			fixed_loc.getWorld().playSound(fixed_loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
		}
		fixed_loc.getWorld().spawnParticle(Particle.SNOWBALL, fixed_loc.clone().add(0, current_height, 0), 300, radius * 0.5, 0.1, radius * 0.5, 0);
		
		
		// 피격 체크
		List<Entity> entities = new ArrayList<Entity>(fixed_loc.getWorld().getNearbyEntities(fixed_loc, radius * 2, radius * 2, radius * 2));
		for(Entity en : entities)
		{
			if (!(en instanceof LivingEntity))
				continue;
			if (en == player)
				continue;
			if (fixed_loc.distance(en.getLocation()) > radius)
				continue;

			LivingEntity le = (LivingEntity)en;
			if (le.getNoDamageTicks() == 0)
			{
				if (Damage.Is_Possible(player, le))
				{
					Damage.Attack(player, le, area_damage, DamageType.MAGIC, DamageType.SKILL);
					Buff_Manager.Increase_Buff(le, PotionEffectType.SLOW, 0, 100, PotionEffectType.SPEED,10);
					le.setNoDamageTicks(10);
				}
			}
		}
		
		if(timer <= 1.0 && waved == false)
		{
			waved = true;
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Perfect_Freeze_Wave(player, area_damage, frost_damage, max_radius));
		}
			
		// 마무리 전 이걸 계속 해야하나 체크
		if(timer <= 0.0)
			return;
					
		// 마무리
		timer -= 0.05;
		current_angle += additive_angle * 3;
		current_height = Math.min(radius, current_height + additive_height);
		radius = Math.min(max_radius, radius + max_radius * 0.025);
		count++;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}


class Perfect_Freeze_Wave implements Runnable
{
	Player player;
	double area_damage;
	double frost_damage;
	Location fixed_loc;
	
	double timer = 2.0;
	int count = 0;
	double max_radius = 0.0;
	double radius = 3.0;
	
	public Perfect_Freeze_Wave(Player _player, double _area_damage, double _frost_damage, double _radius)
	{
		player = _player;
		fixed_loc = _player.getLocation();
		
		area_damage = _area_damage;
		frost_damage = _frost_damage;
		max_radius = _radius;
		
		fixed_loc.getWorld().playSound(fixed_loc, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.0f);
		Make_Vec();
	}

	double width = 1.0;
	double height = 10.0;
	Vector[] vecs;
	void Make_Vec()
	{
		width = max_radius / 10.0;
		height = max_radius;
		
		vecs = new Vector[16];
		ArrayList<Vector> temp_vec = new ArrayList<Vector>();
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(0, width, 0));
		
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(width, 0, 0));
		
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(0, -width, 0));
		
		temp_vec.add(new Vector(0, 0, height));
		temp_vec.add(new Vector(-width, 0, 0));
		
		temp_vec.add(new Vector(0, width, 0));
		temp_vec.add(new Vector(width, 0, 0));

		temp_vec.add(new Vector(width, 0, 0));
		temp_vec.add(new Vector(0, -width, 0));
		
		temp_vec.add(new Vector(0, -width, 0));
		temp_vec.add(new Vector(-width, 0, 0));
		
		temp_vec.add(new Vector(-width, 0, 0));
		temp_vec.add(new Vector(0, width, 0));
		
		vecs = new Vector[temp_vec.size()];
		for (int i = 0; i < temp_vec.size(); i++)
			vecs[i] = temp_vec.get(i);
	}
	
	public void run()
	{
		if (count % 5 == 0)
		{
			if (fixed_loc.distance(player.getLocation()) > 0.3)
				return;
		}
		
		if (timer > 0.0)
		{
			DustOptions d_o = new Particle.DustOptions(Color.fromRGB(255, 255, 255), 2.5f);
			for (double angle = 0.0; angle <= 360.0; angle += 360.0 / (36.0 * (radius / 3.0)))
			{
				double rad = Math.toRadians(angle);
				Location loc = fixed_loc.clone().add(Math.cos(rad) * radius, 0.8, Math.sin(rad) * radius);
				loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, d_o);
			}
		}

		// 마무리 전 이걸 계속 해야하나 체크
		if(radius >= max_radius - radius * 0.1)
		{
			timer -= 0.05;
			if (timer <= 0.0 && count % 3 == 0)
			{
				// 피격 체크
				List<Entity> entities = new ArrayList<Entity>(fixed_loc.getWorld().getNearbyEntities(fixed_loc, radius * 2, radius * 2, radius * 2));
				for(Entity en : entities)
				{
					if (!(en instanceof LivingEntity))
						continue;
					if (en == player)
						continue;
					if (fixed_loc.distance(en.getLocation()) > radius)
						continue;
	
					LivingEntity le = (LivingEntity)en;
					if (timer <= 0.0) // 마지막 공격
					{
						le.setNoDamageTicks(0);
						int amp = 0;
						if (le.hasPotionEffect(PotionEffectType.SLOW))
						{
							PotionEffect pe = le.getPotionEffect(PotionEffectType.SLOW);
							amp = pe.getAmplifier() + 1;
							Draw_Entity_Effect(le);
							Buff_Manager.Remove_Buff(le, PotionEffectType.SLOW);
						}
						
						Damage.Attack(player, le, area_damage + frost_damage * amp, DamageType.MAGIC, DamageType.SKILL);
					}
				}
				for (int n = 0; n < 8; n++)
				{
					double rand_range = 360d;
					double rand_pitch = -rand_range + Math.random() * rand_range * 2.0;
					double rand_yaw = -rand_range + Math.random() * rand_range * 2.0;
					Vector[] temp_vecs;
					temp_vecs = TRS.Rotate_X(vecs, rand_pitch);
					temp_vecs = TRS.Rotate_Y(temp_vecs, rand_yaw);
					
					for(int i = 0; i < temp_vecs.length; i += 2)
					{
						Location start_loc = fixed_loc.clone().add(temp_vecs[i]);
						Location end_loc = fixed_loc.clone().add(temp_vecs[i + 1]);
						Location lerped = start_loc.clone();
						double add = 0.1d / start_loc.distance(end_loc);
						for(double j = 0; j < 1.0; j += add)
						{
							lerped.setX(start_loc.getX() + (end_loc.getX() - start_loc.getX()) * j);
							lerped.setY(start_loc.getY() + (end_loc.getY() - start_loc.getY()) * j);
							lerped.setZ(start_loc.getZ() + (end_loc.getZ() - start_loc.getZ()) * j);
							fixed_loc.getWorld().spawnParticle(Particle.END_ROD, lerped, 1, 0, 0, 0, 0);
						}
					}
				}
				fixed_loc.getWorld().playSound(fixed_loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
				fixed_loc.getWorld().playSound(fixed_loc, Sound.BLOCK_GLASS_BREAK, 2.0f, 1.0f);
			}
			
			if (timer <= -1.0)
				return;
		}
		
		// 마무리
		radius = radius + (max_radius - radius) * 0.1;
		count++;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	
	void Draw_Entity_Effect(LivingEntity _target)
	{
		Random rand = new Random();

		for(int count = 0; count < 4; count++)
		{
			Vector rand_vec = new Vector(-1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0).normalize();
			Vector from = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(-3.0));
			Vector to = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(3.0));
	
			Location loc = from.toLocation(_target.getWorld());
			for(double i = 0.0; i < from.distance(to) * 2.0; i += 0.1)
			{
				loc.add(rand_vec.clone().multiply(0.1));
				loc.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0d, 0d, 0d, 0d);//, d_o);
			}
		}
		
		Location loc = _target.getEyeLocation();
		loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 2f, 1f);
		loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_PLACE, 2f, 1.3f);
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
		loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0d, 0d, 0d, 0d);
		
	}
}

