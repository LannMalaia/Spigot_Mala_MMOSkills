package mala.mmoskill.skills.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.CasterMetadata;
import net.Indyuce.mmocore.skill.Skill;
import net.Indyuce.mmocore.skill.metadata.SkillMetadata;
import net.Indyuce.mmocore.skill.metadata.TargetSkillMetadata;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Mix extends Skill
{
	public Mix()
	{
		super();
		
		setName("뒤섞기");
		setLore("&74 거리 내 상대의 타게팅을 자신에게로 옮깁니다.", "상대가 플레이어인 경우 퀵슬롯을 뒤섞습니다.", MsgTBL.Cooldown, MsgTBL.StaCost);
		setMaterial(Material.PRISMARINE_SHARD);
		
		addModifier("cooldown", new LinearValue(60, -2.5));
		addModifier("stamina", new LinearValue(20, 0));
	}
	
	@Override
	public SkillMetadata whenCast(CasterMetadata data, SkillInfo skill)
	{
		TargetSkillMetadata cast = new TargetSkillMetadata(data, skill, 4);

		if (!cast.isSuccessful())
			return cast;
		
		if(!((cast.getTarget() instanceof Mob) || (cast.getTarget() instanceof Player)))
			return cast;

		LivingEntity target = cast.getTarget();
		if(cast.getTarget() instanceof Mob)
		{
			Mob mob = (Mob)cast.getTarget();
			mob.setTarget(null);
		}
		if(cast.getTarget() instanceof Player)
		{
			Player player = (Player)cast.getTarget();
			List<ItemStack> items = new ArrayList<ItemStack>();
			for(int i = 0; i < 9; i++)
				items.add(player.getInventory().getItem(i));
			Collections.shuffle(items);
			for(int i = 0; i < 9; i++)
				player.getInventory().setItem(i, items.get(i));
		}
		target.getWorld().playSound(target.getEyeLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, 1, 2.0f);
		target.getWorld().spawnParticle(Particle.BUBBLE_POP, target.getEyeLocation(), 20, 0.2, 0.2, 0.2, 0);
		
		return cast;
	}
}