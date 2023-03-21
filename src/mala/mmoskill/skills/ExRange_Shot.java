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

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.ArrowSkill_Manager;
import mala.mmoskill.manager.ArrowTip;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class ExRange_Shot extends RegisteredSkill
{
	public ExRange_Shot()
	{	
		super(new ExRange_Shot_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(5.5, 0.5, 5.0, 12.0));
		addModifier("damage", new LinearValue(48, 8));
		addModifier("cooldown", new LinearValue(24.75, 0.25));
		addModifier("stamina", new LinearValue(16.5, 1.5));
	}
}

class ExRange_Shot_Handler extends MalaSkill implements Listener
{
	public ExRange_Shot_Handler()
	{
		super(	"EXRANGE_SHOT",
				"광폭 화살",
				Material.COMPARATOR,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				MsgTBL.ArrowSkill,
				"",
				"&e{second}&7초간 광폭 화살을 발사합니다.",
				"&7광폭 화살은 앞으로 나아가며 주변 적에게 &e{damage}&7의 피해를 줍니다.", 
				"&c다른 " + MsgTBL.ArrowSkill + "을 사용할 경우 자동으로 취소됩니다.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double second = cast.getModifier("second");
		double damage = cast.getModifier("damage"); // 피해량
		Player player = data.getPlayer();
		
		// 효과
		player.getWorld().playSound(player, "mala_sound:skill.bow2", 1, 1);
//		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 2, 1);
		ArrowSkill_Manager.Get_Instance().Register_ArrowSkill(player, new ArrowTip_ExRange(cast, player, second, damage));
	}
}

class ArrowTip_ExRange extends ArrowTip implements Not_Skill
{
	double damage;
	public ArrowTip_ExRange(SkillMetadata cast, Player _player, double _duration, double _damage)
	{
		super(cast, "광폭 화살", _player, _duration);
		damage = _damage;
	}
	
	@Override
	public void Run()
	{
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Edge_Arrow_Skill(cast, player, 30.0, damage));
	}
}

// 광폭 화살 효과
class Edge_Arrow_Skill implements Runnable
{
	SkillMetadata cast;
	Player player;
	double damage;
	double distance;
	List<Entity> entities;
	boolean is_special;
	
	Location pos;
	Vector dir;
	
	double speed = 1.5;
	double angle = 180;
	
	Vector[] vecs;

	public Edge_Arrow_Skill(SkillMetadata _cast, Player _p, double _distance, double _damage)
	{
		this(_cast, _p, _distance, _damage, 0.0, false);
	}
	public Edge_Arrow_Skill(SkillMetadata _cast, Player _p, double _distance, double _damage, double _angle_correct, boolean _special)
	{
		cast = _cast;
		player = _p;
		distance = _distance;
		damage = _damage;
		is_special = _special;
		
		pos = _p.getEyeLocation();
		dir = _p.getLocation().getDirection();
		Vector[] origin_dir = new Vector[1];
		origin_dir[0] = dir.clone();
		dir = TRS.Rotate_Y(origin_dir, _angle_correct)[0];
		
		vecs = new Vector[36];
		for(int i = 0; i < vecs.length; i++)
		{
			double _angle = 90.0 + (angle * -0.5) + i * angle / 36.0;
			vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
		}
		vecs = TRS.Scale(vecs, 5.0, 1.0, 1.0);
		vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch());
		vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw() + _angle_correct);
	}
	
	public void run()
	{
		Location before_pos = pos.clone();
		pos.add(dir.clone().multiply(speed));
		double len = before_pos.distance(pos);
		
		// 파티클 그리기
		pos.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
		for(double i = 0.0; i < len; i += 0.4)
		{
			for(int j = 0; j < vecs.length; j++)
			{
				Location particle_pos = before_pos.clone().add(dir.clone().multiply(i)).add(vecs[j]);
				particle_pos.getWorld().spawnParticle(
						is_special ? Particle.FIREWORKS_SPARK : Particle.CRIT,
						particle_pos, 1, 0, 0, 0, 0);
			}
		}
		
		// 판정 하기
		Location hitbox_axis = before_pos.clone().add(dir.clone().multiply(len * 0.5));
		List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, 10.0, 10.0, 10.0));
		entities = Hitbox.Targets_In_the_BoundingBox(hitbox_axis.toVector(),
				new Vector(pos.getPitch(), pos.getYaw(), 0),
				new Vector(10.0, 4.0, len),
				abc);
		for(Entity en : entities)
		{
			if(!(en instanceof LivingEntity))
				continue;
			if (en == player)
				continue;
			// en.getWorld().spawnParticle(Particle.FLAME, en.getLocation().add(0, 2, 0), 1, 0d, 0d, 0d, 0d);

			Damage.SkillAttack(cast, (LivingEntity)en, damage, DamageType.PROJECTILE, DamageType.SKILL, DamageType.PHYSICAL);
		}
		
		distance -= speed;
		if(distance > 0.0)
		{
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
