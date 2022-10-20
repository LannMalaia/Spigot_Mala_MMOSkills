package mala.mmoskill.manager;

import org.bukkit.entity.Player;

public class ArrowTip
{
	protected String name;
	protected Player player;
	protected double duration;
	
	public ArrowTip(String _name, Player _player, double _duration)
	{
		name = _name;
		player = _player;
		duration = _duration;
	}
	
	public void Send_Enable_Msg()
	{
		player.sendMessage("§b§l[ " + name + " 장전 ]");
	}
	public void Send_Disable_Msg()
	{
		player.sendMessage("§7§l[ " + name + " 장전 해제 ]");
	}
	
	/**
	 * @author jimja
	 * @version 2021. 10. 16.
	 * @apiNote 지속 시간 감소
	 * @param _duration
	 * @return 사라질 때가 됐다면 true
	 */
	public boolean Subtract_Duration(double _duration)
	{
		duration -= _duration;
		return duration <= 0;
	}
	public void Run()
	{
		
	}
}
