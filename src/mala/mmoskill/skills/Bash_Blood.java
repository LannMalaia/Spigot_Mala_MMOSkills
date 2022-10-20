package mala.mmoskill.skills;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Gathering_Strike;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Bash_Blood extends RegisteredSkill
{
	public Bash_Blood()
	{	
		super(new Bash_Blood_Handler(), MalaMMO_Skill.plugin.getConfig());
		
		addModifier("dam_per", new LinearValue(1.65, 0.15));
		addModifier("cooldown", new LinearValue(5, 0));
		addModifier("stamina", new LinearValue(10, 0));
	}
}

class Bash_Blood_Handler extends MalaSkill implements Listener
{
	public Bash_Blood_Handler()
	{
		super(	"BASH_BLOOD",
				"블러드 버스트",
				Material.NETHER_BRICK,
				MsgTBL.NeedSkills,
				"&e 강격 - lv.20",
				"",
				"&7혈흔을 전부 소모해 &e혈흔의 수 * {dam_per}&7%의 추가 피해를 줍니다.",
				"스킬 및 마법 피해에도 적용되나, 하나의 적에게만 추가 피해를 줍니다.",
				"",
				MsgTBL.Cooldown,
				MsgTBL.StaCost);
		;
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if(!Skill_Util.Has_Skill(data, "BASH", 20))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		int stack = Stance_Change.Get_BloodStack(data.getPlayer());
		return new SimpleSkillResult(stack > 0);
	}
	
	@EventHandler
	public void bash_attack(PlayerAttackEvent event)
	{
		if(!event.getPlayer().hasMetadata("malammo.skill.bash_blood"))
			return;

		event.getPlayer().sendMessage("§c§l[ 블러드 버스트 발동 ]");
		double per = event.getPlayer().getMetadata("malammo.skill.bash_blood").get(0).asDouble() * 0.01d;
		event.getAttack().getDamage().multiplicativeModifier(1.0 + per, DamageType.PHYSICAL);
		event.getPlayer().removeMetadata("malammo.skill.bash_blood", MalaMMO_Skill.plugin);
		

		Random rand = new Random();
		
		for(int count = 0; count < 4; count++)
		{
			Vector rand_vec = new Vector(-1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0, -1.0 + rand.nextDouble() * 2.0).normalize();
			Vector from = event.getEntity().getEyeLocation().toVector().add(rand_vec.clone().multiply(-6.0));
			Vector to = event.getEntity().getEyeLocation().toVector().add(rand_vec.clone().multiply(6.0));
	
			Location loc = from.toLocation(event.getEntity().getWorld());
			for(double i = 0.0; i < from.distance(to); i += 0.15)
			{
				loc.add(rand_vec.clone().multiply(0.1));
				loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0d, 0d, 0d, 0d, new Particle.DustOptions(Color.RED, 1));
			}
		}
		
		Location loc = event.getEntity().getEyeLocation();
		loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2f, 1.5f);
		loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0d, 0d, 0d, 0d);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double dam_per = cast.getModifier("dam_per"); // 피해 증가치
		int stack = Stance_Change.Get_BloodStack(data.getPlayer());
		dam_per *= stack;

		Stance_Change.Set_BloodStack(data.getPlayer(), 0);
		data.getPlayer().sendMessage("§b§l[ 블러드 버스트 준비 ]");
		// 효과
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
		data.getPlayer().getWorld().spawnParticle(Particle.WAX_ON, data.getPlayer().getLocation().add(0, data.getPlayer().getHeight() / 2, 0), 20, 0, 0, 0, 0);
		
		data.getPlayer().setMetadata("malammo.skill.bash_blood", new FixedMetadataValue(MalaMMO_Skill.plugin, dam_per));
	}
}
