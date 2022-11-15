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

public class Invoke_SoulFlare extends RegisteredSkill
{
	public Invoke_SoulFlare()
	{	
		super(new Invoke_SoulFlare_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class SoulFlareSpell extends MalaSpell
	{
		public SoulFlareSpell(PlayerData playerData)
		{
			super(playerData, 5.0);
		}

		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
			
			Particle_Drawer_EX.drawCircleUpRandomize(playerCenterLocation, Particle.SOUL_FIRE_FLAME, 5.0,
					0.0, 0.0,
					0, this.durationCounter * 0.3, 0.5);
			Particle_Drawer_EX.drawSquare(playerCenterLocation, Particle.SOUL_FIRE_FLAME, 5.0,
					0.0, 0.0,
					this.durationCounter * 0.3, 0.0);
			Particle_Drawer_EX.drawSquare(playerCenterLocation, Particle.SOUL_FIRE_FLAME, 5.0,
					0.0, 45.0,
					this.durationCounter * 0.3, 0.0);
		}
		
		@Override
		public void whenCount() {
			if (durationCounter % 3 == 0) {
				for (int i = 0; i < 1; i++) {
					Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Runnable() {
						Location loc = playerCenterLocation.clone();
						Vector dir = new Vector(-1.0 + Math.random() * 2.0, 0.0, -1.0 + Math.random() * 2.0).normalize();
						double size = 0.3;
						double speed = 1.0;
						double targetDistance = 10.0;
						int count = 0;
						
						@Override
						public void run() {
							// 불 솟는 효과
							for (int i = 0; i < 3; i++)
							{
								Effect fire = new Effect(loc, Particle.SOUL_FIRE_FLAME);
								if (i == 0 && count % 6 == 0)
									fire.addSound(Sound.BLOCK_LAVA_EXTINGUISH, 0.7, 1.6);
								fire.append2DCircle(size * (0.4 + i * 0.3))
									.setVelocity(0.0, 1.0, 0.0)
									.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 1.0 - i * 0.3)
									.playEffect();
							}
							// 폭발 연기
							for (int i = 0; i < 3; i++)
							{
								Effect impact = new Effect(loc, Particle.SOUL);
								impact.append2DArc(240, size * (0.5 + i * 0.5))
									.rotate(-10.0, Math.random() * 360.0, 0.0)
									.scaleVelocity(0.1)
									.playAnimation(4);
							}
							world.spawnParticle(Particle.EXPLOSION_LARGE, loc, 1, 0, 0, 0, 0);
							// world.spawnParticle(Particle., loc, 5, 0.5, 0.5, 0.5, 0);
							
							size = Math.min(3.0, size + 0.05);
							count++;
							loc.add(dir.clone().multiply(speed));
							targetDistance -= speed;
							if (targetDistance < 0)
								return;
							
							Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
						}
					});
				}
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_SoulFlare_Handler extends MalaPassiveSkill
{
	public Invoke_SoulFlare_Handler()
	{
		super(	"INVOKE_SOULFLARE",
				"마술식 - 소울 플레어(미구현)",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE,
				"",
				"&7주변 무작위 방향으로 얼음의 길을 여러 번 발사합니다.",
				"&7구속에 걸린 적에게 큰 피해를 줍니다.");
	}
}

