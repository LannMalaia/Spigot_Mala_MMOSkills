package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Vehicle_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Armored_Charge extends RegisteredSkill
{
	public static String metaname = "mala.mmoskill.armored_charge";
	
	public Armored_Charge()
	{	
		super(new Armored_Charge_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(66, 6));
		addModifier("slow", new LinearValue(4.1, 0.1, 1.0, 6.0));
		addModifier("cooldown", new LinearValue(14, -1, 2, 40));
		addModifier("stamina", new LinearValue(25, 5, 20, 100));
	}
}

class Armored_Charge_Handler extends MalaSkill implements Listener
{
	public Armored_Charge_Handler()
	{
		super(	"ARMORED_CHARGE",
				"무장 돌격",
				Material.GOLDEN_SWORD,
				MsgTBL.NeedSkills,
				"&e 완전무장 - lv.10",
				"&e 기마술 - lv.10",
				"",
				MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&73초간 전방으로 빠르게 돌진합니다.",
				"&7돌진 중 부딪힌 적들에게는,",
				"- &8{power}&7의 피해를 줍니다.",
				"- 6초동안 구속 &8{slow}&7 버프를 부여합니다.",
				"&7웅크리거나 스킬을 재사용하여 돌진을 취소할 수 있습니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_HORSE + "피해량 50% 증가",
				MsgTBL.WEAPON_SHIELD + "동시에 약화를 부여",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
		registerModifiers("power", "slow");
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		if (player.hasMetadata(Armored_Charge.metaname))
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
			player.sendMessage("§c§l[ 무장 돌격 취소 ]");
			player.removeMetadata(Armored_Charge.metaname, MalaMMO_Skill.plugin);
			return new SimpleSkillResult(false);
		}
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "FULL_ARMORED", 10)
			|| !Skill_Util.Has_Skill(data, "HORSE_RIDING", 10))
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
		
		double damage = cast.getModifier("power"); // 공격력
		double sec = 3.0;
		int amp = (int)cast.getModifier("slow");
		int duration = 6 * 20;
		boolean addWeakness = false;
		
		if (Vehicle_Util.Is_Riding_Horse(data.getPlayer()))
			damage *= 1.5;
		if (Weapon_Identify.Hold_Shield(data.getPlayer()))
			addWeakness = true;
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Armored_Charge_Skill(cast, data.getPlayer(), sec, damage, amp, duration, addWeakness));
	}

	class Armored_Charge_Skill implements Runnable
	{
		SkillMetadata cast;
		Player player;
		double sec;
		double damage;
		int amp, tick;
		boolean addWeakness;
		
		int count = 0;
		double velocity = 0.4;
		double angle = 0.0;
		
		ArrayList<LivingEntity> cant_damage = new ArrayList<LivingEntity>();
		
		public Armored_Charge_Skill(SkillMetadata cast, Player _player, double _sec, double _damage, int _amp, int _tick, boolean _addWeakness)
		{
			this.cast = cast;
			player = _player;
			sec = _sec;
			damage = _damage;
			amp = _amp;
			tick = _tick;
			addWeakness = _addWeakness;
			
			angle = player.getLocation().getYaw();

			player.setMetadata(Armored_Charge.metaname, new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
		}
		
		public Location Calculate_Y_Pos(Location _loc)
		{
			Location loc = _loc.clone().add(0.0, 0.5, 0.0);
			Block b = loc.clone().add(loc.getDirection().multiply(0.1)).getBlock();
			if (b.getType().isSolid()) // 바로 앞에 블럭 있음
			{
				b = b.getLocation().add(0.0, 1.0, 0.0).getBlock();
				if (!b.getType().isSolid())
					loc.add(0.0, 0.5, 0.0);
			}
			else // 비어있는 경우
			{
				b = b.getLocation().add(0.0, -1.0, 0.0).getBlock();
				if (b.getType().isSolid()) // 바로 앞 바닥에 블럭 있음
				{
					return _loc;
				}
				else // 바로 앞에 블럭 없음
				{
					b = b.getLocation().add(0.0, -1.0, 0.0).getBlock();
					if (!b.getType().isSolid())
						return null;
					loc.add(0.0, -1.5, 0.0);
				}
			}
			
			return loc;
		}
		
		void Slerp()
		{
			double new_angle = player.getLocation().getYaw();
			
			double gap = new_angle - angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			
			gap = Math.min(2.0, Math.max(-2.0, gap));
			angle += gap;
			
			// Bukkit.broadcastMessage("gap = " + gap);
		}
		
		public void run()
		{
			sec -= 0.05;
			count += 1;
			Slerp();
			if (player.isSneaking())
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
				player.sendMessage("§c§l[ 무장 돌격 취소 ]");
				player.removeMetadata(Armored_Charge.metaname, MalaMMO_Skill.plugin);
				return;
			}
			if (sec <= 0.0 || !player.hasMetadata(Armored_Charge.metaname))
			{
				player.removeMetadata(Armored_Charge.metaname, MalaMMO_Skill.plugin);
				return;
			}
			
			if (count % 2 == 0)
			{
				// player.getWorld().playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.5f, 0.5f);			
			}
			
			velocity = Math.min(1.4, velocity + 0.05);
			
			
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)), 0.0, Math.sin(Math.toRadians(angle + 90)));
			Vector vc = dir.multiply(velocity);
			vc.setY(player.getVelocity().getY());
			// player.teleport(new_loc);
			Vehicle_Util.Get_Last_Vehicle(player).setVelocity(vc);
			//player.setVelocity(vc);

			player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
			
			for (Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 4.0, 4.0, 4.0))
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;
				if (!Hitbox.Is_BoundingBox_Collide_Other_BoundingBox(en.getBoundingBox(), player.getBoundingBox()))
					continue;
				if (cant_damage.contains(en))
					continue;
				
				
				LivingEntity le = (LivingEntity)en;
				if (Damage.Is_Possible(player, le))
				{
					Damage.SkillAttack(cast, le, damage,
							DamageType.SKILL, DamageType.PHYSICAL);
					
					Location loc = en.getLocation().add(0.0, en.getHeight() * 0.5, 0.0);
					player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 10, 0.4, 0.4, 0.4, 0.0);
					player.getWorld().playSound(en.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
					
					Buff_Manager.Add_Buff(le, PotionEffectType.SLOW, amp, tick, PotionEffectType.SPEED);
					if (addWeakness) {
						Buff_Manager.Add_Buff(le, PotionEffectType.WEAKNESS, amp, tick, PotionEffectType.INCREASE_DAMAGE);
					}
				}
				else
					cant_damage.add(le);
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

}