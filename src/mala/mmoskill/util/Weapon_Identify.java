package mala.mmoskill.util;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.Type;
import io.lumine.mythic.lib.api.item.NBTItem;

public class Weapon_Identify
{

	public static boolean Weapon_Restrict(Player _player)
	{
		if (_player.getInventory().getItemInMainHand() == null)
			return false;
		if (_player.getInventory().getItemInOffHand() == null)
			return false;

		boolean main_null = true, off_null = true;
		boolean main_is_two_handed = false, off_is_two_handed = false;
		
		ItemStack main_item = _player.getInventory().getItemInMainHand();
		NBTItem main_nbt = NBTItem.get(main_item);
		if (main_nbt == null)
			main_null = true;
		else // 뭔가 들고 있기는 함
		{
			main_null = false;
			if (main_nbt.getTags().contains("MMOITEMS_TWO_HANDED"))
				main_is_two_handed = true;
		}
		//_player.sendMessage("main = " + main_nbt.getTags().toString());
		
		ItemStack off_item = _player.getInventory().getItemInOffHand();
		// _player.sendMessage("off = " + off_item.toString());
		NBTItem off_nbt = NBTItem.get(off_item);
		if (off_item.getType() == Material.AIR)
			off_null = true;
		else if (off_nbt == null)
			off_null = true;
		else
		{
			off_null = false;
			if (off_nbt.getTags().contains("MMOITEMS_TWO_HANDED"))
				off_is_two_handed = true;
		}

		/*
		_player.sendMessage("main two = " + main_is_two_handed
				+ "\noff two = " + off_is_two_handed
				+ "\nmain null = " + main_null
				+ "\noff null = " + off_null);
				*/
		
		if (!off_null && !main_null)
		{
			if (main_is_two_handed || off_is_two_handed)
				return true;
		}
		return false;
	}
	
	// 맨 주먹이여?
	public static boolean Has_No_Item(Player _player)
	{
		return _player.getInventory().getItemInMainHand().getType() == Material.AIR
				&& _player.getInventory().getItemInOffHand().getType() == Material.AIR;
	}	
	// 이게 창이여?
	public static boolean Hold_MMO_Spear(Player _player)
	{
		if (_player.getInventory().getItemInMainHand() == null)
			return false;
		
		ItemStack item = _player.getInventory().getItemInMainHand();
		NBTItem nbt = NBTItem.get(item);
		if (nbt == null)
			return false;
		if (!nbt.hasType())
			return false;
		
		if (nbt.getType().matches(Type.SPEAR.getId()))
			return true;

		return false;
	}
	// 이게 창이여?
	public static boolean Hold_Spear(Player _player)
	{
		ItemStack main_hand = _player.getInventory().getItemInMainHand();
		if (main_hand == null)
			return false;

		switch(main_hand.getType())
		{
		case TRIDENT:
			return true;
		default:
			break;
		}
		return false;
	}

	// 이게 검이여?
	public static boolean Hold_MMO_Sword(Player _player)
	{
		if (_player.getInventory().getItemInMainHand() == null)
			return false;
		
		ItemStack item = _player.getInventory().getItemInMainHand();
		NBTItem nbt = NBTItem.get(item);
		if (nbt == null)
			return false;
		if (!nbt.hasType())
			return false;
		
		if (nbt.getType().matches(Type.SWORD.getId())
				|| nbt.getType().matches(Type.DAGGER.getId())
				|| nbt.getType().matches(Type.get("GREATSWORD").getId()))
			return true;

		return false;
	}
	// 이게 검이여?
	public static boolean Hold_Sword(Player _player)
	{
		ItemStack main_hand = _player.getInventory().getItemInMainHand();
		if (main_hand == null)
			return false;

		switch(main_hand.getType())
		{
		case WOODEN_SWORD:
		case STONE_SWORD:
		case IRON_SWORD:
		case GOLDEN_SWORD:
		case DIAMOND_SWORD:
		case NETHERITE_SWORD:
			return true;
		default:
			break;
		}
		return false;
	}

	// 이게 도끼여?
	public static boolean Hold_Axe(Player _player)
	{
		ItemStack main_hand = _player.getInventory().getItemInMainHand();
		if (main_hand == null)
			return false;

		switch(main_hand.getType())
		{
		case WOODEN_AXE:
		case STONE_AXE:
		case IRON_AXE:
		case GOLDEN_AXE:
		case DIAMOND_AXE:
		case NETHERITE_AXE:
			return true;
		default:
			break;
		}
		return false;
	}

	// 이게 채찍이여?
	public static boolean Hold_MMO_Whip(Player _player)
	{
		if (_player.getInventory().getItemInMainHand() == null)
			return false;
		
		ItemStack item = _player.getInventory().getItemInMainHand();
		NBTItem nbt = NBTItem.get(item);
		if (nbt == null)
			return false;
		if (!nbt.hasType())
			return false;
		
		if (nbt.getType().matches(Type.WHIP.getId()))
			return true;

		return false;
	}
	
	// 이게 석궁이여?
	public static boolean Hold_Crossbow(Player _player)
	{
		ItemStack main_hand = _player.getInventory().getItemInMainHand();
		if (main_hand == null)
			return false;

		switch(main_hand.getType())
		{
		case CROSSBOW:
			return true;
		default:
			break;
		}
		return false;
	}
	// 이게 활이여?
	public static boolean Hold_Bow(Player _player)
	{
		ItemStack main_hand = _player.getInventory().getItemInMainHand();
		if (main_hand == null)
			return false;

		switch(main_hand.getType())
		{
		case BOW:
			return true;
		default:
			break;
		}
		return false;
	}

	// 이게 방패여?
	public static boolean Hold_Shield(Player _player)
	{
		ItemStack hand = _player.getInventory().getItemInOffHand();
		if (hand != null)
		{
			switch(hand.getType())
			{
			case SHIELD:
				return true;
			default:
				break;
			}
		}
		ItemStack off_hand = _player.getInventory().getItemInOffHand();
		if (off_hand == null)
			return false;

		switch(off_hand.getType())
		{
		case SHIELD:
			return true;
		default:
			break;
		}
		return false;
	}

	
}
