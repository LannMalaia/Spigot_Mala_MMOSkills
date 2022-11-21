package mala.mmoskill.skills.passive;

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

public class Invoke_Barrier extends RegisteredSkill
{
	public Invoke_Barrier()
	{	
		super(new Invoke_Barrier_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class BarrierSpell extends MalaSpell
	{
		DustTransition dts = new DustTransition(Color.fromRGB(200, 200, 255), Color.fromRGB(255, 255, 255), 0.5f);
		double size = 1.0 / 12.0, targetSize = 10.0 / 12.0;
		Location circleLocation;
		Vector[] circleAngles;
		
		public BarrierSpell(PlayerData playerData)
		{
			super(playerData, 13.0);
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
			if (durationCounter % 10 == 0)
				new Effect(circleLocation, Particle.SOUL_FIRE_FLAME)
					.append2DImage("barrierCircle.png", 2)
					.scale(size)
					.setVelocity(0, 0, 0)
					.rotatePoint(0, durationCounter * 0.4, 0)
					.addSound(Sound.ENTITY_PLAYER_LEVELUP, 2.0, 1.2)
					.playEffect();
			
			double realSize = size * 12.0;
			// 마법진 끄트머리
			new Effect(circleLocation, Particle.SOUL_FIRE_FLAME)
				.append2DCircle(realSize)
				.setVelocity(0, 1, 0)
				.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.3)
				.playEffect();
			
			for (int i = 0; i < circleAngles.length; i++) {
				circleAngles[i].add(new Vector(1.3, 3.7, 5.6));
				new Effect(circleLocation, Particle.END_ROD)
					.append2DCircle(realSize)
					.setVelocity(0, 0, 0)
					.rotatePoint(circleAngles[i].getX(), circleAngles[i].getY(), circleAngles[i].getZ())
					.playEffect();
			}
			
			size = size + (targetSize - size) * 0.05;
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_Barrier_Handler extends MalaPassiveSkill
{
	public Invoke_Barrier_Handler()
	{
		super(	"INVOKE_BARRIER",
				"마술식 - 배리어",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7주변 10m내의 아군이 받는 피해를 크게 줄입니다.",
				"&7피해를 막을 때마다 일정량의 마나를 회복합니다.",
				"&7움직일 경우 시전이 취소됩니다.");
	}
}

