package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Not_Skill;
import mala.mmoskill.util.Hitbox;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Aerial_Slash_Bind extends RegisteredSkill
{
	public Aerial_Slash_Bind()
	{	
		super(new Aerial_Slash_Bind_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("size", new LinearValue(3.0, 0.2));
		addModifier("second", new LinearValue(0.075, 0.075, 0.0, 3.0));
		addModifier("cooldown", new LinearValue(21.5, -0.5, 15, 50));
		addModifier("stamina", new LinearValue(20, 1.5, 20, 50));
	}
}

class Aerial_Slash_Bind_Handler extends SkillHandler<SimpleSkillResult> implements Listener, Not_Skill
{
	public static ConfigurationSection Altered_Config(ConfigurationSection config)
	{
		config.set("name", "�ð��� �θ޶�");
		List<String> lores = new ArrayList<String>();
		lores.add(MsgTBL.NeedSkills);
		lores.add("&e �θ޶� Į�� - lv.15");
		lores.add("");
		lores.add("&e{size}&7m ũ���� �ð��� �θ޶��� �����ϴ�.");
		lores.add("&7�ð��� �θ޶��� �ε��� ������ ���ڸ��� ������,");
		lores.add("&7���ƿ��鼭 ������ ������ �����ɴϴ�.");
		lores.add("&7���ķε� &e{second}&7�ʰ� ������ �� ������ϴ�.");
		lores.add("");
		lores.add(MsgTBL.WEAPON_EFFECT);
		lores.add(MsgTBL.WEAPON_SWORD + "���ư��� �Ÿ� 30% ����");
		lores.add(MsgTBL.WEAPON_SPEAR + "���� 50% ����");
		lores.add("");
		lores.add(MsgTBL.Cooldown);
		lores.add(MsgTBL.StaCost);
		config.set("lore", lores);
		config.set("material", "LEAD");
		return config;
	}
	
	public Aerial_Slash_Bind_Handler()
	{
		super(Altered_Config(MalaMMO_Skill.plugin.getConfig()), "AERIAL_SLASH_BIND");
		registerModifiers("size", "second");
		
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata skillmeta)
	{
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult data, SkillMetadata skillmeta)
	{
		PlayerData pd = MMOCore.plugin.dataProvider.getDataManager().get(skillmeta.getCaster().getPlayer());
		if (!Skill_Util.Has_Skill(pd, "AERIAL_SLASH", 15))
		{
			skillmeta.getCaster().getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return;
		}
		
		double size = (int)skillmeta.getModifier("size");
		double remaining_time = skillmeta.getModifier("second");
		double speed = size * 0.1;

		// ���� ȿ��
		if (Weapon_Identify.Hold_MMO_Sword(pd.getPlayer()))
			speed *= 1.3;
		else if (Weapon_Identify.Hold_MMO_Spear(pd.getPlayer()))
			size *= 1.5;
		
		pd.getPlayer().swingMainHand();
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Aerial_Slash_Bind_Skill(pd.getPlayer(), size * 0.5, speed, remaining_time));
	}

	// �˼� �������  ȿ��
	class Aerial_Slash_Bind_Skill implements Runnable
	{
		Player player;
		Location origin_pos, pos;
		Vector dir;

		ArrayList<Location> binded_pos = new ArrayList<Location>();
		ArrayList<LivingEntity> enemies = new ArrayList<LivingEntity>();
		ArrayList<LivingEntity> binding_enemies = new ArrayList<LivingEntity>();
		ArrayList<LivingEntity> cant_damage = new ArrayList<LivingEntity>();
		double size;

		double max_speed = 0.8;
		double current_speed = 0.8;
		double decelerate = -0.02;
		Vector[] vecs;
		
		double correct_angle = 0.0;
		double correct_Zangle = 0.0;
		int count = 0;
		
		boolean is_reverse = false;
		boolean is_waiting = false;
		
		public Aerial_Slash_Bind_Skill(Player p, double _size, double _speed, double _remaining_time)
		{
			player = p;
			size = _size;
			wait_time = _remaining_time;
			
			max_speed = _speed; //size * 0.6;
			current_speed = _speed; //size * 0.6;
			decelerate = current_speed * 0.03;
			
			pos = p.getEyeLocation().add(0, -0.3, 0);
			origin_pos = pos.clone();
			dir = p.getLocation().getDirection();
			
			Make_Vecs();
			vecs = TRS.Scale(vecs, size, size, size);
			correct_Zangle = -30.0 + Math.random() * 60.0;
		}
		
		void Make_Vecs()
		{
			vecs = new Vector[10];
			vecs[0] = new Vector(-0.7, 0.0, 0.0);
			vecs[1] = new Vector(-0.85, 0.0, 0.3);
			vecs[2] = new Vector(-1.0, 0.0, 0.0);
			vecs[3] = new Vector(-0.85, 0.0, -0.3);
			vecs[4] = new Vector(-0.7, 0.0, 0.0);
			vecs[5] = new Vector(0.7, 0.0, 0.0);
			vecs[6] = new Vector(0.85, 0.0, 0.3);
			vecs[7] = new Vector(1.0, 0.0, 0.0);
			vecs[8] = new Vector(0.85, 0.0, -0.3);
			vecs[9] = new Vector(0.7, 0.0, 0.0);
		}
		
		public void run()
		{
			count += 1;
			
			// �˱�
			if (count % 4 == 0)
				player.getWorld().playSound(pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2f, 1.5f);
			
			// ��ƼŬ �׸���
			correct_angle += 16.0;
			Vector[] temp_vecs = TRS.Rotate_Y(vecs, correct_angle);
			temp_vecs = TRS.Rotate_Z(temp_vecs, correct_Zangle);
			temp_vecs = TRS.Rotate_X(temp_vecs, pos.getPitch());
			temp_vecs = TRS.Rotate_Y(temp_vecs, pos.getYaw());

			for(int i = 0; i < temp_vecs.length - 1; i++)
			{
				Location start = pos.clone().add(temp_vecs[i]);
				Location end = pos.clone().add(temp_vecs[i + 1]);
				Particle_Drawer.Draw_Line(start, end, Particle.CRIT_MAGIC, 0.1);
			}
			
			// ���� ����
			List<Entity> abc = new ArrayList<Entity>(pos.getWorld().getNearbyEntities(pos, size * 3, size * 3, size * 3));
			List<Entity> entities = Hitbox.Targets_In_the_BoundingBox(pos.toVector(),
					new Vector(pos.getPitch(), pos.getYaw(), 0),
					new Vector(size * 2, 2.5, size * 2),
					abc);
			
			for (Entity temp : entities)
			{
				if (!(temp instanceof LivingEntity))
					continue;
				if (temp == player)
					continue;
				if (is_reverse)
				{
					if (binding_enemies.contains(temp))
						continue;
				}
				else
				{
					if (enemies.contains(temp))
						continue;					
				}
				if (cant_damage.contains(temp))
					continue;
				
				LivingEntity temp2 = (LivingEntity)temp;
				EntityDamageEvent ede = new EntityDamageByEntityEvent(player, temp2, DamageCause.MAGIC, 0);
				Bukkit.getPluginManager().callEvent(ede);
				if (!ede.isCancelled())
				{
					if (is_reverse) // ���ƿ��� ��?
						binding_enemies.add(temp2);
					else // ���ư��� ��?
					{
						enemies.add(temp2);
						binded_pos.add(temp2.getLocation());
					}
				}
				else
					cant_damage.add(temp2);
			}

			for (int i = 0; i < enemies.size(); i++) // ���ư��鼭 ������ ����
			{
				LivingEntity le = enemies.get(i);
				if (binding_enemies.contains(le)) // ���ƿ��鼭 �ε��� ���鿡 ���ԵǾ� �־��� ��쿡�� �н�
					continue;
				Location loc = binded_pos.get(i);
				le.teleport(loc, TeleportCause.PLUGIN);
			}
			for (int i = 0; i < binding_enemies.size(); i++) // ���ƿ��鼭 �ٽ� ���� ����
			{
				LivingEntity le = binding_enemies.get(i);
				le.teleport(pos, TeleportCause.PLUGIN);
			}
			
			if (is_waiting)
				waiting();
			else
			{
				running();
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
			}
		}

		void running()
		{
			// ��� ���� �ð�
			if (current_speed > -max_speed)
			{
				pos.add(dir.clone().multiply(current_speed));
				current_speed -= decelerate;
				if (current_speed <= 0.0)
					is_reverse = true;
			}
			else
			{
				pos = origin_pos;
				is_waiting = true;
			}
		}
		
		double wait_time = 3.0;
		void waiting()
		{
			wait_time -= 0.05;
			if (wait_time > 0.0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
		}
	}
}
