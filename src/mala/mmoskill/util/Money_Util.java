package mala.mmoskill.util;

import org.bukkit.entity.Player;

import mala_mmoskill.main.MalaMMO_Skill;
import net.milkbowl.vault.economy.EconomyResponse;

public class Money_Util
{
	public static boolean Check_Enough_Money(Player _player, double _amount)
	{
		return MalaMMO_Skill.econ.getBalance(_player) >= _amount;
	}
	public static boolean Withdraw_Money(Player _player, double _amount)
	{
		EconomyResponse er = MalaMMO_Skill.econ.withdrawPlayer(_player, _amount);
		if (er.transactionSuccess())
			return true;
		return false;
	}
	public static void Send_Msg_NotEnoughMoney(Player _player)
	{
		_player.sendMessage("§c[ 돈이 충분하지 않습니다. ]");
	}
}
