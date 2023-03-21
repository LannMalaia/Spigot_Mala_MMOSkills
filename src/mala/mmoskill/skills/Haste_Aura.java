package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Mastery_Buff;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Haste_Aura extends RegisteredSkill
{
	public Haste_Aura()
	{	
		super(new Haste_Aura_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("cooldown", new LinearValue(50, 0));
		addModifier("mana", new LinearValue(10, 0));
	}
}

class Haste_Aura_Handler extends MalaSkill implements Listener
{
	public Haste_Aura_Handler()
	{
		super(	"HASTE_AURA",
				"가속의 장",
				Material.MUSIC_DISC_13,
				MsgTBL.NeedSkills,
				"&e 신속화 - lv.10",
				"",
				"&7자신을 포함한 주변 10m에",
				"&7신속 4, 성급함 4, 점프 강화 2를 부여합니다.",
				"&7구속, 채굴 피로에 걸려 있는 경우, 해당 디버프의 수준을 낮춥니다.",
				"&8버프는 15초간 지속됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "HASTE_SELF", 10))
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
		
		data.getPlayer().getWorld().playSound(data.getPlayer().getEyeLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 2, 2);
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new HasteAuraEffect(data.getPlayer(), 10));

		// 자신에게도 적용
		Buff_Manager.Add_Buff(data.getPlayer(), PotionEffectType.SPEED, 3, 300, PotionEffectType.SLOW);
		Buff_Manager.Add_Buff(data.getPlayer(), PotionEffectType.FAST_DIGGING, 3, 300, PotionEffectType.SLOW_DIGGING);
		Buff_Manager.Add_Buff(data.getPlayer(), PotionEffectType.JUMP, 1, 300, null);
		
		for(Entity e : data.getPlayer().getNearbyEntities(10, 10, 10))
		{
			if(e instanceof Player)
			{
				e.sendMessage("§b" + data.getPlayer().getDisplayName() + "§b님의 가속장으로 몸이 빨라졌습니다.");
				Buff_Manager.Add_Buff((Player)e, PotionEffectType.SPEED, 3, 300, PotionEffectType.SLOW);
				Buff_Manager.Add_Buff((Player)e, PotionEffectType.FAST_DIGGING, 3, 300, PotionEffectType.SLOW_DIGGING);
				Buff_Manager.Add_Buff((Player)e, PotionEffectType.JUMP, 1, 300, null);
			}
		}
	}
}

class HasteAuraEffect implements Runnable
{
	Player player;
	int current_angle = 0;
	int additive_angle = 18;
	double distance = 4.0;
	double upper = 0.0;
	int time = 0;
	Location loc;
	
	public HasteAuraEffect(Player _player, double _distance)
	{
		player = _player;
		loc = player.getLocation().add(0, player.getHeight() * 0.5, 0).clone();
		time = 10;
		distance = _distance;
	}
	
	public void run()
	{
		for(int i = 0; i < 360; i += 15)
		{
			double x = Math.cos(Math.toRadians(i + current_angle)) * distance;
			double z = Math.sin(Math.toRadians(i + current_angle)) * distance;
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, upper, z), 1, 0, 0, 0, 0);
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, -upper, z), 1, 0, 0, 0, 0);

			x = Math.cos(Math.toRadians(i - current_angle)) * distance;
			z = Math.sin(Math.toRadians(i - current_angle)) * distance;
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, upper, z), 1, 0, 0, 0, 0);
			player.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(x, -upper, z), 1, 0, 0, 0, 0);
		}
		upper += 0.3;
		current_angle += additive_angle;
		player.getWorld().playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 2, 2f);
		if(time-- > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}
