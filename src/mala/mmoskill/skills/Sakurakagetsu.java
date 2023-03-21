package mala.mmoskill.skills;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import mala.mmoskill.events.PhysicalSkillEvent;
import mala.mmoskill.skills.Stance_Change.Stance_Type;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.BOX;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Vehicle_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Sakurakagetsu extends RegisteredSkill
{
	public static final String metaname = "mala.mmoskill.sakura_charge";
	public static DustTransition dtr = new DustTransition(Color.fromRGB(255, 128, 192), Color.WHITE, 1);
	public static DustTransition dtr_small = new DustTransition(Color.fromRGB(255, 128, 192), Color.WHITE, 0.5f);
	
	public Sakurakagetsu()
	{	
		super(new Sakurakagetsu_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(10.75, 0.75));
		addModifier("power", new LinearValue(67, 7));
		addModifier("sec", new LinearValue(1.0, 0.05));
		addModifier("cooldown", new LinearValue(29, -1, 15, 30));
		addModifier("stamina", new LinearValue(28, 3));
	}
	
	public static void Draw_Flower(Location _loc, long _latency)
	{
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new Runnable() {
			
			@Override
			public void run()
			{
				_loc.getWorld().playSound(_loc, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.5f, 1.2f);
				_loc.getWorld().playSound(_loc, Sound.BLOCK_GLASS_BREAK, 0.8f, 1.5f);
				Random rand = new Random();
				
				for(int count = 0; count < 10; count++)
				{
					Vector rand_vec = new Vector(-1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0).normalize();
					Vector from = _loc.toVector().add(rand_vec.clone().multiply(-3.0));
					Vector to = _loc.toVector().add(rand_vec.clone().multiply(3.0));
			
					Location loc = from.toLocation(_loc.getWorld());
					for(double i = 0.0; i < from.distance(to); i += 0.2)
					{
						loc.add(rand_vec.clone().multiply(0.2));
						loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0d, 0d, 0d, 0d, dtr);
					}
				}
			}
		}, _latency);
		
	}
	public static void Draw_Entity_Effect(LivingEntity _target)
	{
		Random rand = new Random();
		
		for(int count = 0; count < 1; count++)
		{
			Vector rand_vec = new Vector(-1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0).normalize();
			Vector from = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(-6.0));
			Vector to = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(6.0));
	
			Location loc = from.toLocation(_target.getWorld());
			for(double i = 0.0; i < from.distance(to); i += 0.15)
			{
				loc.add(rand_vec.clone().multiply(0.15));
				loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0d, 0d, 0d, 0d, dtr_small);
			}
		}
		
		Location loc = _target.getEyeLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
		loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 1.5f, 2f);
		loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0d, 0d, 0d, 0d);
	}
}

