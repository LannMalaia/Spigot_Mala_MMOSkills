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
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class HackSlash_Infinite extends RegisteredSkill
{
	public HackSlash_Infinite()
	{	
		super(new HackSlash_Infinite_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(11, 1, 10, 50));
		addModifier("cost", new LinearValue(2, 0.1));
		addModifier("cooldown", new LinearValue(10, 0));
		addModifier("stamina", new LinearValue(10, 0));
	}
}

class HackSlash_Infinite_Handler extends MalaSkill implements Listener
{
	public HackSlash_Infinite_Handler()
	{
		super(	"HACKSLASH_INFINITE",
				"난격",
				Material.NETHERITE_HOE,
				MsgTBL.NeedSkills,
				"&e 난도질 - lv.15",
				"",
				MsgTBL.WEAPON + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&e{power}&7의 힘으로 부채꼴 범위 적 전체에게 피해를 줍니다.",
				"&7매 공격당 &e{cost}&7의 스태미나를 사용하며,",
				"&7스태미나가 닳을 때까지 계속해서 공격합니다.",
				"&7스킬 시전이 길어질수록 더욱 빨리 공격하지만,",
				"&7소모하는 스태미나도 점점 증가합니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_WHIP + "소모 스태미나 50%, 범위 100% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost_All);
	}
	
	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "HACKSLASH", 15))
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
		double cost = cast.getModifier("cost");
		double size = 2.5;
		boolean isWhip = false;
		
		if (Weapon_Identify.Hold_MMO_Whip(data.getPlayer()))
		{
			size *= 2.0;
			cost *= 1.5;
			isWhip = true;
		}
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HackSlash_Infinite_Skill(data.getPlayer(), cost, power, size, isWhip));
	}

	class HackSlash_Infinite_Skill implements Runnable
	{
		Player player;
		PlayerData data;
		List<Entity> entities;
		double damage;
		double cost;
		double size = 2;
		double angle = 180;
		Particle particle;
		
		Vector[] vecs;
		
		public HackSlash_Infinite_Skill(Player p, double _cost, double _damage, double _size, boolean _isWhip)
		{
			player = p; cost = _cost; size = _size; damage = _damage;
			data = PlayerData.get(p);
			particle = _isWhip ? Particle.WAX_ON : Particle.CRIT;
			
			vecs = new Vector[(int)(30 * size)];
			for(int i = 0; i < (int)(30 * size); i++)
			{
				double _angle = 90.0 + (angle * -0.5) + i * angle / (30.0 * size);
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			}
			vecs = TRS.Scale(vecs, size, size, size * 1.5);
		}
		
		long waitingTick = 10;
		public void run()
		{
			player.swingMainHand();
			
			// 검기
			player.getWorld().playSound(player.getEyeLocation(),
					Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
			
			// 파티클 그리기
			Vector[] temp_vecs = TRS.Rotate_Z(vecs, Math.random() * 360.0);
			temp_vecs = TRS.Rotate_X(temp_vecs, player.getLocation().getPitch() + -15.0 + Math.random() * 30.0);
			temp_vecs = TRS.Rotate_Y(temp_vecs, player.getLocation().getYaw() + -15.0 + Math.random() * 30.0);
			for(int i = 0; i < temp_vecs.length; i++)
			{
				Location loc = player.getEyeLocation().add(temp_vecs[i]);
				player.getWorld().spawnParticle(particle, loc, 1, 0d, 0d, 0d, 0d);
			}
			
			// 범위 판정
			Location judge_loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(size * 0.5));
			for(Entity temp : player.getWorld().getNearbyEntities(judge_loc, size, size, size))
			{
				if(Damage.Is_Possible(player, temp) && temp instanceof LivingEntity)
				{
					Location loc = temp.getLocation();
					
					if(judge_loc.distance(loc) < size)
					{
						LivingEntity temp2 = (LivingEntity)temp;
						
						temp2.setNoDamageTicks(0);
						Damage.Attack(player, temp2, damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
					}
				}
			}			
			
			// 잠시 쉬는 시간
			data.giveStamina(-cost, UpdateReason.SKILL_COST);
			if(data.getStamina() > 0)
			{
				cost = cost + 0.05;
				waitingTick = Math.max(1, waitingTick - 1);
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, waitingTick);
			}
		}
	}
}
