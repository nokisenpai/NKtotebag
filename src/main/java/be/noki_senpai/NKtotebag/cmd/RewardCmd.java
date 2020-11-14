package be.noki_senpai.NKtotebag.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import be.noki_senpai.NKtotebag.cmd.GiveReward.*;
import be.noki_senpai.NKtotebag.cmd.Reward._REffect;
import be.noki_senpai.NKtotebag.cmd.Reward._RGrowArea;
import be.noki_senpai.NKtotebag.managers.ConfigManager;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.utils.CheckType;
import be.noki_senpai.NKtotebag.utils.CoordTask;
import org.bukkit.scheduler.BukkitRunnable;

public class RewardCmd implements CommandExecutor
{
	private PlayerManager playerManager = null;
	private QueueManager queueManager = null;
	private RewardManager rewardManager = null;

	public RewardCmd(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	@Override public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args)
	{
		String playerName = null;
		String homeName = null;
		// Command called by a player
		if(sender instanceof Player)
		{
			if(!hasRewardPermissions(sender))
			{
				// Send that the player does not have the permission
				sender.sendMessage(ChatColor.RED + " Vous n'avez pas la permission !");
				return true;
			}

			// if no argument
			if (args.length == 0)
			{
				sender.sendMessage(ChatColor.GREEN + "/reward <rewardType> <tagName>");
				return true;
			}

			args[0] = args[0].toLowerCase();
			switch (args[0])
			{
				case "effect":
					return new _REffect(playerManager, queueManager, rewardManager).effect(sender, args);
				case "growarea":
					return new _RGrowArea(playerManager, queueManager, rewardManager).growArea(sender, args);
				default:
					sender.sendMessage(ChatColor.GREEN + "/reward <rewardType> <tagName>");
					return true;
			}
			//return true;
		}

		// Command called by Console
		if(sender instanceof ConsoleCommandSender)
		{
			sender.sendMessage(ChatColor.RED + " Vous ne pouvez pas utiliser cette commande dans la console.");
			return true;
		}

		return true;
	}

	private boolean hasRewardPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.reward")
				|| sender.hasPermission("nktotebag.user") || sender.hasPermission("nktotebag.admin");
	}
}