class Sakurakagetsu_Handler extends MalaSkill implements Listener
{
	public Sakurakagetsu_Handler()
	{
		super(	"SAKURAKAGETSU",
				"앵화월소",
				Material.BRAIN_CORAL,
				MsgTBL.NeedSkills,
				"&e 순간베기 - lv.15",
				"",
				MsgTBL.WEAPON + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&c[ 버서크 스탠스 ]",
				"&7전방 &8{distance}&7m의 적 전체에게 &8{power}&7의 시간차 피해를 줍니다.",
				"",
				"&f[ &b이베이드 스탠스 &f& &7스탠스 해제 &f]",
				"&8{sec}&7초간 전방으로 빠르게 움직입니다.",
				"&7이후 지나간 자리에 &8{power}&7의 시간차 피해를 줍니다.",
				"&7웅크리거나 스킬을 재사용하여 돌진을 취소할 수 있습니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "이베이드 피해량 30% 증가",
				MsgTBL.WEAPON_AXE + "버서크 피해량 30% 증가",
				MsgTBL.WEAPON_SPEAR + "측면 범위 30% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		if (player.hasMetadata(Sakurakagetsu.metaname))
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
			player.sendMessage("§c§l[ 앵화월소 취소 ]");
			player.removeMetadata(Sakurakagetsu.metaname, MalaMMO_Skill.plugin);
			return new SimpleSkillResult(false);
		}
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "SLASH", 15))
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
		Stance_Type type = !data.getPlayer().hasMetadata(Stance_Change.meta_name) ? Stance_Type.NORMAL : Stance_Type.valueOf(data.getPlayer().getMetadata(Stance_Change.meta_name).get(0).asString());
		
		double distance = cast.getModifier("distance");
		double damage = cast.getModifier("power");
		double sec = (int)cast.getModifier("sec");
		double width_mult = 1.0;
		
		// 무기 효과
		if (Weapon_Identify.Hold_Spear(data.getPlayer()) || Weapon_Identify.Hold_MMO_Spear(data.getPlayer()))
		{
			width_mult = 1.5;
		}
		
		if (type == Stance_Type.BERSERK)
		{
			if (Weapon_Identify.Hold_Axe(data.getPlayer()))
				damage *= 1.5;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new Berserk_Slash(data.getPlayer(), distance, damage, width_mult), 10);
		}
		else
		{
			if (Weapon_Identify.Hold_Sword(data.getPlayer()) || Weapon_Identify.Hold_MMO_Sword(data.getPlayer()))
				damage *= 1.3;
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Evade_Charge(data.getPlayer(), sec, damage, width_mult));
		}
	}
	
	class Berserk_Slash implements Runnable
	{
		Player player;
		double damage;
		double distance;
		double width_mult;
		List<Entity> entities;
		
		double angle = 240;
		Vector[] skill_flower_pos;
		
		public Berserk_Slash(Player p, double _distance, double _damage, double _width_mult)
		{
			player = p;
			distance = _distance;
			damage = _damage;
			width_mult = _width_mult;
			
			Draw_First_Effect();
			Check_Damage_Entity();
		}
		
		public void run()
		{
			// 좀 기다렸다가
			Draw_After_Effect();
		}

		void Draw_First_Effect()
		{
			int size = (int)distance * 40;
			Vector[] vecs = new Vector[size * 3];
			for(int i = 0; i < vecs.length; i += 3)
			{
				double _angle = 90.0 + (angle * -0.5) + i * angle / (double)vecs.length;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
				vecs[i + 1] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.95, 0, Math.sin(Math.toRadians(_angle)) * 0.95);
				vecs[i + 2] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.9, 0, Math.sin(Math.toRadians(_angle)) * 0.9);
			}
			vecs = TRS.Scale(vecs, 6.0 * width_mult, 6.0 * width_mult, distance);
			vecs = TRS.Rotate_Z(vecs, -40.0 + Math.random() * 80.0);
			vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch());
			vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw());
			for(int i = 0; i < vecs.length; i++)
			{
				Location loc = player.getEyeLocation().add(vecs[i]);
				player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, Sakurakagetsu.dtr);
			}
		}
		
		void Check_Damage_Entity()
		{
			Location temp_loc = player.getLocation();
			Vector temp_dir = player.getLocation().getDirection();
			
			temp_loc.getWorld().playSound(temp_loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 1f);
			Location hitbox_axis = player.getLocation().add(temp_dir.clone().multiply(distance * 0.5));
			entities = Hitbox.Targets_In_the_Box(hitbox_axis.toVector(),
					new Vector(temp_loc.getPitch(), temp_loc.getYaw(), 0),
					new Vector(12.0 * width_mult, 12.0 * width_mult, distance),
					player.getNearbyEntities(distance, distance, distance));
			skill_flower_pos = Hitbox.Random_Location_In_the_Box(hitbox_axis.toVector().add(new Vector(0, 3.0, 0)),
					new Vector(temp_loc.getPitch(), temp_loc.getYaw(), 0),
					new Vector(12.0 * width_mult, 12.0 * width_mult, distance), (int)(distance));
			for(Entity en : entities)
			{
				en.getWorld().spawnParticle(Particle.FLAME, en.getLocation().add(0, 2, 0), 1, 0d, 0d, 0d, 0d);
			}
		}
		
		void Draw_After_Effect()
		{
			for (int i = 0; i < skill_flower_pos.length; i++)
			{
				Location _loc = skill_flower_pos[i].toLocation(player.getWorld());
				Sakurakagetsu.Draw_Flower(_loc, i / 3);
			}
			
			for(Entity en : entities)
			{
				if (!(en instanceof LivingEntity))
					continue;
				
				Sakurakagetsu.Draw_Entity_Effect((LivingEntity)en);
				AttackUtil.attack(player, (LivingEntity)en, damage, null,
						DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
			}
		}
		
	}
	
	// 돌진 및 히트박스 생성
	class Evade_Charge implements Runnable
	{
		
		Player player;
		double sec;
		double damage;
		double width_mult;
		
		int count = 0;
		double velocity = 1.4;
		double angle = 0.0;
		
		Location before_loc;
		
		public Evade_Charge(Player _player, double _sec, double _damage, double _width_mult)
		{
			player = _player;
			sec = _sec;
			damage = _damage;
			width_mult = _width_mult;
			
			angle = player.getLocation().getYaw();
			before_loc = player.getLocation();
			
			player.setMetadata(Sakurakagetsu.metaname, new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.5f, 1.5f);
		}
		
		void Slerp()
		{
			double new_angle = player.getLocation().getYaw();
			
			double gap = new_angle - angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			
			gap = Math.min(1.0, Math.max(-1.0, gap));
			angle += gap;
		}
		
		public void run()
		{
			sec -= 0.05;
			count += 1;
			Slerp();
			if (player.isSneaking())
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
				player.removeMetadata(Sakurakagetsu.metaname, MalaMMO_Skill.plugin);
				return;
			}
			if (sec <= 0.0 || !player.hasMetadata(Sakurakagetsu.metaname))
			{
				Make_Hit();
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
				player.removeMetadata(Sakurakagetsu.metaname, MalaMMO_Skill.plugin);
				return;
			}
			
			velocity = Math.min(1.4, velocity + 0.05);
			
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)), 0.0, Math.sin(Math.toRadians(angle + 90)));
			Vector vc = dir.multiply(velocity);
			vc.setY(player.getVelocity().getY());
			
			Vehicle_Util.Get_Last_Vehicle(player).setVelocity(vc);
			player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
			
			if (player.getLocation().distance(before_loc) > 5.0)
			{
				Make_Hit();
			}
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
		
		void Make_Hit()
		{
			double length = player.getLocation().distance(before_loc);
			Location loc = before_loc.clone().add(player.getLocation().subtract(before_loc).multiply(0.5));
			float yaw = before_loc.getYaw();
			loc.setYaw(yaw);
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
					new Evade_Charge_Hit(player, loc, 8.0 * width_mult, length, damage), 10);
			
			before_loc = player.getLocation();
			before_loc.setYaw(yaw);
		}
	}

	// 히트박스를 통한 피해 처리
	class Evade_Charge_Hit implements Runnable
	{
		Player player;
		Location location;
		double damage;
		List<Entity> entities;
		Vector[] skill_flower_pos;
		
		public Evade_Charge_Hit(Player _player, Location _loc, double _box_width, double _box_length, double _damage)
		{
			player = _player;
			location = _loc;
			damage = _damage;
			entities = Hitbox.Targets_In_the_Box(location.toVector(),
					new Vector(location.getPitch(), location.getYaw(), 0),
					new Vector(_box_width, _box_width, _box_length),
					player.getNearbyEntities(Math.max(_box_width, _box_length), Math.max(_box_width, _box_length), Math.max(_box_width, _box_length)));
			skill_flower_pos = Hitbox.Random_Location_In_the_Box(location.toVector().add(new Vector(0, 3.0, 0)),
					new Vector(location.getPitch(), location.getYaw(), 0),
					new Vector(_box_width, _box_width, _box_length), 6);
			Draw_hitline(location.getWorld(), location.toVector().add(new Vector(0, 0.15, 0)),
					new Vector(0, location.getYaw(), 0),
					new Vector(_box_width, _box_width, _box_length));
			player.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);
		}
		
		public void Draw_hitline(World world, Vector _box_pos, Vector _box_rot, Vector _box_size)
		{
			BOX box = new BOX(_box_size);
			
			Vector[] vecs = new Vector[4];
			vecs[0] = new Vector(box.left, 0.0, box.front);
			vecs[1] = new Vector(box.right, 0.0, box.front);
			vecs[2] = new Vector(box.left, 0.0, box.back);
			vecs[3] = new Vector(box.right, 0.0, box.back);
			
			vecs = TRS.Rotate_Z(vecs, _box_rot.getZ());
			vecs = TRS.Rotate_Y(vecs, _box_rot.getY());
			vecs = TRS.Rotate_X(vecs, _box_rot.getX());
			vecs = TRS.Translate(vecs, _box_pos.getX(), _box_pos.getY(), _box_pos.getZ());

			Location[] pos = new Location[4];
			for (int i = 0; i < pos.length; i++)
			{
				pos[i] = _box_pos.clone().add(new Vector(vecs[i].getX(), vecs[i].getY(), vecs[i].getZ())).toLocation(world);
			}
			Particle_Drawer.Draw_Line(pos[0], pos[2], Sakurakagetsu.dtr_small, 0.15);
			Particle_Drawer.Draw_Line(pos[1], pos[3], Sakurakagetsu.dtr_small, 0.15);
		}
		public void run()
		{
			for (int i = 0; i < skill_flower_pos.length; i++)
			{
				Location _loc = skill_flower_pos[i].toLocation(player.getWorld());
				Sakurakagetsu.Draw_Flower(_loc, i);
				player.getWorld().playSound(_loc, Sound.ITEM_TRIDENT_HIT_GROUND, 1.5f, 1.5f);
			}
			
			for(Entity en : entities)
			{
				if (!(en instanceof LivingEntity))
					continue;
				
				Sakurakagetsu.Draw_Entity_Effect((LivingEntity)en);
				AttackUtil.attack(player, (LivingEntity)en, damage, null,
						DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
			}
		}
	}
}















