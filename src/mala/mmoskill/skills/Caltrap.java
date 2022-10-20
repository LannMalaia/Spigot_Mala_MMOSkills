package mala.mmoskill.skills;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Caltrap extends RegisteredSkill
{
	public Caltrap()
	{	
		super(new Caltrap_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(4.4, 0.4));
		addModifier("sec", new LinearValue(3.3, 0.3));
		addModifier("cooldown", new LinearValue(35.5, 0.5));
		addModifier("stamina", new LinearValue(0, 0));
	}
}

class Caltrap_Handler extends MalaLocationSkill implements Listener
{
	public Caltrap_Handler()
	{
		super(	"CALTRAP",
				"마름쇠",
				Material.MEDIUM_AMETHYST_BUD,
				MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&77m 범위의 마름쇠를 뿌립니다.",
				"&7마름쇠를 밟는 적들은 1초마다 &e{damage}&7의 피해를 받습니다.",
				"&7마름쇠는 &e{sec}&7초간 유지됩니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "피해량 30% 증가",
				MsgTBL.WEAPON_WHIP + "구속 1을 5초간 부여",
				"",
				MsgTBL.Cooldown);
	}

	@Override
	public void whenCast(LocationSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		_data.getTarget();
		double duration = cast.getModifier("sec");
		double damage = cast.getModifier("damage");
		boolean isSlow = false;
		if (Weapon_Identify.Hold_Sword(data.getPlayer()))
			damage *= 1.3;
		if (Weapon_Identify.Hold_MMO_Whip(data.getPlayer()))
			isSlow = true;
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Caltrap_Skill(data.getPlayer(), _data.getTarget(), damage, duration, isSlow));
	}
}

class Caltrap_Skill implements Runnable
{
	Player player;
	Location skillLoc;
	double damage;
	double duration;
	boolean isSlow;
	
	Particle particle;
	Set<Location> locations;
	
	public Caltrap_Skill(Player _player, Location _skillLoc, double _damage, double _duration, boolean _isSlow)
	{
		player = _player; skillLoc = _skillLoc; damage = _damage; duration = _duration; isSlow = _isSlow;

		locations = new HashSet<>();
		particle = isSlow ? Particle.CRIT_MAGIC : Particle.CRIT;
		
		skillLoc.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
		Random rand = new Random();
		for (int i = 0; i < 36; i++)
		{
			Location loc = skillLoc.clone();
			loc.add(rand.nextDouble(7.0) - 3.5, 0.1, rand.nextDouble(7.0) - 3.5);
			Particle_Drawer.Draw_Line(player.getEyeLocation(), loc, particle, 0.1);
			locations.add(loc);

			if (i % 8 == 0)
			{
				skillLoc.getWorld().playSound(skillLoc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 2);
				skillLoc.getWorld().playSound(skillLoc, Sound.BLOCK_GLASS_BREAK, 2, 0.7f);
			}
		}
	}
	
	int counter = 0;
	public void run()
	{
		for (Location loc : locations)
			skillLoc.getWorld().spawnParticle(particle, loc, 1, 0d, 0d, 0d, 0d);
		
		if (counter++ % 20 == 0)
		{
			for (Entity entity : skillLoc.getWorld().getNearbyEntities(skillLoc, 7.0, 7.0, 7.0))
			{
				if (Damage.Is_Possible(player, entity) && entity instanceof LivingEntity)
				{
					if (entity.isOnGround())
					{
						Damage.Attack(player, (LivingEntity)entity, damage, DamageType.PHYSICAL, DamageType.SKILL);
						if (isSlow)
							Buff_Manager.Increase_Buff((LivingEntity)entity, PotionEffectType.SLOW, 0, 100, null, 0);
					}
				}
			}
		}
		
		duration -= 0.05;
		if (duration > 0.0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
