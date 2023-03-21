package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class True_Sight extends RegisteredSkill
{
	public True_Sight()
	{	
		super(new True_Sight_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(11, 1));
		addModifier("second", new LinearValue(11.5, 1.5));
		addModifier("cooldown", new LinearValue(19, -1, 10, 20));
		addModifier("stamina", new LinearValue(20, 0.0));
	}
}

class True_Sight_Handler extends MalaSkill implements Listener
{
	public True_Sight_Handler()
	{
		super(	"TRUE_SIGHT",
				"섬광탄",
				Material.NETHER_STAR,
				"&7주변 &8{distance}&7m 내 적들에게 발광과 구속 버프를 부여합니다.",
				"&7버프는 &8{second}&7초간 지속됩니다.",
				"&7투명화에 걸려있는 경우 투명화가 해제됩니다.",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), "mala_sound:skill.flash", 2, 1.5f);
//		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2, 1.5f);
//		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1.5f);
		
		int ticks = (int)(cast.getModifier("second") * 20.0);
		double distance = cast.getModifier("distance");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new True_Sight_Skill(data.getPlayer(), distance));
		
		for(Entity e : data.getPlayer().getNearbyEntities(distance, distance, distance))
		{
			if (e instanceof LivingEntity && e != data.getPlayer())
			{
				LivingEntity le = (LivingEntity)e;
				if (Damage.Is_Possible(data.getPlayer(), le))
				{
					Buff_Manager.Add_Buff(le, PotionEffectType.GLOWING, 0, ticks, PotionEffectType.INVISIBILITY);
					Buff_Manager.Add_Buff(le, PotionEffectType.SLOW, 0, ticks, PotionEffectType.SPEED);
				}
			}
		}
	}
}

class True_Sight_Skill implements Runnable
{
	Player player;
	double current_distance = 2.0;
	double distance = 4.0;
	int time = 0;
	Location loc;
	
	public True_Sight_Skill(Player _player, double _distance)
	{
		player = _player;
		loc = player.getLocation().add(0, player.getHeight() * 0.2, 0).clone();
		
		time = 4;
		distance = _distance;
	}
	
	public void run()
	{
		Location origin_loc = loc.clone();
		Location temp_loc = loc.clone();
		for(double angle = 0.0; angle < 360.0; angle += 10.0 / current_distance)
		{
			temp_loc.setX(origin_loc.getX() + Math.cos(Math.toRadians(angle)) * current_distance);
			temp_loc.setZ(origin_loc.getZ() + Math.sin(Math.toRadians(angle)) * current_distance);
			temp_loc.getWorld().spawnParticle(Particle.FLASH, temp_loc, 1, 0, 0, 0, 0);
		}
		
		current_distance += 2.0;
		if(current_distance < distance)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
