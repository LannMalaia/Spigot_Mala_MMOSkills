package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import io.lumine.mythic.lib.skill.result.def.TargetSkillResult;
import laylia_core.main.Damage;
import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.skills.passive.Mastery_Fire;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.MalaTargetSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class HellFire extends RegisteredSkill
{
	public HellFire()
	{	
		super(new HellFire_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage", new LinearValue(0.7, 0.2));
		addModifier("cooldown", new LinearValue(120, 0));
		addModifier("mana", new LinearValue(10, 0));
	}
}

class HellFire_Handler extends MalaTargetSkill implements Listener
{
	public HellFire_Handler()
	{
		super(	"HELLFIRE",
				"헬파이어",
				Material.REDSTONE,
				MsgTBL.SKILL + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC,
				"",
				"&720m내의 대상에게 모든 마나를 집중시켜 폭발을 일으킵니다.",
				"&7대상은 소모한 마나 * &8{damage}&7 만큼의 피해를 받습니다.",
				"&7시전 이후, 5초동안 마나가 0으로 고정됩니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost_All);
		range = 20.0;
	}
	
	@Override
	public void whenCast(TargetSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double mana = data.getMana();
		data.setMana(0.0);
		
		double damage = cast.getModifier("damage");
		damage *= Mastery_Fire.Get_Mult(data.getPlayer());
		damage *= mana;

		DamageMetadata dm = new DamageMetadata(damage);
		Bukkit.getPluginManager().callEvent(new FireMagicEvent(data.getPlayer(), dm));
		damage = dm.getDamage();
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Hell_Fire_Task(data, _data.getTarget(), damage));
	}
}

class Hell_Fire_Task implements Runnable
{
	PlayerData player;
	LivingEntity target;
	double damage;
	
	double timer = 3.0;
	double mana_burn_timer = 5.0;
	boolean damaged = false;
	int counter = 0;
	World world;
	
	public Hell_Fire_Task(PlayerData _player, LivingEntity _target, double _damage)
	{
		player = _player;
		target = _target;
		damage = _damage;
		world = target.getWorld();
	}
	
	public void run()
	{
		timer -= 0.25;
		mana_burn_timer -= 0.25;
		Location loc = target.getLocation();
		loc.add(0.0, target.getHeight() * 0.5, 0.0);
		
		if (!damaged)
		{
			if (timer < 0.0)
			{
				damaged = true;
				for (int i = 0; i < 8; i++)
				{
					Vector dir = new Vector(-1.0 + Math.random() * 2.0, -1.0 + Math.random() * 2.0, -1.0 + Math.random() * 2.0);
					Location start = loc.clone().add(dir.clone().multiply(-7.0));
					Location end = loc.clone().add(dir.clone().multiply(7.0));
					Particle_Drawer.Draw_Line(start, end, Particle.FLAME, 0.1);
					Particle_Drawer.Draw_Line(start, end, Particle.LAVA, 0.2);
				}
				target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE,
						target.getLocation(), (int)(target.getHeight() * 40), target.getHeight(), target.getHeight(), target.getHeight(), 0);
				Particle_Drawer.Draw_Circle(loc, Particle.EXPLOSION_LARGE, target.getHeight() * 0.5);
				world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 2.0f, 1.2f);
				world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.0f);
				world.playSound(loc, Sound.ENTITY_BLAZE_DEATH, 2.0f, 1.2f);
				
				Damage.Attack(player.getPlayer(), target, damage, DamageType.SKILL, DamageType.MAGIC);
			}
			else
			{
				Particle_Drawer.Draw_Circle(loc, Particle.ENCHANTMENT_TABLE, timer + 1.0);
				world.playSound(loc, Sound.ENTITY_BLAZE_AMBIENT, 1.0f, 0.5f);
			}
		}
		if (mana_burn_timer > 0.0)
			player.setMana(0.0);
		
		if (mana_burn_timer > 0.0 || timer > 0.0)
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 5);
	}
}