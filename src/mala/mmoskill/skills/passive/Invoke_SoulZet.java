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

public class Invoke_SoulZet extends RegisteredSkill
{
	public Invoke_SoulZet()
	{	
		super(new Invoke_SoulZet_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class SoulZetSpell extends MalaSpell
	{
		public SoulZetSpell(PlayerData playerData)
		{
			super(playerData, 2.0);
		}

		@Override
		public void whenStart() {
			world.playSound(targetLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
			Effect force = new Effect(playerCenterLocation, Particle.END_ROD);
			force.append2DCircle(5.0)
				.scalePoint(0.2)
				.scaleVelocity(0.1)
				.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
				.playEffect();
			force.setParticle(Particle.END_ROD)
				.scaleVelocity(1.5)
				.playEffect();
			force.setParticle(Particle.END_ROD)
				.scaleVelocity(1.5)
				.playEffect();
		}
		
		@Override
		public void whenCount() {
			Vector dir = frontLocation.getDirection();
			Effect zet = new Effect(playerCenterLocation.clone().add(dir.clone().multiply(-0.5)), Particle.SOUL_FIRE_FLAME);
			zet.append2DArc(120, 3.0).rotate(0.0, 180.0, 0.0)
				.append2DArc(120, 3.0).rotate(0.0, -60.0, 0.0)
				.scalePoint(0.2)
				.scaleVelocity(0.2)
				.rotate(0, durationCounter * 4.0, 0.0)
				.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
				.playEffect();
			Effect zet2 = new Effect(playerCenterLocation.clone().add(dir.clone().multiply(-0.4)), Particle.SOUL_FIRE_FLAME);
			zet2.append2DArc(120, 3.0).rotate(0.0, 180.0, 0.0)
				.append2DArc(120, 3.0).rotate(0.0, -60.0, 0.0)
				.scalePoint(0.2)
				.scaleVelocity(0.13)
				.rotate(0, 90.0 - durationCounter * 4.0, 0.0)
				.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
				.playEffect();
			Effect soulBurn = new Effect(playerCenterLocation.clone().add(dir.clone().multiply(-0.3)), Particle.SOUL);
			soulBurn.append2DCircle(3.0)
				.scalePoint(0.2)
				.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
				.setVelocity(-dir.getX(), -dir.getY(), -dir.getZ())
				.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.5)
				.playEffect();
			Effect force = new Effect(playerCenterLocation.clone().add(dir.clone().multiply(0.5)), Particle.CRIT);
			force.append2DCircle(5.0)
				.scalePoint(0.2)
				.scaleVelocity(0.3)
				.rotate(frontLocation.getPitch() - 90.0, frontLocation.getYaw(), 0.0)
				.playEffect();
			player.setVelocity(player.getLocation().getDirection().multiply(2.0));
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_SoulZet_Handler extends MalaPassiveSkill
{
	public Invoke_SoulZet_Handler()
	{
		super(	"INVOKE_SOULZET",
				"마술식 - 소울 제트",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE,
				"&e즉시 시전",
				"",
				"&7전방을 향해 영혼의 불길을 내뿜으며 돌진합니다.",
				"&7돌진 중 부딪히는 적들은 큰 피해를 입습니다.",
				"&c탑승물을 움직이지 못하며, 발동시 취소가 불가능합니다.");
	}
}

