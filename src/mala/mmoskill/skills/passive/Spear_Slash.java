package mala.mmoskill.skills.passive;


import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;

public class Spear_Slash extends RegisteredSkill
{
	public Spear_Slash()
	{	
		super(new Spear_Slash_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage", new LinearValue(0.5, 0.5));
	}
}

class Spear_Slash_Handler extends MalaPassiveSkill implements Listener
{
	public Spear_Slash_Handler()
	{
		super(	"SPEAR_SLASH",
				"창날파",
				Material.GOLDEN_HOE,
				"&7창을 든 상태에서 적에게 무기 공격을 시도했을 때,",
				"&7앞으로 미미한 검기가 나아가며 &e{damage}&7의 물리 스킬 피해를 줍니다.");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler (priority = EventPriority.HIGHEST)
	public void attack_spearslash(PlayerAttackEvent event)
	{
		Player player = event.getAttacker().getPlayer();
		PlayerData data = PlayerData.get(player);
		
		if (!event.getDamage().hasType(DamageType.WEAPON))
			return;
		
		// 스킬 체크
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("SPEAR_SLASH");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// 아이템 체크
		if (!Weapon_Identify.Hold_Spear(player))
			return;
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Spear_SlashSkill(player, 0, skill.getModifier("damage", level)));
	}
	
	class Spear_SlashSkill implements Runnable
	{
		Player player;
		Location skill_pos;
		Vector dir, dir_2;
		int count;
		List<Entity> entities;
		double damage;
		double power = 3;
		double angle = 180;
		
		Vector[] vecs;
		
		public Spear_SlashSkill(Player p, int _count, double _damage)
		{
			player = p;
			count = _count;
			damage = _damage;
			skill_pos = player.getLocation().clone().add(new Vector(0, 1, 0));
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
			vecs = TRS.Rotate_Z(vecs, -45.0 + Math.random() * 90.0);
			vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch() + -15.0 + Math.random() * 30.0);
			vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw());// + -15.0 + Math.random() * 30.0);
		}
		
		public void run()
		{
			// 검기
			//player.getWorld().playSound(skill_pos,
			//		Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
			
			// 파티클 그리기
			for(int i = 0; i < vecs.length; i++)
			{
				Location loc = skill_pos.clone().add(vecs[i]);
				player.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0d, 0d, 0d, 0d);
			}
			
			// 범위 판정
			Location judge_loc = skill_pos.clone().add(skill_pos.getDirection().multiply(power * 0.5));
			for(Entity temp : skill_pos.getWorld().getNearbyEntities(judge_loc, power, power, power))
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
					Damage.NormalAttack(player, temp2, damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PHYSICAL);
				}
			}
			
			// 잠시 쉬는 시간
			if(count > 0)
			{
				count--;
				skill_pos.add(dir_2.clone().multiply(power * 0.6));
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2L);
			}
		}
	}
}
