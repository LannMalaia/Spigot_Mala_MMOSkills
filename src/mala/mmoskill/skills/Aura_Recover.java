package mala.mmoskill.skills;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Aura_Flag;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Aura_Recover extends RegisteredSkill
{
	public Aura_Recover()
	{	
		super(new Aura_Recover_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("mana_heal", new LinearValue(8.8, .8));
		addModifier("sta_heal", new LinearValue(3.3, .3));
		addModifier("heal", new LinearValue(16.5, 1.5));
		addModifier("cooldown", new LinearValue(60, -1.5));
		addModifier("stamina", new LinearValue(16.5, 1.5));
	}
}

class Aura_Recover_Handler extends MalaSkill implements Listener
{
	public Aura_Recover_Handler()
	{
		super(	"AURA_RECOVER",
				"회복의 진",
				Material.MUSIC_DISC_FAR,
				MsgTBL.NeedSkills,
				"&e 지휘술 - lv.15",
				"",
				"&7마나와 스태미나를 회복하는 지휘 깃발을 꽂습니다.",
				"&7깃발의 영향을 받는 플레이어는,",
				"&7마나를 &8{mana_heal}&7, 스태미나를 &8{sta_heal}&7 회복합니다.",
				"&7자신이 깃발의 영향을 받을 경우,",
				"&7HP를 &8{heal}&7 회복합니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}
	
	@Override
	public SimpleSkillResult getResult(SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		if(!Skill_Util.Has_Skill(data, "MASTERY_FLAG", 15))
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

		double heal = cast.getModifier("heal");
		double mana_heal = cast.getModifier("mana_heal");
		double sta_heal = cast.getModifier("sta_heal");
		
		Recover_Flag flag = Spawn_Flag(data, data.getPlayer().getLocation().add(0, 0.5, 0), heal, mana_heal, sta_heal);
		if (flag == null)
			data.getPlayer().sendMessage("§c깃발을 더 이상 생성할 수 없습니다.");
	}

	public Recover_Flag Spawn_Flag(PlayerData _data, Location _loc, double _hpheal, double _manaheal, double _staheal)
	{
		EntityType type = EntityType.ARMOR_STAND;

		double second = 5;
		double temp_radius = 3.0;
		int max = 1;
		RegisteredSkill mastery = MMOCore.plugin.skillManager.getSkill("MASTERY_FLAG");
		int level = _data.getSkillLevel(mastery);
		if(_data.getProfess().hasSkill(mastery))
		{
			second = mastery.getModifier("second", level);
			temp_radius = mastery.getModifier("radius", level);
			max = (int)mastery.getModifier("max", level);
		}

		if (!Summon_Manager.Get_Instance().Check_Summon(_data.getPlayer(), "Aura_Flag", max))
			return null;
		
		Recover_Flag flag = new Recover_Flag(_data.getPlayer(), _loc,
				(int)(second * 20), type, temp_radius, _hpheal, _manaheal, _staheal);

		Entity flag_en = Summon_Manager.Get_Instance().Summon(flag);
		ArmorStand as = (ArmorStand)flag_en;
		as.setCustomName("§a§l회복 깃발");
		as.setCustomNameVisible(true);
		as.setInvisible(true);
		ItemStack item = new ItemStack(Material.GREEN_BANNER);
		as.getEquipment().setHelmet(item);
		as.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
		
		return flag;
	}

	class Recover_Flag extends Aura_Flag
	{
		double hpheal = 0.0;
		double manaheal = 0.0;
		double staheal = 0.0;
		
		public Recover_Flag(Player _player, Location _loc, int _tick,
				EntityType _type, double _radius, double _hpheal, double _manaheal, double _staheal)
		{
			super(_player, _loc, _tick, _type, _radius);
			hpheal = _hpheal;
			manaheal = _manaheal;
			staheal = _staheal;
		}

		@Override
		protected void Particle_Setup()
		{
			super.Particle_Setup();
			m_Particle = Particle.REDSTONE;
			m_Dust = new DustOptions(Color.LIME, 1.0f);
		}
		
		@Override
		protected void Effect_Sec()
		{
			for (Player p : players)
			{
				try
				{
					PlayerData pd = PlayerData.get(p);
					double max_mana = pd.getStats().getStat(StatType.MAX_MANA);
					double max_stamina = pd.getStats().getStat(StatType.MAX_STAMINA);
					pd.setMana(Math.min(max_mana, pd.getMana() + manaheal));
					pd.setStamina(Math.min(max_stamina, pd.getStamina() + staheal));
					
					if (p == this.player)
					{
						p.setHealth(Math.min(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), p.getHealth() + hpheal));
					}
				}
				catch (Exception e)
				{
					continue;
				}
			}
		}
	}
}


