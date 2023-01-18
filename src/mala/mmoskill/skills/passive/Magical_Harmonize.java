package mala.mmoskill.skills.passive;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import io.lumine.mythic.lib.player.cooldown.CooldownInfo;
import mala.mmoskill.events.FireMagicEvent;
import mala.mmoskill.events.IceMagicEvent;
import mala.mmoskill.events.LightningMagicEvent;
import mala.mmoskill.manager.SpellChainType;
import mala.mmoskill.skills.Fire_Bolt;
import mala.mmoskill.skills.Flare_Disc;
import mala.mmoskill.skills.Frost_Wave;
import mala.mmoskill.skills.Ice_Bolt;
import mala.mmoskill.skills.Lightning_Bolt;
import mala.mmoskill.skills.Thunder;
import mala.mmoskill.util.MalaPassiveSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.ClassSkill;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Magical_Harmonize extends RegisteredSkill
{
	public static Magical_Harmonize skill;
	public static HashMap<Player, SpellChainType> lastUsedSpellType = new HashMap<Player, SpellChainType>();
	
	public Magical_Harmonize()
	{	
		super(new Magical_Harmonize_Handler(), MalaMMO_Skill.plugin.getConfig());
		addModifier("sec", new LinearValue(0.15, 0.15));
		skill = this;
	}
	
	public static void addSpellType(Player player, SpellChainType scType) {
		if (!lastUsedSpellType.containsKey(player)) {
			lastUsedSpellType.put(player, scType);
			return;
		}
		if (lastUsedSpellType.get(player) != scType) {
			lastUsedSpellType.put(player, scType);
			Reduce_Cooldown(player, scType);
		}
	}
	
	public static void Reduce_Cooldown(Player _player, SpellChainType ignoreType)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (data.getSkillLevel(skill) < 1)
			return;
		double reduceSec = skill.getModifier("sec", data.getSkillLevel(skill));
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			if (ignoreType != SpellChainType.FIRE) {
				if (cs.getSkill() instanceof Fire_Bolt
					|| cs.getSkill() instanceof Flare_Disc)
					ci.reduceFlat(reduceSec);
			}
			if (ignoreType != SpellChainType.ICE) {
				if (cs.getSkill() instanceof Ice_Bolt
					|| cs.getSkill() instanceof Frost_Wave)
					ci.reduceFlat(reduceSec);
			}
			if (ignoreType != SpellChainType.LIGHTNING) {
				if (cs.getSkill() instanceof Lightning_Bolt
					|| cs.getSkill() instanceof Thunder)
					ci.reduceFlat(reduceSec);
			}
		}
	}
	public static void Initialize_Cooldown(Player _player)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(_player);
		if (data.getSkillLevel(skill) < 1)
			return;
		for (ClassSkill cs : data.getProfess().getSkills())
		{
			CooldownInfo ci = data.getCooldownMap().getInfo(cs);
			if (ci == null)
				continue;
			if (cs.getSkill() instanceof Fire_Bolt
				|| cs.getSkill() instanceof Flare_Disc)
				ci.reduceFlat(9999.9);
			if (cs.getSkill() instanceof Ice_Bolt
				|| cs.getSkill() instanceof Frost_Wave)
				ci.reduceFlat(9999.9);
			if (cs.getSkill() instanceof Lightning_Bolt
				|| cs.getSkill() instanceof Thunder)
				ci.reduceFlat(9999.9);
		}
	}
}

class Magical_Harmonize_Handler extends MalaPassiveSkill implements Listener
{
	public Magical_Harmonize_Handler()
	{
		super(	"MAGICAL_HARMONIZE",
				"������ ��ȭ",
				Material.TOTEM_OF_UNDYING,
				"&7���� ������ �Ӽ��� �ٸ� ������ ����� ������",
				"&7���� �Ӽ��� ���� ��ų�� ������",
				"&7���� ��ų���� ���� ���ð��� &e{sec}&7�� �����մϴ�.",
				"",
				"&f&l[ &9��ų ��� &f&l]",
				"&c���̾� ��Ʈ, �÷��� ��ũ",
				"&b���̽� ��Ʈ, ���ν�Ʈ ���̺�",
				"&e����Ʈ�� ��Ʈ, ���");

		Bukkit.getPluginManager().registerEvents(this, MalaMMO_Skill.plugin);
	}

	@EventHandler
	public void magical_harmonize_fire(FireMagicEvent event)
	{
		Magical_Harmonize.addSpellType(event.getCaster(), SpellChainType.FIRE);
	}
	@EventHandler
	public void magical_harmonize_ice(IceMagicEvent event)
	{
		Magical_Harmonize.addSpellType(event.getCaster(), SpellChainType.ICE);
	}
	@EventHandler
	public void magical_harmonize_lightning(LightningMagicEvent event)
	{
		Magical_Harmonize.addSpellType(event.getCaster(), SpellChainType.LIGHTNING);
	}
}
