package mala.mmoskill.skills;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.skills.Arrow_Rain.ArrowRainSkill;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaLocationSkill;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaSkillEffect;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Arrow_Rain extends RegisteredSkill
{
	public static Arrow_Rain skill;
	public Arrow_Rain()
	{	
		super(new Arrow_Rain_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(8.8, 0.8));
		addModifier("duration", new LinearValue(1.15, 0.15));
		addModifier("cooldown", new LinearValue(30, 0));
		addModifier("stamina", new LinearValue(50, 0));
		
		skill = this;
	}
	
	public static class ArrowRainSkill extends MalaSkillEffect
	{
		double radius = 7.0;
		double damage = 0;
		double speed = 1.0;
		Vector dir;
		
		public ArrowRainSkill(PlayerData playerData, double dmgMult, double spdMult)
		{
			super(playerData.getPlayer(), 4.0);
			damage = skill.getModifier("damage", playerData.getSkillLevel(skill));
			damage *= dmgMult;
			targetDuration = skill.getModifier("duration", playerData.getSkillLevel(skill));
			dir = frontLocation.getDirection().setY(0).normalize();
			speed *= spdMult;
		}
		
		@Override
		public void whenCount() {
			targetLocation.add(dir.clone().multiply(speed * 0.05));
			
			new Effect(targetLocation, Particle.CRIT)
				.append2DCircle(radius)
				.scaleVelocity(0)
				.playEffect();
			world.playSound(targetLocation, Sound.ENTITY_ARROW_SHOOT, 2.0f, 1.5f);
			for(int i = 0; i < 4; i++)
			{
				Location loc = targetLocation.clone().add(
						-radius + Math.random() * radius * 2.0,
						20.0,
						-radius + Math.random() * radius * 2.0);
				new Effect(loc, Particle.CRIT)
					.append2DLine(20.0, 6.0)
					.rotate(90.0, 0, 0)
					.setVelocity(0, -1, 0)
					.scaleVelocity(2.0)
					.playEffect();
			}
			world.playSound(targetLocation, Sound.ENTITY_ARROW_HIT, 1.5f, 1.5f);

			if (durationCounter % 2 == 0)
			{
				AttackUtil.attackCylinder(attacker,
						targetLocation, radius, 20.0,
						damage, null, true,
						DamageType.PROJECTILE, DamageType.SKILL);
			}
		}

		@Override
		public void whenStart() {
		}

		@Override
		public void whenEnd() {
		}
	}
}

class Arrow_Rain_Handler extends MalaLocationSkill implements Listener
{
	public static HashMap<Player, Location> last_location;
	
	public Arrow_Rain_Handler()
	{
		super(	"ARROW_RAIN",
				"화살비",
				Material.TIPPED_ARROW,
				MsgTBL.NeedSkills,
				"&e 일제 사격 - lv.20",
				"",
				MsgTBL.PROJECTILE + MsgTBL.SKILL,
				"",
				"&7해당 지역에 화살비를 내립니다.",
				"&7화살비는 천천히 나아가며, 해당 구역의 적들에게",
				"&70.1초마다 &e{damage}&7의 피해를 줍니다.",
				"&7화살비는 &e{duration}&7초간 지속됩니다.",
				"&c활 또는 석궁을 들고 있어야 합니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_BOW + "피해량 30% 증가",
				MsgTBL.WEAPON_CROSSBOW + "전진 속도 30% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);

		last_location = new HashMap<Player, Location>();
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public LocationSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "BARRAGE", 20))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new LocationSkillResult(cast, 0.0);
		}
		if (!(Weapon_Identify.Hold_Bow(data.getPlayer()) || Weapon_Identify.Hold_Crossbow(data.getPlayer())))
		{
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			return new LocationSkillResult(cast, 0.0);
		}
		return new LocationSkillResult(cast, range);
	}

	@Override
	public void whenCast(LocationSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Location loc = _data.getTarget().add(0, 1.1, 0);

		double dmgMult = Weapon_Identify.Hold_Bow(data.getPlayer()) ? 1.3 : 1.0;
		double spdMult = Weapon_Identify.Hold_Crossbow(data.getPlayer()) ? 1.3 : 1.0;
		
		last_location.put(data.getPlayer(), loc);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new ArrowRainSkill(data, dmgMult, spdMult));
	}
	
}

