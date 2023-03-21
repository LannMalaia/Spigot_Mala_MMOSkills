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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mala.mmoskill.events.IceMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Ice;
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

public class Ice_Bolt extends RegisteredSkill
{
	public Ice_Bolt()
	{	
		super(new Ice_Bolt_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(20.5, 0.5));
		addModifier("bolt_count", new LinearValue(1.5, 0.5));
		addModifier("damage", new LinearValue(7, 2));
		addModifier("cooldown", new LinearValue(5, 0.25));
		addModifier("mana", new LinearValue(5, 1));
	}
}

class Ice_Bolt_Handler extends MalaSkill implements Listener
{
	public Ice_Bolt_Handler()
	{
		super(	"ICE_BOLT",
				"아이스 볼트",
				Material.SNOWBALL,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC,
				"",
				"&e{distance}&7m 거리까지 나아가는 빙결탄을 &e{bolt_count}&7발 발사합니다.",
				"&7빙결탄은 &e{damage}&7의 피해를 줍니다.",
				"&7맞은 적은 이동속도가 낮아집니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double distance = cast.getModifier("distance");
		double damage = cast.getModifier("damage");
		damage *= Mastery_Ice.Get_Mult(data.getPlayer());

		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
		for (int i = 0; i < cast.getModifier("bolt_count"); i++) {
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
					new IceBolt_Bolt(
							cast,
							data.getPlayer(),
							damage,
							3.5,
							distance),
					2 * i);
		}
	}
	

	public static boolean Slow_Target(LivingEntity _target, int _amp, int _ticks, boolean _superslow)
	{
		int amp = _amp;
		int ticks = _ticks;
		if (_superslow)
			amp += 2;
		
		if(_target.hasPotionEffect(PotionEffectType.SPEED))
		{
			PotionEffect pe = _target.getPotionEffect(PotionEffectType.SPEED);
			int current_amp = pe.getAmplifier();
			_target.removePotionEffect(PotionEffectType.SPEED);
			if(current_amp - amp >= 0)
				_target.addPotionEffect(new PotionEffect(pe.getType(), pe.getDuration(), current_amp - amp));
			return true;
		}
		if(_target.hasPotionEffect(PotionEffectType.SLOW))
		{
			PotionEffect pe = _target.getPotionEffect(PotionEffectType.SLOW);
			int current_amp = pe.getAmplifier();
			if(current_amp < 2)
			{
				_target.removePotionEffect(PotionEffectType.SLOW);
				_target.addPotionEffect(new PotionEffect(pe.getType(), pe.getDuration() + ticks / 2, current_amp + 1));
			}
			else
			{
				_target.removePotionEffect(PotionEffectType.SLOW);
				_target.addPotionEffect(new PotionEffect(pe.getType(), pe.getDuration() + ticks / 2, current_amp));
			}
			return true;
		}

		_target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ticks, amp));
		return true;
	}
}

class IceBolt_Bolt implements Runnable
{
	SkillMetadata cast;
	Player player;
	double damage;
	double max_distance;
	double speed;
	Vector dir;

	double current_distance = 0;
	Location before_loc, current_loc;
	
	public IceBolt_Bolt(SkillMetadata cast, Player _player, double _damage, double _speed, double _max_distance)
	{
		this.cast = cast;
		player = _player;
		damage = _damage;
		speed = _speed;
		max_distance = _max_distance;

		player.getWorld().getChunkAt(player.getLocation()).getEntities();
		
	}
	
	boolean started = false;
	public void run()
	{
		if (!started) {
			started = true;
			current_loc = player.getEyeLocation().add(0, -0.2, 0);
			before_loc = current_loc.clone();
			dir = current_loc.getDirection();
			current_loc.getWorld().playSound(current_loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
		}
		
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
			current_loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 1, 0, 0, 0, 0);
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
				DamageMetadata ar = new DamageMetadata(damage, DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
				IceMagicEvent ime = new IceMagicEvent(player, ar);
				Bukkit.getPluginManager().callEvent(ime);
				damage = ar.getDamage();
				
				LivingEntity target = (LivingEntity)e;
				Damage.SkillAttack(cast, target, damage, DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
				loc.getWorld().playSound(current_loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 2);
				loc.getWorld().playSound(current_loc, Sound.BLOCK_GLASS_BREAK, 2, 0.7f);
				current_loc.getWorld().spawnParticle(Particle.SNOWBALL, loc, 1, 0, 0, 0, 0);
				current_loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 1, 0, 0, 0, 0);
				current_loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 40, 0, 0, 0, 0);
				Ice_Bolt_Handler.Slow_Target(target, 1, 100, ime.getSuperSlow());
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