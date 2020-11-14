package be.noki_senpai.NKtotebag.cmd;

import be.noki_senpai.NKtotebag.cmd.GiveReward.*;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class GiveRewardCmd implements CommandExecutor
{
	private PlayerManager playerManager = null;
	private QueueManager queueManager = null;
	private RewardManager rewardManager = null;

	public GiveRewardCmd(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	@Override public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		// /givereward <rewardType> <playerName> <tagName> <effectName|flyDuration|nbChunk|itemName> [effectLevel]
		String playerName = null;
		String homeName = null;
		// Command called by a player
		if(sender instanceof Player || sender instanceof ConsoleCommandSender)
		{
			// Check if sender has permission
			if(!hasGiveRewardPermissions(sender))
			{
				sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
				return true;
			}

			// if no argument
			if (args.length == 0)
			{
				sender.sendMessage(ChatColor.GREEN + "/givereward <rewardType> <playerName> <tagName|spawnerType> <effectName|flyDuration|nbChunk|itemName> [effectLevel]");
				return true;
			}

			args[0] = args[0].toLowerCase();
			switch (args[0])
			{
				case "effect":
					return new _Effect(playerManager, queueManager, rewardManager).effect(sender, args);
				case "fly":
					return new _Fly(playerManager, queueManager, rewardManager).fly(sender, args);
				case "growarea":
					return new _GrowArea(playerManager, queueManager, rewardManager).growArea(sender, args);
				case "infiniteitem":
					return new _InfiniteItem(playerManager, queueManager).infiniteItem(sender, args);
				case "fallprotection":
					return new _FallProtection(playerManager, queueManager, rewardManager).fallProtection(sender, args);
				case "spawner":
					return new _Spawner(playerManager, queueManager).spawner(sender, args);
				default:
					sender.sendMessage(ChatColor.RED + "Bad reward type");
					return true;
			}
		}

		return true;
	}

	private boolean hasGiveRewardPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.givereward")
				|| sender.hasPermission("nktotebag.admin");
	}
}
