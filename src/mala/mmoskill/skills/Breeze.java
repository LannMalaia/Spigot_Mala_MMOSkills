package mala.mmoskill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.result.def.SimpleSkillResult;
import mala.mmoskill.util.MalaSkill;
import mala_mmoskill.main.MalaMMO_Skill;
import mala_mmoskill.main.MsgTBL;
import net.Indyuce.mmocore.skill.RegisteredSkill;
import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.util.math.formula.LinearValue;

public class Breeze extends RegisteredSkill
{
	public static Breeze skill;
	
	public Breeze()
	{	
		super(new Breeze_Handler(), MalaMMO_Skill.plugin.getConfig());

		addModifier("duration", new LinearValue(0.3, 0.05));
		addModifier("cooldown", new LinearValue(16.5, -0.5, 10, 30));
		addModifier("mana", new LinearValue(10, .5));
		
		skill = this;
	}
}

class Breeze_Handler extends MalaSkill implements Listener
{
	public Breeze_Handler()
	{
		super(	"BREEZE",
				"�긮��",
				Material.PAPER,
				10155,
				"&7{duration}�ʰ� �������� �����Դϴ�.",
				"&7���������� �̵��� �����մϴ�.",
				"&7��ũ���� ����� �� �ֽ��ϴ�.",
				"",
				MsgTBL.Cooldown, MsgTBL.ManaCost);
	}

	@Override
	public void whenCast(SimpleSkillResult _data, SkillMetadata cast)
	{
		PlayerData data = MMOCore.plugin.dataProvider.getDataManager().get(cast.getCaster().getPlayer());
		
		double sec = cast.getModifier("duration");
		
		Bukkit.getScheduler().runTask(MalaMMO_Skill.plugin, new Breeze_Skill(data.getPlayer(), sec));
	}

	class Breeze_Skill implements Runnable
	{
		Player player;
		double sec;
	
		int count = 0;
		double velocity = 1.0;
		double angle = 0.0;
		
		
		public Breeze_Skill(Player _player, double _sec)
		{
			player = _player;
			sec = _sec;
			
			angle = player.getLocation().getYaw();

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.5f, 1.5f);
		}
		
		public Location Calculate_Y_Pos(Location _loc)
		{
			Location loc = _loc.clone().add(0.0, 0.5, 0.0);
			Block b = loc.clone().add(loc.getDirection().multiply(0.1)).getBlock();
			if (b.getType().isSolid()) // �ٷ� �տ� �� ����
			{
				b = b.getLocation().add(0.0, 1.0, 0.0).getBlock();
				if (!b.getType().isSolid())
					loc.add(0.0, 0.5, 0.0);
			}
			else // ����ִ� ���
			{
				b = b.getLocation().add(0.0, -1.0, 0.0).getBlock();
				if (b.getType().isSolid()) // �ٷ� �� �ٴڿ� �� ����
				{
					return _loc;
				}
				else // �ٷ� �տ� �� ����
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
				player.sendMessage("��c��l[ �긮�� ��� ]");
				return;
			}
			if (sec <= 0.0)
				return;
			
			// velocity = Math.min(1.4, velocity + 0.05);
			
			Vector dir = new Vector(Math.cos(Math.toRadians(angle + 90)), player.getLocation().getDirection().getY(), Math.sin(Math.toRadians(angle + 90)));
			Vector vc = dir.multiply(velocity);
			player.setVelocity(vc);
			player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.5, 0), 10, 0.3, 0.5, 0.3, 0.0);
			
			Bukkit.getScheduler().runTaskLater(MalaMMO_Skill.plugin, this, 1);
		}
	}

}