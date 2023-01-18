package mala.mmoskill.skills;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Animals;
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

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.LocationSkillResult;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.skills.Arrow_Bomb_Rain.ArrowBombRainSkill;
import mala.mmoskill.skills.Arrow_Rain.ArrowRainSkill;
import mala.mmoskill.util.AttackUtil;
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
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Arrow_Bomb_Rain extends RegisteredSkill
{
	public static Arrow_Bomb_Rain skill;
	public Arrow_Bomb_Rain()
	{	
		super(new Arrow_Bomb_Rain_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("damage", new LinearValue(30, 3));
		addModifier("duration", new LinearValue(2.25, 0.25));
		addModifier("cooldown", new LinearValue(40, 0));
		addModifier("stamina", new LinearValue(70, 0));
		
		skill = this;
	}
	
	public static class ArrowBombRainSkill extends MalaSkillEffect
	{
		double radius = 12.0;
		double damage = 0;
		double speed = 1.0;
		Vector dir;
		
		public ArrowBombRainSkill(PlayerData playerData, double dmgMult, double spdMult)
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
			
			new Effect(targetLocation, Particle.FLAME)
				.append2DCircle(radius)
				.scaleVelocity(0)
				.playEffect();
			world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 1.5f);
			for(int i = 0; i < 4; i++)
			{
				Location loc = targetLocation.clone().add(
						-radius + Math.random() * radius * 2.0,
						20.0,
						-radius + Math.random() * radius * 2.0);
				new Effect(loc, Particle.SMALL_FLAME)
					.append2DLine(20.0, 3.0)
					.rotate(90.0, 0, 0)
					.setVelocity(0, -1, 0)
					.scaleVelocity(1.0)
					.playEffect();
				loc.add(0, -20.0, 0);
				world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0);
				world.spawnParticle(Particle.LAVA, loc, 10, 0, 0, 0);
			}
			world.playSound(targetLocation, Sound.ENTITY_ARROW_HIT, 1.5f, 1.5f);

			if (durationCounter % 6 == 0) {
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

class Arrow_Bomb_Rain_Handler extends MalaLocationSkill implements Listener
{
	public static HashMap<Player, Location> last_location;
	
	public Arrow_Bomb_Rain_Handler()
	{
		super(	"ARROW_BOMB_RAIN",
				"����ȭ���",
				Material.TNT,
				MsgTBL.NeedSkills,
				"&e ȭ��� - lv.10",
				"&e ���� ȭ�� - lv.15",
				"",
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7�ش� ������ ����ȭ��� �����ϴ�.",
				"&7����ȭ���� õõ�� ���ư���, �ش� ������ ���鿡��",
				"&70.3�ʸ��� &e{damage}&7�� ���ظ� �ݴϴ�.",
				"&7����ȭ���� &e{duration}&7�ʰ� ���ӵ˴ϴ�.",
				"&cȰ �Ǵ� ������ ��� �־�� �մϴ�.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_BOW + "���ط� 30% ����",
				MsgTBL.WEAPON_CROSSBOW + "���� �ӵ� 30% ����",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);

		last_location = new HashMap<Player, Location>();
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}
	
	@Override
	public LocationSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "ARROW_RAIN", 10)
			|| !Skill_Util.Has_Skill(data, "EXPLODE_ARROW", 15))
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
				new ArrowBombRainSkill(data, dmgMult, spdMult));
	}
}
