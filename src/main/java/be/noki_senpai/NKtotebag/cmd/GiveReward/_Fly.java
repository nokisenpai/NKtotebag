package be.noki_senpai.NKtotebag.cmd.GiveReward;

import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import be.noki_senpai.NKtotebag.utils.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class _Fly
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;
	RewardManager rewardManager = null;

	public _Fly(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	// /givereward fly <playerName> <duration>
	public boolean fly(CommandSender sender, String[] args)
	{
		int duration = 0;
		// Check if sender has permission
		if(!hasGiveRewardFlyPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if(args.length < 3)
		{
			sender.sendMessage(ChatColor.GREEN + "/givereward fly <playerName> <duration>");
			return true;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(args[1]);
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
			return true;
		}

		if(CheckType.isNumber(args[2]))
		{
			duration = Integer.parseInt(args[2]);
			if(duration == 0)
			{
				duration = 1;
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "La durée de fly doit être un nombre.");
			return true;
		}

		int finalDuration = duration;
		Player player = Bukkit.getPlayer(nkPlayer.getPlayerUUID());
		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				rewardManager.insertRewardFly(nkPlayer.getId(), finalDuration, player);
				Bukkit.getPlayer(nkPlayer.getPlayerUUID()).sendMessage(ChatColor.GREEN + "Vous avez reçu " + ChatColor.AQUA + finalDuration + ChatColor.GREEN + " seconde(s) de fly.");
				return null;
			}
		});
		sender.sendMessage(
				"" + ChatColor.GREEN + "La durée de fly de " + ChatColor.AQUA + nkPlayer.getPlayerName() + ChatColor.GREEN + " a été augmentée de "
						+ ChatColor.AQUA + duration + " seconde(s).");

		return true;
	}

	private boolean hasGiveRewardFlyPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.givereward.fly")
				|| sender.hasPermission("nktotebag.givereward.*") || sender.hasPermission("nktotebag.admin");
	}
}
