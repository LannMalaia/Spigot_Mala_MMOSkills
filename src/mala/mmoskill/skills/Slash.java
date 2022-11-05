package mala.mmoskill.skills;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import mala.mmoskill.events.PhysicalSkillEvent;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;

public class Slash extends RegisteredSkill
{
	public Slash()
	{	
		super(new Slash_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(39.5, -0.5));
		addModifier("distance", new LinearValue(11, 1, 10, 25));
		addModifier("power", new LinearValue(28.5, 3.5));
		addModifier("stamina", new LinearValue(16.5, 1.5));
	}
}

class Slash_Handler extends MalaSkill implements Listener
{
	public Slash_Handler()
	{
		super(	"SLASH",
				"순간베기",
				Material.IRON_SWORD,
				MsgTBL.WEAPON + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7전방 &8{distance}&7m의 적 전체에게 &8{power}&7의 피해를 줍니다.",
				"",
				MsgTBL.WEAPON_EFFECT,
				MsgTBL.WEAPON_SWORD + "피해량 50% 증가",
				MsgTBL.WEAPON_SPEAR + "범위 30% 증가",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double damage = cast.getModifier("power"); // 공격력

		double distance = cast.getModifier("distance");

		// 무기 효과
		if (Weapon_Identify.Hold_MMO_Sword(data.getPlayer()))
			damage *= 1.5;
		else if (Weapon_Identify.Hold_MMO_Spear(data.getPlayer()))
			distance *= 1.3;
		
		data.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new SlashSkill(data.getPlayer(), distance, damage));
	}

	// 검술 베어가르기  효과
	class SlashSkill implements Runnable
	{
		Player player;
		double damage;
		double distance;
		List<Entity> entities;
		
		double angle = 180;
		
		public SlashSkill(Player p, double _distance, double _damage)
		{
			player = p;
			distance = _distance;
			damage = _damage;

			DamageMetadata ar = new DamageMetadata(damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
			Bukkit.getPluginManager().callEvent(new PhysicalSkillEvent(player, ar));
			damage = ar.getDamage();
			
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
				double _angle = 90.0 + (angle * -0.5) + i * angle / (double)size;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
				vecs[i + 1] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.95, 0, Math.sin(Math.toRadians(_angle)) * 0.95);
				vecs[i + 2] = new Vector(Math.cos(Math.toRadians(_angle)) * 0.9, 0, Math.sin(Math.toRadians(_angle)) * 0.9);
			}
			vecs = TRS.Scale(vecs, 4.0, 4.0, distance);
			vecs = TRS.Rotate_Z(vecs, -60.0 + Math.random() * 120.0);
			vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch());
			vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw());
			for(int i = 0; i < vecs.length; i++)
			{
				Location loc = player.getEyeLocation().add(vecs[i]);
				player.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0d, 0d, 0d, 0d);
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
					new Vector(8.0, 8.0, distance),
					player.getNearbyEntities(distance, distance, distance));

			for(Entity en : entities)
			{
				en.getWorld().spawnParticle(Particle.FLAME, en.getLocation().add(0, 2, 0), 1, 0d, 0d, 0d, 0d);
			}
		}
		
		void Draw_After_Effect()
		{
			for(Entity en : entities)
			{
				if (!(en instanceof LivingEntity))
					continue;
				
				Draw_Entity_Effect((LivingEntity)en);
				
				Damage.Attack(player, (LivingEntity)en, damage, DamageType.WEAPON, DamageType.SKILL, DamageType.PHYSICAL);
			}
		}
		void Draw_Entity_Effect(LivingEntity _target)
		{
			Random rand = new Random();
			
			for(int count = 0; count < 2; count++)
			{
				Vector rand_vec = new Vector(-1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0).normalize();
				Vector from = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(-3.0));
				Vector to = _target.getEyeLocation().toVector().add(rand_vec.clone().multiply(3.0));
		
				Location loc = from.toLocation(_target.getWorld());
				for(double i = 0.0; i < from.distance(to); i += 0.2)
				{
					loc.add(rand_vec.clone().multiply(0.1));
					loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0d, 0d, 0d, 0d, new Particle.DustOptions(Color.WHITE, 2));
				}
			}
			
			Location loc = _target.getEyeLocation();
			loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2f, 1.5f);
			loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0d, 0d, 0d, 0d);
		}
	}
}
