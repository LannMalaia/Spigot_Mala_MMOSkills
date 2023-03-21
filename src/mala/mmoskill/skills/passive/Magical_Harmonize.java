package mala.mmoskill.skills.passive;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

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
import mala.mmoskill.util.Buff_Manager;
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
		addModifier("sec", new LinearValue(0.2, 0.2));
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
		
		// 공중에 있을 경우 약간 띄움
		if (data.getSkillLevel(skill) >= 10) {
			if (!((LivingEntity)_player).isOnGround()) {
				Vector vel = _player.getVelocity();
				vel.setY(0.05);
				Buff_Manager.Add_Buff(_player, PotionEffectType.LEVITATION, 0, 10, null);
				Buff_Manager.Increase_Buff(_player, PotionEffectType.SLOW_FALLING, 0, 40, null, 0);
				_player.setVelocity(vel);
			}
		}
		
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
				"마법의 조화",
				Material.TOTEM_OF_UNDYING,
				"&7이전 마법과 속성이 다른 마법을 사용할 때마다",
				"&7같은 속성을 지닌 스킬을 제외한",
				"&7다음 스킬들의 재사용 대기시간이 &e{sec}&7초 감소합니다.",
				"",
				"&f&l[ &9스킬 목록 &f&l]",
				"&c파이어 볼트, 플레어 디스크",
				"&b아이스 볼트, 프로스트 웨이브",
				"&e라이트닝 볼트, 썬더",
				"",
				"&eLv.10 - 발동시 공중에 있다면 약간 떠오릅니다.");

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
