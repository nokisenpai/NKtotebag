package be.noki_senpai.NKtotebag.cmd.GiveReward;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.data.RewardedItem;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;

import java.awt.*;
import java.util.function.Function;

/**
 *
 */
public class _Spawner
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;

	public _Spawner(PlayerManager playerManager, QueueManager queueManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
	}

	/**
	 * Execution for "/giwereward spawner" command
	 * @param sender {@link CommandSender}
	 * @param args Command's args
	 * @return always true
	 */
	public boolean spawner(CommandSender sender, String[] args)
	{
		// Check if sender has permission
		if(!hasGiveRewardSpawnerPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if(args.length < 3)
		{
			sender.sendMessage(ChatColor.GREEN + "/givereward spawner <playerName> <spawnerType>");
			return true;
		}

		if(!isValidSpawnerType(args[2]))
		{
			sender.sendMessage(ChatColor.RED + "'" + args[2] + "' n'est pas un type de spawner valide.");
			return true;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(args[1]);
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
			return true;
		}

		ItemStack itemStack = new ItemStack(Material.SPAWNER, 1);

		BlockStateMeta blockMeta = (BlockStateMeta) itemStack.getItemMeta();
		CreatureSpawner spawner = (CreatureSpawner) blockMeta.getBlockState();
		spawner.setSpawnedType(EntityType.valueOf(args[2].toUpperCase()));
		blockMeta.setBlockState(spawner);
		blockMeta.setDisplayName("" + ChatColor.YELLOW + ChatColor.BOLD + "Spawner à " + args[2].toUpperCase());
		blockMeta.getPersistentDataContainer().set(new NamespacedKey(NKtotebag.getPlugin(), "NKprotect"), PersistentDataType.INTEGER, 1);
		itemStack.setItemMeta(blockMeta);

		Player player = Bukkit.getPlayer(args[1]);
		if(player != null)
		{
			Inventory inventory = player.getInventory();
			if(inventory.firstEmpty() != -1)
			{
				inventory.addItem(itemStack);
				Bukkit.getPlayer(nkPlayer.getPlayerUUID()).sendMessage(ChatColor.GREEN + "Vous avez reçu le  " + ChatColor.AQUA + "spawner à " + args[2].toUpperCase() + ChatColor.GREEN + ".");
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

	private boolean hasGiveRewardSpawnerPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.givereward.spawner")
				|| sender.hasPermission("nktotebag.givereward.*") || sender.hasPermission("nktotebag.admin");
	}

	public boolean isValidSpawnerType(String spawnerType)
	{
		for(EntityType entityType : EntityType.values())
		{
			if(entityType.name().equalsIgnoreCase(spawnerType))
			{
				return true;
			}
		}
		return false;
	}
}
