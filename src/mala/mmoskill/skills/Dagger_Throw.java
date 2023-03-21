package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.skills.passive.Make_Doppel;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Dagger_Throw extends RegisteredSkill
{
	public Dagger_Throw()
	{	
		super(new Dagger_Throw_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(30, 3));
		addModifier("damage", new LinearValue(12, 4));
		addModifier("additive", new LinearValue(10, 1.0));
		addModifier("cooldown", new LinearValue(1, 0));
		addModifier("stamina", new LinearValue(3.6, 0.6));
	}
}

class Dagger_Throw_Handler extends MalaSkill implements Listener
{
	public Dagger_Throw_Handler()
	{
		super(	"DAGGER_THROW",
				"표창 투척",
				Material.NETHER_STAR,
				MsgTBL.PHYSICAL + MsgTBL.PROJECTILE,
				"",
				"&8{distance}&7m 거리까지 나아가는 표창을 던집니다.",
				"&7표창은 &8{damage}&7의 피해를 줍니다.",
				"&7벽에 튕길 수 있으며, 튕길 때마다 피해량이 &8{additive}&7만큼 증가합니다.",
				"&7이 공격은 스킬로 취급되지 않습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double distance = cast.getModifier("distance");
		double damage = cast.getModifier("damage");
		double additive = cast.getModifier("additive");

		if (!Make_Doppel.Try_Doppel_Make(data, this))
		{
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
					new Dagger_Throw_Skill(data.getPlayer().getEyeLocation(), data.getPlayer(),
							data.getPlayer().getLocation().getDirection(), damage, additive, distance));
		}
		
		for (Summoned_OBJ so : Summon_Manager.Get_Instance().Get_Summoned_OBJs(data.getPlayer(), "Doppelganger"))
		{
			LivingEntity as = (LivingEntity)so.entity;
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
					new Dagger_Throw_Skill(as.getEyeLocation(), data.getPlayer(),
							as.getLocation().getDirection(), damage, additive, distance));			
		}
	}
}

class Dagger_Throw_Skill implements Runnable
{
	World world;
	Player player;
	double damage;
	double damage_additive;
	double max_distance;

	double speed = 6;
	boolean is_first = false;
	
	Location start_loc;
	Vector dir;

	double current_distance = 0;
	Location before_loc, current_loc;
	
	boolean is_blind = false;
	boolean blind_effect = false;
	int blind_tick = 0;
	DustOptions dop = new DustOptions(Color.NAVY, 1.0f);

	public Dagger_Throw_Skill(Location _start_loc, Player _player, Vector _dir, double _damage, double _additive, double _max_distance)
	{
		start_loc = _start_loc;
		player = _player;
		dir = _dir;
		world = player.getWorld();

		damage = _damage;
		damage_additive = _additive;
		max_distance = _max_distance;
		
		current_loc = start_loc.clone().add(dir.clone().multiply(0.5));
		before_loc = current_loc.clone();
		
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(player);
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("DAGGER_POISON");
		int level = data.getSkillLevel(skill);
		if (data.getProfess().hasSkill(skill) && level >= 10)
		{
			is_blind = true;
			blind_tick = (int)(skill.getModifier("second", level) * 20);
			blind_effect = level >= 10;
		}
	}
	
	public void run()
	{
		if (!is_first)
		{
			world.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 2);
			is_first = true;
		}
		
		current_distance += speed;
		if(max_distance < current_distance)
			speed = max_distance - current_distance;
		
		double target_dist = speed;
		
		// 거리를 전부 소모할 때까지 반복
		while (target_dist > 0.0)
		{
			// player.sendMessage("start");
			
			RayTraceResult rtr = world.rayTrace(current_loc, dir, speed, FluidCollisionMode.NEVER, true, 0.1, null);
			if (rtr != null)
			{
				// 어딘가에 부딪힘

				// player.sendMessage("trace ok");
				
				
				Entity entity = rtr.getHitEntity();
				
				if (entity != null && entity instanceof LivingEntity && entity != player)
				{
					world.playSound(current_loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 2);
					world.playSound(current_loc, Sound.BLOCK_GLASS_BREAK, 2, 0.7f);
					AttackUtil.attack(player, (LivingEntity)entity, damage, null, DamageType.PHYSICAL, DamageType.PROJECTILE);
					Buff_Manager.Add_Buff((LivingEntity)entity, PotionEffectType.BLINDNESS, 0, blind_tick, null);
					return;
				}
				
				BlockFace block = rtr.getHitBlockFace();
				if (rtr.getHitBlock() != null) // 블록에 부딪힘
				{
					// player.sendMessage("block hit");
					
					damage += damage_additive;
					
					double dist = rtr.getHitPosition().distance(current_loc.toVector()); 
					current_loc = rtr.getHitPosition().toLocation(world);
					if (blind_effect)
						Particle_Drawer.Draw_Line(before_loc, current_loc, dop, 0.1);
					else
						Particle_Drawer.Draw_Line(before_loc, current_loc, Particle.CRIT, 0.1);
					before_loc = current_loc.clone();
					
					dir = TRS.Reflect(dir, block.getDirection());
					dir.normalize();
					current_loc.getWorld().playSound(current_loc, Sound.ENTITY_ITEM_BREAK, 1, 2);
					current_loc.add(dir.clone().multiply(0.75));
					
					target_dist -= dist + 0.3;
					
					if (blind_effect)
						Particle_Drawer.Draw_Line(before_loc, current_loc, dop, 0.1);
					else
						Particle_Drawer.Draw_Line(before_loc, current_loc, Particle.CRIT, 0.1);
				}
				else
				{
					// player.sendMessage("block not hit");
					current_loc.add(dir.clone().multiply(target_dist));
					target_dist -= target_dist;
				}
				// player.sendMessage("trace end");
				if (blind_effect)
					Particle_Drawer.Draw_Line(before_loc, current_loc, dop, 0.1);
				else
					Particle_Drawer.Draw_Line(before_loc, current_loc, Particle.CRIT, 0.1);
				before_loc = current_loc.clone();
			}
			else // 어딘가에 부딪히지 않았다
			{
				// player.sendMessage("trace no");
				
				current_loc.add(dir.clone().multiply(target_dist));
				target_dist -= target_dist;
				if (blind_effect)
					Particle_Drawer.Draw_Line(before_loc, current_loc, dop, 0.1);
				else
					Particle_Drawer.Draw_Line(before_loc, current_loc, Particle.CRIT, 0.1);
			}
			// player.sendMessage("end");
		}
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(current_distance > max_distance)
			return;
					
		// 마무리
		before_loc = current_loc.clone();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}

}





