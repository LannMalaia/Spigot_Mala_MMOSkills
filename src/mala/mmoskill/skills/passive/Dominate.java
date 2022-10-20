package mala.mmoskill.skills.passive;

import java.util.StringTokenizer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerCombatEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaPassiveSkill;

public class Dominate extends RegisteredSkill
{
	public Dominate()
	{	
		super(new Dominate_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(10, 10, 0, 600));
	}
}

class Dominate_Handler extends MalaPassiveSkill implements Listener
{
	public Dominate_Handler()
	{
		super(	"DOMINATE",
				"제압의 일격",
				Material.KELP,
				"&7첫 공격이 {per}%의 추가 피해를 줍니다.",
				"&75초의 재사용 대기시간이 있습니다.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void passive_dominate_attack(PlayerAttackEvent event)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(event.getPlayer());
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("DOMINATE");
		int level = data.getSkillLevel(skill);
		
		 // 스킬을 알고 있지 않으면 취소
		if(!data.getProfess().hasSkill(skill))
			return;
		
		if (data.getPlayer().hasMetadata("malammo.skill.dominate_use"))
			return;
		
		data.getPlayer().setMetadata("malammo.skill.dominate_use", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, new Runnable()
		{
			@Override
			public void run() {
				data.getPlayer().removeMetadata("malammo.skill.dominate_use", MalaMMO_Skill.plugin);
			}
		}, 100);
		
		// 이전에 공격 했나 확인
		String namelist = "";
		if(event.getEntity().hasMetadata("malammo.skill.dominate"))
		{
			namelist = event.getEntity().getMetadata("malammo.skill.dominate").get(0).asString();
			StringTokenizer st = new StringTokenizer(namelist, "%%");
			while(st.hasMoreTokens())
			{
				if(st.nextToken().equals(data.getPlayer().getName()))
					return;
			}
			namelist += "%%";
		}
		namelist += data.getPlayer().getName();
		event.getEntity().setMetadata("malammo.skill.dominate", new FixedMetadataValue(MalaMMO_Skill.plugin, namelist));
		
		double per = skill.getModifier("per", level) * 0.01d;
		if(Math.random() > per)
			return;
		
		Location loc = event.getEntity().getEyeLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1, 2.0f);
		loc.getWorld().spawnParticle(Particle.CRIT, loc, 25, 0.3, 0.3, 0.3, 0);
		
		// 버프거는 일격 확인용
		if (level >= 20)
		{
			RegisteredSkill skill_2 = MMOCore.plugin.skillManager.getSkill("BLIND_DOMINATION");
			if(!data.getProfess().hasSkill(skill_2))
				return;
			int level_2 = data.getSkillLevel(skill_2);
			
			int ticks = (int)(skill_2.getModifier("second", level_2) * 20);
			Buff_Manager.Add_Buff(event.getEntity(), PotionEffectType.BLINDNESS, 0, ticks, null);
			
			skill_2 = MMOCore.plugin.skillManager.getSkill("BIND_DOMINATION");
			level_2 = data.getSkillLevel(skill_2);
			if (!data.getProfess().hasSkill(skill_2))
				return;
			
			ticks = (int)(skill_2.getModifier("second", level_2) * 20);
			Buff_Manager.Add_Buff(event.getEntity(), PotionEffectType.SLOW, 1, ticks, PotionEffectType.SPEED);
			
			skill_2 = MMOCore.plugin.skillManager.getSkill("ROSE_DOMINATION");
			level_2 = data.getSkillLevel(skill_2);
			if (!data.getProfess().hasSkill(skill_2))
				return;
			
			ticks = (int)(skill_2.getModifier("second", level_2) * 20);
			Buff_Manager.Add_Buff(event.getEntity(), PotionEffectType.WEAKNESS, 0, ticks, PotionEffectType.INCREASE_DAMAGE);
			Buff_Manager.Add_Buff(event.getEntity(), PotionEffectType.WITHER, 0, ticks, null);
		}
		// 사각 저격 확인
		if (event.getDamage().hasType(DamageType.PROJECTILE)
			&& Critical_Domination.Can_Critical(data.getPlayer()))
		{
			event.getDamage().multiplicativeModifier(1.5);
		}
		
		event.getDamage().multiplicativeModifier(1.0 + per);
	}

	@EventHandler
	public void passive_dominate_dead(PlayerCombatEvent event)
	{
		if(!event.entersCombat())
			event.getPlayer().removeMetadata("malammo.skill.dominate", MalaMMO_Skill.plugin);
	}
}
