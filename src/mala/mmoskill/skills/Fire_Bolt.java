package mala.mmoskill.skills;

import java.util.ArrayList;
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

import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import laylia_core.main.Damage;

public class Fire_Bolt extends RegisteredSkill
{
	public Fire_Bolt()
	{	
		super(new Fire_Bolt_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("bolt_count", new LinearValue(1.2, 0.2));
		addModifier("damage", new LinearValue(10, 10));
		addModifier("cooldown", new LinearValue(5.25, 0.25));
		addModifier("mana", new LinearValue(5, 1));
	}
}

class Fire_Bolt_Handler extends MalaTargetSkill implements Listener
{
	public Fire_Bolt_Handler()
	{
		super(	"FIRE_BOLT",
				"파이어 볼트",
				Material.BLAZE_POWDER,
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&7적에게 &e{bolt_count}&7개의 화염탄을 한번에 발사합니다.",
				"&7화염탄은 &e{damage}&7의 피해를 줍니다.",
				"&7맞은 적은 발화 상태에 걸립니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public TargetSkillResult getResult(SkillMetadata cast)
	{
		TargetSkillResult tsr = new TargetSkillResult(cast, 35.0, InteractionType.OFFENSE_SKILL);
		if (tsr.isSuccessful(cast))
			return tsr;
		return new TargetSkillResult(cast, 0.0, InteractionType.OFFENSE_SKILL);
	}
	
	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("damage");
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());

		DamageMetadata ar = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new FireMagicEvent(data.getPlayer(), ar));
		damage = ar.getDamage();
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new FireBolt_Bolt(
						cast,
						data.getPlayer(),
						_data.getTarget(),
						(int)cast.getModifier("bolt_count"),
						damage));
	}
}

class FireBolt_Bolt implements Runnable
{
	SkillMetadata cast;
	Player player;
	LivingEntity target;
	double damage;

	long lastTicks = 20;
	Location location;
	List<Vector> boltTargetLocations = new ArrayList<>();
	List<Vector> boltCurrentLocations = new ArrayList<>();
	
	public FireBolt_Bolt(SkillMetadata cast, Player _player, LivingEntity _target, int _boltCount, double _damage)
	{
		this.cast = cast;
		player = _player;
		target = _target;
		damage = _damage;

		location = player.getEyeLocation();
		new Effect(location, Particle.FLASH)
			.addSound(Sound.ENTITY_BLAZE_SHOOT)
			.playEffect();
		for (int i = 0; i < _boltCount; i++) {
			boltTargetLocations.add(new Vector(
					-1.0 + Math.random() * 2.0,
					-0.3 + Math.random() * 0.6,
					-1.0 + Math.random() * 2.0).multiply(4.0));
			boltCurrentLocations.add(new Vector());
		}
	}
	
	public void run()
	{
		if (lastTicks > 0) {
			lastTicks -= 1;
			for (int i = 0; i < boltCurrentLocations.size(); i++) {
				Vector target = boltTargetLocations.get(i).clone();
				Vector curr = boltCurrentLocations.get(i);
				curr.add(target.subtract(curr).multiply(0.1));
				Location loc = location.clone().add(curr.toLocation(location.getWorld()));
				location.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
			}
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		} else {
			for (int i = 0; i < boltCurrentLocations.size(); i++) {
				Location loc = location.clone().add(boltCurrentLocations.get(i));
				Vector dir = target.getEyeLocation().subtract(loc).toVector();
				double length = dir.length();
				new Effect(loc, Particle.SMALL_FLAME)
					.append2DLine(dir.normalize(), length)
					.scaleVelocity(0.0)
					.playEffect();
			}
			new Effect(target.getEyeLocation(), Particle.CRIT)
				.addSound(Sound.ENTITY_GENERIC_EXPLODE, 1.5, 1.5)
				.append2DCircle(2.0)
				.rotate(Math.random() * 360.0, Math.random() * 360.0, Math.random() * 360.0)
				.scaleVelocity(0.5)
				.playEffect();
			Damage.SkillAttack(cast, target, damage,
					DamageType.MAGIC, DamageType.PROJECTILE, DamageType.SKILL);
			target.setFireTicks(100);
		}
	}
}