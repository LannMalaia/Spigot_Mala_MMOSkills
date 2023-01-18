package mala.mmoskill.skills.passive;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_BlazeWeb extends RegisteredSkill
{
	private static Invoke_BlazeWeb instance;
	
	public Invoke_BlazeWeb()
	{	
		super(new Invoke_BlazeWeb_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_base", new LinearValue(15.0, 0.0));
		addModifier("damage_add", new LinearValue(1.5, 0.0));
		addModifier("duration_base", new LinearValue(1.0, 0.0));
		addModifier("duration_add", new LinearValue(0.08, 0.0));
		
		instance = this;
	}

	public static class BlazeWebSpell extends MalaSpellEffect
	{
		DustTransition dtsReady = new DustTransition(Color.YELLOW, Color.ORANGE, 1.5f);
		DustTransition dts = new DustTransition(Color.BLACK, Color.ORANGE, 1.5f);
		double size = 10.0 / 21.0;
		double damage;
		
		HashMap<LivingEntity, Location> webbedEntities = new HashMap<LivingEntity, Location>();
		
		public BlazeWebSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 5.0);
			damage = instance.getModifier("damage_base", 1)
					+ instance.getModifier("damage_add", 1) * Cast_Ignis.getLevel(playerData);
			targetDuration = 1.0 + instance.getModifier("duration_base", 1)
					+ instance.getModifier("duration_add", 1) * Cast_Ventus.getLevel(playerData);
			size *= spellPower;
			CooldownFixer.Add_Cooldown(playerData, Cast_Ignis.getInstance(), 12.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 6.0);
		}
		public BlazeWebSpell(LivingEntity attacker, double damage, double size, double duration)
		{
			super(attacker, 5.0);
			this.damage = damage;
			this.targetDuration = duration;
			this.size = size * spellPower;
		}

		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
		}
		
		@Override
		public void whenCount() {
			double realSize = size * 21.0;
			if (durationCounter < 20) {
				if (durationCounter % 10 == 0) {
					// 마법진
					if (Particle_Manager.isReduceMode(attacker)) {
						new Effect(targetLocation, Particle.SOUL_FIRE_FLAME)
							.append2DCircle(realSize)
							.setVelocity(0, 1, 0)
							.rotatePoint(0, 0, 0)
							.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.2)
							.playEffect();
					} else {
						new Effect(targetLocation, Particle.SOUL_FIRE_FLAME)
							.setDustTransition(dtsReady)
							.append2DImage("blazeWeb.png", 5)
							.scale(size)
							.setVelocity(0, 0, 0)
							.rotatePoint(0, 0, 0)
							.playEffect();						
					}
				}
			}
			else {
				// 마법진
				if (durationCounter % 10 == 0) {
					if (Particle_Manager.isReduceMode(attacker)) {
						new Effect(targetLocation, Particle.SMALL_FLAME)
							.append2DCircle(realSize)
							.setVelocity(0, 1, 0)
							.rotatePoint(0, 0, 0)
							.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.2)
							.playEffect();
					} else {
						new Effect(targetLocation, Particle.SMALL_FLAME)
							.append2DImage("blazeWeb.png", 5)
							.scale(size)
							.setVelocity(0, 1, 0)
							.rotatePoint(0, 0, 0)
							.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.2)
							.playEffect();
						new Effect(targetLocation, Particle.SOUL_FIRE_FLAME)
							.setDustTransition(dts)
							.append2DImage("blazeWeb.png", 3)
							.scale(size)
							.setVelocity(0, 0, 0)
							.rotatePoint(0, 0, 0)
							.playEffect();
					}
				}
				if (durationCounter % 5 == 0) {
					double randYaw = Math.random() * 360.0;
					Location electricLoc = targetLocation.clone().add(
							Math.cos(Math.toRadians(randYaw)) * realSize,
							0.0,
							Math.sin(Math.toRadians(randYaw)) * realSize);

					if (!Particle_Manager.isReduceMode(attacker))
						new Effect(electricLoc, Particle.CRIT)
							.append3DLightningLine(realSize * 2.0, 7.0, 7)
							.scalePoint(1.0, 0.2, 1.0)
							.rotate(0, randYaw + 90.0, 0)
							.setVelocity(0, 0, 0)
							.playEffect();
					world.playSound(targetLocation, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 2.0f);
					
					AttackUtil.attackSphere(attacker,
							targetLocation, realSize,
							damage, (target) -> {
								if (target.isOnGround() && !webbedEntities.containsKey(target)) {
									target.sendMessage("§c거미줄에 발이 묶였다!");
									webbedEntities.put(target, target.getLocation());
								}
							}, DamageType.MAGIC, DamageType.SKILL);
				}
				
				for (LivingEntity entity : webbedEntities.keySet()) {
					entity.teleport(webbedEntities.get(entity), TeleportCause.PLUGIN);
				}
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_BlazeWeb_Handler extends MalaPassiveSkill
{
	public Invoke_BlazeWeb_Handler()
	{
		super(	"INVOKE_BLAZE_WEB",
				"마술식 - 블레이즈 웹",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_FIRE,
				"",
				"&7지정한 구역 주변 10m의 적들에게 피해를 줍니다.",
				"&7피해를 받은 적들은 해당 위치에 속박됩니다.",
				"&7공중에 떠있는 대상에게도 피해를 가하지만, 속박되지 않습니다.",
				"",
				"&f&l[ &9술식 위력 &f&l]",
				"&f&l[ &c피해량 &f{damage_base} + {damage_add} * 이그니스 &f&l]",
				"&f&l[ &b지속 시간 &f{duration_base} + {duration_add} * 벤투스 &f&l]");
	}
}

