package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.events.IceMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Ice;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Frost_Wave extends RegisteredSkill
{
	public Frost_Wave()
	{	
		super(new Frost_Wave_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(10, 0.5, 10, 30));
		addModifier("damage", new LinearValue(82, 22));
		addModifier("cooldown", new LinearValue(20, 0));
		addModifier("mana", new LinearValue(48, 8));
	}
}

class Frost_Wave_Handler extends MalaSkill implements Listener
{
	public Frost_Wave_Handler()
	{
		super(	"FROST_WAVE",
				"프로스트 웨이브",
				Material.LIGHT_BLUE_DYE,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC,
				"",
				"&8{distance}&7 거리까지 나아가는 차가운 파장을 발사합니다.",
				"&7파장은 지나가며 &8{damage}의 피해를 줍니다.",
				"&7맞은 적은 구속 상태에 걸립니다.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double distance = cast.getModifier("distance");
		double damage = cast.getModifier("damage");
		damage *= Mastery_Ice.Get_Mult(data.getPlayer());
		Vector dir = data.getPlayer().getLocation().getDirection();
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Frost_Wave_Wave(data.getPlayer().getEyeLocation().subtract(0, 0.2, 0), data.getPlayer(), dir, damage, 0.4, distance));
		
	}
}

class Frost_Wave_Wave implements Runnable
{
	Player player;
	double damage;
	double max_distance;
	double speed;
	Location start_loc;
	Vector dir;

	double current_distance = 0;
	Location before_loc, current_loc;
	Vector[] vecs;
	List<LivingEntity> damaged_entities;
	IceMagicEvent ime;
	
	public Frost_Wave_Wave(Location _start_loc, Player _player, Vector _dir, double _damage, double _speed, double _max_distance)
	{
		start_loc = _start_loc;
		player = _player;
		dir = _dir;
		damage = _damage;
		speed = _speed;
		max_distance = _max_distance;
		damaged_entities = new ArrayList<LivingEntity>();
		
		current_loc = start_loc.clone();
		before_loc = start_loc.clone();
		player.getWorld().getChunkAt(player.getLocation()).getEntities();

		DamageMetadata ar = new DamageMetadata(damage);
		ime = new IceMagicEvent(player, ar);
		Bukkit.getPluginManager().callEvent(ime);
		damage = ar.getDamage();
		
		vecs = new Vector[25];
		for(int i = 0; i <= 24; i++)
		{
			double angle = i * (180.0 / 24.0);
			vecs[i] = new Vector(Math.cos(Math.toRadians(angle)) * 3.0, 0, Math.sin(Math.toRadians(angle)));
		}
		//vecs = TRS.Scale(vecs, 3, 1, 1);
		
		vecs = TRS.Rotate_X(vecs, _start_loc.getPitch());
		vecs = TRS.Rotate_Y(vecs, _start_loc.getYaw());
	}
	
	public void run()
	{
		current_distance += speed;
		if(max_distance < current_distance)
			speed = max_distance - current_distance;
		current_loc.add(dir.clone().multiply(speed));
		
		// 라인 그리기
		Vector gap = current_loc.clone().subtract(before_loc).toVector();
		if(gap.length() <= 0.01)
			return;
		
		for(int i = 0; i < vecs.length; i++)
		{
			Location loc = current_loc.clone().add(vecs[i]);
			current_loc.getWorld().spawnParticle(Particle.SNOWBALL, loc, 1, 0, 0, 0, 0);
			current_loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0, 0, 0, 0);
			// current_loc.getWorld().spawnParticle(Particle.BARRIER, loc, 1, 0, 0, 0, 0);
			
			loc = before_loc.clone().add(vecs[i]);
			current_loc.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
		}

		
		// 주변 적 찾기
		for(double i = 0; i <= gap.length(); i += 1)
		{
			Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
			if(loc.getBlock().getType().isSolid())
				return;
			else
			{
				for(Entity e : loc.getWorld().getNearbyEntities(loc, 3, 3, 3))
				{
					if(!(e instanceof LivingEntity))
						continue;
					if(e == player)
						continue;
					if(damaged_entities.contains(e))
						continue;
					
					// 찾은 경우
					LivingEntity target = (LivingEntity)e;

					Damage.Attack(player, target, damage,
							DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
					Ice_Bolt_Handler.Slow_Target(target, 1, 200, ime.getSuperSlow());

					current_loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 2, 0.5f);
					damaged_entities.add(target);
					
					break;
				}
			}
		}
		current_loc.getWorld().playSound(current_loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2, 1);
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(current_distance >= max_distance)
			return;
					
		// 마무리
		before_loc = current_loc.clone();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
