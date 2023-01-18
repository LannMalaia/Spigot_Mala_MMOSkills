package mala.mmoskill.skills;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.events.PhysicalSkillEvent;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class HackSlash extends RegisteredSkill
{
	public HackSlash()
	{	
		super(new HackSlash_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(14.3, 1.3));
		addModifier("count", new LinearValue(3.2, 0.2));
		addModifier("cooldown", new LinearValue(10, 0));
		addModifier("stamina", new LinearValue(16, 1.0));
	}
}

class HackSlash_Handler extends MalaSkill implements Listener
{
	public HackSlash_Handler()
	{
		super(	"HACKSLASH",
				"난도질",
				Material.STONE_HOE,
				MsgTBL.WEAPON + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&8{power}&7의 힘으로 부채꼴 범위 적 전체에게 피해를 줍니다.",
				"&7총 &8{count}&7번 공격합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double power = cast.getModifier("power"); // 공격력
		
		int count = (int)cast.getModifier("count");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HackSlashSkill(data.getPlayer(), count, power));
		
		for (Summoned_OBJ so : Summon_Manager.Get_Instance().Get_Summoned_OBJs(data.getPlayer(), "Doppelganger"))
		{
			LivingEntity as = (LivingEntity)so.entity;
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
					new HackSlashSkill_Doppel(as, data.getPlayer(), count, power));
		}
	}

	class HackSlashSkill implements Runnable
	{
		Player player;
		Vector skill_pos, dir, dir_2;
		int count;
		List<Entity> entities;
		double damage;
		double power = 3;
		double angle = 180;
		
		Vector[] vecs;
		
		public HackSlashSkill(Player p, int _count, double _damage)
		{
			player = p;
			count = _count;
			damage = _damage;
			skill_pos = player.getLocation().toVector().clone().add(new Vector(0, 1, 0));
			dir_2 = player.getLocation().getDirection().clone();
			dir_2.setY(0.0);
			dir = new Vector(Math.cos(Math.toRadians(player.getLocation().getYaw())), 3d * Math.cos(Math.random() * Math.PI ), Math.sin(Math.toRadians(player.getLocation().getYaw()))).normalize().clone();
			
			vecs = new Vector[60];
			for(int i = 0; i < 60; i++)
			{
				double _angle = 90.0 + (angle * -0.5) + i * angle / 60.0;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			}
			vecs = TRS.Scale(vecs, power, power, power * 1.5);
		}
		
		public void run()
		{
			player.swingMainHand();
			
			// 검기
			player.getWorld().playSound(skill_pos.toLocation(player.getWorld()),
					Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
			
			// 파티클 그리기
			Vector[] temp_vecs = TRS.Rotate_Z(vecs, Math.random() * 360.0);
			temp_vecs = TRS.Rotate_X(temp_vecs, player.getLocation().getPitch() + -15.0 + Math.random() * 30.0);
			temp_vecs = TRS.Rotate_Y(temp_vecs, player.getLocation().getYaw() + -15.0 + Math.random() * 30.0);
			for(int i = 0; i < temp_vecs.length; i++)
			{
				Location loc = player.getEyeLocation().add(temp_vecs[i]);
				player.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0d, 0d, 0d, 0d);
			}
			
			// 범위 판정
			Location judge_loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(power * 0.5));
			for(Entity temp : player.getWorld().getNearbyEntities(judge_loc, power, power, power))
			{
				if(!(temp instanceof LivingEntity))
					continue;
				if(temp == player)
					continue;
				
				Location loc = temp.getLocation();

				if(judge_loc.distance(loc) < power)
				{
					LivingEntity temp2 = (LivingEntity)temp;
					
					temp2.setNoDamageTicks(0);
					Damage.Attack(player, temp2, damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
				}
			}
			
			// 잠시 쉬는 시간
			if(count > 0)
			{
				count--;
				skill_pos.add(dir_2.clone().multiply(power * 0.3));
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2L);
			}
		}
	}

	class HackSlashSkill_Doppel implements Runnable
	{
		LivingEntity as;
		Player player;
		Vector skill_pos, dir, dir_2;
		int count;
		List<Entity> entities;
		double damage;
		double power = 2;
		double angle = 180;
		
		Vector[] vecs;
		
		public HackSlashSkill_Doppel(LivingEntity _as, Player p, int _count, double _damage)
		{
			as = _as;
			player = p;
			count = _count;
			damage = _damage;
			skill_pos = as.getLocation().toVector().clone().add(new Vector(0, 1, 0));
			dir_2 = as.getLocation().getDirection().clone();
			dir_2.setY(0.0);
			dir = new Vector(Math.cos(Math.toRadians(as.getLocation().getYaw())), 3d * Math.cos(Math.random() * Math.PI ), Math.sin(Math.toRadians(as.getLocation().getYaw()))).normalize().clone();
			
			vecs = new Vector[60];
			for(int i = 0; i < 60; i++)
			{
				double _angle = 90.0 + (angle * -0.5) + i * angle / 60.0;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			}
			vecs = TRS.Scale(vecs, power, power, power * 1.5);
		}
		
		public void run()
		{
			as.swingMainHand();
			
			// 검기
			as.getWorld().playSound(skill_pos.toLocation(as.getWorld()),
					Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
			
			// 파티클 그리기
			Vector[] temp_vecs = TRS.Rotate_Z(vecs, Math.random() * 360.0);
			temp_vecs = TRS.Rotate_X(temp_vecs, as.getLocation().getPitch() + -15.0 + Math.random() * 30.0);
			temp_vecs = TRS.Rotate_Y(temp_vecs, as.getLocation().getYaw() + -15.0 + Math.random() * 30.0);
			for(int i = 0; i < temp_vecs.length; i++)
			{
				Location loc = as.getEyeLocation().add(temp_vecs[i]);
				as.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0d, 0d, 0d, 0d);
			}
			
			// 범위 판정
			Location judge_loc = as.getEyeLocation().add(as.getLocation().getDirection().multiply(power * 0.5));
			for(Entity temp : as.getWorld().getNearbyEntities(judge_loc, power, power, power))
			{
				if(!(temp instanceof LivingEntity))
					continue;
				if(temp == player)
					continue;
				
				Location loc = temp.getLocation();

				if(judge_loc.distance(loc) < power)
				{
					LivingEntity temp2 = (LivingEntity)temp;
					
					temp2.setNoDamageTicks(0);
					Damage.Attack(player, temp2, damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
				}
			}
			
			// 잠시 쉬는 시간
			if(count > 0)
			{
				count--;
				skill_pos.add(dir_2.clone().multiply(power * 0.3));
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2L);
			}
		}
	}

}
