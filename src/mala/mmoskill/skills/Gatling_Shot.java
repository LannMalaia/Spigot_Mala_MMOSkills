package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.RayUtil;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Gatling_Shot extends RegisteredSkill
{
	public Gatling_Shot()
	{	
		super(new Gatling_Shot_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("count", new LinearValue(8, 3));
		addModifier("damage", new LinearValue(7.7, 0.7));
		addModifier("cooldown", new LinearValue(30, 0));
		addModifier("stamina", new LinearValue(21, 1));
	}
}

class Gatling_Shot_Handler extends MalaSkill implements Listener
{
	public Gatling_Shot_Handler()
	{
		super(	"GATLING_SHOT",
				"개틀링 샷",
				Material.CROSSBOW,
				MsgTBL.NeedSkills,
				"&e 속사 - lv.15",
				"",
				MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7전방을 향해 아주 빠르게 화살을 발사합니다.",
				"&7화살은 매우 빠르기 때문에, 25m 내의 모든 적을 관통합니다.",
				"&7총 {count}개 발사하며, 화살 한 발 당 {damage}의 피해를 줍니다.",
				"&c석궁을 들고 있어야 합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if (!Weapon_Identify.Hold_Crossbow(data.getPlayer()))
		{
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			return new SimpleSkillResult(false);
		}
		if(!Skill_Util.Has_Skill(data, "RAPID_FIRE", 15))
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

		int count = (int)cast.getModifier("count");
		double damage = cast.getModifier("damage");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Gatling_Shot_Task(cast, data.getPlayer(), count, damage));
	}
}

class Gatling_Shot_Task implements Runnable
{
	Player player;
	Location loc;
	int count;
	double damage;
	SkillMetadata cast;
	
	public Gatling_Shot_Task(SkillMetadata _cast, Player _player, int _count, double _damage)
	{
		cast = _cast;
		player = _player;
		count = _count;
		damage = _damage;
	}
	
	public void run()
	{
		if (count-- < 0)
			return;
		
		Vector dir = player.getLocation().getDirection().add(new Vector(
				-0.07 + Math.random() * 0.14,
				-0.07 + Math.random() * 0.14,
				-0.07 + Math.random() * 0.14
				));
		new Effect(player.getEyeLocation().add(0, -0.3, 0), Particle.CRIT)
			.addSound(Sound.ITEM_CROSSBOW_SHOOT)
			.append2DLine(dir, 25.0, 2.0)
			.setVelocity(dir.getX(), dir.getY(), dir.getZ())
			.playEffect();
		for (LivingEntity target : RayUtil.getLivingEntities(player, dir, 25.0)) {
			target.setNoDamageTicks(0);
			cast.attack(target, damage, DamageType.WEAPON, DamageType.PHYSICAL, DamageType.PROJECTILE, DamageType.SKILL);
		}
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
