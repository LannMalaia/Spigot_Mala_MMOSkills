package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Aerial_Slash extends RegisteredSkill
{
	public Aerial_Slash()
	{	
		super(new Aerial_Slash_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("power", new LinearValue(12, 2));
		addModifier("size", new LinearValue(1.5, 0.2));
		addModifier("cooldown", new LinearValue(13, -0.2));
		addModifier("stamina", new LinearValue(12, 0.7, 15, 35));
	}
}

class Aerial_Slash_Handler extends SkillHandler<SimpleSkillResult> implements Listener, Not_Skill
{
	public static ConfigurationSection Altered_Config(ConfigurationSection config)
	{
		config.set("name", "부메랑 칼날");
		List<String> lores = new ArrayList<String>();
		lores.add(MsgTBL.PROJECTILE + MsgTBL.SKILL);
		lores.add("");
		lores.add("&7전방으로 날카로운 검기를 날려보냅니다.");
		lores.add("&7검기는 일정 거리 나아가다가 다시 돌아옵니다.");
		lores.add("&7검기에 닿은 적들은 계속해서 &8{power}&7의 피해를 받습니다.");
		lores.add("");
		lores.add(MsgTBL.WEAPON_EFFECT);
		lores.add(MsgTBL.WEAPON_SWORD + "피해량 50% 증가");
		lores.add(MsgTBL.WEAPON_SPEAR + "범위 100% 증가");
		lores.add("");
		lores.add(MsgTBL.Cooldown);
		lores.add(MsgTBL.StaCost);
		config.set("lore", lores);
		config.set("material", "SUNFLOWER");
		
//		addModifier("power", new LinearValue(12, 4));
//		addModifier("size", new LinearValue(1.5, 0.2));
//		addModifier("cooldown", new LinearValue(20, -0.4));
//		addModifier("mana", new LinearValue(12, 2.5));
//		addModifier("stamina", new LinearValue(10, 0.5));
		return config;
	}
	
	public Aerial_Slash_Handler()
	{
		super(Altered_Config(MalaMMO_Skill.plugin.getConfig()), "AERIAL_SLASH");
		registerModifiers("power", "size");
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata arg0)
	{
		return new SimpleSkillResult(true);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("power"); // 공격력
		double size = (int)cast.getModifier("size");

		// 무기 효과
		if (Weapon_Identify.Hold_MMO_Sword(data.getPlayer()))
			damage *= 1.5;
		else if (Weapon_Identify.Hold_MMO_Spear(data.getPlayer()))
			size *= 2.0;
		
		data.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Aerial_SlashSkill(cast, data.getPlayer(), damage, size));
	}
}

// 검술 베어가르기  효과
class Aerial_SlashSkill implements Runnable
{
	SkillMetadata cast;
	Player player;
	Location pos;
	Vector dir;
	
	List<Entity> entities;
	double damage;
	double size;

	double max_speed = 0.6;
	double current_speed = 0.6;
	double decelerate = -0.015;
	Vector[] vecs;
	
	double correct_angle = 0.0;
	double correct_Zangle = 0.0;
	
	public Aerial_SlashSkill(SkillMetadata cast, Player p, double _damage, double _size)
	{
		this.cast = cast;
		player = p;
		damage = _damage;
		size = _size;

		max_speed = size * 0.3;
		current_speed = size * 0.3;
		decelerate = current_speed * 0.03;
		
		pos = p.getEyeLocation().add(0, -0.3, 0);
		dir = p.getLocation().getDirection();
		
		vecs = new Vector[(int) (size * 60)];
		double additive = 360.0 / (size * 60);
		for(int i = 0; i < vecs.length; i += 2)
		{
			double _angle = -180.0 + additive * i;
			vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			vecs[i + 1] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.85, 0, Math.sin(Math.toRadians(_angle)) * 0.85);// * 0.7);
		}
		vecs = TRS.Scale(vecs, size, size, size);
		correct_Zangle = -30.0 + Math.random() * 60.0;
	}
	
	public void run()
	{
		// 검기
		player.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
		
		// 파티클 그리기
		correct_angle += 40.0;
		Vector[] temp_vecs = TRS.Rotate_Y(vecs, correct_angle);
		temp_vecs = TRS.Rotate_Z(temp_vecs, correct_Zangle);
		temp_vecs = TRS.Rotate_X(temp_vecs, pos.getPitch());
		temp_vecs = TRS.Rotate_Y(temp_vecs, pos.getYaw());

		for(int i = 0; i < temp_vecs.length; i++)
		{
			Location loc = pos.clone().add(temp_vecs[i]);
			player.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0d, 0d, 0d, 0d);
		}
		
		// 범위 판정
		List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, size * 3, size * 3, size * 3));
		List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(),
				new Vector(pos.getPitch(), pos.getYaw(), 0),
				new Vector(size * 2, 2.5, size * 2),
				abc);
		for (Entity temp : entities)
		{
			if(!(temp instanceof LivingEntity))
				continue;
			if(temp == player)
				continue;
			LivingEntity temp2 = (LivingEntity)temp;
			if (Damage.Is_Possible(player, temp2))
			{
				Damage.SkillAttack(cast, temp2, damage,
						DamageType.PROJECTILE, DamageType.SKILL);
				temp2.setNoDamageTicks(10);
			}
		}
		
		// 잠시 쉬는 시간
		if(current_speed > -max_speed)
		{
			pos.add(dir.clone().multiply(current_speed));
			current_speed -= decelerate;
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
		}
	}
}