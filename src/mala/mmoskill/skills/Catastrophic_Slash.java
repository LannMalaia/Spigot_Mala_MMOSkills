package mala.mmoskill.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.TRS;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Catastrophic_Slash extends RegisteredSkill
{
	public static DustTransition dtr = new DustTransition(Color.fromRGB(192, 0, 0), Color.BLACK, 1.5f);
	
	public static Catastrophic_Slash skill;
	
	public Catastrophic_Slash()
	{	
		super(new Catastrophic_Slash_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(30, 5));
		addModifier("sta_damage", new LinearValue(0.77, 0.07));
		addModifier("blood_damage", new LinearValue(0.33, 0.03));
		addModifier("wither_add", new LinearValue(1.2, 0.2));
		addModifier("wither_max", new LinearValue(12, 2));
		addModifier("cooldown", new LinearValue(120, 0));
		addModifier("stamina", new LinearValue(10, 0));
		
		skill = this;
	}
}

class Catastrophic_Slash_Handler extends MalaSkill implements Listener
{
	public Catastrophic_Slash_Handler()
	{
		super(	"CATASTROPHIC_SLASH",
				"파멸의 창상",
				Material.ENDER_EYE,
				MsgTBL.NeedSkills,
				"&e 강격 - lv.15, 블러드 버스트 - lv.3",
				"&e 순간베기 - lv.15, 앵화월소 - lv.3",
				"&e 부메랑 칼날 - lv.15, 선풍검 - lv.3",
				"&e 스피어 차지 - lv.15, 유성격 - lv.3",
				"",
				MsgTBL.WEAPON + MsgTBL.PROJECTILE + MsgTBL.SKILL + MsgTBL.PHYSICAL,
				"",
				"&7스태미나와 혈흔을 전부 소모해 10개의 검기를 발사합니다.",
				"&7검기는 &e{distance}&7m만큼 나아가며,",
				"&7피해량은 &e소모한 스태미나 * {sta_damage} + 혈흔의 수 * {blood_damage}&7가 됩니다.",
				"&7피해를 받은 적들은 20초간 &e{wither_add}&7 레벨 &8시듦&7 버프를 부여받으며,",
				"&7해당 버프는 최대 &e{wither_max}&7 레벨까지 부여됩니다.",
				"&7마지막으로 발사되는 검기는 크기가 크고 느리게 나아갑니다.",
				"",
				MsgTBL.Cooldown_Fixed, MsgTBL.StaCost_All);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if (Skill_Util.Has_Skill(data, "BASH", 15) && Skill_Util.Has_Skill(data, "BASH_BLOOD", 3)
			&& Skill_Util.Has_Skill(data, "SLASH", 15) && Skill_Util.Has_Skill(data, "SAKURAKAGETSU", 3)
			&& Skill_Util.Has_Skill(data, "AERIAL_SLASH", 15) && Skill_Util.Has_Skill(data, "SWORD_CIRCLE", 3)
			&& Skill_Util.Has_Skill(data, "SPEAR_CHARGE", 15) && Skill_Util.Has_Skill(data, "SPIRAL_SHOOT", 3))
		{
			return new SimpleSkillResult(true);
		}
		data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
		return new SimpleSkillResult(false);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double distance = cast.getModifier("distance");
		double sta_damage = cast.getModifier("sta_damage");
		double blood_damage = cast.getModifier("blood_damage");
		int wither_add = (int)cast.getModifier("wither_add");
		int wither_max = (int)cast.getModifier("wither_max");
		
		double stamina = data.getStamina();
		int bloodstack = Stance_Change.Get_BloodStack(data.getPlayer());
		data.setStamina(0);
		Stance_Change.Set_BloodStack(data.getPlayer(), 0);
		
		double damage = sta_damage * stamina + blood_damage * bloodstack;

		CooldownFixer.Fix_Cooldown(data, Catastrophic_Slash.skill);
		
		for (int i = 0; i < 10; i++)
		{
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin,
				new Catastrophic_Slash_Skill(cast, data.getPlayer(), distance, damage, wither_add, wither_max, i == 9), i * 5);
		}
	}
	
	class Catastrophic_Slash_Skill implements Runnable
	{
		SkillMetadata cast;
		double slash_size = 5.0;
		double speed = 2.0;
		
		Player player;
		double distance;
		double damage;
		int wither_level, wither_max;
		
		Vector[] vecs;
		Location skill_pos;
		Vector dir;
		List<Entity> damaged_entities = new ArrayList<Entity>();
		List<Entity> entities;
		
		public Catastrophic_Slash_Skill(SkillMetadata cast, Player p, double _distance, double _damage, int _wither, int _wither_max, boolean _is_big)
		{
			this.cast = cast;
			player = p;
			distance = _distance;
			damage = _damage;
			wither_level = _wither;
			wither_max = _wither_max;

			slash_size *= _is_big ? 2.0 : 1.0;
			speed *= _is_big ? 0.2 : 1.0;
			
			skill_pos = player.getEyeLocation();
			dir = player.getLocation().getDirection().clone();
			//dir = new Vector(Math.cos(Math.toRadians(player.getLocation().getYaw())), 3d * Math.cos(Math.random() * Math.PI ), Math.sin(Math.toRadians(player.getLocation().getYaw()))).normalize().clone();
			entities = player.getWorld().getEntities();
			Make_Vecs();
		}
		
		void Make_Vecs()
		{
			double angle = 260.0;
			int size = (int)(slash_size * 12);
			vecs = new Vector[size];
			for(int i = 0; i < vecs.length; i += 1)
			{
				double _angle = 90.0 + (angle * -0.5) + i * angle / (double)vecs.length;
				vecs[i] = new Vector(Math.cos(Math.toRadians(_angle)), 0, Math.sin(Math.toRadians(_angle)));
			}
			vecs = TRS.Scale(vecs, slash_size, slash_size, slash_size);
			vecs = TRS.Rotate_Z(vecs, -40.0 + Math.random() * 80.0);
			vecs = TRS.Rotate_X(vecs, player.getLocation().getPitch());
			vecs = TRS.Rotate_Y(vecs, player.getLocation().getYaw());
		}
		
		boolean init = false;
		int count = 0;
		public void run()
		{
			double angle = 150.0;
			
			if (!init)
			{
				init = true;
				skill_pos = player.getEyeLocation();
				dir = player.getLocation().getDirection().clone();
				//dir = new Vector(Math.cos(Math.toRadians(player.getLocation().getYaw())), 3d * Math.cos(Math.random() * Math.PI ), Math.sin(Math.toRadians(player.getLocation().getYaw()))).normalize().clone();
				entities = player.getWorld().getEntities();
				Make_Vecs();
				player.swingMainHand();
				player.getWorld().playSound(skill_pos, Sound.ENTITY_BLAZE_SHOOT, 1.5f, 2f);
				player.setVelocity(player.getLocation().getDirection().multiply(-0.1));
			}
			
			// 검기 그리기
			if (count++ % 2 == 0)
				player.getWorld().playSound(skill_pos, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
			for (int i = 0; i < vecs.length; i++)
			{
				Location loc = skill_pos.clone().add(vecs[i]);
				player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc, 1, 0, 0, 0, 0, Catastrophic_Slash.dtr);
			}
			
			// 범위 판정
			for (int i = 0; i < entities.size(); i++)
			{
				Entity temp = entities.get(i);
				Location loc = temp.getLocation();
				loc.subtract(skill_pos);

				if (temp == player)
					continue;
				if (temp instanceof Animals)
					continue;
				if (!(temp instanceof LivingEntity))
					continue;
					
				if(Math.sqrt(loc.getX() * loc.getX() + loc.getZ() * loc.getZ()) < (slash_size)
						&& loc.getY() < slash_size && loc.getY() > -slash_size)
				{
					double vec = dir.dot(loc.toVector().normalize());
					if(vec > Math.cos(Math.toRadians(angle / 2)))
					{
						LivingEntity temp2 = (LivingEntity)temp;
						if (!damaged_entities.contains(temp2))
						{
							if (Damage.Is_Possible(player, temp2))
							{
								Damage.SkillAttack(cast, temp2, damage, DamageType.WEAPON, DamageType.PHYSICAL, DamageType.SKILL, DamageType.PROJECTILE);
								Buff_Manager.Increase_Buff(temp2, PotionEffectType.WITHER, wither_level - 1, 200, null, wither_max - 1);
							}
							damaged_entities.add(temp2);
						}
					}
				}
			}
			
			// 잠시 쉬는 시간
			if (distance > 0)
			{
				distance -= speed;
				skill_pos.add(dir.clone().multiply(speed));
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1L);
			}
		}
	}
}
