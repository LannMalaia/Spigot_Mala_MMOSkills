package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Mastery_Buff;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Protect_Wave extends RegisteredSkill
{
	public Protect_Wave()
	{	
		super(new Protect_Wave_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(3.75, 0.75, 2, 20));
		addModifier("cooldown", new LinearValue(60, 0));
		addModifier("mana", new LinearValue(66, 6));
	}
}

class Protect_Wave_Handler extends MalaSkill implements Listener
{
	public Protect_Wave_Handler()
	{
		super(	"PROTECT_WAVE",
				"에리어 프로텍트",
				Material.MUSIC_DISC_STAL,
				"&7자신을 포함한 주변 &8{distance}&7 거리에 프로텍트를 부여합니다.",
				"&7이 때 효과는 기존 프로텍트를 따릅니다.",
				"&8프로텍트를 배우지 않았다면 사용할 수 없습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "PROTECT", 1))
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
		
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("PROTECT");
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 2, 2);
		
		int lv = data.getSkillLevel(skill);
		int amp = (int)(skill.getModifier("tier", lv)) - 1;
		int tick = (int)(skill.getModifier("second", lv) * 20.0);
		tick *= Mastery_Buff.Get_Mult(data.getPlayer());
		double distance = cast.getModifier("distance");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new ProtectWaveEffect(data.getPlayer(), distance));

		Protect_Handler.Protect_Target(data.getPlayer(), amp, tick);
		
		for(Entity e : data.getPlayer().getNearbyEntities(distance, distance, distance))
		{
			if(e instanceof Player)
			{
				e.sendMessage("§b" + data.getPlayer().getDisplayName() + "§b님으로부터 가호를 부여받았습니다.");
				Protect_Handler.Protect_Target((Player)e, amp, tick);
			}
		}
		
		Aggro.Add_Threat_Area(data.getPlayer(), distance * 1.5, 5.0);
	}
}

class ProtectWaveEffect implements Runnable
{
	Player player;
	int current_angle = 0;
	int additive_angle = 45;
	double distance = 4.0;
	double upper = 0.0;
	int time = 0;
	Location loc;
	Vector[] shield_vec;
	
	public ProtectWaveEffect(Player _player, double _distance)
	{
		player = _player;
		loc = player.getLocation().add(0, player.getHeight() * 0.5, 0).clone();
		
		additive_angle = 360 / 5;
		time = 4;
		
		distance = _distance;
		
		shield_vec = new Vector[6];
		shield_vec[0] = new Vector(-1.5, 3.0, distance);
		shield_vec[1] = new Vector(1.5, 3.0, distance);
		shield_vec[2] = new Vector(1.5, 0.0, distance);
		shield_vec[3] = new Vector(0, -1.5, distance);
		shield_vec[4] = new Vector(-1.5, 0.0, distance);
		shield_vec[5] = new Vector(-1.5, 3.0, distance);
	}
	
	Vector[] Get_Rotated_Shield(int angle)
	{
		Vector[] new_vecs = new Vector[shield_vec.length];

		double[][] mat = new double[4][4];
		mat[0][0] = Math.cos(Math.toRadians(angle));
		mat[2][0] = -Math.sin(Math.toRadians(angle));
		mat[1][1] = 1;
		mat[0][2] = Math.sin(Math.toRadians(angle));
		mat[2][2] = Math.cos(Math.toRadians(angle));
		mat[3][3] = 1;
		
		double[] vec_mat = new double[4];
		
		for(int i = 0; i < shield_vec.length; i++)
		{
			vec_mat[0] = shield_vec[i].getX();
			vec_mat[1] = shield_vec[i].getY();
			vec_mat[2] = shield_vec[i].getZ();
			vec_mat[3] = 1;
			
			double[] new_mat = new double[4];
			for(int j = 0; j < 4; j++)
			{
				for(int k = 0; k < 4; k++)
				{
					new_mat[j] += mat[k][j] * vec_mat[k];
				}
			}
			new_vecs[i] = new Vector(new_mat[0], new_mat[1], new_mat[2]);
		}
		
		return new_vecs;
	}
	
	public void run()
	{
		Vector[] vecs = Get_Rotated_Shield(current_angle);
		player.getWorld().playSound(loc.clone().add(vecs[0]), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 2);
		for(int j = 0; j < vecs.length - 1; j++)
		{
			for(double k = 0; k < 1.0; k += 0.1)
			{
				Vector lerped_pos = vecs[j + 1].clone().subtract(vecs[j]).multiply(k);
				Location new_loc = loc.clone().add(vecs[j]).add(lerped_pos);
				player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, new_loc, 1, 0, 0, 0, 0);
			}
		}

		current_angle += additive_angle;
		if(time-- > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
	}
}