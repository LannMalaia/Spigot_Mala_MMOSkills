package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Vehicle_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Spiral_Shoot extends RegisteredSkill
{
	public static String metaname = "mala.mmoskill.spiral_shoot.aerial";
	public Spiral_Shoot()
	{	
		super(new Spiral_Shoot_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("sec", new LinearValue(0.32, 0.02));
		addModifier("dash_damage", new LinearValue(21, 6));
		addModifier("distance", new LinearValue(8.5, 0.5));
		addModifier("fall_damage", new LinearValue(47, 7));
		addModifier("cooldown", new LinearValue(0.5, 0));
		addModifier("stamina", new LinearValue(32.5, 2.5));
	}
}

class Spiral_Shoot_Handler extends MalaSkill implements Listener
{
	public Spiral_Shoot_Handler()
	{
		super(	"SPIRAL_SHOOT",
				"유성격",
				Material.RABBIT_FOOT,
				MsgTBL.NeedSkills,
				"&e 스피어 차지 - lv.15",
				"",
				MsgTBL.WEAPON + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&e[ 지상 ]",
				"&e{sec}&7초간 바라보는 곳을 향해 빠르게 돌진합니다.",
				"&7돌진 궤적에 있던 적들은 &e{dash_damage}&7의 피해를 받습니다.",
				"&7웅크리거나 스킬을 재사용하여 돌진을 취소할 수 있습니다.",
				"&7공중으로도 이동할 수 있습니다.",
				"",
				"&b[ 공중 ]",
				"&7수직으로 밑을 향해 떨어집니다.",
				"&7떨어진 높이에 따라 범위와 피해량이 달라지며, 20m 높이에서 최대가 됩니다.",
				"&7최대 &e{distance}&7m의 적들에게 &e{fall_damage}&7의 피해를 줍니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "돌진 피해량 30% 증가",
				MsgTBL.WEAPON_AXE + "착지 피해량 30% 증가",
				MsgTBL.WEAPON_SPEAR + "이동량 및 범위 50% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		if (player.hasMetadata(Spiral_Shoot.metaname))
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
			player.sendMessage("§c§l[ 유성격 취소 ]");
			player.removeMetadata(Spiral_Shoot.metaname, MalaMMO_Skill.plugin);
			return new SimpleSkillResult(false);
		}
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "SPEAR_CHARGE", 15))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double sec = cast.getModifier("sec");
		double dash_damage = cast.getModifier("dash_damage");
		double distance = cast.getModifier("distance");
		double fall_damage = cast.getModifier("fall_damage");
		if (data.getPlayer().isOnGround())
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Spiral_Aerial(data.getPlayer(), sec, dash_damage));
		else
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Spiral_Fall(data.getPlayer(), distance, dash_damage, fall_damage));
		
	}
	
	class Spiral_Aerial implements Runnable
	{
		
		Player player;
		double sec;
		double damage;
		
		int count = 0;
		double velocity = 1.4;
		double angle = 0.0;
		double y_angle = 0.0;
		
		Vector[] vecs;
		
		public Spiral_Aerial(Player _player, double _sec, double _damage)
		{
			player = _player;
			sec = _sec;
			damage = _damage;
			
			angle = player.getLocation().getYaw();
			y_angle = player.getLocation().getPitch();
			Make_Vecs();
			
			player.setMetadata(Spiral_Shoot.metaname, new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
		}
		
		void Make_Vecs()
		{
			vecs = new Vector[8];
			vecs[0] = new Vector(-1.5, 1.5, -1.5);
			vecs[1] = new Vector(-0.25, 0.25, 3.5);
			vecs[2] = new Vector(1.5, 1.5, -1.5);
			vecs[3] = new Vector(0.25, 0.25, 3.5);
			vecs[4] = new Vector(-1.5, -1.5, -1.5);
			vecs[5] = new Vector(-0.25, -0.25, 3.5);
			vecs[6] = new Vector(1.5, -1.5, -1.5);
			vecs[7] = new Vector(0.25, -0.25, 3.5);
		}
		
		void Slerp()
		{
			double new_angle = player.getLocation().getYaw();
			double new_y_angle = player.getLocation().getPitch();
			
			double gap = new_angle - angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			gap = Math.min(2.0, Math.max(-2.0, gap));
			angle += gap;
			
			gap = new_y_angle - y_angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			gap = Math.min(2.0, Math.max(-2.0, gap));
			y_angle = Math.min(90.0, Math.max(-90.0, y_angle + gap));
		}
		
		public void run()
		{
			sec -= 0.05;
			count += 1;
			Slerp();
			if (player.isSneaking())
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
				player.sendMessage("§c§l[ 유성격 취소 ]");
				player.removeMetadata(Spiral_Shoot.metaname, MalaMMO_Skill.plugin);
				return;
			}
			if (sec <= 0.0 || !player.hasMetadata(Spiral_Shoot.metaname))
			{
				player.removeMetadata(Spiral_Shoot.metaname, MalaMMO_Skill.plugin);
				return;
			}
			
			velocity = Math.min(1.4, velocity + 0.05);
			
			double y_amount = 1.0 - Math.abs(y_angle / 90.0);
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)) * y_amount, y_angle / -90.0, Math.sin(Math.toRadians(angle + 90)) * y_amount);
			Vector vc = dir.multiply(velocity);
			// vc.setY(player.getVelocity().getY());
			
			Vehicle_Util.Get_Last_Vehicle(player).setVelocity(vc);
			// player.setVelocity(vc);
			
			Vector[] new_vecs = TRS.Rotate_Z(vecs, count * 9.0d);
			new_vecs = TRS.Rotate_X(new_vecs, y_angle);
			new_vecs = TRS.Rotate_Y(new_vecs, angle);
			for (int i = 0; i < 8; i += 2)
			{
				Location start = player.getEyeLocation().add(new_vecs[i]);
				Location end = player.getEyeLocation().add(new_vecs[i + 1]);
				Particle_Drawer.Draw_Line(start, end, Particle.SCRAPE, 0.2);
			}
			player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, player.getEyeLocation(), 5, 0.0, 0.4, 0.0, 0.0);
			
			for(Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 2.5, 2.5, 2.5))
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;
				
				if (Damage.Is_Possible(player, en))
				{
					Damage.Attack(player, (LivingEntity)en, damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
					Location loc = en.getLocation().add(0.0, en.getHeight() * 0.5, 0.0);
					player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.4, 0.4, 0.4, 0.0);
					player.getWorld().playSound(en.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
				}
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

	class Spiral_Fall implements Runnable
	{
		public DustTransition dtr = new DustTransition(Color.fromRGB(128, 192, 255), Color.WHITE, 3);

		Player player;
		double radius;
		double damage;
		double fall_damage;
		
		double start_y;
		int count = 0;
		double velocity = 0.0;
		
		double max_sec = 10.0;
		Vector[] vecs;
		
		public Spiral_Fall(Player _player, double _radius, double _damage, double _fall_damage)
		{
			player = _player;
			radius = _radius;
			damage = _damage;
			fall_damage = _fall_damage;
			
			start_y = player.getLocation().getY();
			Make_Vecs();
			
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
		}
		
		void Make_Vecs()
		{
			vecs = new Vector[8];
			vecs[0] = new Vector(-1.5, 1.5, -1.5);
			vecs[1] = new Vector(-0.5, 0.5, 3.5);
			vecs[2] = new Vector(1.5, 1.5, -1.5);
			vecs[3] = new Vector(0.5, 0.5, 3.5);
			vecs[4] = new Vector(-1.5, -1.5, -1.5);
			vecs[5] = new Vector(-0.5, -0.5, 3.5);
			vecs[6] = new Vector(1.5, -1.5, -1.5);
			vecs[7] = new Vector(0.5, -0.5, 3.5);
		}
		
		
		public void run()
		{
			max_sec -= 0.05;
			count += 1;
			
			if (count % 2 == 0)
			{
				// player.getWorld().playSound(player.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 0.5f, 0.5f);			
			}
			
			velocity = Math.min(3.0, velocity + 0.15);
			
			Vector dir = new Vector(0, -1, 0);
			Vector vc = dir.multiply(velocity);

			Vehicle_Util.Get_Last_Vehicle(player).setVelocity(vc);
			
			Vector[] new_vecs = TRS.Rotate_Z(vecs, count * 12d);
			new_vecs = TRS.Rotate_X(new_vecs, 90);
			for (int i = 0; i < 8; i += 2)
			{
				Location start = player.getLocation().add(new_vecs[i]);
				Location end = player.getLocation().add(new_vecs[i + 1]);
				Particle_Drawer.Draw_Line(start, end, Particle.CRIT_MAGIC, 0.15);
				start.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, start, 1, 0d, 0d, 0d, 0d, dtr);
				start.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, end, 1, 0d, 0d, 0d, 0d, dtr);
			}

			for(Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 2.0, 2.0, 2.0))
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;
				
				if (Damage.Is_Possible(player, en))
				{
					Damage.Attack(player, (LivingEntity)en, damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
					Location loc = en.getLocation().add(0.0, en.getHeight() * 0.5, 0.0);
					player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.4, 0.4, 0.4, 0.0);
					player.getWorld().playSound(en.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
				}
			}
			if (player.isOnGround())
			{
				Location temp_loc = player.getLocation().add(0, 0.5, 0);
				double y_gap = start_y - player.getLocation().getY();
				y_gap = Math.min(1.0, Math.max(0.1, y_gap / 20.0));
				double rad = radius * y_gap;
				double dam = fall_damage * y_gap;
				player.getWorld().spawnParticle(Particle.FLASH, player.getLocation(), 1, 0, 0, 0, 0.0);
				player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, player.getLocation(), (int)(rad * 16), rad, rad, rad, 0.0);
				player.getWorld().spawnParticle(Particle.SCRAPE, player.getLocation(), (int)(rad * 50), rad, rad, rad, 0.0);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
				
				for (int i = 0; i < 4; i++)
				{
					double temp_rad = (i / 4.0) + rad * (i / 4.0);
					Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new Runnable()
					{
						@Override
						public void run()
						{
							Particle_Drawer.Draw_Circle(temp_loc, Particle.WAX_OFF, Math.max(0.0, temp_rad - 0.05));
							Particle_Drawer.Draw_Circle(temp_loc, Particle.WAX_OFF, temp_rad);
							Particle_Drawer.Draw_Circle(temp_loc, Particle.WAX_OFF, temp_rad + 0.05);
						}
					}, (long)(1 * i));
				}
				
				for(Entity en : player.getWorld().getNearbyEntities(player.getLocation(), rad, rad, rad))
				{
					if (!(en instanceof LivingEntity))
						continue;
					if (en == player)
						continue;
					
					if (Damage.Is_Possible(player, en))
					{
						Damage.Attack(player, (LivingEntity)en, dam, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
						Location loc = en.getLocation().add(0.0, en.getHeight() * 0.5, 0.0);
						player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 3, 0.4, 0.4, 0.4, 0.0);
						player.getWorld().playSound(en.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
					}
				}
				return;
			}
			
			if (max_sec > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}















