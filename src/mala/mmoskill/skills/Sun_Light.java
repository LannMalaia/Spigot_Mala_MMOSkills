package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
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

public class Sun_Light extends RegisteredSkill
{
	public Sun_Light()
	{	
		super(new Sun_Light_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(34, 4));
		addModifier("radius", new LinearValue(8.2, 0.2));
		addModifier("cooldown", new LinearValue(30, 0));
		addModifier("mana", new LinearValue(40, 7));
	}
}

class Sun_Light_Handler extends MalaSkill implements Listener
{
	public Sun_Light_Handler()
	{
		super(	"SUN_LIGHT",
				"선 라이트",
				Material.SUNFLOWER,
				MsgTBL.SKILL + MsgTBL.MAGIC_SUN + MsgTBL.MAGIC,
				"",
				"&710초 동안 섬광구를 만듭니다.",
				"&7고리가 유지되는 동안,",
				"&7주변 &e{radius}&7m내 가장 가까운 대상에게 &e{damage}&7 만큼의 피해를 줍니다.",
				"&7섬광구는 시간이 지날수록 피해량이 증가해, 최대 150%까지 늘어납니다.",
				"&c움직일 경우 시전이 취소됩니다.",
				"", MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("damage");
		double radius = cast.getModifier("radius");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Sun_Light_Task(cast, data.getPlayer().getLocation(), data.getPlayer(), damage, radius));
	}
}

class Sun_Light_Task implements Runnable
{
	SkillMetadata cast;
	Location pos;
	Player player;
	double damage;
	
	double beam_radius = 0.5;
	double beam_max_radius;
	double max_radius = 2.0;
	double cur_radius = 0.5;

	double timer = 10.0;
	int count = 0;
	double mult = 1.0;
	
	double angle_correct = 0.0;
	
	Vector[] vecs;
	
	public Sun_Light_Task(SkillMetadata cast, Location _pos, Player _player, double _damage, double _beam_max_radius)
	{
		this.cast = cast;
		pos = _pos;
		player = _player;
		damage = _damage;
		beam_max_radius = _beam_max_radius;

		pos.getWorld().playSound(pos, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.5f);
		Make_Ball();
	}
	
	void Make_Ball()
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		angle_correct += 0.02;
		
		for(int i = -90; i <= 90; i += 60.0 / cur_radius)
		{
			double altitude = Math.toRadians(i + angle_correct);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);
			
			for(double angle = 0.0; angle <= 360.0; angle += 60.0 / cur_radius)
			{
				double rad = Math.toRadians(angle);
				double rad_cos = Math.cos(rad);
				double rad_sin = Math.sin(rad);
				
				Vector pos = new Vector(rad_cos * alt_cos, alt_sin, rad_sin * alt_cos);
				temp_vecs.add(pos.clone());
			}
		}
		
		// 옮겨심기
		vecs = new Vector[temp_vecs.size()];
		for(int i = 0; i < temp_vecs.size(); i++)
			vecs[i] = temp_vecs.get(i);

		//angle_correct += 3;
		vecs = TRS.Scale(vecs, cur_radius, cur_radius, cur_radius);
		vecs = TRS.Translate(vecs, 0.0, 7.5, 0.0);
		// vecs = TRS.Rotate_X(vecs, 45);
		// vecs = TRS.Rotate_Y(vecs, angle_correct);
	}
	
	public void run()
	{
		timer -= 0.05;
		count += 1;
		
		Make_Ball();
		
		// 파티클
		DustOptions dop = new Particle.DustOptions(Color.YELLOW, 20f);
		for (int i = 0; i < vecs.length; i++)
		{
			Location loc = pos.clone().add(vecs[i]).add(0.0, 7.5, 0.0);
			pos.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dop);
			// pos.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
		}
		dop = new Particle.DustOptions(Color.YELLOW, 1.4f);
		for(double angle = 0.0; angle <= 360.0; angle += 15.0 / cur_radius)
		{
			Location temp_pos = pos.clone().add(Math.cos(Math.toRadians(angle)) * beam_radius, 0.5, Math.sin(Math.toRadians(angle)) * beam_radius);
			pos.getWorld().spawnParticle(Particle.REDSTONE, temp_pos, 1, 0, 0, 0, 0, dop);
		}
		
		// 적 찾고 피해주기
		if (count % 5 == 0)
		{
			mult = Math.min(1.5, mult + 0.1);
			LivingEntity target = null;
			double minimum_range = 9999.9;
			for (Entity entity : pos.getWorld().getNearbyEntities(pos, beam_radius, beam_radius, beam_radius))
			{
				if(!(entity instanceof LivingEntity))
					continue;
				if(entity instanceof Tameable)
					continue;
				if(entity instanceof Horse)
					continue;
				if(entity instanceof ArmorStand)
					continue;
				if(entity == player)
					continue;
				if(pos.distance(entity.getLocation()) > minimum_range)
					continue;
				
				target = (LivingEntity)entity;
				minimum_range = pos.distance(entity.getLocation());
			}
			if (target != null)
			{
				Location start = pos.clone().add(0, 7.5 + cur_radius, 0);
				Location end = target.getEyeLocation();
				Particle_Drawer.Draw_Line(start, end, Particle.END_ROD, 0.15);
				pos.getWorld().playSound(target.getEyeLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 2.0f, 0.5f);
				Damage.SkillAttack(cast, target, damage * mult, DamageType.MAGIC, DamageType.SKILL);
			}
		}
		
		if (pos.distance(player.getLocation()) >= 0.5)
		{
			player.sendMessage("§c[ 발동중이던 스킬이 취소되었습니다. ]");
			return;
		}
		
		if (!player.isOnline())
			return;
		
		if (timer <= 0.0)
		{
			player.sendMessage("§c[ 선라이트 시전이 끝났습니다. ]");
			return;
		}
		
		// 마무리
		cur_radius = cur_radius + (max_radius - cur_radius) * 0.1;
		beam_radius = beam_radius + (beam_max_radius - beam_radius) * 0.1;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}