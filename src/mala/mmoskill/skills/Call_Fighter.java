package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.skills.passive.Mastery_Dog;
import mala.mmoskill.util.Aggro;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Particle_Drawer;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Call_Fighter  extends RegisteredSkill
{
	public static String[] names = {
		"�����", "���ؽ�", "��ġ", "���󸮾�", "������", "���̵�", "�η�Ÿ", "���̾�", "�Ḯ��", "��Ÿ"
	};
	
	public Call_Fighter()
	{	
		super(new Call_Fighter_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("second", new LinearValue(300, 0));
		addModifier("max", new LinearValue(1, 0));
		addModifier("hp", new LinearValue(80, 25));
		addModifier("def", new LinearValue(2, 1));
		addModifier("atk", new LinearValue(10, 3));
		addModifier("speed", new LinearValue(0.25, 0.0));
		addModifier("cooldown", new LinearValue(120, 0));
		addModifier("stamina", new LinearValue(20, 0));
	}
}

class Call_Fighter_Handler extends MalaSkill implements Listener
{
	public Call_Fighter_Handler()
	{
		super(	"CALL_FIGHTER",
				"����",
				Material.WOODEN_SWORD,
				MsgTBL.SKILL,
				"&7�Բ� �ο� �� ���Ḧ �θ��ϴ�.",
				"&7����� &8{second}&7�ʰ� �����˴ϴ�.",
				"&7�ִ� &8{max}&7 ����� ���ÿ� ������ �� �ֽ��ϴ�.",
				"",
				"&f[ &e���� �ɷ�ġ &f]",
				"&f&l[ &a���� &f{hp} &f&l][ &e��� &f{def} &f&l]",
				"&f&l[ &c���� &f{atk} &f&l][ &b�̵� &f{speed} &f&l]",
				"",
				"&f[ &e���� ��ų &f]",
				"&fLv.1 - 7�ʸ��� 5m ���� ������ �� ���� �����մϴ�.",
				"&fLv.10 - ������ 10m�� �����մϴ�.",
				"&fLv.15 - ���� �󵵰� 3�ʷ� �پ��ϴ�.",
				"&fLv.20 - �ڽ��� HP�� �ʴ� 5�� ȸ���մϴ�.",
				"",
				"&e�� ������ ����°� ���ݷ��� ��ų ���ط� ����ġ�� ���� �� �����մϴ�.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Summon_Manager sm = Summon_Manager.Get_Instance();
		
		double second = cast.getModifier("second");
		int max = (int)cast.getModifier("max");
		int level = data.getSkillLevel(MMOCore.plugin.skillManager.getSkill("CALL_FIGHTER"));
		
		if (!sm.Check_Summon(data.getPlayer(), "S_Fighter", max))
		{
			data.getPlayer().sendMessage("��c���Ḧ �� �̻� �θ� �� �����ϴ�.");
			return;
		}
		
		S_Fighter s_wolf = new S_Fighter(data.getPlayer(), data.getPlayer().getLocation(), (int)(second * 20), level);
		Wolf wolf = (Wolf)sm.Summon(s_wolf);
		String name = Call_Fighter.names[(int)(Math.random() * Call_Fighter.names.length)];
		wolf.setCustomName(data.getPlayer().getName() + "�� ���� " + name);
		wolf.setCustomNameVisible(true);
		wolf.setTamed(true);
		wolf.setOwner(data.getPlayer());
		wolf.setAge(0);
		wolf.setCollarColor(DyeColor.BLACK);
		wolf.setAgeLock(true);

		double hp = cast.getModifier("hp");
		double def = cast.getModifier("def");
		double atk = cast.getModifier("atk");
		double speed = cast.getModifier("speed");
		double skill_per = 1.0 + data.getStats().getStat("SKILL_DAMAGE") * 0.01;
		
		hp = hp * skill_per;
		atk = atk * skill_per;
		// speed = 0.3 + skill_per;
		
		wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hp);
		wolf.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(def);
		wolf.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).setBaseValue(4.0);
		wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(atk);
		wolf.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
		
		wolf.setHealth(wolf.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

		PlayerDisguise pd = new PlayerDisguise("Icecreamfreak");
		pd.setNameVisible(true);
		pd.setEntity(wolf);
		pd.setName(wolf.getCustomName());
		pd.startDisguise();
		
		wolf.teleport(data.getPlayer().getLocation().add(0, 0.5, 0));
	}
	
	class S_Fighter extends Summoned_OBJ implements Runnable
	{
		int time_counter = 0;
		int caLatency = 7;
		double caRange = 5.0;
		boolean autoHeal = false;
		
		public S_Fighter(Player _player, Location _loc, int _tick, int _level)
		{
			super(_player, "S_Fighter", EntityType.WOLF, _loc, _tick);
			_loc.getWorld().spawnParticle(Particle.HEART, _loc, 40, 0.5, 0.5, 0.5, 0);
			_loc.getWorld().playSound(_loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.5f, 1.5f);

			if (_level >= 10)
				caRange = 10.0;
			if (_level >= 15)
				caLatency = 3;
			autoHeal = _level >= 20;
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 20);
		}
		
		public void run()
		{
			time_counter += 1;
			
			if (!entity.isValid())
				return;

			if (time_counter % caLatency == 0)
			{
				entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);
				Particle_Drawer.Draw_Circle(entity.getLocation(), Particle.CRIT, 5.0);
				for (Entity e : entity.getNearbyEntities(caRange, caRange, caRange))
				{
					if (e instanceof Damageable && !(e instanceof Animals))
					{
						double damage = ((LivingEntity)entity).getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue();
						((Damageable)e).damage(damage, entity);
					}
				}
			}
			if (autoHeal) {
				LivingEntity le = (LivingEntity)this.entity;
				le.setHealth(Math.min(le.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), le.getHealth() + 5));
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
}
