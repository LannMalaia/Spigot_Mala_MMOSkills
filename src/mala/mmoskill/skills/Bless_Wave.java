package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Mastery_Buff;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Bless_Wave extends RegisteredSkill
{
	public Bless_Wave()
	{	
		super(new Bless_Wave_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("distance", new LinearValue(3.75, 0.75, 2, 20));
		addModifier("cooldown", new LinearValue(60, 0));
		addModifier("mana", new LinearValue(44, 4));
	}
}

class Bless_Wave_Handler extends MalaSkill implements Listener
{
	public Bless_Wave_Handler()
	{
		super(	"BLESS_WAVE",
				"에리어 블레스",
				Material.MUSIC_DISC_STRAD,
				"&7자신을 포함한 주변 &8{distance}&7 거리에 블레스를 부여합니다.",
				"&7이 때 효과는 기존 블레스를 따릅니다.",
				"&8블레스를 배우지 않았다면 사용할 수 없습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "BLESS", 1))
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
		
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("BLESS");

		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 2, 2);
		
		int lv = data.getSkillLevel(skill);
		int amp = (int)(skill.getModifier("tier", lv)) - 1;
		int tick = (int)(skill.getModifier("second", lv) * 20 * Mastery_Buff.Get_Mult(data.getPlayer()));
		double distance = cast.getModifier("distance");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new BlessWaveEffect(data.getPlayer(), distance));

		// 자신에게도 적용
		Bless_Handler.Bless_Target(data.getPlayer(), amp, tick);
		
		for(Entity e : data.getPlayer().getNearbyEntities(distance, distance, distance))
		{
			if (e instanceof Player)
			{
				e.sendMessage("§b" + data.getPlayer().getDisplayName() + "§b님으로부터 축복을 부여받았습니다.");
				Bless_Handler.Bless_Target((Player)e, amp, tick);
			}
		}
		
		Aggro.Add_Threat_Area(data.getPlayer(), distance * 1.5, 5.0);
	}
}

class BlessWaveEffect implements Runnable
{
	Player player;
	int current_angle = 0;
	int additive_angle = 18;
	double distance = 4.0;
	double upper = 0.0;
	int time = 0;
	Location loc;
	
	public BlessWaveEffect(Player _player, double _distance)
	{
		player = _player;
		loc = player.getLocation().add(0, player.getHeight() * 0.5, 0).clone();
		time = 10;
		distance = _distance;
	}
	
	public void run()
	{
		for(int i = 0; i < 360; i += 30)
		{
			double x = Math.cos(Math.toRadians(i + current_angle)) * distance;
			double z = Math.sin(Math.toRadians(i + current_angle)) * distance;
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, 3 - upper, z), 1, 0, 0, 0, 0);
			x = Math.cos(Math.toRadians(i - current_angle)) * distance;
			z = Math.sin(Math.toRadians(i - current_angle)) * distance;
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, 3 - upper, z), 1, 0, 0, 0, 0);

			x = Math.cos(Math.toRadians(i + current_angle)) * distance;
			z = Math.sin(Math.toRadians(i + current_angle)) * distance;
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, -3 + upper, z), 1, 0, 0, 0, 0);
			x = Math.cos(Math.toRadians(i - current_angle)) * distance;
			z = Math.sin(Math.toRadians(i - current_angle)) * distance;
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, -3 + upper, z), 1, 0, 0, 0, 0);
		}
		for(int i = 0; i < 360; i += 10)
		{
			double x = Math.cos(Math.toRadians(i + current_angle)) * distance;
			double z = Math.sin(Math.toRadians(i + current_angle)) * distance;
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, 3, z), 1, 0, 0, 0, 0);
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, 0, z), 1, 0, 0, 0, 0);
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, -3, z), 1, 0, 0, 0, 0);
		}

		player.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2, 1.5f);
		upper += 0.3;
		current_angle += additive_angle;
		if(time-- > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}