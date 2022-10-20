package mala.mmoskill.skills;

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

import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Flare_Disc extends RegisteredSkill
{
	public Flare_Disc()
	{	
		super(new Flare_Disc_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(20, 1, 20, 80));
		addModifier("damage", new LinearValue(60, 20));
		addModifier("cooldown", new LinearValue(12, 0));
		addModifier("mana", new LinearValue(30, 6));
	}
}

class Flare_Disc_Handler extends MalaSkill implements Listener
{
	public Flare_Disc_Handler()
	{
		super(	"FLARE_DISC",
				"플레어 디스크",
				Material.SUNFLOWER,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&8{distance}&7 거리까지 나아가는 원반을 발사합니다.",
				"&7원반은 터질 시 주변에 &8{damage}의 피해를 줍니다.",
				"&7맞은 적은 발화 상태에 걸립니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double distance = cast.getModifier("distance");
		double damage = cast.getModifier("damage");
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		Vector dir = data.getPlayer().getLocation().getDirection();

		DamageMetadata ar = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new FireMagicEvent(data.getPlayer(), ar));
		damage = ar.getDamage();
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Flare_Disc_Disc(data.getPlayer().getEyeLocation().subtract(0, 0.3, 0), data.getPlayer(), dir, damage, 0.2, distance, 0.0));
		
	}
}

class Flare_Disc_Disc implements Runnable
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
	
	public Flare_Disc_Disc(Location _start_loc, Player _player, Vector _dir, double _damage, double _speed, double _max_distance, double _roll)
	{
		start_loc = _start_loc;
		player = _player;
		dir = _dir;
		damage = _damage;
		speed = _speed;
		max_distance = _max_distance;

		current_loc = start_loc.clone();
		before_loc = start_loc.clone();
		
		vecs = new Vector[36];
		for(int i = 0; i < 36; i ++)
		{
			double angle = i * 360.0 / 36.0;
			vecs[i] = new Vector(Math.cos(Math.toRadians(angle)), 0, Math.sin(Math.toRadians(angle)));
		}
		vecs = TRS.Rotate_Z(vecs, _roll);
		vecs = TRS.Rotate_X(vecs, _start_loc.getPitch());
		vecs = TRS.Rotate_Y(vecs, _start_loc.getYaw());
		vecs = TRS.Scale(vecs, 1.5, 1.5, 1.5);
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
			current_loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
			if (i % 3 == 0)
				current_loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 1, 0, 0, 0, 0);
		}
		current_loc.getWorld().spawnParticle(Particle.FLAME, current_loc, 1, 0, 0, 0, 0);

		
		// 주변 적 찾기
		boolean detected = false;
		for(double i = 0; i <= gap.length(); i += 1)
		{
			Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
			if(loc.getBlock().getType().isSolid())
				detected = true;
			else
			{
				for(Entity e : loc.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5))
				{
					if(!(e instanceof LivingEntity))
						continue;
					if(e == player)
						continue;
					
					// 찾은 경우
					detected = true;
					break;
				}
			}
			if(detected)
			{
				current_loc = loc;
				break;
			}
		}
		
		if(detected)
		{
			for(Entity e : current_loc.getWorld().getNearbyEntities(current_loc, 6, 6, 6))
			{
				if (!(e instanceof LivingEntity))
					continue;
				if (e.isDead())
					continue;
				if (e == player)
					continue;
				
				LivingEntity target = (LivingEntity)e;
				if (Damage.Is_Possible(player, target))
				{
					Damage.Attack(player, target, damage, 
						DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
					target.setFireTicks(100);
				}
			}

			current_loc.getWorld().playSound(current_loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
			current_loc.getWorld().playSound(current_loc, Sound.ITEM_TOTEM_USE, 2, 1);
			// current_loc.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, current_loc, 50, 3, 3, 3, 0);
			current_loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, current_loc, 45, 3, 3, 3, 0);
			current_loc.getWorld().spawnParticle(Particle.LAVA, current_loc, 70, 3, 3, 3, 0);
			current_loc.getWorld().spawnParticle(Particle.FLAME, current_loc, 70, 3, 3, 3, 0);
			return;
		}
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(current_distance >= max_distance)
			return;
					
		// 마무리
		before_loc = current_loc.clone();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}