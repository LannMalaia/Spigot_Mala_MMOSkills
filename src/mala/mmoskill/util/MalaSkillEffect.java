package mala.mmoskill.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import laylia_core.main.CooldownManager;
import mala.mmoskill.events.SpellCastEvent;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.api.player.PlayerData;

public abstract class MalaSkillEffect implements Runnable
{
	protected LivingEntity attacker;
	
	protected PlayerData playerData;
	protected Location centerLocation;
	protected Location frontLocation;
	protected Location targetLocation;
	protected World world;
	protected double targetDuration;
	
	protected int durationCounter = 0;
	public double spellPower = 1.0;
	public boolean cancelled = false;

	public MalaSkillEffect(Player player, double duration, String cooldownName, double cooldown)
	{
		this(player, duration);
	}
	public MalaSkillEffect(LivingEntity attacker, double duration)
	{
		this.attacker = attacker;
		this.centerLocation = attacker.getLocation().add(0.0, 0.5, 0.0);
		this.frontLocation = attacker.getEyeLocation().add(attacker.getEyeLocation().getDirection().multiply(2.0));
		this.targetLocation = RayUtil.getLocation(attacker).add(0.0, 0.1, 0.0);
		this.world = targetLocation.getWorld();
		this.targetDuration = duration;
		
		if (attacker instanceof Player)
			this.playerData = PlayerData.get((Player)attacker);
	}
	
	@Override
	public void run()
	{
		if (cancelled) {
			attacker.sendMessage("§c시전중이던 스킬이 취소됐습니다.");
			return;
		}
		
		this.centerLocation = attacker.getLocation().add(0.0, 0.5, 0.0);
		this.frontLocation = attacker.getEyeLocation().add(attacker.getEyeLocation().getDirection().multiply(2.0));
		if (durationCounter == 0) {
			whenStart();
		}
		
		durationCounter += 1;
		if (durationCounter > targetDuration * 20) {
			whenEnd();
			return;
		}
		
		whenCount();
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
	public abstract void whenStart();
	public abstract void whenCount();
	public abstract void whenEnd();
}
