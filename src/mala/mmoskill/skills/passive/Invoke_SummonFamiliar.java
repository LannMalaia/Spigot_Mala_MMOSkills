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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import io.lumine.mythic.lib.damage.DamageType;

import org.bukkit.Sound;

import mala.mmoskill.events.SpellCastEvent;
import mala.mmoskill.manager.Particle_Manager;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.skills.Cast_Glacia;
import mala.mmoskill.skills.Cast_Ignis;
import mala.mmoskill.skills.Cast_Invoke;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.CooldownFixer;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.Effect.RANDOMIZE_TYPE;
import mala.mmoskill.util.MalaPassiveSkill;
import mala.mmoskill.util.MalaSpellEffect;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Invoke_SummonFamiliar extends RegisteredSkill
{
	private static Invoke_SummonFamiliar instance;
	
	public Invoke_SummonFamiliar()
	{	
		super(new Invoke_SummonFamiliar_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("damage_wolf", new LinearValue(12.0, 0.0));
		addModifier("damage_explode", new LinearValue(33.0, 0.0));
		addModifier("duration_mult", new LinearValue(0.02, 0.0));
		
		instance = this;
	}

	public static class SummonFamiliarSpell extends MalaSpellEffect
	{
		int firePosition = 0; // 이그니스를 소환한 인덱스값
		
		double damageWolf, damageExplode;
		double durationMult;
		
		public SummonFamiliarSpell(PlayerData playerData, int firePosition)
		{
			super(playerData.getPlayer(), 3.0);
			this.firePosition = firePosition;

			damageWolf = instance.getModifier("damage_wolf", 1) * Cast_Ignis.getLevel(playerData);
			damageExplode = instance.getModifier("damage_explode", 1) * Cast_Ignis.getLevel(playerData);
			durationMult = 1.0 + instance.getModifier("duration_mult", 1) * Cast_Glacia.getLevel(playerData);
			durationMult *= spellPower;
		}

		@Override
		public void whenStart() {
			if (Particle_Manager.isReduceMode(attacker)) {
				new Effect(targetLocation, Particle.SOUL)
					.append2DCircle(3.0, 0.5)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.4)
					.playEffect();
			} else {
				new Effect(targetLocation, Particle.SOUL)
					.append2DCircle(3.0)
					.setVelocity(0, 1.0, 0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.4)
					.playEffect();
			}
		}
		
		@Override
		public void whenCount() {
			if (Particle_Manager.isReduceMode(attacker)) {
				if (durationCounter % 5 == 0)
					new Effect(targetLocation, Particle.SOUL)
						.append2DCircle(3.0)
						.setVelocity(0, 1.0, 0)
						.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.4)
						.playEffect();
			} else {
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
		}
		
		@Override
		public void whenEnd() {
			Summon_Manager sm = Summon_Manager.Get_Instance();
			if (!(attacker instanceof Player))
				return;
			Player player = (Player)attacker;
			switch (firePosition)
			{
			case 1:
				if (!sm.Check_Summon(player, "S_Familiar", 1)) {
					attacker.sendMessage("§c퍼밀리어를 더 이상 부를 수 없습니다.");
					return;
				}
				SpellWolf s_wolf = new SpellWolf(player, targetLocation, (int)(20 * 20 * durationMult));
				Wolf wolf = (Wolf)sm.Summon(s_wolf);
				wolf.setCustomName(player.getName() + "의 혼령 늑대");
				wolf.setCustomNameVisible(true);
				wolf.setTamed(true);
				wolf.setOwner(player);
				wolf.setAge(0);
				wolf.setCollarColor(DyeColor.BLUE);
				wolf.setAgeLock(true);
				wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(9999);
				wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(damageWolf);
				wolf.setInvulnerable(true);
				wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
				break;
			case 2:
				if (!sm.Check_Summon(player, "S_Familiar", 1)) {
					attacker.sendMessage("§c퍼밀리어를 더 이상 부를 수 없습니다.");
					return;
				}
				SpellGolem s_golem = new SpellGolem(player, targetLocation, damageExplode, (int)(10 * 20 * durationMult));
				IronGolem golem = (IronGolem)sm.Summon(s_golem);
				golem.setCustomName(player.getName() + "의 허수아비 골렘");
				golem.setCustomNameVisible(true);
				golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(8500 * spellPower);
				golem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1);
				golem.setHealth(golem.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
				break;
			case 3:
				if (!sm.Check_Summon(player, "S_Familiar_Cat", 1)) {
					attacker.sendMessage("§c검은 고양이를 더 이상 부를 수 없습니다.");
					return;
				}
				SpellCat s_cat = new SpellCat(player, targetLocation, (int)(30 * 20 * durationMult));
				Cat cat = (Cat)sm.Summon(s_cat);
				cat.setCustomName(player.getName() + "의 검은 고양이");
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

				if (time_counter % 2 == 0)
				{
					entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
					Particle_Drawer.Draw_Circle(entity.getLocation(), Particle.CRIT, 5.0);
					for (Entity e : entity.getNearbyEntities(5.0, 5.0, 5.0))
					{
						if (e == this.player)
							continue;
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
			double damage;
			
			public SpellGolem(Player _player, Location _loc, double _damage, int _tick)
			{
				super(_player, "S_Familiar", EntityType.IRON_GOLEM, _loc, _tick);
				damage = _damage;
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
				entity.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, entity.getLocation(), 1, 0, 0, 0, 0);
				new Effect(entity.getLocation(), Particle.FIREWORKS_SPARK)
					.addSound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.8, 2.0)
					.addSound(Sound.ENTITY_GENERIC_EXPLODE, 0.8, 2.0)
					.append3DSphere(12.0)
					.scalePoint(0.0)
					.randomizeVelocity(RANDOMIZE_TYPE.MULTIPLY, 0.1, 0.5)
					.playEffect();
				AttackUtil.attackSphere(attacker, entity.getLocation(),
						12.0, damage, null, 
						DamageType.MAGIC, DamageType.SKILL);
				super.Remove();
			}
		}
		class SpellCat extends Summoned_OBJ implements Runnable
		{
			int time_counter = 0;
			
			public SpellCat(Player _player, Location _loc, int _tick)
			{
				super(_player, "S_Familiar_Cat", EntityType.CAT, _loc, _tick);
				_loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, _loc, 40, 0.5, 0.5, 0.5, 0);
				_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 60);
			}
			
			public void run()
			{
				time_counter += 1;
				
				if (!entity.isValid())
					return;
				
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
			}
			
			@EventHandler (priority = EventPriority.LOWEST)
			public void playerCastSpell(SpellCastEvent event) {
				event.getSpell().spellPower += 0.2;
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
		super(	"INVOKE_SUMMON_FAMILIAR",
				"마술식 - 서먼 퍼밀리어",
				Material.WRITABLE_BOOK,
				"§93단계 술식",
				"",
				"&7명령을 충실히 따라주는 동반자를 소환합니다.",
				"&7마법식을 조합한 순서에 따라 소환되는 동반자가 다릅니다.",
				"&7동반자의 유지 시간은 &e글래시아&7의 수준에 비례해 조금 더 증가합니다.",
				"",
				"&c[ 혼령 늑대 ] " + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE,
				"&7주변 적들을 한번에 공격하는 늑대를 부릅니다.",
				"&7늑대는 (&e{damage_wolf} * 이그니스&7)의 공격력을 갖습니다.",
				"&720초 동안 유지되며, 절대 쓰러지지 않습니다.",
				"",
				"&e[ 허수아비 골렘 ] " + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_FIRE + MsgTBL.MAGIC_ICE,
				"&7생명력이 높으며, 주변 적들을 도발하는 골렘을 만듭니다.",
				"&710초 동안 유지되며, 쓰러지거나 소환시간이 끝나면 폭발해 피해를 줍니다.",
				"&7폭발은 (&e{damage_explode} * 이그니스&7)의 피해를 가합니다.",
				"",
				"&9[ 검은 고양이 ] " + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_ICE + MsgTBL.MAGIC_FIRE,
				"&7아무것도 하지 않는 고양이를 부릅니다.",
				"&760초 동안 유지되며, 시전자의 마술식 마법의 위력을 20% 증가시켜줍니다.",
				"&7아무것도 하지 않기 때문에, 소환 제한에도 걸리지 않습니다.");
	}
}

