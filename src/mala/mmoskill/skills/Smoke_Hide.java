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
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Smoke_Hide extends RegisteredSkill
{
	public Smoke_Hide()
	{	
		super(new Smoke_Hide_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("sta_heal", new LinearValue(4, 2));
		addModifier("second", new LinearValue(3.2, 0.2));
		addModifier("distance", new LinearValue(2, 0.5));
		addModifier("cooldown", new LinearValue(5, 0));
	}
}

class Smoke_Hide_Handler extends MalaSkill implements Listener
{
	public Smoke_Hide_Handler()
	{
		super(	"SMOKE_HIDE",
				"연막 은신",
				Material.FIREWORK_STAR,
				"&8{second}&7초간 연막을 펼치며 &8{sta_heal}&7의 스태미나를 회복합니다.",
				"&7추가로 자신은 10초간 투명화 상태가 됩니다.",
				"&7연막에 닿은 대상은 5초간 실명에 걸립니다.",
				"", MsgTBL.Cooldown);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		Buff_Manager.Add_Buff(data.getPlayer(), PotionEffectType.INVISIBILITY, 0, 200, null);

		double sta_heal = cast.getModifier("sta_heal");
		double second = cast.getModifier("second");
		data.giveStamina(sta_heal, UpdateReason.SKILL_REGENERATION);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Smoke_Hide_Skill(data.getPlayer().getLocation(), data.getPlayer(), 5.0, second));
	}
}

class Smoke_Hide_Skill implements Runnable
{
	World world;
	Player player;
	double radius;
	int tick;
	
	Location start_loc;
	
	public Smoke_Hide_Skill(Location _start_loc, Player _player, double _radius, double _second)
	{
		start_loc = _start_loc;
		player = _player;
		world = player.getWorld();

		tick = (int)(_second * 20);
		radius = _radius;
		
		world.playSound(player.getEyeLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 2, 1.5f);
		world.playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 2, 2);
	}
	
	double cur_angle = 0.0;
	public void run()
	{
		Location loc = start_loc.clone();
		loc.add(Math.cos(radius * 0.5 * Math.toRadians(cur_angle)), 0.7, Math.sin(radius * 0.5 * Math.toRadians(cur_angle)));
		if (tick % 10 == 0)
			world.playSound(loc, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);
		world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, (int)(radius * 7), radius * 0.5, 0.7, radius * 0.5, 0);
		
		for (Entity e : world.getNearbyEntities(start_loc, radius, radius, radius))
		{
			if (!(e instanceof LivingEntity))
				continue;
			if (e == player)
				continue;
			if (!Damage.Is_Possible(player, e))
				continue;
			LivingEntity le = (LivingEntity)e;
			if (!le.hasPotionEffect(PotionEffectType.BLINDNESS))
				Buff_Manager.Add_Buff(le, PotionEffectType.BLINDNESS, 0, 100, null);
		}
		
		
		// 마무리 전 이걸 계속 해야하나 체크
		if(tick-- <= 0)
		{
			return;
		}
					
		// 마무리
		cur_angle += 10.0;
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}

}