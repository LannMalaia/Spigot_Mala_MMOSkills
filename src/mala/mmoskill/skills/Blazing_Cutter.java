package mala.mmoskill.skills;

import java.util.ArrayList;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Blazing_Cutter extends RegisteredSkill
{
	public Blazing_Cutter()
	{	
		super(new Blazing_Cutter_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(25, 5));
		addModifier("size", new LinearValue(1.5, 0.05));
		addModifier("cooldown", new LinearValue(40, 0));
		addModifier("mana", new LinearValue(57.5, 7.5));
	}
}

class Blazing_Cutter_Handler extends MalaSkill implements Listener
{
	public Blazing_Cutter_Handler()
	{
		super(	"BLAZING_CUTTER",
				"블레이징 커터",
				Material.MAGMA_CREAM,
				MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&715초간 &e{size}&7m 크기의 불의 칼날을 만들어 주변을 돌게 합니다.",
				"&7칼날에 닿은 적들은 &e{power}&7의 피해를 입고서 멀리 날아갑니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("power"); // 공격력
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		double size = (int)cast.getModifier("size");
		
		for (double angle = 0.0; angle <= 360; angle += 180.0)
		{
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Blazing_Cutter_Skill(data.getPlayer(), damage, size, angle));
		}
	}

	class Blazing_Cutter_Skill implements Runnable
	{
		Player player;
		Location pos;
		Vector dir;
		
		List<Entity> entities;
		double damage;
		double size;

		Vector[] vecs;
		
		double angle = 0.0;
		double correction_angle = 0.0;
		double duration = 7.0;
		
		public Blazing_Cutter_Skill(Player p, double _damage, double _size, double _angle)
		{
			player = p;
			damage = _damage;
			size = _size;
			angle = _angle;
			
			pos = p.getEyeLocation().add(0, -0.4, 0);
			dir = p.getLocation().getDirection();

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 2, 1);
			
			vecs = new Vector[(int) (size * 20)];
			double additive = 360.0 / (size * 20);
			for(int i = 0; i < vecs.length; i++)
			{
				double _temp_angle = additive * i;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_temp_angle)), 0, Math.sin(Math.toRadians(_temp_angle)));
			}
			vecs = TRS.Scale(vecs, size, size, size);
		}
		
		DustOptions dop = new DustOptions(Color.ORANGE, 0.5f);
		public void run()
		{			
			// 파티클 그리기
			pos = player.getLocation().add(Math.cos(Math.toRadians(angle)) * (size + 2.5), 1, Math.sin(Math.toRadians(angle)) * (size + 2.5));
			angle += 10.0;
			correction_angle += 3.5;
			Vector[] temp_vecs = TRS.Rotate_Y(vecs, correction_angle);
			temp_vecs = TRS.Rotate_Z(temp_vecs, 0.0);
			temp_vecs = TRS.Rotate_Y(temp_vecs, angle - 90);
			// temp_vecs = TRS.Rotate_X(temp_vecs, pos.getPitch());

			for(int i = 0; i < temp_vecs.length; i++)
			{
				Location loc = pos.clone().add(temp_vecs[i]);
				player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0d, 0d, 0d, 0d);//, dop);//, new Particle.DustOptions(Color.fromRGB(255, 200, 255), 0.1f));
			}
			
			// 범위 판정
			double range = Math.max(8, size * 2.5);
			List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, range, range, range));
			abc.remove(player);
			List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(),
					new Vector(0, 0, 0),
					new Vector(size * 2, 2.5, size * 2),
					abc);
			for(Entity temp : entities)
			{
				if(!(temp instanceof LivingEntity))
					continue;
				if(temp == player)
					continue;
				
				LivingEntity temp2 = (LivingEntity)temp;
				if (temp2.getNoDamageTicks() == 0)
				{
					if (Damage.Is_Possible(player, temp2))
					{
						Damage.Attack(player, temp2, damage,
								DamageType.SKILL, DamageType.MAGIC);
						
						temp2.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, temp2.getEyeLocation(), 15, 0.4, 0.4, 0.4, 0);
						temp2.getWorld().playSound(temp2.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.3f);
						Vector dir = temp2.getLocation().subtract(player.getLocation()).toVector().normalize();
						dir.setY(dir.getY() + 0.1);
						dir.multiply(1.5);
						temp2.setVelocity(dir);
					}
				}
			}
			
			// 잠시 쉬는 시간
			duration -= 0.05;
			if(duration > 0.0)
			{
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
			}
		}
	}
}
