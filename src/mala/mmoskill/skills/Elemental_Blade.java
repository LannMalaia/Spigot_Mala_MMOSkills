package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.events.IceMagicEvent;
import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.events.PhysicalSkillEvent;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Elemental_Blade extends RegisteredSkill
{
	public static Elemental_Blade skill;
	
	public Elemental_Blade()
	{	
		super(new Elemental_Blade_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("power", new LinearValue(25, 3));
		addModifier("fire_power", new LinearValue(53, 8));
		addModifier("ice_power", new LinearValue(40, 5));
		addModifier("lightning_power", new LinearValue(82, 12));
		addModifier("mana_power", new LinearValue(2.1, .1));
		addModifier("cooldown", new LinearValue(59.5, -0.5));
		addModifier("mana", new LinearValue(63, 3));
		
		skill = this;
	}
}

class Elemental_Blade_Handler extends MalaSkill implements Listener
{
	public Elemental_Blade_Handler()
	{
		super(	"ELEMENTAL_BLADE",
				"엘리멘탈 블레이드",
				Material.QUARTZ,
				"&c화염&7, &b냉기&7, &a번개&7, &9마나&7의 원소 칼날을 소환합니다.",
				"&7소환된 원소 칼날은 30초간 유지되며, 다음과 같은 상황에서 3초간 효력을 발휘하고 사라집니다.",
				"&c화염의 칼날",
				"&7 - 화염 마법 시전시 &8{fire_power}&7의 화염 피해를 주는 투사체를 날립니다.",
				"&b냉기의 칼날",
				"&7 - 냉기 마법 시전시 피해량이 &8{ice_power}&7 증가하고, 둔화 수준이 증가합니다.",
				"&a번개의 칼날",
				"&7 - 전격 마법 시전시 근처 5m의 적들에게 &8{lightning_power}&7의 번개 피해를 주고 멀리 날려보냅니다.",
				"&9마나의 칼날",
				"&7 - 순수 물리 스킬 시전시 &8{mana_power}&7배의 마법 피해를 적용합니다.",
				"&7 - 이렇게 변경된 스킬은 마법 피해량 증가치가 적용됩니다.",
				"&7발현되지 않은 원소 칼날은 시전자 주변을 돌며 &8{power}&7의 피해를 줍니다.",
				"",
				MsgTBL.Cooldown_Fixed, MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double damage = cast.getModifier("power");
		double fire_damage = cast.getModifier("fire_power");
		double ice_damage = cast.getModifier("ice_power");
		double lightning_damage = cast.getModifier("lightning_power");
		double mana_mult = cast.getModifier("mana_power");

		CooldownFixer.Fix_Cooldown(data, Elemental_Blade.skill);
		data.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Elemental_Blade_Fire(data.getPlayer(), damage, fire_damage, 0.0));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Elemental_Blade_Ice(data.getPlayer(), damage, ice_damage, 90.0));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Elemental_Blade_Lightning(data.getPlayer(), damage, lightning_damage, 180.0));
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Elemental_Blade_Mana(data.getPlayer(), damage, mana_mult, 270.0));
	}

	class Elemental_Blade_Base
	{
		Player player;
		double damage;
		
		double blade_width = 1.25;
		double blade_length = 7;
		Vector[] original_vecs;
		Vector[] vecs;
		DustOptions dop = new Particle.DustOptions(Color.YELLOW, 20f);
		Color particle_color;

		boolean fired = false;
		double angle;
		double duration = 30.0;
		
		public Elemental_Blade_Base(Player _p, double _damage)
		{
			player = _p;
			damage = _damage;
		}
		
		public void Make_Blade()
		{
			original_vecs = new Vector[5];
			original_vecs[0] = new Vector(0.0, 0.75, 2.0);
			original_vecs[1] = new Vector(blade_width, 0.75, 2.0 + blade_width);
			original_vecs[2] = new Vector(0.0, 0.75, 2.0 + blade_length);
			original_vecs[3] = new Vector(-blade_width, 0.75, 2.0 + blade_width);
			original_vecs[4] = new Vector(0.0, 0.75, 2.0);
		}
		
		public void Draw_Blade(double _angle)
		{
			vecs = TRS.Rotate_Y(original_vecs, _angle);
			float size = (float)(duration > 5.0 ? 0.8 : 0.8 * (duration / 5.0));
			dop = new Particle.DustOptions(particle_color, size);
			
			
			for(int i = 0; i < vecs.length - 1; i++)
			{
				Location start = player.getLocation().add(vecs[i]);
				Location end = player.getLocation().add(vecs[i + 1]);
				Particle_Drawer.Draw_Line(start, end, dop, 0.15);
			}
		}
		
		public void Damage()
		{
			Location hitbox_axis = player.getLocation().add(0.0, 0.75, 0.0);
			double rad = 2.0 + blade_length * 0.5;
			hitbox_axis.add(Math.cos(Math.toRadians(angle)) * rad, 0.0, Math.sin(Math.toRadians(angle)) * rad);
			
			// hitbox_axis.getWorld().spawnParticle(Particle.BARRIER, hitbox_axis.clone().add(0.0, 1.0, 0.0), 1, 0, 0, 0, 0);
			
			List<Entity> abc = new ArrayList<Entity>(hitbox_axis.getWorld().getNearbyEntities(hitbox_axis, blade_length, blade_length, blade_length));
			List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(hitbox_axis.toVector(),
					new Vector(0.0, angle, 0),
					new Vector(blade_width * 1.5, 2.5, rad),
					abc);
			for(Entity en : entities)
			{
				if (!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;
				
				laylia_core.main.Damage.Attack(player, (LivingEntity)en, damage,
						DamageType.SKILL, DamageType.PHYSICAL, DamageType.MAGIC);
			}
		}
		
		public void Set_Fire()
		{
			if (!fired)
			{
				fired = true;
				duration = 3.0;
			}
		}
		
		public void Update()
		{
			duration -= 0.05;
			
			angle += 4;
			Draw_Blade(angle);
			Damage();
		}
	}

	class Elemental_Blade_Fire extends Elemental_Blade_Base implements Runnable, Listener
	{
		double fire_damage;
		
		public Elemental_Blade_Fire(Player _p, double _damage, double _fire, double _start_angle)
		{
			super(_p, _damage);
			fire_damage = _fire;
			angle = _start_angle;
			particle_color = Color.ORANGE;
			
			Make_Blade();
			Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		}
		
		@EventHandler
		public void When_Attack(FireMagicEvent _event)
		{
			if (_event.getCaster() != player)
				return;
			
			Set_Fire();
			FireMagicEvent.getHandlerList().unregister(this);
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Fire_Blade_Activate(player, 30, fire_damage));
		}
		
		@Override
		public void run()
		{
			super.Update();
			if (duration > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			else
			{
				FireMagicEvent.getHandlerList().unregister(this);
			}
		}
	}
	class Fire_Blade_Activate implements Runnable
	{
		Player player;
		double damage;
		double distance;
		List<Entity> entities;
		
		Location pos;
		Vector dir;
		
		double speed = 0.2;
		double angle = 180;
		
		Vector[] vecs;
		
		public Fire_Blade_Activate(Player _p, double _distance, double _damage)
		{
			player = _p;
			distance = _distance;
			damage = _damage;
			
			pos = _p.getEyeLocation();
			dir = _p.getLocation().getDirection();
			
			vecs = new Vector[36];
			for(int i = 0; i < vecs.length; i++)
			{
				double _angle = 90.0 + (angle * -0.5) + i * angle / 36.0;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			}
			vecs = TRS.Scale(vecs, 5.0, 1.0, 1.0);
			vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch());
			vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw());
		}
		
		public void run()
		{
			Location before_pos = pos.clone();
			pos.add(dir.clone().multiply(speed));
			double len = before_pos.distance(pos);
			
			// 파티클 그리기
			pos.getWorld().playSound(pos, Sound.ENTITY_BLAZE_SHOOT, 1f, 2f);
			for(double i = 0.0; i < len; i += 0.4)
			{
				for(int j = 0; j < vecs.length; j++)
				{
					Location particle_pos = before_pos.clone().add(dir.clone().multiply(i)).add(vecs[j]);
					particle_pos.getWorld().spawnParticle(Particle.FLAME, particle_pos, 1, 0, 0, 0, 0);
				}
			}
			
			// 판정 하기
			Location hitbox_axis = before_pos.clone().add(dir.clone().multiply(len * 0.5));
			List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, len, len, len));
			entities = Hitbox.Targets_In_the_BoundingBox(hitbox_axis.toVector(),
					new Vector(pos.getPitch(), pos.getYaw(), 0),
					new Vector(10.0, 10.0, len),
					abc);
			for(Entity en : entities)
			{
				if(!(en instanceof LivingEntity))
					continue;
				if (en == player)
					continue;
				
				Damage.Attack(player, (LivingEntity)en, damage,
						DamageType.PROJECTILE, DamageType.SKILL, DamageType.MAGIC);
			}
			
			distance -= speed;
			if(distance > 0.0)
			{
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			}
		}
	}
	
	class Elemental_Blade_Ice extends Elemental_Blade_Base implements Runnable, Listener
	{
		double ice_damage;
		
		public Elemental_Blade_Ice(Player _p, double _damage, double _fire, double _start_angle)
		{
			super(_p, _damage);
			ice_damage = _fire;
			angle = _start_angle;
			particle_color = Color.SILVER;

			Make_Blade();
			Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		}

		@EventHandler
		public void When_Attack(IceMagicEvent _event)
		{
			if (_event.getCaster() != player)
				return;

			Set_Fire();
			
			_event.getAttack().add(ice_damage);
			_event.setSuperSlow(true);
			/*
			_event.getEntity().getWorld().playSound(_event.getEntity().getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 2.0f, 1.0f);
			_event.getEntity().getWorld().spawnParticle(Particle.SNOWBALL, _event.getEntity().getEyeLocation(),
					50, 1.0, 1.0, 1.0, 0);
			Ice_Bolt.Slow_Target(_event.getEntity(), 3, 200);
			*/
		}
		
		@Override
		public void run()
		{
			super.Update();
			if (duration > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			else
			{
				IceMagicEvent.getHandlerList().unregister(this);
			}
		}
	}
	
	class Elemental_Blade_Lightning extends Elemental_Blade_Base implements Runnable, Listener
	{
		double radius = 5;
		double lightning_damage;
		
		public Elemental_Blade_Lightning(Player _p, double _damage, double _fire, double _start_angle)
		{
			super(_p, _damage);
			lightning_damage = _fire;
			angle = _start_angle;
			particle_color = Color.YELLOW;

			Make_Blade();
			Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		}

		@EventHandler
		public void When_Attack(LightningMagicEvent _event)
		{
			if (_event.getCaster() != player)
				return;

			Set_Fire();
			_event.getHandlers().unregister(this); // 이건 1회 한정임
			
			for (int i = 0; i < 5; i++)
			{
				Location start = player.getEyeLocation().add(
						-radius + Math.random() * radius * 2,
						-radius + Math.random() * radius * 2,
						-radius + Math.random() * radius * 2);
				Location end = player.getEyeLocation().add(
						-radius + Math.random() * radius * 2,
						-radius + Math.random() * radius * 2,
						-radius + Math.random() * radius * 2);
				Lightning_Bolt.Draw_Lightning_Line(start, end);
			}
			for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius))
			{
				if (!(entity instanceof LivingEntity))
					continue;
				if (entity == player)
					continue;

				if (Damage.Is_Possible(player, entity))
				{
					Damage.Attack(player, (LivingEntity)entity, damage, DamageType.SKILL);
					Vector vec = entity.getLocation().subtract(player.getLocation()).toVector().normalize();
					entity.setVelocity(vec.add(new Vector(0.0, 0.4, 0.0)).multiply(2.0));
				}
			}
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 2.0f, 1.2f);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.0f);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 1.0f);
		}
		
		@Override
		public void run()
		{
			super.Update();
			if (duration > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			else
			{
				LightningMagicEvent.getHandlerList().unregister(this);
			}
		}
	}
	class Elemental_Blade_Mana extends Elemental_Blade_Base implements Runnable, Listener
	{
		double mana_mult;
		
		public Elemental_Blade_Mana(Player _p, double _damage, double _fire, double _start_angle)
		{
			super(_p, _damage);
			mana_mult = _fire;
			angle = _start_angle;
			particle_color = Color.fromRGB(170, 170, 255);

			Make_Blade();
			Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
		}
		
		@EventHandler
		public void When_Attack(PhysicalSkillEvent _event)
		{
			if (_event.getCaster() != player)
				return;

			Set_Fire();
			
			_event.addType(DamageType.MAGIC);
			_event.getAttack().multiplicativeModifier(mana_mult);
			player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 2.0f);
			player.getWorld().spawnParticle(Particle.SPELL_WITCH, player.getEyeLocation(),
					50, 2.0, 2.0, 2.0, 0);
			
		}
		
		@Override
		public void run()
		{
			super.Update();
			if (duration > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
			else
			{
				PhysicalSkillEvent.getHandlerList().unregister(this);
			}
		}
	}
	
}
