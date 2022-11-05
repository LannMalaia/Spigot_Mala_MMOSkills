package mala.mmoskill.manager;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustTransition;
import org.bukkit.entity.Player;

import mala.mmoskill.skills.passive.Invoke_Fire;
import mala.mmoskill.skills.passive.Invoke_Fire.FireSpell;
import mala.mmoskill.skills.passive.Invoke_Flame.FlameSpell;
import mala.mmoskill.skills.passive.Invoke_Frost.FrostSpell;
import mala.mmoskill.skills.passive.Invoke_Lightning.LightningSpell;
import mala.mmoskill.skills.passive.Invoke_VaporBlast.VaporBlastSpell;
import mala.mmoskill.util.MalaSpell;
import mala.mmoskill.util.Particle_Drawer_EX;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.skill.RegisteredSkill;

public class CastChain
{
	protected Player player;
	protected double duration;
	protected ArrayList<SpellChainType> chain;
	
	public CastChain(Player _player)
	{
		player = _player;
		chain = new ArrayList<>();
		duration = 5.0;
	}
	
	public void putElement(SpellChainType _type)
	{
		duration = 5.0;
		chain.add(_type);
		String msg = "§6§l[ ";
		for (int i = 0; i < chain.size(); i++)
			msg += SpellChainType.GetName(chain.get(i)) + " ";
		msg += "§6§l]";
		player.sendMessage(msg);
	}
	public void sendDisableMsg()
	{
		player.sendMessage("§6§l[ §f§l캐스팅 해제 §6§l]");
	}
	
	/**
	 * @author jimja
	 * @version 2022. 11. 4.
	 * @apiNote 지속 시간 감소
	 * @param _duration
	 * @return 사라질 때가 됐다면 true
	 */
	public boolean subtractDuration(double _duration)
	{
		duration -= _duration;
		return duration <= 0;
	}

	// 조합식에 맞는 스펠을 찾아 반환
	public MalaSpell findSpell()
	{
		switch (chain.size())
		{
		case 4:
			break;
		case 3:
			break;
		case 2:
			if (chain.get(0) == SpellChainType.FIRE && chain.get(1) == SpellChainType.FIRE)
				return new FlameSpell(PlayerData.get(player));
			if (chain.contains(SpellChainType.FIRE) && chain.contains(SpellChainType.ICE))
				return new VaporBlastSpell(PlayerData.get(player));
			break;
		case 1:
			if (chain.contains(SpellChainType.FIRE))
				return new FireSpell(PlayerData.get(player));
			if (chain.contains(SpellChainType.ICE))
				return new FrostSpell(PlayerData.get(player));
			if (chain.contains(SpellChainType.LIGHTNING))
				return new LightningSpell(PlayerData.get(player));
			break;
		}
		return null;
	}
	
