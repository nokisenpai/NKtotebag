package be.noki_senpai.NKtotebag.data;

import org.bukkit.inventory.ItemStack;

public class RewardedItem
{
	private int id = -1;
	private ItemStack item = null;

	public RewardedItem(int id, ItemStack item)
	{
		this.id = id;
		this.item = item;
	}

	public int getId()
	{
		return id;
	}

	public ItemStack getItem()
	{
		return item;
	}
}
