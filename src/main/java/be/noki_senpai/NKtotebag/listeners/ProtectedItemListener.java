package be.noki_senpai.NKtotebag.listeners;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.RewardedItem;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Iterator;
import java.util.List;

public class ProtectedItemListener implements Listener
{
	private PlayerManager playerManager = null;

	public ProtectedItemListener(PlayerManager playerManager)
	{
		this.playerManager = playerManager;
	}

	// ######################################
	// on Drop item
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onDropItem(PlayerDropItemEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(event.getItemDrop().getItemStack().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(NKtotebag.getPlugin(), "NKprotect"), PersistentDataType.INTEGER))
		{
			event.setCancelled(true);
		}
	}

	// ######################################
	// on Death
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerDeathEvent event)
	{
		List<ItemStack> l = event.getDrops();
		Iterator<ItemStack> i = l.iterator();

		while(i.hasNext())
		{
			ItemStack item = i.next();
			if(item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(NKtotebag.getPlugin(), "NKprotect"), PersistentDataType.INTEGER))
			{
				i.remove();
				playerManager.getPlayer(event.getEntity().getName()).addRewardedItem(new RewardedItem(-1, item));
			}
		}
	}

	// ######################################
	// on Revive
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onDeath(PlayerRespawnEvent event)
	{
		playerManager.getPlayer(event.getPlayer().getName()).checkLostRewardedItems(event.getPlayer().getInventory());
	}
}
