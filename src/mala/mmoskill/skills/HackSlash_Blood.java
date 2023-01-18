package mala.mmoskill.skills;

import java.util.List;

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
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class HackSlash_Blood extends RegisteredSkill
{
	public static HackSlash_Blood skill;
	public HackSlash_Blood()
	{	
		super(new HackSlash_Blood_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(8.7, 1.2));
		addModifier("last_dmg", new LinearValue(87, 12));
		//addModifier("power", new LinearValue(20, 2.0));
		//addModifier("last_dmg", new LinearValue(200, 20));
		addModifier("count", new LinearValue(11, 1));
		addModifier("cooldown", new LinearValue(25, 0));
		addModifier("stamina", new LinearValue(26.75, 1.75));
		
		skill = this;
	}
}

class HackSlash_Blood_Handler extends MalaSkill implements Listener
{
	public HackSlash_Blood_Handler()
	{
		super(	"HACKSLASH_BLOOD",
				"핏빛 검무",
				Material.RED_NETHER_BRICKS,
				MsgTBL.NeedSkills,
				"&e 난도질 - lv.30",
				"",
				MsgTBL.WEAPON + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&8{power}&7의 힘으로 부채꼴 범위 적 전체에게 피해를 줍니다.",
				"&7총 &8{count}&7번 공격합니다.",
				"&7마지막 공격은 범위가 더 넓으며, &8{last_dmg}&7의 피해를 줍니다.",
				"",
				MsgTBL.Cooldown_Fixed, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "HACKSLASH", 30))
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
		double power = cast.getModifier("power"); // 공격력
		
		int count = (int)cast.getModifier("count");
		double last_dmg = cast.getModifier("last_dmg");

		CooldownFixer.Fix_Cooldown(data, HackSlash_Blood.skill);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new BloodHackSlashSkill(data.getPlayer(), count, power, last_dmg));
		}
	class BloodHackSlashSkill implements Runnable
	{
		Player player;
		Vector skill_pos, dir, dir_2;
		int count;
		List<Entity> entities;
		double damage, last_dmg;
		double power = 4;
		double angle = 180;
		
		Vector[] vecs;
		
		public BloodHackSlashSkill(Player p, int _count, double _damage, double _last_dmg)
		{
			player = p;
			count = _count;
			damage = _damage;
			last_dmg = _last_dmg;
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
		
		DustOptions dop = new DustOptions(Color.RED, 1.5f);
		DustOptions dop2 = new DustOptions(Color.BLACK, 2.0f);
		public void run()
		{
			player.swingMainHand();
			
			// 검기
			player.getWorld().playSound(skill_pos.toLocation(player.getWorld()),
					Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
			player.getWorld().playSound(skill_pos.toLocation(player.getWorld()),
					Sound.ENTITY_GHAST_SHOOT, 2f, 2f);
			if (count == 0)
				player.getWorld().playSound(skill_pos.toLocation(player.getWorld()),
						Sound.ITEM_SHIELD_BREAK, 2f, 2f);
			
			// 파티클 그리기
			int atk_count = count == 0 ? 6 : 1;
			for (int k = 0; k < atk_count; k++)
			{
				Vector[] temp_vecs = TRS.Rotate_Z(vecs, Math.random() * 360.0);
				temp_vecs = TRS.Rotate_X(temp_vecs, player.getLocation().getPitch() + -15.0 + Math.random() * 30.0);
				temp_vecs = TRS.Rotate_Y(temp_vecs, player.getLocation().getYaw() + -15.0 + Math.random() * 30.0);
				for(int i = 0; i < temp_vecs.length; i++)
				{
					Location loc = player.getEyeLocation().add(temp_vecs[i]);
					player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0d, 0d, 0d, 0d, dop);
	
					if (count == 0)
						player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0d, 0d, 0d, 0d, dop2);
				}
			}
			
			// 범위 판정
			Location judge_loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(power * 0.5));
			for(Entity temp : player.getWorld().getNearbyEntities(judge_loc, power * 1.5, power * 1.5, power * 1.5))
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
			if(count-- >= 0)
			{
				if (count == 0)
				{
					power = 8;
					vecs = TRS.Scale(vecs, 2.0, 2.0, 2.0);
					damage = last_dmg;
					player.getWorld().playSound(skill_pos.toLocation(player.getWorld()),
							Sound.ENTITY_CREEPER_PRIMED, 2f, 2f);
					Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 15L);
				}
				else if (count > 0)
				{
					skill_pos.add(dir_2.clone().multiply(power * 0.3));
					Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
				}
			}
		}
	}
}
