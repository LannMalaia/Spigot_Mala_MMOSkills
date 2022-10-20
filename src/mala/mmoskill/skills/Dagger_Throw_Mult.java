package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.manager.Summon_Manager;
import mala.mmoskill.manager.Summoned_OBJ;
import mala.mmoskill.skills.passive.Make_Doppel;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class Dagger_Throw_Mult extends RegisteredSkill
{
	public Dagger_Throw_Mult()
	{	
		super(new Dagger_Throw_Mult_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("count", new LinearValue(4, 0.7));
		addModifier("angle_random", new LinearValue(80, -4, 20, 100));
		addModifier("cooldown", new LinearValue(30, -1));
		addModifier("stamina", new LinearValue(12, 0.3));
	}
}

class Dagger_Throw_Mult_Handler extends MalaSkill implements Listener
{
	public Dagger_Throw_Mult_Handler()
	{
		super(	"DAGGER_THROW_MULT",
				"표창 살포",
				Material.PUMPKIN_SEEDS,
				MsgTBL.PHYSICAL + MsgTBL.PROJECTILE,
				"",
				"&8{count}&7개의 표창을 연달아 던집니다.",
				"&8{angle_random}&7%의 부정확도를 가집니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		RegisteredSkill dagger_skill = MMOCore.plugin.skillManager.getSkill("DAGGER_THROW");
		
		int lv = data.getSkillLevel(dagger_skill);
		double distance = dagger_skill.getModifier("distance", lv);
		double damage = dagger_skill.getModifier("damage", lv);
		double additive = dagger_skill.getModifier("additive", lv);

		double range = cast.getModifier("angle_random") * 0.01;

		if (!Make_Doppel.Try_Doppel_Make(data, this))
		{
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
					new Dagger_Throw_Mult_Skill(data, (int)cast.getModifier("count"), range));
		}
		
		for (Summoned_OBJ so : Summon_Manager.Get_Instance().Get_Summoned_OBJs(data.getPlayer(), "Doppelganger"))
		{
			//for (int i = 0; i < cast.getModifier("count"); i++)
			//{
				LivingEntity as = (LivingEntity)so.entity;
				Vector dir = as.getLocation().getDirection();
				/*dir.add(new Vector(
						range * -0.5 + Math.random() * range, 
						range * -0.5 + Math.random() * range, 
						range * -0.5 + Math.random() * range));
				dir.normalize();*/
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, 
						new Dagger_Throw_Skill(as.getEyeLocation(), data.getPlayer(),
								dir, damage, additive, distance), 0);
			//}
		}
	}
	
	class Dagger_Throw_Mult_Skill implements Runnable
	{
		Player player;
		PlayerData playerdata;
		int count = 0;
		double range;
		double distance, damage, additive;
		public Dagger_Throw_Mult_Skill(PlayerData _playerdata, int _count, double _range)
		{
			playerdata = _playerdata;
			player = playerdata.getPlayer();
			count = _count;
			range = _range;
			
			RegisteredSkill dagger_skill = MMOCore.plugin.skillManager.getSkill("DAGGER_THROW");
			int lv = playerdata.getSkillLevel(dagger_skill);
			distance = dagger_skill.getModifier("distance", lv);
			damage = dagger_skill.getModifier("damage", lv);
			additive = dagger_skill.getModifier("additive", lv);
		}
		
		@Override
		public void run()
		{
			Vector dir = player.getLocation().getDirection();
			dir.add(new Vector(
					range * -0.5 + Math.random() * range, 
					range * -0.5 + Math.random() * range, 
					range * -0.5 + Math.random() * range));
			dir.normalize();
			Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, 
					new Dagger_Throw_Skill(player.getEyeLocation(), player,
							dir, damage, additive, distance));
			
			if (--count > 0)
				Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}
}
