package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import mala.mmoskill.skills.passive.Mastery_Buff;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala.mmoskill.util.RayUtil;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Protect extends RegisteredSkill
{
	public static Protect skill;
	public Protect()
	{	
		super(new Protect_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("tier", new LinearValue(1.1, 0.1, 1, 2));
		addModifier("tier2", new LinearValue(3.5, 0.5, 3, 10));
		addModifier("second", new LinearValue(15, 15, 15, 600));
		addModifier("cooldown", new LinearValue(30, 0));
		addModifier("mana", new LinearValue(33, 3));
		
		skill = this;
	}
}

class Protect_Handler extends MalaSkill implements Listener
{
	public Protect_Handler()
	{
		super(	"PROTECT",
				"프로텍트",
				Material.GUNPOWDER,
				"&7방어와 흡수 &e{tier}&7 버프를 부여합니다.",
				"&7흡수의 경우 2배 더 높은 등급이 적용됩니다.",
				"&7버프는 &e{second}&7초 간 지속합니다.",
				"&7웅크리고 있으면 자신에게 사용합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		LivingEntity target = RayUtil.getPlayer(data.getPlayer(), 25.0);
		double mana = cast.getModifier("mana");
		int tier = (int) cast.getModifier("tier") - 1;
		int second = (int) cast.getModifier("second");
		
		if (data.getPlayer().isSneaking()) {
			target = data.getPlayer();
		}
		if (target == null) {
			CooldownFixer.Initialize_Cooldown(data, Protect.skill);
			data.giveMana(mana, UpdateReason.SKILL_COST);
			return;
		}
		second *= Mastery_Buff.Get_Mult(data.getPlayer());
		Protect_Target(target, tier, second * 20);
		Aggro.Taunt_Area(data.getPlayer(), 15.0, 3);
	}
	
	public static boolean Protect_Target(LivingEntity _target, int _amp, int _ticks)
	{
		Buff_Manager.Add_Buff(_target, PotionEffectType.DAMAGE_RESISTANCE, _amp, _ticks, null);
		Buff_Manager.Add_Buff(_target, PotionEffectType.ABSORPTION, _amp * 2, _ticks, null);
		
		_target.getWorld().playSound(_target.getEyeLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new ProtectEffect(_target, 10));
		
		return true;
	}
}

class ProtectEffect implements Runnable
{
	LivingEntity target;
	int current_angle = 0;
	int additive_angle = 0;
	double current_distance = 2.0;
	double additive_distance = 0.0;
	int time = 0;
	
	public ProtectEffect(LivingEntity _target, int _time)
	{
		target = _target;
		time = _time;
		additive_angle = 90 / time;
		additive_distance = current_distance / time;
	}
	
	public void run()
	{
		for(int i = 0; i < 4; i++)
		{
			double x = Math.cos(Math.toRadians(i * 90.0 + current_angle)) * current_distance;
			double z = Math.sin(Math.toRadians(i * 90.0 + current_angle)) * current_distance;
			Location loc = target.getLocation().add(x, target.getHeight() / 2.0, z);
			target.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
		}
		current_angle += additive_angle;
		current_distance -= additive_distance;
		if(time-- > 0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 2);
	}
}