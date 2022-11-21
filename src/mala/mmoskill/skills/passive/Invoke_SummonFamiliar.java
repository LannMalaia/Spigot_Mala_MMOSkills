package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Cat.Type;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.Sound;

import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_SummonFamiliar extends RegisteredSkill
{
	public Invoke_SummonFamiliar()
	{	
		super(new Invoke_SummonFamiliar_Handler(), MalaMMO_Skill.plugin.getConfig());
	}

	public static class SummonFamiliarSpell extends MalaSpell
	{
		int firePosition = 0;
		
		public SummonFamiliarSpell(PlayerData playerData, int firePosition)
		{
			super(playerData, 3.0);
			this.firePosition = firePosition;
		}

		@Override
		public void whenStart() {
			new Effect(targetLocation, Particle.SOUL)
				.append2DCircle(3.0)
				.setVelocity(0, 1.0, 0)
				.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.4)
				.playEffect();
		}
		
		@Override
		public void whenCount() {
			new Effect(targetLocation, Particle.SOUL)
				.append2DCircle(3.0)
				.setVelocity(0, 1.0, 0)
				.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.4)
				.playEffect();
			new Effect(targetLocation, Particle.SOUL)
				.append2DShape(5, 3.0)
				.rotatePoint(0, durationCounter, 0)
				.setVelocity(0, 0, 0)
				.playEffect();
		}
		
		@Override
		public void whenEnd() {
			Summon_Manager sm = Summon_Manager.Get_Instance();
			if (!sm.Check_Summon(player, "S_Familiar", 1))
			{
				player.sendMessage("��c�۹и�� �� �̻� �θ� �� �����ϴ�.");
				return;
			}
			switch (firePosition)
			{
			case 1:
				SpellWolf s_wolf = new SpellWolf(player, targetLocation, (int)(20 * 20));
				Wolf wolf = (Wolf)sm.Summon(s_wolf);
				wolf.setCustomName(player.getName() + "�� ȥ�� ����");
				wolf.setCustomNameVisible(true);
				wolf.setTamed(true);
				wolf.setOwner(player);
				wolf.setAge(0);
				wolf.setCollarColor(DyeColor.BLUE);
				wolf.setAgeLock(true);
				wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(9999);
				wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(50);
				wolf.setInvulnerable(true);
				wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
				break;
			case 2:
				SpellGolem s_golem = new SpellGolem(player, targetLocation, (int)(10 * 20));
				IronGolem golem = (IronGolem)sm.Summon(s_golem);
				golem.setCustomName(player.getName() + "�� ����ƺ� ��");
				golem.setCustomNameVisible(true);
				golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(600);
				golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1);
				golem.setHealth(golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
				break;
			case 3:
				SpellCat s_cat = new SpellCat(player, targetLocation, (int)(30 * 20));
				Cat cat = (Cat)sm.Summon(s_cat);
				cat.setCustomName(player.getName() + "�� ���� �����");
				cat.setCustomNameVisible(true);
				cat.setTamed(true);
				cat.setOwner(player);
				cat.setAge(0);
				cat.setCollarColor(DyeColor.BLUE);
				cat.setCatType(Type.BLACK);
				cat.setAgeLock(true);
				cat.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(9999);
				cat.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(50);
				cat.setInvulnerable(true);
				cat.setHealth(cat.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
				break;
			}
		}
		
		class SpellWolf extends Summoned_OBJ implements Runnable
		{
			int time_counter = 0;
			
			public SpellWolf(Player _player, Location _loc, int _tick)
			{
				super(_player, "S_Familiar", EntityType.WOLF, _loc, _tick);
				_loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, _loc, 40, 0.5, 0.5, 0.5, 0);
				_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
			}
			
			public void run()
			{
				time_counter += 1;
				
				if (!entity.isValid())
					return;

				if (time_counter % 5 == 0)
				{
					entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
					Particle_Drawer.Draw_Circle(entity.getLocation(), Particle.CRIT, 5.0);
					for (Entity e : entity.getNearbyEntities(5.0, 5.0, 5.0))
					{
						if (e instanceof Damageable && !(e instanceof Animals))
						{
							double damage = ((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
							((Damageable)e).damage(damage, entity);
						}
					}
				}
				
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
			}
			
			@Override
			public void Remove()
			{
				entity.getWorld().spawnParticle(Particle.SMOKE_LARGE, entity.getLocation(), 40, 0.5, 0.5, 0.5, 0);
				super.Remove();
			}
		}
		class SpellGolem extends Summoned_OBJ implements Runnable
		{
			int time_counter = 0;
			
			public SpellGolem(Player _player, Location _loc, int _tick)
			{
				super(_player, "S_Familiar", EntityType.IRON_GOLEM, _loc, _tick);
				_loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, _loc, 40, 0.5, 0.5, 0.5, 0);
				_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
			}
			
			public void run()
			{
				time_counter += 1;
				
				if (!entity.isValid())
					return;

				if (time_counter % 2 == 0)
				{
					Aggro.Taunt_Area((LivingEntity)entity, 12.0);
					entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.6f, 0.8f);
					Particle_Drawer.Draw_Circle(entity.getLocation().add(0, 1.0, 0), Particle.WAX_ON, 12.0);
				}
				
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
			}
			
			@Override
			public void Remove()
			{
				entity.getWorld().spawnParticle(Particle.SMOKE_LARGE, entity.getLocation(), 40, 0.5, 0.5, 0.5, 0);
				super.Remove();
			}
		}
		class SpellCat extends Summoned_OBJ implements Runnable
		{
			int time_counter = 0;
			
			public SpellCat(Player _player, Location _loc, int _tick)
			{
				super(_player, "S_Familiar", EntityType.CAT, _loc, _tick);
				_loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, _loc, 40, 0.5, 0.5, 0.5, 0);
				_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
			}
			
			public void run()
			{
				time_counter += 1;
				
				if (!entity.isValid())
					return;
				
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
			}
			
			@Override
			public void Remove()
			{
				entity.getWorld().spawnParticle(Particle.SMOKE_LARGE, entity.getLocation(), 40, 0.5, 0.5, 0.5, 0);
				super.Remove();
			}
		}
	}
}

class Invoke_SummonFamiliar_Handler extends MalaPassiveSkill
{
	public Invoke_SummonFamiliar_Handler()
	{
		super(	"INVOKE_SUMMON_GOLEM",
				"������ - ���� �۹и���",
				Material.WRITABLE_BOOK,
				"��93�ܰ� ����",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_FIRE,
				"",
				"&7����� ����� �����ִ� �����ڸ� ��ȯ�մϴ�.",
				"&7�������� ������ ������ ���� ��ȯ�Ǵ� �����ڰ� �ٸ��ϴ�.",
				"",
				MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE,
				"&c[ ȥ�� ���� ]",
				"&7�ֺ� ������ �ѹ��� �����ϴ� ���븦 �θ��ϴ�.",
				"&720�� ���� �����Ǹ�, ���� �������� �ʽ��ϴ�.",
				"",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE,
				"&e[ ����ƺ� �� ]",
				"&7������� ������, �ֺ� ������ �����ϴ� ���� ����ϴ�.",
				"&710�� ���� �����Ǹ�, �������ų� ��ȯ�ð��� ������ ������ ���ظ� �ݴϴ�.",
				"",
				MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_FIRE,
				"&9[ ���� ����� ]",
				"&7�ƹ��͵� ���� �ʴ� ����̸� �θ��ϴ�.",
				"&730�� ���� �����Ǹ�, �������� ���� ���ط��� ������ŵ�ϴ�.");
	}
}

