package be.noki_senpai.NKtotebag.cmd.GiveReward;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.data.RewardedItem;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Function;

public class _InfiniteItem
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;

	public _InfiniteItem(PlayerManager playerManager, QueueManager queueManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
	}

	/**
	 * Execution for "/giwereward infiniteItem" command
	 * @param sender {@link CommandSender}
	 * @param args Command's args
	 * @return always true
	 */
	public boolean infiniteItem(CommandSender sender, String[] args)
	{
		// Check if sender has permission
		if(!hasGiveRewardInfiniteItemPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if(args.length < 3)
		{
			sender.sendMessage(ChatColor.GREEN + "/givereward infiniteItem <playerName> <itemType>");
			return true;
		}

		if(!isValidInfiniteItemType(args[2]))
		{
			sender.sendMessage(ChatColor.RED + "'" + args[2] + "' n'est pas un type d'item valide.");
			return true;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(args[1]);
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
			return true;
		}

		ItemStack itemStack = new ItemStack(Material.valueOf(args[2].toUpperCase()), 1);

		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setDisplayName("" + ChatColor.YELLOW + ChatColor.BOLD + "Infinite " + args[2].toUpperCase());
		itemMeta.getPersistentDataContainer().set(new NamespacedKey(NKtotebag.getPlugin(), "NKprotect"), PersistentDataType.INTEGER, 1);
		itemMeta.getPersistentDataContainer().set(new NamespacedKey(NKtotebag.getPlugin(), "NKinfinite"), PersistentDataType.INTEGER, 1);
		itemStack.setItemMeta(itemMeta);

		Player player = Bukkit.getPlayer(args[1]);
		if(player != null)
		{
			Inventory inventory = player.getInventory();
			if(inventory.firstEmpty() != -1)
			{
				inventory.addItem(itemStack);
				Bukkit.getPlayer(nkPlayer.getPlayerUUID()).sendMessage(ChatColor.GREEN + "Vous avez reçu l'item infini " + ChatColor.AQUA + itemStack.getType().name() + ChatColor.GREEN + ".");
			}
			else
			{
				player.sendMessage(ChatColor.GREEN
						+ "Vous n'avez pas de place dans votre inventaire pour recevoir votre récompense. \nLibérez votre inventaire pour la récupérer lors de votre prochaine connexion.");
				nkPlayer.addRewardedItem(new RewardedItem(-1, itemStack));
			}
		}
		else
		{
			nkPlayer.addRewardedItem(new RewardedItem(-1, itemStack));
			queueManager.addToQueue(new Function()
			{
				@Override
				public Object apply(Object o)
				{
					nkPlayer.saveRewardedItem();
					return null;
				}
			});
		}

		return true;
	}

	private boolean hasGiveRewardInfiniteItemPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.givereward.infiniteitem")
				|| sender.hasPermission("nktotebag.givereward.*") || sender.hasPermission("nktotebag.admin");
	}

	public boolean isValidInfiniteItemType(String infiniteItemType)
	{
		for(Material materialType : Material.values())
		{
			if(materialType.name().equalsIgnoreCase(infiniteItemType))
			{
				return true;
			}
		}
		return false;
	}
}
