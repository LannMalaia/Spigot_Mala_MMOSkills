package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Suicide_Bomb extends RegisteredSkill
{
	public Suicide_Bomb()
	{	
		super(new Suicide_Bomb_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(4, 0.2));
		addModifier("cooldown", new LinearValue(600, -10));
	}
}

class Suicide_Bomb_Handler extends MalaSkill implements Listener
{
	public Suicide_Bomb_Handler()
	{
		super(	"SUICIDE_BOMB",
				"ȭ���� ������",
				Material.TNT,
				MsgTBL.SKILL,
				"",
				"&75�� ��, &8{distance}&7m �� �ڽ��� ������ �ֺ� ��ο���",
				"&8�ڽ��� �ִ� HP�� 2��&7��ŭ�� &f���� ����&7�� �ݴϴ�.",
				"&7�ڽ� ���� ���� ���ظ� �Ա� ������ ������,",
				"&730%�� Ȯ���� 1�� HP�� �����ϸ� ��Ƴ��� �� �ֽ��ϴ�.",
				"&7�� �� �����ϸ� ���� �ʴ� �� ��Ұ� �Ұ����մϴ�.",
				"&c�� ���ش� ����� ��ų ���ط� ���ʽ��� ���� �ʽ��ϴ�.",
				"",
				MsgTBL.Cooldown);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double distance = cast.getModifier("distance");
		double damage = data.getStats().getStat("MAX_HEALTH") * 2.0;
		double sec = 5.0;
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Suicide_Bomb_Skill(data.getPlayer(), damage, distance, sec));
	}
	
	class Suicide_Bomb_Skill implements Runnable
	{
		Player player;
		double damage;
		double distance;
		double sec;
		Location loc;
		
		double cur_distance = 0.0;
		double cur_sec = 0.0;
		int count = 0;
		
		public Suicide_Bomb_Skill(Player _player, double _damage, double _distance, double _sec)
		{
			player = _player; damage = _damage; distance = _distance; sec = _sec;
			loc = player.getLocation();
			
			for (Entity e : player.getNearbyEntities(distance, distance, distance))
			{
				if (e instanceof Player)
					((Player)e).sendMessage("��c��l[ " + player.getDisplayName() + "��c��l���� ������ �غ��ϰ� �ֽ��ϴ�...!! ]");
			}
		}
		
		public void run()
		{
			if (player.isDead())
				return;
			
			World world = player.getWorld();
			for (double angle = 0.0; angle <= 360.0; angle += 30.0 / cur_distance)
			{
				Location loc = player.getLocation().add(Math.cos(Math.toRadians(angle)) * cur_distance, 0.7, Math.sin(Math.toRadians(angle)) * cur_distance);
				world.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
				world.spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, 0);
			}
			
			if (count++ % 20 == 0)
				world.playSound(player.getEyeLocation(), Sound.ENTITY_TNT_PRIMED, 2.0f, 1.0f);
			
			cur_distance = cur_distance + (distance - cur_distance) * 0.1;
			
			if (sec <= 0.0)
			{
				world.spawnParticle(Particle.EXPLOSION_HUGE, loc, (int)distance * 10, distance, distance, distance, 0);
				world.playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
				world.playSound(player.getEyeLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 2.0f, 1.0f);
				
				for (Entity en : player.getNearbyEntities(distance, distance, distance))
				{
					if (!(en instanceof LivingEntity))
						continue;
					if (en == player)
						continue;
					
					LivingEntity le = (LivingEntity)en;
					EntityDamageEvent ede = new EntityDamageByEntityEvent(player, le, DamageCause.ENTITY_ATTACK, damage);
					Bukkit.getPluginManager().callEvent(ede);
					if (!ede.isCancelled())
						le.setHealth(Math.max(0.0, le.getHealth() - damage));
					
				}
				EntityDamageEvent ede = new EntityDamageEvent(player, DamageCause.SUICIDE, damage);

				Bukkit.getPluginManager().callEvent(ede);
				if (!ede.isCancelled())
				{
					if (Math.random() <= 0.3)
						player.setHealth(1.0);
					else
						player.setHealth(0.0);
				}
					
				return;
			}
			sec -= 0.05;
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
