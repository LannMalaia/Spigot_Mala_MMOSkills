package mala.mmoskill.skills;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Doppel;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.util.Buff_Manager;
import mala.mmoskill.util.MalaSkill;
import mala.mmoskill.util.Skill_Util;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Doppelganger extends RegisteredSkill
{
	public Doppelganger()
	{	
		super(new Doppelganger_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("count", new LinearValue(2.25, 0.25));
		addModifier("cooldown", new LinearValue(24, -1, 15, 40));
		addModifier("stamina", new LinearValue(17, 2));
	}
}

class Doppelganger_Handler extends MalaSkill implements Listener
{
	public Doppelganger_Handler()
	{
		super(	"DOPPELGANGER",
				"�׸��� �нż�",
				Material.TOTEM_OF_UNDYING,
				MsgTBL.NeedSkills,
				"&e �н� �����͸� - lv.10",
				"",
				"&7�ֺ��� �н��� &8{count}&7�� �����մϴ�.",
				"&7�ڽ��� 10�ʰ� ����ȭ ���°� �˴ϴ�.",
				"&7�ִ�ġ�� �Ѿ�� ��쿡�� �������� �ʽ��ϴ�.",
				"",
				"&f[ &e�����ϴ� ��ų &f]",
				"&f-&bǥâ ��ô",
				"&f-&bǥâ ����",
				"&f-&b������",
				"", MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		Summon_Manager.Get_Instance();

		// ���� ��ų Ȯ��
		if(!Skill_Util.Has_Skill(data, "MASTERY_DOPPEL", 10))
		{
			data.getPlayer().sendMessage(MsgTBL.You_Has_no_Skill);
			return;
		}

		Buff_Manager.Add_Buff(data.getPlayer(), PotionEffectType.INVISIBILITY, 0, 200, null);
		for (int i = 0; i < cast.getModifier("count"); i++)
		{
			
			Location loc = data.getPlayer().getLocation();
			double radius = 3.0;
			loc.add(-radius + Math.random() * radius * 2.0, 3.0, -radius + Math.random() * radius * 2.0);
			RayTraceResult rtr = loc.getWorld().rayTraceBlocks(loc, new Vector(0, -1, 0), 4.0, FluidCollisionMode.NEVER);
			if (rtr != null && rtr.getHitBlock() != null)
				loc.setY(rtr.getHitPosition().getY() + 1.0);
			else
				loc.add(0, -2.0, 0);
			
			Doppel doppel = Doppel.Spawn_Doppel(data, loc);
			if (doppel == null)
			{
				data.getPlayer().sendMessage("��c�н��� �� �̻� ���� �� �����ϴ�.");
				return;
			}
			
			for (Entity e : data.getPlayer().getNearbyEntities(10.0, 10.0, 10.0))
			{
				if (e instanceof Mob && doppel.entity instanceof LivingEntity)
				{
					Mob m = (Mob)e;
					if (m.getTarget() == data.getPlayer())
						m.setTarget((LivingEntity)doppel.entity);
				}
			}
		}
	}	
}