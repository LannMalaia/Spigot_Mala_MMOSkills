package mala.mmoskill.skills.passive;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Double_Shot extends RegisteredSkill
{
	public Double_Shot()
	{	
		super(new Double_Shot_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("per", new LinearValue(2.5, 2.5));
	}
}

class Double_Shot_Handler extends MalaPassiveSkill implements Listener
{
	public Double_Shot_Handler()
	{
		super(	"DOUBLE_SHOT",
				"ġ������ �� ��",
				Material.ARROW,
				"&7����ü ������ &7{per}%�� Ȯ���� &e1.3&7���� ���ظ� �ݴϴ�.");
		
		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void attack_double_shot(PlayerAttackEvent event)
	{
		Player player = event.getPlayer();
		PlayerData data = PlayerData.get(player);

		// ���� üũ
		if (!event.getDamage().hasType(DamageType.PROJECTILE))
			return;
		
		// ��ų üũ
		RegisteredSkill skill = MMOCore.plugin.skillManager.getSkill("DOUBLE_SHOT");
		int level = data.getSkillLevel(skill);
		if (!data.getProfess().hasSkill(skill))
			return;

		// �߱� üũ
		RegisteredSkill glow_skill = MMOCore.plugin.skillManager.getSkill("GOOD_SHOT_WHEN_GLOWING");
		int glow_level = data.getSkillLevel(glow_skill);
		
		// Ȯ�� üũ
		double per = skill.getModifier("per", level) * 0.01d;
		if (level >= 10
			&& data.getProfess().hasSkill(glow_skill)) // �� ������ �� ����� ��ų�� ������ ���
		{
			if (event.getEntity().hasPotionEffect(PotionEffectType.GLOWING))
				per += glow_skill.getModifier("per", glow_level) * 0.01d;
		}
		
		if (Math.random() < per)
			return;
		
		// ��ġ�� üũ
		boolean tripled = false;
		RegisteredSkill triple_skill = MMOCore.plugin.skillManager.getSkill("TRIPLE_SHOT");
		int triple_level = data.getSkillLevel(triple_skill);
		if (data.getProfess().hasSkill(triple_skill))
		{
			per = triple_skill.getModifier("per", triple_level) * 0.01d;
			if (Math.random() < per)
				tripled = true;
		}
		
		// ������ �ϰ� �ʱ�ȭ üũ
		if (Critical_Domination.Can_Init_Dominate(player)
			&& event.getEntity().hasMetadata("malammo.skill.dominate"))
		{
			event.getEntity().removeMetadata("malammo.skill.dominate", MalaMMO_Skill.plugin);
		}
		
//		for (DamagePacket dp : event.getDamage().getPackets())
//		{
//			String tmsg = "";
//			for (DamageType t : dp.getTypes())
//				tmsg += t.toString() + "-" + event.getDamage().getDamage(t) + ", ";
//			player.sendMessage("packet - " + tmsg + dp.getValue() + " ... " + dp.getFinalValue());
//		}
		
		Location loc = event.getEntity().getEyeLocation().add(0.0, 0.5, 0.0);
		if (tripled)
		{
			loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.5f, 2.0f);
			loc.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 70, 1, 1, 1, 0);
			// loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
			loc.getWorld().spawnParticle(Particle.WAX_ON, loc, 20, 0, 0, 0, 50.0);
			// event.getDamage().additiveModifier(1.0, DamageType.PROJECTILE);
			event.getDamage().multiplicativeModifier(2.0, DamageType.PROJECTILE);
		}
		else
		{
			loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 1.4f);
			// loc.getWorld().spawnParticle(Particle.CRIT, loc, 40, 1, 1, 1, 0);
			loc.getWorld().spawnParticle(Particle.WAX_OFF, loc, 20, 0, 0, 0, 50.0);
			// event.getDamage().additiveModifier(0.5, DamageType.PROJECTILE);
			event.getDamage().multiplicativeModifier(1.5, DamageType.PROJECTILE);
		}

//		for (DamagePacket dp : event.getDamage().getPackets())
//		{
//			String tmsg = "";
//			for (DamageType t : dp.getTypes())
//				tmsg += t.toString() + "-" + event.getDamage().getDamage(t) + ", ";
//			player.sendMessage("after packet - " + tmsg + dp.getValue() + " ... " + dp.getFinalValue());
//		}
	}
}
