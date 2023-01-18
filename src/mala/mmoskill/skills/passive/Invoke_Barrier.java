package mala.mmoskill.skills.passive;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.skills.Cast_Ventus;
import mala.mmoskill.util.Buff_Manager;
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

public class Invoke_Barrier extends RegisteredSkill
{
	private static Invoke_Barrier instance;
	
	public Invoke_Barrier()
	{	
		super(new Invoke_Barrier_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("absorb_base", new LinearValue(3.0, 0.0));
		addModifier("absorb_add", new LinearValue(0.15, 0.0));
		addModifier("duration_base", new LinearValue(3.0, 0.0));
		addModifier("duration_add", new LinearValue(0.2, 0.0));
		
		instance = this;
	}

	public static class BarrierSpell extends MalaSpellEffect
	{
		DustTransition dts = new DustTransition(Color.fromRGB(200, 200, 255), Color.fromRGB(255, 255, 255), 0.5f);
		double size = 1.0 / 12.0, targetSize = 20.0 / 12.0;
		Location circleLocation;
		Vector[] circleAngles;
		
		double amp;
		
		public BarrierSpell(PlayerData playerData)
		{
			super(playerData.getPlayer(), 8.0);
			circleLocation = attacker.getLocation().add(0, .1, 0);
			circleAngles = new Vector[3];
			for (int i = 0; i < circleAngles.length; i++)
				circleAngles[i] = new Vector(Math.random() * 360.0, Math.random() * 360.0, Math.random() * 360.0);

			amp = instance.getModifier("absorb_base", 1)
					+ instance.getModifier("absorb_add", 1) * Cast_Glacia.getLevel(playerData);
			amp *= spellPower;
			targetDuration = instance.getModifier("duration_base", 1)
					+ instance.getModifier("duration_add", 1) * Cast_Ventus.getLevel(playerData);
			CooldownFixer.Add_Cooldown(playerData, Cast_Glacia.getInstance(), 12.0);
			CooldownFixer.Add_Cooldown(playerData, Cast_Ventus.getInstance(), 6.0);
		}
		public BarrierSpell(LivingEntity attacker, int amp, double duration)
		{
			super(attacker, duration);
			circleLocation = attacker.getLocation().add(0, .1, 0);
			circleAngles = new Vector[3];
			for (int i = 0; i < circleAngles.length; i++)
				circleAngles[i] = new Vector(Math.random() * 360.0, Math.random() * 360.0, Math.random() * 360.0);
			
			this.amp = amp * spellPower;
		}

		@Override
		public void whenStart() {
			world.playSound(centerLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 2.0f);
		}
		
		@Override
		public void whenCount() {
			if (circleLocation.distance(attacker.getLocation()) > 2.0) {
				cancelled = true;
				return;
			}
			
			// ������
			if (durationCounter % 10 == 0) {
				if (Particle_Manager.isReduceMode(attacker)) {
					new Effect(circleLocation, Particle.SOUL_FIRE_FLAME)
						.addSound(Sound.ENTITY_PLAYER_LEVELUP, 2.0, 0.8)
						.playEffect();	
				} else {
					new Effect(circleLocation, Particle.SOUL_FIRE_FLAME)
						.append2DImage("barrierCircle.png", 2)
						.scale(size)
						.setVelocity(0, 0, 0)
						.addSound(Sound.ENTITY_PLAYER_LEVELUP, 2.0, 0.8)
						.playEffect();					
				}
			}
			
			double realSize = size * 12.0;
			// ������ ��Ʈ�Ӹ�
			if (Particle_Manager.isReduceMode(attacker)) {
				new Effect(circleLocation, Particle.SOUL_FIRE_FLAME)
					.append2DCircle(realSize, 0.5)
					.setVelocity(0, 1, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.3)
					.playEffect();
			} else {
				new Effect(circleLocation, Particle.SOUL_FIRE_FLAME)
					.append2DCircle(realSize)
					.setVelocity(0, 1, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.01, 0.3)
					.playEffect();
				
				for (int i = 0; i < circleAngles.length; i++) {
					circleAngles[i].add(new Vector(1.3, 3.7, 5.6));
					new Effect(circleLocation, Particle.CRIT)
						.append2DCircle(realSize)
						.setVelocity(0, 0, 0)
						.rotatePoint(circleAngles[i].getX(), circleAngles[i].getY(), circleAngles[i].getZ())
						.playEffect();
				}
			}
			
			size = size + (targetSize - size) * 0.05;
			
			if (durationCounter % 10 == 0) {
				for (Entity entity : world.getNearbyEntities(circleLocation, realSize, realSize, realSize)) {
					if (attacker instanceof Player) {
						// �÷��̾ ����
						if (entity instanceof Player) {
							Player target = (Player)entity;
							Buff_Manager.Add_Buff(target, PotionEffectType.ABSORPTION, (int)amp - 1, 100, null);
						}
					} else {
						// ���� ����
						if (!(entity instanceof Player) && entity instanceof LivingEntity) {
							LivingEntity target = (LivingEntity)entity;
							Buff_Manager.Add_Buff(target, PotionEffectType.ABSORPTION, (int)amp - 1, 100, null);
						}
					}
				}
			}
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
				"������ - �踮��",
				Material.WRITABLE_BOOK,
				"��93�ܰ� ����",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_LIGHTNING,
				"",
				"&7�ֺ� 20m���� �Ʊ����� 5�ʰ� �����Ǵ� ����� �ο��մϴ�.",
				"&7�踮� �����Ǵ� ���� ����� ����ؼ� �ο��˴ϴ�.",
				"&7������ ��� ������ ��ҵ˴ϴ�.",
				"",
				"&f&l[ &9���� ���� &f&l]",
				"&f&l[ &a��� ���� &f{absorb_base} + {absorb_add} * �۷��þ� &f&l]",
				"&f&l[ &b������ ���� �ð� &f{duration_base} + {duration_add} * ������ &f&l]");
	}
}

