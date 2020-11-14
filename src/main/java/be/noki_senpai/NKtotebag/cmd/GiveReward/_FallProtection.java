package be.noki_senpai.NKtotebag.cmd.GiveReward;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.data.RewardedItem;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import be.noki_senpai.NKtotebag.utils.CheckType;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Function;

public class _FallProtection
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;
	RewardManager rewardManager = null;

	public _FallProtection(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	// /givereward fallprotection <playerName>
	public boolean fallProtection(CommandSender sender, String[] args)
	{
		// Check if sender has permission
		if(!hasGiveRewardFallProtectionPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if(args.length < 2)
		{
			sender.sendMessage(ChatColor.GREEN + "/givereward fallprotection <playerName>");
			return true;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(args[1]);
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
			return true;
		}

		if(rewardManager.checkFallProtection(nkPlayer.getId()))
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur est déjà insensible aux dégats de chute !");
			return true;
		}

		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				rewardManager.insertRewardFallProtection(nkPlayer.getId());
				Bukkit.getPlayer(nkPlayer.getPlayerUUID()).sendMessage(ChatColor.GREEN + "Vous avez reçu la " + ChatColor.AQUA + "protection contre les dégats de chute" + ChatColor.GREEN + ".");
				return null;
			}
		});

		return true;
	}

	private boolean hasGiveRewardFallProtectionPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.givereward.fallprotection")
				|| sender.hasPermission("nktotebag.givereward.*") || sender.hasPermission("nktotebag.admin");
	}
}
