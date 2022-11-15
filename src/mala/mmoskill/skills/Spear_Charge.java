package mala.mmoskill.skills;

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
import org.bukkit.util.Vector;

import mala.mmoskill.events.PhysicalSkillEvent;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
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

public class Spear_Charge extends RegisteredSkill
{
	public static String metaname = "mala.mmoskill.spear_charge";
	
	public Spear_Charge()
	{	
		super(new Spear_Charge_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("power", new LinearValue(24, 6));
		addModifier("sec", new LinearValue(1.0, 0.05));
		// addModifier("distance", new LinearValue(10, 0.5));
		//addModifier("cooldown", new LinearValue(2, 0));
		//addModifier("stamina", new LinearValue(2, 0));
		addModifier("cooldown", new LinearValue(15, -0.5, 5, 20));
		addModifier("stamina", new LinearValue(15, 1.2));
	}
}

class Spear_Charge_Handler extends MalaSkill implements Listener
{
	public Spear_Charge_Handler()
	{
		super(	"SPEAR_CHARGE",
				"스피어 차지",
				Material.TRIDENT,
				MsgTBL.WEAPON + MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&e{sec}&7초간 전방으로 빠르게 돌진합니다.",
				"&7돌진 궤적에 있던 적들은 &e{power}&7의 피해를 받습니다.",
				"&7웅크리거나 스킬을 재사용하여 돌진을 취소할 수 있습니다.",
				"&c창을 들고 있어야 합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		Player player = cast.getCaster().getPlayer();
		if (player.hasMetadata(Spear_Charge.metaname))
		{
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
			player.sendMessage("§c§l[ 스피어 차지 취소 ]");
			player.removeMetadata(Spear_Charge.metaname, MalaMMO_Skill.plugin);
			return new SimpleSkillResult(false);
		}
		
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if (!Weapon_Identify.Hold_Spear(data.getPlayer()))
		{
			data.getPlayer().sendMessage(MsgTBL.Equipment_Not_Correct);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		double damage = cast.getModifier("power"); // 공격력
		
		// double distance = (int)cast.getModifier("distance");
		// distance *= stamina_mult;
		// data.getPlayer().swingMainHand();
		// Charge(data.getPlayer(), distance, damage);
		
		double sec = cast.getModifier("sec");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Spear_Charge_Skill2(data.getPlayer(), sec, damage));
	}
	
	/*
	void Charge(Player _player, double _distance, double _damage)
	{
		double real_distance = _distance;
		
		Location temp_loc = _player.getLocation().add(0, 0.1, 0);
		Vector temp_dir = _player.getLocation().getDirection();
		for(double i = 0.0; i < _distance; i += 0.25)
		{
			Location temp_temp_loc = temp_loc.clone().add(temp_dir.clone().multiply(i));
			Block block = temp_temp_loc.getBlock();
			Block block2 = temp_temp_loc.clone().add(0, 1, 0).getBlock();
			
			if (block.getType().isSolid() || block2.getType().isSolid())
			{
				real_distance = Math.max(0.0, i - 0.25);
				break;
			}
		}
		
		temp_loc.getWorld().playSound(temp_loc, Sound.ENTITY_BLAZE_SHOOT, 2f, 1.5f);
		Location hitbox_axis = temp_loc.clone().add(temp_dir.clone().multiply(real_distance * 0.5));
		
		List<Entity> abc = new ArrayList<Entity>(temp_loc.getWorld().getNearbyEntities(temp_loc, _distance, _distance, _distance));
		List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(hitbox_axis.toVector(),
				new Vector(temp_loc.getPitch(), temp_loc.getYaw(), 0),
				new Vector(5.0, 5.0, real_distance),
				abc);
		for(Entity en : entities)
		{
			if (!(en instanceof LivingEntity))
				continue;
			if (en == _player)
				continue;
			
			Draw_Effect((LivingEntity)en);

			AttackResult ar = new AttackResult(_damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
			Bukkit.getPluginManager().callEvent(new PhysicalSkillEvent(_player, ar));
			
			MMOLib.plugin.getDamage().damage(_player, (LivingEntity)en, ar);
		}
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Spear_Charge_Skill(_player, 5.0, 5.0, real_distance));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Spear_Charge_Skill(_player, 5.0, 5.0, real_distance));
		
		_player.teleport(temp_loc.add(temp_dir.multiply(real_distance)).add(0, 0.1, 0), TeleportCause.PLUGIN);
		temp_loc.getWorld().playSound(temp_loc, Sound.ENTITY_BLAZE_SHOOT, 2f, 1.5f);
	}
	
	void Draw_Effect(LivingEntity _target)
	{
		Location loc = _target.getEyeLocation();
		loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc, 1, 0d, 0d, 0d, 1d);
	}
		
	class Spear_Charge_Skill implements Runnable
	{
		Player player;
		Location pos;
		Vector dir;
		
		Vector[] vecs;
		
		public Spear_Charge_Skill(Player p, double _width, double _height, double _distance)
		{
			player = p;
			
			pos = p.getLocation();
			dir = p.getLocation().getDirection();
			
			vecs = new Vector[(int) (_distance)];
			for(int i = 0; i < vecs.length; i += 4)
			{
				if (vecs.length >= i + 1)
					vecs[i] = new Vector(_width * 0.5, _height * 0.5, i + 0.0);
				if (vecs.length >= i + 2)
					vecs[i + 1] = new Vector(_width * 0.5, _height * -0.5, i + 1);
				if (vecs.length >= i + 3)
					vecs[i + 2] = new Vector(_width * -0.5, _height * -0.5, i + 2);
				if (vecs.length >= i + 4)
					vecs[i + 3] = new Vector(_width * -0.5, _height * 0.5, i + 3);
			}
			vecs = TRS.Rotate_X(vecs, pos.getPitch());
			vecs = TRS.Rotate_Y(vecs, pos.getYaw());
			
		}
		
		public void run()
		{
			// 검기
			player.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 2f);
			
			// 파티클 그리기
			for(int i = 0; i < vecs.length - 1; i++)
			{
				Location loc = pos.clone().add(vecs[i]);
				Location loc2 = pos.clone().add(vecs[i + 1]);
				Vector diff = loc2.clone().subtract(loc).toVector();
				
				for(double j = 0; j <1.0; j += 0.05)
				{
					Location loc_temp = loc.clone().add(diff.clone().multiply(j));
					player.getWorld().spawnParticle(Particle.CRIT, loc_temp, 1, 0d, 0d, 0d, 0d);
				}
				
				
			}
		}
	}
	*/

	class Spear_Charge_Skill2 implements Runnable
	{
		Player player;
		double sec;
		double damage;
		
		int count = 0;
		double velocity = 1.4;
		double angle = 0.0;
		
		Vector[] vecs;
		
		public Spear_Charge_Skill2(Player _player, double _sec, double _damage)
		{
			player = _player;
			sec = _sec;
			damage = _damage;
			
			angle = player.getLocation().getYaw();
			Make_Vecs();


			DamageMetadata dm = new DamageMetadata(damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
			Bukkit.getPluginManager().callEvent(new PhysicalSkillEvent(player, dm));
			damage = dm.getDamage();
			
			player.setMetadata(Spear_Charge.metaname, new FixedMetadataValue(MalaMMO_Skill.plugin, true));
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
		}
		
		void Make_Vecs()
		{
			vecs = new Vector[8];
			vecs[0] = new Vector(-1.5, 2.5, -1.5);
			vecs[1] = new Vector(0.0, 1.0, 3.5);
			vecs[2] = new Vector(1.5, 2.5, -1.5);
			vecs[3] = new Vector(0.0, 1.0, 3.5);
			vecs[4] = new Vector(-1.5, -0.5, -1.5);
			vecs[5] = new Vector(0.0, 1.0, 3.5);
			vecs[6] = new Vector(1.5, -0.5, -1.5);
			vecs[7] = new Vector(0.0, 1.0, 3.5);
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
				player.sendMessage("§c§l[ 스피어 차지 취소 ]");
				player.removeMetadata(Spear_Charge.metaname, MalaMMO_Skill.plugin);
				return;
			}
			if (sec <= 0.0 || !player.hasMetadata(Spear_Charge.metaname))
			{
				player.removeMetadata(Spear_Charge.metaname, MalaMMO_Skill.plugin);
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
			
			Vehicle_Util.Get_Last_Vehicle(player).setVelocity(vc);
			// player.setVelocity(vc);
			
			Vector[] new_vecs = TRS.Rotate_Y(vecs, angle);
			for (int i = 0; i < 8; i += 2)
			{
				Location start = player.getLocation().add(new_vecs[i]);
				Location end = player.getLocation().add(new_vecs[i + 1]);
				Particle_Drawer.Draw_Line(start, end, Particle.CRIT, 0.15);
			}
			player.getWorld().spawnParticle(Particle.FLAME, player.getEyeLocation(), 5, 0.0, 0.4, 0.0, 0.0);
			
			for(Entity en : player.getWorld().getNearbyEntities(player.getLocation(), 3.0, 3.0, 3.0))
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

}















