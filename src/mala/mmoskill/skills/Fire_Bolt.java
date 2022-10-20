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

import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.MalaSkill;
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

public class Fire_Bolt extends RegisteredSkill
{
	public Fire_Bolt()
	{	
		super(new Fire_Bolt_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(10, 0.25));
		addModifier("damage", new LinearValue(5, 5));
		addModifier("cooldown", new LinearValue(7, 0));
		addModifier("mana", new LinearValue(3, 0.7));
	}
}

class Fire_Bolt_Handler extends MalaSkill implements Listener
{
	public Fire_Bolt_Handler()
	{
		super(	"FIRE_BOLT",
				"파이어 볼트",
				Material.BLAZE_POWDER,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&8{distance}&7 거리까지 나아가는 화염탄을 발사합니다.",
				"&7화염탄은 &8{damage}의 피해를 줍니다.",
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
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new FireBolt_Bolt(data.getPlayer().getEyeLocation().subtract(0, 0.2, 0), data.getPlayer(), dir, damage, 1.2, distance));
	}
}

class FireBolt_Bolt implements Runnable
{
	Player player;
	double damage;
	double max_distance;
	double speed;
	Location start_loc;
	Vector dir;

	double current_distance = 0;
	Location before_loc, current_loc;
	
	public FireBolt_Bolt(Location _start_loc, Player _player, Vector _dir, double _damage, double _speed, double _max_distance)
	{
		start_loc = _start_loc;
		player = _player;
		dir = _dir;
		damage = _damage;
		speed = _speed;
		max_distance = _max_distance;

		current_loc = start_loc.clone();
		before_loc = start_loc.clone();
		player.getWorld().getChunkAt(player.getLocation()).getEntities();
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
		
		for(double i = 0; i < gap.length(); i += 0.1)
		{
			Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
			current_loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
		}

		
		// 주변 적 찾기
		for(double i = 0; i <= gap.length(); i += 1)
		{
			Location loc = before_loc.clone().add(gap.clone().normalize().multiply(i));
			for(Entity e : loc.getWorld().getNearbyEntities(loc, 1, 1, 1))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e == player)
					continue;
				
				// 찾은 경우 피해 주고 그냥 끝냄
				LivingEntity target = (LivingEntity)e;
				Damage.Attack(player, target, damage,
						DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
				loc.getWorld().playSound(current_loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
				current_loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0, 0);
				current_loc.getWorld().spawnParticle(Particle.LAVA, loc, 20, 0, 0, 0, 0);
				target.setFireTicks(100);
				return;
			}
		}
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(current_distance > max_distance)
			return;
					
		// 마무리
		before_loc = current_loc.clone();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}