	double test = 0;
	public void draw()
	{
		test += 1;
		for (int i = 0; i < chain.size(); i++)
		{
			SpellChainType sct = chain.get(i);
			switch (sct)
			{
			case FIRE:
				drawFire(i);
				break;
			case ICE:
				drawIce(i);
				break;
			case LIGHTNING:
				drawLightning(i);
				break;
			}
		}
	}
	private void drawFire(int level)
	{
		Location loc = player.getLocation().add(0, 1.5, 0);
		loc.add(loc.getDirection().multiply(2.0));
		DustTransition dts = new DustTransition(Color.ORANGE, Color.BLACK, 0.5f);
		
		switch (level)
		{
		case 0:
			Particle_Drawer_EX.drawCircle(loc, Particle.SMALL_FLAME,
					2.75, loc.getPitch() - 90.0, loc.getYaw(), 3, test * 2.0, 0.01);
			Particle_Drawer_EX.drawCircle(loc, Particle.FLAME,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 0, test, 0.0);
//			Particle_Drawer_EX.drawSquare(loc, dts,
//					2.7, loc.getPitch(), loc.getYaw() + 90, test * 1.5);
//			Particle_Drawer_EX.drawSquare(loc, dts,
//					2.7, loc.getPitch(), loc.getYaw() + 90, -test * 1.5);
			Particle_Drawer_EX.drawTriangle(loc, dts,
					3.4, loc.getPitch() - 90.0, loc.getYaw(), test);
			break;
		case 1:
			//Particle_Drawer_EX.drawCircle(loc, Particle.FLAME,
			//		1.7, loc.getPitch(), loc.getYaw() + 90, 0, test, 0.0);
			Particle_Drawer_EX.drawCircle(loc, Particle.SMALL_FLAME,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), 3, test * -6.0, -0.01);
//			Particle_Drawer_EX.drawCircle(loc, dts,
//					2.2, loc.getPitch(), loc.getYaw() + 90, 0, test);
			Particle_Drawer_EX.drawTriangle(loc, dts,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), -test);
			Particle_Drawer_EX.drawTriangle(loc, dts,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), -test - 180.0);
			break;
		case 2:
			Particle_Drawer_EX.drawCircle(loc, Particle.SOUL,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 8, test, -0.04);
			Particle_Drawer_EX.drawTriangle(loc, dts,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), test * 1.0);
			Particle_Drawer_EX.drawTriangle(loc, dts,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), 180.0 + test * 1.0);
			break;
		case 3:
			Particle_Drawer_EX.drawCircle(loc, Particle.SMOKE_NORMAL,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), 0, test, 0);
			Particle_Drawer_EX.drawCircle(loc, Particle.SMOKE_NORMAL,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), 6, test, -0.04);
			break;
		}
	}
	private void drawIce(int level)
	{
		Location loc = player.getLocation().add(0, 1.5, 0);
		loc.add(loc.getDirection().multiply(2.0));
		DustTransition dts = new DustTransition(Color.fromRGB(180, 180, 255), Color.WHITE, 0.5f);

		switch (level)
		{
		case 0:
			Particle_Drawer_EX.drawCircle(loc, Particle.WATER_WAKE,
					2.75, loc.getPitch() - 90.0, loc.getYaw(), 3, test * 2.0, -0.01);
			Particle_Drawer_EX.drawCircle(loc, Particle.SOUL_FIRE_FLAME,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 0, test, 0.0);
			Particle_Drawer_EX.drawSquare(loc, dts,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), test * 1.5);
			break;
		case 1:
//			Particle_Drawer_EX.drawCircle(loc, dts,
//					1.75, loc.getPitch() - 90.0, loc.getYaw(), 0, test);
			Particle_Drawer_EX.drawCircle(loc, Particle.SOUL_FIRE_FLAME,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), 2, test * -4.0, -0.01);
			Particle_Drawer_EX.drawCircle(loc, Particle.SOUL_FIRE_FLAME,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), 2, test * -4.0, 0.01);
			Particle_Drawer_EX.drawSquare(loc, dts,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), -test);
			Particle_Drawer_EX.drawSquare(loc, dts,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), -test - 45.0);
			break;
		case 2:
			Particle_Drawer_EX.drawCircle(loc, Particle.CRIT,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 4, test * 2.5, -0.25);
			Particle_Drawer_EX.drawSquare(loc, dts,
					3.8, loc.getPitch() - 90.0, loc.getYaw(), test * 1.0);
			Particle_Drawer_EX.drawSquare(loc, dts,
					3.8, loc.getPitch() - 90.0, loc.getYaw(), test * 1.0 + 45.0);
			break;
		case 3:
			Particle_Drawer_EX.drawCircle(loc, Particle.SNOWFLAKE,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), 0, test, 0);
			Particle_Drawer_EX.drawCircle(loc, Particle.SNOWFLAKE,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), 6, test, -0.05);
			break;
		}
	}
	private void drawLightning(int level)
	{
		Location loc = player.getLocation().add(0, 1.5, 0);
		loc.add(loc.getDirection().multiply(2.0));
		DustTransition dts = new DustTransition(Color.fromRGB(255, 255, 180), Color.YELLOW, 0.5f);

		switch (level)
		{
		case 0:
			Particle_Drawer_EX.drawCircle(loc, Particle.CRIT,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 3, test * 6.0, 0.15);
			Particle_Drawer_EX.drawCircle(loc, Particle.CRIT,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 3, test * 6.0, -0.25);
			Particle_Drawer_EX.drawCircle(loc, Particle.ELECTRIC_SPARK,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 0, test, 0.0);
			break;
		case 1:
			Particle_Drawer_EX.drawCircle(loc, dts,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), 0, test);
			Particle_Drawer_EX.drawStar(loc, dts,
					1.75, loc.getPitch() - 90.0, loc.getYaw(), test * -3.0);
			break;
		case 2:
			Particle_Drawer_EX.drawCircle(loc, Particle.CRIT_MAGIC ,
					2.7, loc.getPitch() - 90.0, loc.getYaw(), 4, test * 4.0, 0.7);
			Particle_Drawer_EX.drawStar(loc, dts,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), test * 1.0);
			break;
		case 3:
			Particle_Drawer_EX.drawCircle(loc, Particle.END_ROD,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), 5, test, -0.04);
			Particle_Drawer_EX.drawCircle(loc, Particle.END_ROD,
					4.8, loc.getPitch() - 90.0, loc.getYaw(), 0, test, 0);
			break;
		}
	}


}
