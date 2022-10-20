package mala.mmoskill.skills.unused;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mala.mmoskill.util.Buff_Manager;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.skill.metadata.TargetSkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Confuse extends Skill
{
	public Confuse()
	{
		super();
		
		setName("혼란");
		setLore("&74 거리 내 상대에게 구속과 멀미를 부여합니다.", "상대가 플레이어인 경우 시야를 돌립니다.", MsgTBL.Cooldown, MsgTBL.StaCost);
		setMaterial(Material.STRING);
		
		addModifier("cooldown", new LinearValue(60, -2.5));
		addModifier("stamina", new LinearValue(20, 0));
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		TargetSkillMetadata cast = new TargetSkillMetadata(data, skill, 4);
		
		
		if (!cast.isSuccessful())
			return cast;
		
		LivingEntity target = cast.getTarget();

		Buff_Manager.Add_Buff(target, PotionEffectType.CONFUSION, 0, 200, null);
		Buff_Manager.Add_Buff(target, PotionEffectType.SLOW, 0, 200, PotionEffectType.SPEED);
		
		if(target instanceof Player)
		{
			Location loc = target.getLocation();
			loc.setDirection(new Vector(-1.0 + Math.random() * 2.0, -1.0 + Math.random() * 2.0, -1.0 + Math.random() * 2.0));
			target.teleport(loc);
		}
		
		target.getWorld().playSound(target.getEyeLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 2.0f);
		target.getWorld().spawnParticle(Particle.BUBBLE_POP, target.getEyeLocation(), 20, 0.2, 0.2, 0.2, 0);
		
		return cast;
	}
}
