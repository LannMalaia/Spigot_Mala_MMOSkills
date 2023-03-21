package mala.mmoskill.skills;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Chase_Cut extends RegisteredSkill
{
	public static Chase_Cut skill;
	
	public Chase_Cut()
	{	
		super(new Chase_Cut_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("radius", new LinearValue(3.2, 0.2));
		addModifier("damage", new LinearValue(22.0, 2.0));
		addModifier("cooldown", new LinearValue(7.8, -0.2, 4, 10));
		addModifier("stamina", new LinearValue(4.2, .2));
		skill = this;
	}
}

class Chase_Cut_Handler extends MalaSkill implements Listener
{
	public HashMap<Player, LivingEntity> final_attacked;
	
	public Chase_Cut_Handler()
	{
		super(	"CHASE_CUT",
				"추격베기",
				Material.LEAD,
				MsgTBL.NeedSkills,
				"&e 표식 - lv.15",
				"&e 후방 습격 - lv.10",
				"",
				MsgTBL.WEAPON + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&7마지막으로 공격한 적에게 빠르게 접근합니다.",
				"&7이후 주변 &e{radius}&7m에 &e{damage}&7의 피해를 줍니다.",
				"&7대상이 죽거나 3초동안 접근하지 못할 경우 서있는 위치에서 공격합니다.",
				"&eLv.10 - 피해를 입은 적에게 추가로 표식을 부여합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		final_attacked = new HashMap<Player, LivingEntity>();
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	
	@EventHandler
	public void chase_cut_attack(PlayerAttackEvent event)
	{
		PlayerData data = PlayerData.get(event.getAttacker().getPlayer());
		// 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill("CHASE_CUT"))
			return;
		final_attacked.put(data.getPlayer(), event.getEntity());
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if (!Skill_Util.Has_Skill(data, "TARGET_MARK", 15)
			|| !Skill_Util.Has_Skill(data, "BACK_ATTACK", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		if (!final_attacked.containsKey(player))
		{
			data.getPlayer().sendMessage("§c마지막으로 공격한 적이 없거나 죽었습니다.");
			return new SimpleSkillResult(false);
		}
		LivingEntity le = final_attacked.get(player);
		if (!le.isValid() || le.getWorld() != player.getWorld())
		{
			data.getPlayer().sendMessage("§c마지막으로 공격한 적이 없거나 죽었습니다.");
			return new SimpleSkillResult(false);
		}
		
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double rad = cast.getModifier("radius");
		double damage = cast.getModifier("damage");
		int level = data.getSkillLevel(Chase_Cut.skill);
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Chase_Cut_Skill(cast, data.getPlayer(), final_attacked.get(data.getPlayer()), rad, damage, level >= 10));
	}

	class Chase_Cut_Skill implements Runnable
	{
		SkillMetadata cast;
		Player player;
		LivingEntity target;
		double radius, damage;
		boolean marking;
		
		double sec = 3.0;
		int count = 0;
		double velocity = 1.3;
		
		
		public Chase_Cut_Skill(SkillMetadata cast, Player _player, LivingEntity _target, double _radius, double _damage, boolean _marking)
		{
			this.cast = cast;
			player = _player;
			target = _target;
			radius = _radius;
			damage = _damage;
			marking = _marking;

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 2.0f);
		}
		
		void Attack()
		{
			player.setVelocity(new Vector());
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.5f, 1.5f);
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.5f, 2.0f);
			for (int i = 0; i < 3; i++)
				Particle_Drawer.Draw_Circle(player.getEyeLocation(), Particle.CRIT_MAGIC,
					radius, -40 + Math.random() * 80.0, Math.random() * 360.0);
			for(Entity en : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius))
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;

				LivingEntity le = (LivingEntity)en;
				if (Damage.Is_Possible(player, le) && le.getNoDamageTicks() == 0)
				{
					Damage.SkillAttack(cast, le, damage, DamageType.WEAPON, DamageType.PHYSICAL, DamageType.SKILL);
					if (marking)
						Target_Mark.Mark_Enemy(player, le);
				}
			}
		}
		
		public void run()
		{
			sec -= 0.05;
			count += 1;
			if (player.getWorld() != target.getWorld() || sec <= 0.0)
			{
				Attack();
				return;
			}
			
			Vector dir = target.getLocation().subtract(player.getLocation()).toVector().normalize();
			Vector vc = dir.multiply(velocity);
			player.setVelocity(vc);
			player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 0.2, 0), 10, 0.4, 0.2, 0.4, 0.0);
			
			if (target.getLocation().distance(player.getLocation()) < 1.0)
			{
				Attack();
				return;
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

}