package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_BlazeWeb extends RegisteredSkill
{
	public Invoke_BlazeWeb()
	{	
		super(new Invoke_BlazeWeb_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class BlazeWebSpell extends MalaSpell
	{
		DustTransition dts = new DustTransition(Color.BLACK, Color.ORANGE, 1.5f);
		double size = 13.0 / 21.0;
		Location circleLocation;
		Vector[] circleAngles;
		
		public BlazeWebSpell(PlayerData playerData)
		{
			super(playerData, 5.0);
			circleLocation = player.getLocation().add(0, .1, 0);
			circleAngles = new Vector[3];
			for (int i = 0; i < circleAngles.length; i++)
				circleAngles[i] = new Vector(Math.random() * 360.0, Math.random() * 360.0, Math.random() * 360.0);
		}

		@Override
		public void whenStart() {
			world.playSound(playerCenterLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
		}
		
		@Override
		public void whenCount() {
			// 마법진
			if (durationCounter % 10 == 0) {
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
			if (durationCounter % 5 == 0) {
				double realSize = size * 21.0;
				double randYaw = Math.random() * 360.0;
				Location electricLoc = targetLocation.clone().add(
						Math.cos(Math.toRadians(randYaw)) * realSize,
						0.0,
						Math.sin(Math.toRadians(randYaw)) * realSize);
				new Effect(electricLoc, Particle.CRIT)
					.append3DLightningLine(realSize * 2.0, 7.0, 7)
					.scalePoint(1.0, 0.2, 1.0)
					.rotate(0, randYaw + 90.0, 0)
					.setVelocity(0, 0, 0)
					.playEffect();
				world.playSound(targetLocation, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 2.0f);
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
				"&7주변 10m내의 적들을 잠시 움직이지 못하게 합니다.");
	}
}

