package mala.mmoskill.skills;

import java.util.ArrayList;

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
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Dark_Ball extends RegisteredSkill
{
	public Dark_Ball()
	{	
		super(new Dark_Ball_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(100, 20));
		addModifier("cooldown", new LinearValue(15, 0));
		addModifier("mana", new LinearValue(60, 10));
	}
}

class Dark_Ball_Handler extends MalaSkill implements Listener
{
	public Dark_Ball_Handler()
	{
		super(	"DARK_BALL",
				"다크 볼",
				Material.COAL,
				MsgTBL.SKILL + MsgTBL.MAGIC_DARKNESS + MsgTBL.MAGIC,
				"",
				"&7아주 천천히 나아가는 암흑구를 발사합니다.",
				"&7암흑구에 닿은 적들은 &e{damage}&7의 피해를 받고 &d실명&7에 걸립니다.",
				"&7암흑구는 시간이 지날수록 피해량이 감소해, 최소 50%까지 줄어듭니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double damage = cast.getModifier("damage");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Dark_Ball_Skill(cast, data, damage));
	}
}


class Dark_Ball_Skill implements Runnable
{
	SkillMetadata cast;
	Location pos;
	Vector dir;
	PlayerData playerdata;
	Player player;
	
	double damage;
	
	double radius = 5.0;
	double timer = 10.0;
	double mult = 1.0;
	double speed = 1.0;
	int count = 0;
	
	double angle_correct = 0.0;
	Vector[] vecs;
		
	public Dark_Ball_Skill(SkillMetadata cast, PlayerData _playerdata, double _damage)
	{
		this.cast = cast;
		playerdata = _playerdata;
		player = playerdata.getPlayer();
		
		dir = player.getLocation().getDirection();
		pos = player.getLocation().add(dir.clone().multiply(radius)).add(0.0, 1.0, 0.0);
		damage = _damage;
		
		Make_Ball();
		// pos.getWorld().playSound(pos, Sound.ITEM_TOTEM_USE, 2, 2f);
		pos.getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 2, 0.5f);
		player.setVelocity(dir.clone().multiply(-0.5));
	}
	
	void Make_Ball()
	{
		ArrayList<Vector> temp_vecs = new ArrayList<Vector>();
		
		for(int i = -90; i <= 90; i += 90.0 / radius)
		{
			double altitude = Math.toRadians(i);
			double alt_cos = Math.cos(altitude);
			double alt_sin = Math.sin(altitude);
			
			for(double angle = 0.0; angle <= 360.0; angle += 90.0 / radius)
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

		angle_correct += 8;
		vecs = TRS.Scale(vecs, radius, radius, radius);
		vecs = TRS.Rotate_X(vecs, 45);
		vecs = TRS.Rotate_Y(vecs, angle_correct);
	}

	@Override
	public void run()
	{
		timer -= 0.05;
		count += 1;
		
		if(timer <= 0)
			return;
		
		Make_Ball();
		
		pos.add(pos.getDirection().multiply(0.075));			
		dir = pos.getDirection();
		
		// 파티클
		Draw_Particle();
					
		// 적 찾고 피해주기
		if (count % 10 == 0)
		{
			for (Entity entity : pos.getWorld().getNearbyEntities(pos, radius, radius, radius))
			{
				if(!(entity instanceof LivingEntity))
					continue;
				if(entity == player)
					continue;
				if(pos.distance(entity.getLocation()) > radius)
					continue;
				
				LivingEntity target = (LivingEntity)entity;
				target.setNoDamageTicks(0);
				Damage.SkillAttack(cast, target, damage, DamageType.MAGIC, DamageType.SKILL);
			}
			mult = Math.max(0.5, mult - 0.1);
			radius = Math.max(2.5, radius - 0.3);
		}
		
		// 마무리
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	
	void Draw_Particle()
	{
		DustOptions dop = new Particle.DustOptions(Color.BLACK, 100f);
		for (int i = 0; i < vecs.length; i++)
		{
			Location loc = pos.clone().add(vecs[i]);
			//pos.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
			//pos.getWorld().spawnParticle(Particle.LAVA, loc, 1, 0, 0, 0, 0);
			pos.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dop);
		}
	}
}
