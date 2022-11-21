package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.bukkit.Particle.DustTransition;

import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Force extends RegisteredSkill
{
	public Invoke_Force()
	{	
		super(new Invoke_Force_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class ForceSpell extends MalaSpell
	{
		Location forceLocation;
		double size = 0.5, targetSize = 10.0;
		
		public ForceSpell(PlayerData playerData)
		{
			super(playerData, 1.0);
			forceLocation = playerCenterLocation.clone();
		}

		@Override
		public void whenStart() {
			world.playSound(forceLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
			
			for (int i = 0; i < 5; i++) {
				for (int j = 0; j < 3; j++) {
					new Effect(forceLocation, Particle.CLOUD)
					.append2DArc(120.0, 2.0 + 2.0 * i + 0.3 * j)
					.scaleVelocity(0.15)
					.rotate(-15.0, Math.random() * 360.0, 0.0)
					.playEffect();
				}
			}
			new Effect(forceLocation, Particle.FIREWORKS_SPARK)
				.addSound(Sound.ENTITY_GENERIC_EXPLODE, 1.0, 2.0)
				.addSound(Sound.ITEM_TOTEM_USE, 1.5, 2.0)
				.append3DSphere(10.0, 1.3)
				.scalePoint(0.0)
				.scaleVelocity(0.2)
				.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.5, 1.2)
				.playEffect();

			world.spawnParticle(Particle.FLASH, forceLocation, 1, 0, 0, 0, 0);
			world.spawnParticle(Particle.EXPLOSION_HUGE, forceLocation, 1, 0, 0, 0, 0);
		}
		
		@Override
		public void whenCount() {
			size += (targetSize - size) * 0.1;
			new Effect(forceLocation, Particle.CRIT)
				.append2DCircle(size)
				.scaleVelocity(-0.05)
				.rotate(-30.0, 0.0, 0.0)
				.playEffect();
			new Effect(forceLocation, Particle.CRIT)
				.append2DCircle(size)
				.scaleVelocity(-0.05)
				.rotate(0.0, 0.0, 0.0)
				.playEffect();
			new Effect(forceLocation, Particle.CRIT)
				.append2DCircle(size)
				.scaleVelocity(-0.05)
				.rotate(30.0, 0.0, 0.0)
				.playEffect();
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_Force_Handler extends MalaPassiveSkill
{
	public Invoke_Force_Handler()
	{
		super(	"INVOKE_FORCE",
				"마술식 - 포스",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7세 원소를 충돌시켜 큰 압력을 지닌 폭발을 만들어냅니다.",
				"&7주변 적들을 멀리 밀어내며, 큰 피해를 줍니다.");
	}
}

