package mala.mmoskill.skills.unused;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import laylia_core.main.Damage;

public class Chakram_Barrier extends Skill
{
	public Chakram_Barrier()
	{
		super();
		
		setName("차크람 배리어");
		setLore("&7거대한 차크람 네 개를 준비합니다.", "&7차크람은 주변을 돌며 &8{power}&7의 피해를 줍니다.",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		setMaterial(Material.END_CRYSTAL);

		addModifier("power", new LinearValue(40, 10));
		addModifier("size", new LinearValue(1, 0.2));
		addModifier("cooldown", new LinearValue(40, -0.25));
		addModifier("stamina", new LinearValue(40, 2));
	}

	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		SkillMetadata cast = new SkillMetadata(data, skill);
		double damage = cast.getModifier("power"); // 공격력
		
		if (!cast.isSuccessful())
			return cast;
		
		double size = (int)cast.getModifier("size");
		
		data.getPlayer().swingMainHand();
		for (double angle = 0.0; angle <= 360; angle += 90.0)
		{
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Chakram_Barrier_Skill(data.getPlayer(), damage, size, angle));
		}
		
		return cast;
	}

	class Chakram_Barrier_Skill implements Runnable
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
		double duration = 30.0;
		
		public Chakram_Barrier_Skill(Player p, double _damage, double _size, double _angle)
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
		
		public void run()
		{			
			// 파티클 그리기
			pos = player.getLocation().add(Math.cos(Math.toRadians(angle)) * size * 3, 1, Math.sin(Math.toRadians(angle)) * size * 3);
			angle += 6.0;
			correction_angle += 2.0;
			Vector[] temp_vecs = TRS.Rotate_Y(vecs, correction_angle);
			temp_vecs = TRS.Rotate_Z(temp_vecs, 20.0);
			temp_vecs = TRS.Rotate_Y(temp_vecs, angle - 90);
			// temp_vecs = TRS.Rotate_X(temp_vecs, pos.getPitch());

			for(int i = 0; i < temp_vecs.length; i++)
			{
				Location loc = pos.clone().add(temp_vecs[i]);
				player.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0d, 0d, 0d, 0d);//, new Particle.DustOptions(Color.fromRGB(255, 200, 255), 0.1f));
			}
			
			// 범위 판정
			double range = Math.max(10, size * 3);
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
				
				Location loc = temp.getLocation();
				LivingEntity temp2 = (LivingEntity)temp;
				if (temp2.getNoDamageTicks() == 0)
				{
					Damage.Attack(player, temp2, damage, DamageType.PROJECTILE, DamageType.SKILL, DamageType.PHYSICAL);
					temp2.setNoDamageTicks(10);
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
