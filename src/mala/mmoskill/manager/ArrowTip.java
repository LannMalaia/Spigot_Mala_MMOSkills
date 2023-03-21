package mala.mmoskill.manager;

import org.bukkit.entity.Player;

import io.lumine.mythic.lib.skill.SkillMetadata;

public class ArrowTip
{
	protected SkillMetadata cast;
	protected String name;
	protected Player player;
	protected double duration;
	
	public ArrowTip(SkillMetadata _cast, String _name, Player _player, double _duration)
	{
		cast = _cast;
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
