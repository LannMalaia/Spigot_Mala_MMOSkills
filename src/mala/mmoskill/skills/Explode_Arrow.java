package mala.mmoskill.skills;

import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.ArrowSkill_Manager;
import mala.mmoskill.manager.ArrowTip;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Explode_Arrow extends RegisteredSkill
{
	public Explode_Arrow()
	{	
		super(new Explode_Arrow_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(3.3, 0.3, 3.0, 9.0));
		addModifier("damage", new LinearValue(14, 4));
		addModifier("cooldown", new LinearValue(13.3, 0.3, 5, 50));
		addModifier("stamina", new LinearValue(22, 2));
	}
}

class Explode_Arrow_Handler extends MalaSkill implements Listener
{
	public Explode_Arrow_Handler()
	{
		super(	"EXPLODE_ARROW",
				"폭탄 화살",
				Material.FIREWORK_STAR,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				MsgTBL.ArrowSkill,
				"",
				"&e{second}&7초간 폭탄 화살을 발사합니다.",
				"&7폭탄 화살은 맞은 자리 주변 적들에게",
				"&75초 동안 초당 &e{damage}&7의 피해를 줍니다.", 
				"&c다른 " + MsgTBL.ArrowSkill + "을 사용할 경우 자동으로 취소됩니다.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double second = cast.getModifier("second");
		double damage = cast.getModifier("damage"); // 피해량
		Player player = data.getPlayer();
		
		// 효과
		player.getWorld().playSound(player, "mala_sound:skill.bow2", 1, 1);
//		player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
		ArrowSkill_Manager.Get_Instance().Register_ArrowSkill(player, new ArrowTip_Explode(cast, player, second, damage));
	}
}

class ArrowTip_Explode extends ArrowTip implements Not_Skill
{
	double damage;
	public ArrowTip_Explode(SkillMetadata cast, Player _player, double _duration, double _damage)
	{
		super(cast, "폭탄 화살", _player, _duration);
		damage = _damage;
	}
	
	@Override
	public void Run()
	{
		Vector dir = player.getLocation().getDirection();
		Location loc = player.getEyeLocation().add(dir.clone().multiply(0.7));
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 2.0f, 0.6f);
		Predicate<Entity> filter = (entity) -> entity instanceof LivingEntity;
		RayTraceResult rtr = loc.getWorld().rayTrace(loc, dir, 40.0, FluidCollisionMode.NEVER, true, 0.1, filter);
		if (rtr == null)
			return;
		Location end_point = rtr.getHitPosition().toLocation(loc.getWorld());
		Particle_Drawer.Draw_Line(loc, end_point, Particle.FIREWORKS_SPARK, 0.5);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new ArrowTip_Explode_Skill(cast, player, damage, end_point));
	}
}

class ArrowTip_Explode_Skill implements Runnable
{
	SkillMetadata cast;
	Player player;
	double damage;
	Location loc;
	
	int counter = 0;
	
	public ArrowTip_Explode_Skill(SkillMetadata _cast, Player _player, double _damage, Location _location)
	{
		cast = _cast;
		player = _player; damage = _damage; loc = _location;
	}
	
	public void run()
	{
		double radius = 2.0;
		
		if (counter % 20 == 0)
		{
			// 파티클 그리기r
			loc.getWorld().spawnParticle(Particle.CRIT, loc, 100, 0, 0, 0, 2.0);
			loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
			Particle_Drawer.Draw_Sphere(loc, Particle.CRIT, radius, 0.0, 0.5);
			loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.5f, 0.7f);
			loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 2f, 1.5f);
			
			for(Entity e : loc.getWorld().getNearbyEntities(loc, radius, radius, radius))
			{
				if(!(e instanceof LivingEntity))
					continue;
				if(e instanceof Animals)
					continue;
				if(e == player)
					continue;
				LivingEntity target = (LivingEntity)e;
				Damage.SkillAttack(cast, target, damage,
						DamageType.PROJECTILE, DamageType.SKILL, DamageType.PHYSICAL);
			}
		}
		else
		{
			loc.getWorld().spawnParticle(Particle.LAVA, loc, 1, 0, 0, 0, 0.1);
		}
		
		if (++counter < 100)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
