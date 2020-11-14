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

public class _GrowArea
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;
	RewardManager rewardManager = null;

	public _GrowArea(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	// /givereward growarea <playerName>
	public boolean growArea(CommandSender sender, String[] args)
	{
		// Check if sender has permission
		if(!hasGiveRewardGrowAreaPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if(args.length < 2)
		{
			sender.sendMessage(ChatColor.GREEN + "/givereward growarea <playerName>");
			return true;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(args[1]);
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
			return true;
		}

		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				if(rewardManager.checkGrowArea(nkPlayer.getId()))
				{
					sender.sendMessage(
							"" + ChatColor.RED + "Le joueur " + ChatColor.AQUA + nkPlayer.getPlayerName() + ChatColor.RED + " possède déjà un chunk de pousse instantané.");
					return null;
				}
				rewardManager.insertRewardGrowArea(nkPlayer.getId());
				sender.sendMessage(
						"" + ChatColor.GREEN + "Le joueur " + ChatColor.AQUA + nkPlayer.getPlayerName() + ChatColor.GREEN + " a obtenu un chunk de pousse instantané.");
				Bukkit.getPlayer(nkPlayer.getPlayerUUID()).sendMessage(ChatColor.GREEN + "Vous avez reçu un " + ChatColor.AQUA + "growArea" + ChatColor.GREEN + ".");
				return null;
			}
		});


		return true;
	}

	private boolean hasGiveRewardGrowAreaPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.givereward.growarea")
				|| sender.hasPermission("nktotebag.givereward.*") || sender.hasPermission("nktotebag.admin");
	}
}
