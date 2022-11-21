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
import mala.mmoskill.util.Particle_Drawer_EX;
import mala.mmoskill.util.Particle_Drawer_Expand;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_MasterSpark extends RegisteredSkill
{
	public Invoke_MasterSpark()
	{	
		super(new Invoke_MasterSpark_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class MasterSparkSpell extends MalaSpell
	{
		public MasterSparkSpell(PlayerData playerData)
		{
			super(playerData, 10.0);
		}
		
		Location tempLocation;
		double length = 50.0;
		double height = 0.5;
		
		@Override
		public void whenStart() {
			tempLocation = this.frontLocation.clone();
			// ������
			new Effect(tempLocation, Particle.FIREWORKS_SPARK)
				.append2DImage("mastersparkCircle.png", 2)
				.scalePoint(0.0)
				.scaleVelocity(0.08)
				//.rotate(0, durationCounter, 0)
				.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0.0)
				.playEffect();
		}
		
		@Override
		public void whenCount() {
			if (durationCounter <= 60) {
				if (durationCounter % 20 == 0) {
				int count = durationCounter / 20;
				// ���� ����
				Effect beam = new Effect(tempLocation, Particle.END_ROD);
				for (double len = 0; len <= length; len += 0.1) {
					beam.append2DArc(3.0, 2.0 + 0.5 * count).rotate(0, 180, 0)
						.append2DArc(3.0, 2.0 + 0.5 * count).rotate(0, 180, 0)
						.rotate(0, 3.0 * (count % 2 == 0 ? -1 : 1), 0)
						.translatePoint(0, 0.1, 0);
				}
				beam.scalePoint(0.0, 1.0, 0.0)
					.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
					.scaleVelocity(0.15)
					.reverse()
					.addSound(Sound.ENTITY_TNT_PRIMED, 0.5 + 0.3 * count, 1.0 + 0.15 * count)
					.playAnimation(40 + 20 * count);
				}
				
			}
			if (durationCounter == 100) {
				new Effect(tempLocation, Particle.END_ROD)
					.addSound(Sound.ITEM_TOTEM_USE, 2.0, 2.0)
					.playEffect();
				// ������
				new Effect(tempLocation, Particle.SOUL_FIRE_FLAME)
					.append2DImage("mastersparkCircle.png", 2)
					.scalePoint(0.0)
					.scaleVelocity(0.08)
					//.rotate(0, durationCounter, 0)
					.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0.0)
					.playEffect();
			}
			if (durationCounter >= 100) {
				// �߻� ����Ʈ
				for (int i = 0; i < 1; i++) {
					new Effect(tempLocation, Particle.END_ROD)
						.append2DCircle(4.0 + 1.5 * i)
						.scale(0.2)
						.scaleVelocity(0.7)
						.translateVelocity(0, 0.8 + 0.2 * i, 0)
						.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.75, 1.0)
						.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
						.playEffect();
//					new Effect(tempLocation.clone().add(tempLocation.getDirection().clone().multiply(3.0)), Particle.END_ROD)
//					.append2DCircle(5.5 + 1.5 * i)
//					.scale(0.1)
//					.translateVelocity(0, 0.8 + 0.2 * i, 0)
//					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.95, 1.0)
//					.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
//					.playEffect();
				}
				// ��
				if (durationCounter % 2 == 0) {
					for (double d = 0.5; d <= length; d += 2.0) {
						double vel = Math.cos(Math.min(Math.PI * 0.5, d * 0.25)) * 0.2;
						new Effect(tempLocation, Particle.END_ROD)
							.append2DCircle(0.2 + (2.8 * Math.sin(Math.min(Math.PI * 0.5, d * 0.25))))
							.translatePoint(0, d, 0)
							.scaleVelocity(vel)
							.translateVelocity(0, 1.0, 0)
							.randomizePoint(RANDOMIZE_TYPE.MULTIPLY, 1.0, 0.7, 1.0, 1.0, 1.0, 1.0)
							.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 1.0)
							.rotate(0, Math.random() * 360.0, 0)
							.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
							.playEffect();
					}
				}
				// �� ����
				for (int i = 0; i < 4; i++) {
					Effect ring = new Effect(tempLocation, Particle.CRIT)
						.append2DCircle(4.0)
						.translatePoint(0, (durationCounter + i * (length / 4.0)) % length, 0)
						.rotate(90.0 + tempLocation.getPitch(), tempLocation.getYaw(), 0)
						.scaleVelocity(0.2);
					if (durationCounter % 3 == 0)
						ring.addSound(Sound.ENTITY_BLAZE_SHOOT, 2.0, 2.0);
					ring.playEffect();
				}
			}
			else {
				new Effect(tempLocation, Particle.CRIT)
					.append2DLine(length)
					.rotate(tempLocation.getPitch(), tempLocation.getYaw(), 0)
					.scaleVelocity(0)
					.playEffect();
			}
		}
		
		@Override
		public void whenEnd() {
		}
	}
}

class Invoke_MasterSpark_Handler extends MalaPassiveSkill
{
	public Invoke_MasterSpark_Handler()
	{
		super(	"INVOKE_MASTER_SPARK",
				"������ - ������ ����ũ",
				Material.WRITABLE_BOOK,
				"��93�ܰ� ����",
				MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_LIGHTNING + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7�ٶ󺸴� �������� �Ŵ��� ������ ������ �߻��մϴ�.",
				"&7������ �۾��� ������ ū ���ظ� �Խ��ϴ�.");
	}
}

