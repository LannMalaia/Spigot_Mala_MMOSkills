package mala.mmoskill.skills.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;

import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_Flame extends RegisteredSkill
{
	public Invoke_Flame()
	{	
		super(new Invoke_Flame_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class FlameSpell extends MalaSpell
	{
		public FlameSpell(PlayerData playerData)
		{
			super(playerData, 5.0);
		}
		
		double height = 0.5;
		
		@Override
		public void whenStart() {
			// world.playSound(targetLocation, Sound.ENTITY_WITHER_SHOOT, 1.0f, 0.4f);
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 0.8f);
//			Particle_Drawer_Expand.drawCircle(targetLocation, Particle.FLAME, 3.0, 0.0, 0.0, 0, 0, 0.15);
//			Particle_Drawer_Expand.drawTriangle(targetLocation, Particle.FLAME, 3.0, 0.0, 0.0, 0, 0.15);
		}
		
		@Override
		public void whenCount() {
			if (this.durationCounter < 40)
			{
				if (this.durationCounter % 3 == 0)
				{
					Particle_Drawer_EX.drawCircle(targetLocation, Particle.SMALL_FLAME, 5.0,
							0.0, 0.0,
							0, this.durationCounter * 0.5, 0.005);
					Particle_Drawer_EX.drawTriangle(targetLocation, Particle.SMALL_FLAME, 5.0,
							0.0, 0.0,
							this.durationCounter * 0.5, 0.005);
					Particle_Drawer_EX.drawTriangle(targetLocation, Particle.SMALL_FLAME, 5.0,
							0.0, 180.0,
							this.durationCounter * 0.5, 0.005);
				}
			}
			else
			{
				height = Math.min(15.0, height + 0.7);
				if (this.durationCounter % 6 <= 2)
					world.playSound(targetLocation, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.6f);

				for (double d = (this.durationCounter % 10) * 0.15; d <= height; d += 1.5)
				{
					Location loc = targetLocation.clone().add(0, d, 0);
					Particle_Drawer_Expand.drawCircleRandomize(loc, Particle.FLAME, 1.0,
							0.0, 0.0,
							2, this.durationCounter * 5.0 + d * 5.0, 0.6);
				}
				for (double d = 0; d <= height; d += 2.0)
				{
					Location loc = targetLocation.clone().add(0, d, 0);
					Particle_Drawer_EX.drawCircleUpRandomize(loc, Particle.FLAME, 4.5,
							0.0, 0.0,
							6, this.durationCounter * 5.0 + d * 5.0, 1.0);
					Particle_Drawer_EX.drawCircleUp(loc, Particle.FLAME, 5.0,
							0.0, 0.0,
							6, this.durationCounter * 5.0 + d * 5.0, 0.8);
				}
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_Flame_Handler extends MalaPassiveSkill
{
	public Invoke_Flame_Handler()
	{
		super(	"INVOKE_FLAME",
				"마술식 - 플레임",
				Material.WRITABLE_BOOK,
				"§92단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE,
				"",
				"&7바라보는 위치에 5m 크기의 불기둥을 일으킵니다.",
				"&7불길에 휘말린 적들은 피해를 입고 발화 상태가 되며, 위로 떠오릅니다.");
	}
}

