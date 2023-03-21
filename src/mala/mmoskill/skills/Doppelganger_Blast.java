package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;


public class Doppelganger_Blast extends RegisteredSkill
{
	public Doppelganger_Blast()
	{	
		super(new Doppelganger_Blast_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(220, 20));
		addModifier("cooldown", new LinearValue(30, 0));
		addModifier("stamina", new LinearValue(5, 0));
	}
}

class Doppelganger_Blast_Handler extends MalaSkill implements Listener
{
	public Doppelganger_Blast_Handler()
	{
		super(	"DOPPELGANGER_BLAST",
				"분신 폭발",
				Material.COBWEB,
				MsgTBL.NeedSkills,
				"&e 그림자 분신술 - lv.10",
				"",
				MsgTBL.PHYSICAL + MsgTBL.SKILL,
				"",
				"&7생성한 분신들을 차례대로 폭파시킵니다.",
				"&7폭파시 주변 대상들은 &8{damage}&7의 피해를 입습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}
	
	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "DOPPELGANGER", 10))
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
		
		double damage = cast.getModifier("damage");
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Doppel_Blast_Task(cast, data.getPlayer(), damage));
	}
}

class Doppel_Blast_Task implements Runnable
{
	SkillMetadata cast;
	Player player;
	double damage;
	double radius = 2;
	
	public Doppel_Blast_Task(SkillMetadata cast, Player _player, double _damage)
	{
		this.cast = cast;
		player = _player;
		damage = _damage;
	}
	
	public void run()
	{
		Summon_Manager sm = Summon_Manager.Get_Instance();
		ArrayList<Summoned_OBJ> list = sm.Get_Summoned_OBJs(player, "Doppelganger");
		if (list.size() == 0)
			return;
		Summoned_OBJ so = list.get(0);
		
		so.entity.getWorld().playSound(so.entity.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
		so.entity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, so.entity.getLocation(), 80, radius, radius, radius);
		for (Entity e : so.entity.getNearbyEntities(radius, radius, radius))
		{
			if (e == player)
				continue;
			if (!(e instanceof LivingEntity))
				continue;
			
			LivingEntity le = (LivingEntity)e;
			le.setNoDamageTicks(0);
			Damage.SkillAttack(cast, le, damage, DamageType.PHYSICAL, DamageType.SKILL);
		}
		
		so.Remove();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 3);
	}
}
