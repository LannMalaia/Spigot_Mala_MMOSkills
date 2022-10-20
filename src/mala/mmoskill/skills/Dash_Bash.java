package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Dash_Bash extends RegisteredSkill
{
	public Dash_Bash()
	{	
		super(new Dash_Bash_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("duration", new LinearValue(0.2, 0.01));
		addModifier("dam_per", new LinearValue(22, 2, 0, 300));
		addModifier("cooldown", new LinearValue(6.0, 0.0));
		addModifier("stamina", new LinearValue(8.0, .3));
	}
}

class Dash_Bash_Handler extends MalaSkill implements Listener
{
	public Dash_Bash_Handler()
	{
		super(	"DASH_BASH",
				"질주 강격",
				Material.FEATHER,
				"&7{duration}초간 전방으로 움직이며 다음 공격을 강화합니다.",
				"&7다음 공격은 &e{dam_per}&7%의 추가 피해를 줍니다.",
				"&7웅크려서 취소할 수 있습니다.",
				"",
				MsgTBL.Cooldown, MsgTBL.StaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		double dam_per = cast.getModifier("dam_per"); // 피해 증가치
		double sec = cast.getModifier("duration");

		data.getPlayer().sendMessage("§b§l[ 질주 강격 준비 ]");
		// 효과
		data.getPlayer().getWorld().playSound(data.getPlayer().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
		data.getPlayer().getWorld().spawnParticle(Particle.LAVA, data.getPlayer().getLocation().add(0, data.getPlayer().getHeight() / 2, 0), 20, 0, 0, 0, 0);
		
		data.getPlayer().setMetadata("malammo.skill.dash_bash", new FixedMetadataValue(MalaMMO_Skill.plugin, dam_per));
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Dash_Skill(data.getPlayer(), sec));
	}

	@EventHandler
	public void bash_attack(PlayerAttackEvent event)
	{
		if(!event.getPlayer().hasMetadata("malammo.skill.dash_bash"))
			return;

		event.getPlayer().sendMessage("§c§l[ 질주 강격 발동 ]");
		double per = event.getPlayer().getMetadata("malammo.skill.dash_bash").get(0).asDouble() * 0.01d;
		event.getAttack().getDamage().multiplicativeModifier(1.0 + per, DamageType.PHYSICAL);
		event.getPlayer().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
		event.getPlayer().removeMetadata("malammo.skill.dash_bash", MalaMMO_Skill.plugin);
	}
	
	class Dash_Skill implements Runnable
	{
		Player player;
		double sec;
	
		int count = 0;
		double velocity = 1.0;
		double angle = 0.0;
		
		
		public Dash_Skill(Player _player, double _sec)
		{
			player = _player;
			sec = _sec;
			
			angle = player.getLocation().getYaw();

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 2.0f);
		}
		
		public Location Calculate_Y_Pos(Location _loc)
		{
			Location loc = _loc.clone().add(0.0, 0.5, 0.0);
			Block b = loc.clone().add(loc.getDirection().multiply(0.1)).getBlock();
			if (b.getType().isSolid()) // 바로 앞에 블럭 있음
			{
				b = b.getLocation().add(0.0, 1.0, 0.0).getBlock();
				if (!b.getType().isSolid())
					loc.add(0.0, 0.5, 0.0);
			}
			else // 비어있는 경우
			{
				b = b.getLocation().add(0.0, -1.0, 0.0).getBlock();
				if (b.getType().isSolid()) // 바로 앞 바닥에 블럭 있음
				{
					return _loc;
				}
				else // 바로 앞에 블럭 없음
				{
					b = b.getLocation().add(0.0, -1.0, 0.0).getBlock();
					if (!b.getType().isSolid())
						return null;
					loc.add(0.0, -1.5, 0.0);
				}
			}
			
			return loc;
		}
		
		void Slerp()
		{
			double new_angle = player.getLocation().getYaw();
			
			double gap = new_angle - angle;
			if (gap < -180.0)
				gap += 360.0;
			else if (gap > 180.0)
				gap -= 360.0;
			
			gap = Math.min(2.0, Math.max(-2.0, gap));
			angle += gap;
			
			// Bukkit.broadcastMessage("gap = " + gap);
		}
		
		public void run()
		{
			sec -= 0.05;
			count += 1;
			Slerp();
			if (player.isSneaking())
			{
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1.0f, 1.0f);;
				player.sendMessage("§c§l[ 질주 취소 ]");
				return;
			}
			if (sec <= 0.0)
				return;
			
			// velocity = Math.min(1.4, velocity + 0.05);
			
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)), 0, Math.sin(Math.toRadians(angle + 90)));
			Vector vc = dir.multiply(velocity);
			player.setVelocity(vc);
			player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, player.getLocation().add(0, 0.5, 0), 10, 0.3, 0.5, 0.3, 0.0);
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

}