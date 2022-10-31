package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Mastery_Heal;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Heal extends RegisteredSkill
{
	public static Heal skill;
	
	public Heal()
	{	
		super(new Heal_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("heal", new LinearValue(12, 3));
		addModifier("cooldown", new LinearValue(13, 0));
		addModifier("mana", new LinearValue(10, 2));
		
		skill = this;
	}
}

class Heal_Handler extends MalaSkill implements Listener
{
	public Heal_Handler()
	{
		super(	"HEAL",
				"��",
				Material.REDSTONE,
				"&720m �� �Ʊ� �� ���� &a{heal}&7��ŭ ȸ����ŵ�ϴ�.",
				"&7��ũ���� ������ �ڽ��� ȸ������ ���ݸ�ŭ ȸ����ŵ�ϴ�.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		LivingEntity target = null;
		double heal = cast.getModifier("heal");
		heal *= Mastery_Heal.Get_Mult(data.getPlayer());
		
		Vector dir = data.getPlayer().getEyeLocation().getDirection();
		RayTraceResult rtr = data.getPlayer().getWorld().rayTrace(
				data.getPlayer().getEyeLocation().add(dir.multiply(1.5)),
				dir,
				20.0, FluidCollisionMode.NEVER, true, 0.1, null);

		if(data.getPlayer().isSneaking())
		{
			target = data.getPlayer();
			heal *= 0.5;
		}
		else
		{
			if (rtr != null)
			{
				if (rtr.getHitEntity() != null
						&& rtr.getHitEntity().getType() == EntityType.PLAYER)
				{
					target = (Player)rtr.getHitEntity();
				}
			}
		}
		if (target == null)
		{
			for (ClassSkill cs : data.getProfess().getSkills())
			{
				if (cs.getSkill() != Heal.skill)
					continue;
				CooldownInfo ci = data.getCooldownMap().getInfo(cs);
				if (ci == null)
					continue;
				ci.reduceFlat(999.0);
			}
			return;
		}
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Heal_Task(data.getPlayer(), (Player)target, heal));
		Aggro.Taunt_Area(data.getPlayer(), 15.0, 3);
	}
}

class Heal_Task implements Runnable
{
	LivingEntity target;
	double y = 0;
	int counter = 2;
	
	public Heal_Task(Player _player, Player _target, double _damage)
	{
		target = _target;
		
		if(target != _player)
			target.sendMessage("��b��l[" + _player.getDisplayName() + "��b��l���� �������� " + _damage + " HP�� ȸ���Ǿ����ϴ�. ]");
		double max = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		target.setHealth(Math.min(max, target.getHealth() + _damage));
	}
	
	public void run()
	{
		Location origin_loc = target.getLocation().clone();
		Location loc = target.getLocation().clone();
		for(double angle = 0; angle < 360.0; angle += 12)
		{
			loc.setX(origin_loc.getX() + Math.cos(Math.toRadians(angle)) * 1.5);
			loc.setY(origin_loc.getY() + y);
			loc.setZ(origin_loc.getZ() + Math.sin(Math.toRadians(angle)) * 1.5);
			loc.getWorld().spawnParticle(Particle.HEART, loc, 1, 0, 0, 0, 0);
		}
		loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
		
		y += 0.4;
		
		if(counter-- > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
	}
}