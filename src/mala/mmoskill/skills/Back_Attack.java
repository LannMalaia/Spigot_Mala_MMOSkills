package mala.mmoskill.skills;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import mala.mmoskill.skills.passive.Dog_BackAttack;
import mala.mmoskill.skills.passive.Make_Doppel;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Back_Attack extends RegisteredSkill
{
	public static Back_Attack skill;
	
	public Back_Attack()
	{	
		super(new Back_Attack_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("cooldown", new LinearValue(10, 0));
		addModifier("distance", new LinearValue(6, 1, 5, 25));
		addModifier("stamina", new LinearValue(15, 0));
		skill = this;
	}
}

class Back_Attack_Handler extends MalaTargetSkill implements Listener
{
	public Back_Attack_Handler()
	{
		super(	"BACK_ATTACK",
				"�Ĺ� ����",
				Material.ENDER_PEARL,
				"&8{distance}&7m �Ÿ����� �ִ� ����� �ڷ� �����̵��մϴ�.",
				"&7���� ��󿡰� ������ �ϰ��� ���ߴٸ�,",
				"&7�ٽ� �� �� ������ �ϰ��� ���� �� �ֽ��ϴ�.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);

	}
	@Override
	public TargetSkillResult getResult(SkillMetadata cast)
	{
		return new TargetSkillResult(cast, cast.getModifier("distance"), InteractionType.OFFENSE_ACTION);
	}

	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double max_distance = cast.getModifier("distance"); // �����̵� ������ �Ÿ�		
		
		// Ÿ�� ���
		LivingEntity target = (LivingEntity)_data.getTarget();
		
		// �ʹ� �� ���
		if(data.getPlayer().getLocation().distance(target.getLocation()) > max_distance)
			return;

		// ȿ��	
		if (!Make_Doppel.Try_Doppel_Make(data, this))
		{
			target.getWorld().playSound(data.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
			target.getWorld().spawnParticle(Particle.SMOKE_NORMAL, data.getPlayer().getLocation().add(0, target.getHeight() / 2, 0), 24, 0, 0, 0, .7);
		}
		
		Vector dir = target.getLocation().getDirection().normalize();
		dir.setX(dir.getX() * -1.5d);
		dir.setZ(dir.getZ() * -1.5d);
		dir.setY(0.0d);
		data.getPlayer().teleport(
				target.getLocation().add(dir),
				TeleportCause.ENDER_PEARL
				);
		data.getPlayer().getLocation().setDirection(target.getLocation().getDirection());
		
		Dog_BackAttack.Play_Heal(data.getPlayer(), target);
		
		if (target.hasMetadata("malammo.skill.dominate_use"))
		{
			target.removeMetadata("malammo.skill.dominate_use", MalaMMO_Skill.plugin);
		}
	}
}
