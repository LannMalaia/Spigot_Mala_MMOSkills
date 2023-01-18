package mala.mmoskill.skills;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.skills.passive.Magical_Harmonize;
import mala.mmoskill.util.AttackUtil;
import mala.mmoskill.util.Effect;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala.mmoskill.util.Weapon_Identify;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent;
import net.Indyuce.mmocore.api.event.PlayerResourceUpdateEvent.UpdateReason;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.profess.resource.PlayerResource;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Unleash extends RegisteredSkill
{
	public static Unleash skill;
	public Unleash()
	{	
		super(new Unleash_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("mana_cost", new LinearValue(21.5, -3.5, 8.0, 90.0));
		addModifier("cooldown", new LinearValue(1.0, 0.0));
		addModifier("mana", new LinearValue(80, 0));
		
		skill = this;
	}
}

class Unleash_Handler extends MalaSkill implements Listener
{
	public Unleash_Handler()
	{
		super(	"UNLEASH",
				"�𸮽�",
				Material.EMERALD,
				MsgTBL.NeedSkills,
				"&e ������ ��ȭ - lv.20",
				"",
				"&7�Ͻ������� ��ų�� ������ �� �ִ� ���°� �˴ϴ�.",
				"&7���� �ֺ��� ������ &e1&7�� ���ظ� ���ϸ� �о���ϴ�.",
				"&7������ �� �ִ� ��ų�� &a������ ��ȭ&7�� �����ϴ�.",
				"&7�ʴ� &e{mana_cost}&7�� ������ �Ҹ��ϸ�,",
				"&7�� ���� ������ �ڿ������� ȸ������ �ʽ��ϴ�.",
				"&7������ ���� �Ҹ��ϰų� ��ų�� �ٽ� ����ؼ� ����� �� �ֽ��ϴ�.",
				"",
				MsgTBL.Cooldown_Fixed);
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "MAGICAL_HARMONIZE", 20))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return new SimpleSkillResult(false);
		}
		return new SimpleSkillResult(true);
	}
	
	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());

		if (data.getPlayer().hasMetadata("malammo.skill.unleash")) {
			data.getPlayer().removeMetadata("malammo.skill.unleash", MalaMMO_Skill.plugin);
			data.getPlayer().sendMessage("��c[ �𸮽� ���� ]");
			return;
		}
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin,
				new Unleash_Task(data.getPlayer(), cast.getModifier("mana_cost")));
	}

	@EventHandler
	public void manaHeal(PlayerResourceUpdateEvent event)
	{
		if (event.getPlayer().hasMetadata("malammo.skill.unleash")) {
			if (event.getResource() == PlayerResource.MANA) {
				if (event.getAmount() > 0)
					event.setAmount(0);
			}
		}
	}
	
}

class Unleash_Task implements Runnable
{
	PlayerData playerData;
	Player player;
	Location loc;
	double manaCost;
	
	public Unleash_Task(Player _player, double mana_cost)
	{
		player = _player;
		playerData = PlayerData.get(_player);
		manaCost = mana_cost;

		player.sendMessage("��b[ �𸮽� �ߵ� ]");
		player.playSound(player, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.5f, 1.5f);
		player.setMetadata("malammo.skill.unleash", new FixedMetadataValue(MalaMMO_Skill.plugin, true));
	}
	
	int count = 0;
	public void run()
	{
		count++;
		
		// �¶���?
		if (!player.isOnline()) {
			player.removeMetadata("malammo.skill.unleash", MalaMMO_Skill.plugin);
			return;
		}
		
		// �ð� �� �� ���
		if (playerData.getMana() <= manaCost) {
			player.removeMetadata("malammo.skill.unleash", MalaMMO_Skill.plugin);
			player.sendMessage("��c[ �𸮽� ���� ]");
			return;
		}
		if (!player.hasMetadata("malammo.skill.unleash"))
			return;

		new Effect(player.getLocation().add(0, 0.4, 0), Particle.CRIT)
			.append2DCircle(3.0)
			.scalePoint(0)
			.scaleVelocity(0.5)
			.rotate(0, count * 2.5, 0)
			.playEffect();
		new Effect(player.getLocation().add(0, 0.5, 0), Particle.DRAGON_BREATH)
			.setDustTransition(new DustTransition(Color.PURPLE, Color.WHITE, 0.5f))
			.append2DCircle(3.0, 1.5)
			.append2DShape(3, 3.0)
			.rotate(0, 180.0, 0)
			.append2DShape(3, 3.0)
			.scaleVelocity(0)
			.rotate(0, count * 2.5, 0)
			.playEffect();
		
		AttackUtil.attackCylinder(player,
				player.getLocation(), 3.0, 3.0, 1,
				(target) -> {
					Vector vec = target.getLocation().subtract(player.getLocation()).toVector();
					double power = 3.5 - Math.min(3.0, vec.length());
					target.setVelocity(target.getVelocity().add(vec.normalize().multiply(power * 0.6)));
				}, DamageType.MAGIC);
		
		playerData.giveMana(-manaCost * 0.05, UpdateReason.SKILL_COST);
		Magical_Harmonize.Initialize_Cooldown(player);
		
		Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
	}
}




