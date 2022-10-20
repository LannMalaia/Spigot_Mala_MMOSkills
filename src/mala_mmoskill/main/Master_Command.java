package mala_mmoskill.main;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Master_Command
{
	public static void Show_Mastery_List(Player player)
	{
		Inventory inv = Bukkit.createInventory(null, 54);
		
		for(int i = 0; i < 3; i++)
		{
			Material mat = Material.AIR;
			switch(i)
			{
			case 0:
				mat = Material.LIME_STAINED_GLASS_PANE;
				break;
			case 1:
				mat = Material.RED_STAINED_GLASS_PANE;
				break;
			case 2:
				mat = Material.BLUE_STAINED_GLASS_PANE;
				break;
			}
			ItemStack ui = new ItemStack(mat);
			for(int j = 0; j < 6; j++)
			{
				inv.setItem(j * 9 + i * 3, ui);
				inv.setItem(j * 9 + i * 3 + 1, ui);
				inv.setItem(j * 9 + i * 3 + 2, ui);
			}
		}
		
		player.openInventory(inv);
	}
}