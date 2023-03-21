package mala.mmoskill.skills;

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

public class Lightning_Bolt extends RegisteredSkill
{
	public Lightning_Bolt()
	{	
		super(new Lightning_Bolt_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(20.5, 0.5));
		addModifier("min_damage", new LinearValue(3, 3));
		addModifier("max_damage", new LinearValue(6, 6));
		addModifier("cooldown", new LinearValue(6, 0));
		addModifier("mana", new LinearValue(5, 1));
	}
	public static void Draw_Lightning_Line(Location _start, Location _end)
	{
		Draw_Lightning_Line(_start, _end, Particle.CRIT);
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

			_start.getWorld().spawnParticle(Particle.FLASH, loc.clone().add(vecs.get(i)), 1, 0, 0, 0, 0);
			for(double j = 0; j < dist.length(); j += 0.1)
			{
				_start.getWorld().spawnParticle(_particle, loc.clone().add(vecs.get(i).clone().add(dist.clone().normalize().multiply(j)))
						, 1, 0, 0, 0, 0);
			}
		}

		_start.getWorld().spawnParticle(Particle.FLASH, loc.clone().add(vecs.get(vecs.size() - 1))
				, 1, 0, 0, 0, 0);
		
	}
	
}

class Lightning_Bolt_Handler extends MalaTargetSkill implements Listener
{
	public Lightning_Bolt_Handler()
	{
		super(	"LIGHTNING_BOLT",
				"라이트닝 볼트",
				Material.GHAST_TEAR,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC,
				"",
				"&8{distance}&7 거리 내 적에게 &8{min_damage}~{max_damage}&7의 피해를 줍니다.",
				"&7피해는 즉시 적용됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public TargetSkillResult getResult(SkillMetadata cast)
	{
		TargetSkillResult tsr = new TargetSkillResult(cast, cast.getModifier("distance"), InteractionType.OFFENSE_SKILL);
		if (tsr.isSuccessful(cast))
			return tsr;
		return new TargetSkillResult(cast, 0.0, InteractionType.OFFENSE_SKILL);
	}

	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double min_damage = cast.getModifier("min_damage");
		double max_damage = cast.getModifier("max_damage");
		double damage = min_damage + (max_damage - min_damage) * Math.random();
		damage *= Mastery_Lightning.Get_Mult(data.getPlayer());
		
		// 체인 라이트닝 체크
		RegisteredSkill skill_2 = MMOCore.plugin.skillManager.getSkill("CHAIN_LIGHTNING");
		boolean chain = data.getProfess().hasSkill(skill_2);

		int lv = data.getSkillLevel(skill_2);
		int count = chain ? (int)skill_2.getModifier("move_count", lv) : 0;
		double reduce = chain ? skill_2.getModifier("dam_reduce", lv) * 0.01 : 0;

		DamageMetadata ar = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new LightningMagicEvent(data.getPlayer(), ar));
		damage = ar.getDamage();

		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Lightning_Bolt_Chain(cast, data.getPlayer(), _data.getTarget(), damage, count, reduce));
	}

}

class Lightning_Bolt_Chain implements Runnable
{
	SkillMetadata cast;
	Player player;
	LivingEntity target;
	double damage;
	boolean is_weapon;
	
	Set<LivingEntity> damaged_entities; // 쳐맞은 애들
	int chain_count; // 튕기는 횟수
	double reduce_percent; // 피해 감소치 0.0~1.0

	Location line_start_pos;
	Location line_end_pos;
	
	public Lightning_Bolt_Chain(SkillMetadata cast, Player _player, LivingEntity _start, double _damage, int _count, double _reduce)
	{
		this(cast, _player, _start, _damage, _count, _reduce, false);
	}
	public Lightning_Bolt_Chain(SkillMetadata cast, Player _player, LivingEntity _start, double _damage, int _count, double _reduce, boolean _is_weapon)
	{
		this.cast = cast;
		player = _player;
		target = _start;
		damage = _damage;
		chain_count = _count;
		reduce_percent = _reduce;
		is_weapon = _is_weapon;
		
		damaged_entities = new HashSet<LivingEntity>();
		
		line_start_pos = player.getEyeLocation().clone();
		line_end_pos = target.getEyeLocation().clone();
	}
	
	public void run()
	{
		// 일단 타겟에게 피해 줄 것
		Lightning_Bolt.Draw_Lightning_Line(line_start_pos, line_end_pos);
		if (Damage.Is_Possible(player, target))
		{
			if (is_weapon)
				Damage.SkillAttack(cast, target, damage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
			else
				Damage.SkillAttack(cast, target, damage, DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
		}
		double random_range = 4;
		Vector new_vec = new Vector(
				random_range * -0.5 + Math.random() * random_range,
				2.5,
				random_range * -0.5 + Math.random() * random_range);
		Location loc = line_start_pos.clone().add(new_vec).add(line_start_pos.clone().subtract(line_end_pos).multiply(0.5));
		World world = line_end_pos.getWorld();
		world.playSound(loc, "mala_sound:skill.thunder2", 1, 1.2f);
		world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 2f);
//		world.playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1.5f);
//		world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1.5f);
		
		// 이거 튕겨야 하는지 체크
		if(chain_count > 0)
		{
			// 피해 목록에 체크하고, 피해량은 감소시킴
			damaged_entities.add(target);
			damage *= 1.0 - reduce_percent;
			chain_count -= 1;
			
			// 다음 적 찾기
			boolean searched = false;
			for(Entity e : target.getNearbyEntities(6, 6, 6))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e instanceof ArmorStand)
					continue;
				if(damaged_entities.contains(e))
					continue;
				if(e == player)
					continue;
				LivingEntity le = (LivingEntity)e;
				target = le;
				line_start_pos = line_end_pos.clone();
				line_end_pos = le.getEyeLocation();
				searched = true;
				break;
			}
			
			if(!searched)
				return;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
		}
	}
	
}