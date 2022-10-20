package mala.mmoskill.skills;

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
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Spear_Circle extends RegisteredSkill
{
	public Spear_Circle()
	{	
		super(new Spear_Circle_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(25, 5));
		addModifier("diameter", new LinearValue(10.5, 0.5));
		addModifier("radius", new LinearValue(5.25, 0.25));
		addModifier("second", new LinearValue(3.6, 0.6, 3, 10));
		addModifier("cooldown", new LinearValue(15, -0.5, 10, 20));
		addModifier("stamina", new LinearValue(14, 0.75));
	}
}

class Spear_Circle_Handler extends MalaSkill implements Listener
{
	public Spear_Circle_Handler()
	{
		super(	"SPEAR_CIRCLE",
				"선풍창",
				Material.FEATHER,
				MsgTBL.NeedSkills,
				"&e 스피어 마스터리 - lv.10",
				"",
				MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7창을 휘둘러 &8{diameter}&7m의 칼날 바람을 만듭니다.",
				"&7바람에 닿은 적들은 &8{power}&7의 피해를 받고서 멀리 밀려납니다.",
				"&7바람은 &8{second}&7초간 유지됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		registerModifiers("radius");
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if (!Weapon_Identify.Hold_Spear(data.getPlayer()))
		{
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			return new SimpleSkillResult(false);
		}
		if(!Skill_Util.Has_Skill(data, "MASTERY_SPEAR", 10))
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
		double damage = cast.getModifier("power"); // 공격력
		double radius = (int)cast.getModifier("radius");
		double second = (int)cast.getModifier("second");
		
		data.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Spear_Circle_Skill(data, damage, radius, second));
	}
	
	class Spear_Circle_Skill implements Runnable
	{
		PlayerData data;
		Player player;
		Location pos;
		
		List<Entity> entities;
		double damage;
		double radius;
		double time;
		
		int count = 0;
		Vector[] vecs;
		
		boolean is_attack = false;
		
		public Spear_Circle_Skill(PlayerData p, double _damage, double _radius, double _time)
		{
			data = p;
			player = p.getPlayer();
			damage = _damage;
			radius = _radius;
			pos = player.getLocation().add(0, 0.5, 0);
			time = _time;
			
			Make_Vecs();
			vecs = TRS.Scale(vecs, radius, radius, radius);
			
			player.getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 2f, 0.5f);
		}
		
		void Make_Vecs()
		{
			vecs = new Vector[12];
			vecs[0] = new Vector(0, 0, 0.1);
			vecs[1] = new Vector(0.45, 0, 0.55);
			vecs[2] = new Vector(0, 0, 1.0);
			vecs[3] = new Vector(0, 0, -0.1);
			vecs[4] = new Vector(-0.45, 0, -0.55);
			vecs[5] = new Vector(0, 0, -1.0);
			vecs[6] = new Vector(0.1, 0, 0);
			vecs[7] = new Vector(0.55, 0, -0.45);
			vecs[8] = new Vector(1.0, 0, 0);
			vecs[9] = new Vector(-0.1, 0, 0);
			vecs[10] = new Vector(-0.55, 0, 0.45);
			vecs[11] = new Vector(-1.0, 0, 0);
		}
		
		public void run()
		{
			vecs = TRS.Rotate_Y(vecs, -9.0);
			
			for (int i = 0; i < vecs.length; i += 3)
			{
				Location one = pos.clone().add(vecs[i]);
				Location two = pos.clone().add(vecs[i + 1]);
				Location three = pos.clone().add(vecs[i + 2]);

				Particle_Drawer.Draw_Line(one, two, Particle.CRIT, 0.1);
				Particle_Drawer.Draw_Line(two, three, Particle.CRIT, 0.1);				
			}
			Particle_Drawer.Draw_Circle(pos, Particle.CRIT, radius);
			if (count++ % 4 == 0)
			{
				pos.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.75f);
			}
			
			for (Entity e : pos.getWorld().getNearbyEntities(pos, radius, radius, radius))
			{
				if (!(e instanceof LivingEntity))
					continue;
				if (e == player)
					continue;
				if (pos.distance(e.getLocation()) > radius)
					continue;
				
				LivingEntity le = (LivingEntity)e;
				if (le.getNoDamageTicks() > 0)
					continue;
				
				if (Damage.Is_Possible(player, le))					
				{
					Damage.Attack(player, le, damage, DamageType.SKILL, DamageType.PHYSICAL);
					le.getWorld().spawnParticle(Particle.SWEEP_ATTACK, le.getEyeLocation(), 15, 0.4, 0.4, 0.4, 0);
					le.getWorld().playSound(le.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2.0f, 1.3f);
					Vector dir = le.getLocation().subtract(pos).toVector().normalize();
					dir.setY(dir.getY() + 0.1);
					dir.multiply(1.5);
					le.setVelocity(dir);
				}
			}
			
			time -= 0.05;
			if (time > 0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}

