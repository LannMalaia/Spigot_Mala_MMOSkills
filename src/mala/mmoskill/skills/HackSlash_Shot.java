package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class HackSlash_Shot extends RegisteredSkill
{
	public HackSlash_Shot()
	{	
		super(new HackSlash_Shot_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(9, 2));
		addModifier("cooldown", new LinearValue(30, -0.5, 10, 30));
		addModifier("stamina", new LinearValue(21, 1));
	}
}

class HackSlash_Shot_Handler extends MalaSkill implements Listener
{
	public HackSlash_Shot_Handler()
	{
		super(	"HACKSLASH_SHOT",
				"칼날 방사",
				Material.IRON_HOE,
				MsgTBL.NeedSkills,
				"&e 난도질 - lv.10",
				"&e 일제 사격 - lv.10",
				"",
				MsgTBL.PROJECTILE + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&7칼날 화살을 쏩니다.",
				"&7칼날 화살은 전방 5방향으로 나아가며 &e{damage}&7의 피해를 줍니다.", 
				"&c활이 없어도 사용할 수 있습니다.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "HACKSLASH", 10)
			|| !Skill_Util.Has_Skill(data, "BARRAGE", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double damage = cast.getModifier("damage"); // 피해량
		Player player = data.getPlayer();
		
		// 효과
		player.getWorld().playSound(player, "mala_sound:skill.bow1", 1, 1);

		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HackSlash_Arrow_Skill(cast, player, 15, damage, -80.0, false));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HackSlash_Arrow_Skill(cast, player, 20, damage, -40.0, false));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HackSlash_Arrow_Skill(cast, player, 25, damage, 0.0, false));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HackSlash_Arrow_Skill(cast, player, 20, damage, 40.0, false));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HackSlash_Arrow_Skill(cast, player, 15, damage, 80.0, false));
	}

	// 칼날 화살 효과
	class HackSlash_Arrow_Skill implements Runnable
	{
		SkillMetadata cast;
		Player player;
		double damage;
		double distance;
		List<Entity> entities;
		boolean is_special;
		
		Location pos;
		Vector dir;
		
		double speed = 0.2;
		double angle = 180;
		double corrected_angle = 0;
		
		Vector[] vecs;

		public HackSlash_Arrow_Skill(SkillMetadata cast, Player _p, double _distance, double _damage)
		{
			this(cast, _p, _distance, _damage, 0.0, false);
		}
		public HackSlash_Arrow_Skill(SkillMetadata cast, Player _p, double _distance, double _damage, double _angle_correct, boolean _special)
		{
			this.cast = cast;
			player = _p;
			distance = _distance;
			damage = _damage;
			is_special = _special;
			corrected_angle = _angle_correct;
			
			pos = _p.getEyeLocation();
			dir = _p.getLocation().getDirection();
			Vector[] origin_dir = new Vector[1];
			origin_dir[0] = dir.clone();
			dir = TRS.Rotate_Y(origin_dir, _angle_correct)[0];
			
			vecs = new Vector[64];
			for(int i = 0; i < vecs.length; i++)
			{
				double _angle = 90.0 + (angle * -0.5) + i * angle / 64.0;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			}
			vecs = TRS.Scale(vecs, 3.0, 1.0, 1.0);
		}
		
		int count = 0;
		public void run()
		{
			count++;
			Location before_pos = pos.clone();
			pos.add(dir.clone().multiply(speed));
			
			// 파티클 그리기
			vecs = TRS.Rotate_Z(vecs, Math.random() * 360.0);
			vecs = TRS.Rotate_X(vecs, Math.random() * 360.0);//pos.getPitch());
			vecs = TRS.Rotate_Y(vecs, Math.random() * 360.0);//pos.getYaw() + corrected_angle);
			pos.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.5f);
//			for(double i = 0.0; i < len; i += 0.4)
//			{
				for(int j = 0; j < vecs.length; j++)
				{
					Location particle_pos = before_pos.clone().add(dir.clone().multiply(1.0)).add(vecs[j]);
					particle_pos.getWorld().spawnParticle(
							is_special ? Particle.ELECTRIC_SPARK : Particle.ELECTRIC_SPARK,
							particle_pos, 1, 0, 0, 0, 0);
				}
//			}
			
			// 판정 하기
			if (count % 3 == 0)
			{
				for(Entity en : pos.getWorld().getNearbyEntities(pos, 3.0, 3.0, 3.0))
				{
					if(!(en instanceof LivingEntity))
						continue;
					if (en == player)
						continue;
					// en.getWorld().spawnParticle(Particle.FLAME, en.getLocation().add(0, 2, 0), 1, 0d, 0d, 0d, 0d);
	
					((LivingEntity)en).setNoDamageTicks(0);
					Damage.SkillAttack(cast, (LivingEntity)en, damage, DamageType.PROJECTILE, DamageType.SKILL, DamageType.PHYSICAL);
				}
			}
			distance -= speed;
			if(distance > 0.0)
			{
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			} 
		}
	}
}