package be.noki_senpai.NKtotebag.cmd.Reward;

import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class _RGrowArea
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;
	RewardManager rewardManager = null;

	public _RGrowArea(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	// /reward growarea
	public boolean growArea(CommandSender sender, String[] args)
	{
		// Check if sender has permission
		if(!hasRewardGrowAreaPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(sender.getName());
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Vous semblez ne pas exister. Veuillez contacter un Alpha.");
			return true;
		}

		if(!rewardManager.checkGrowArea(nkPlayer.getId()))
		{
			sender.sendMessage(ChatColor.RED + "Vous ne possédez pas de growArea.");
			return true;
		}

		Chunk chunk = ((Player) sender).getLocation().getChunk();

		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				rewardManager.updateGrowArea(nkPlayer.getId(), chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
				rewardManager.displayGrowArea(chunk.getWorld(), chunk.getX(),chunk.getZ(), ((Player) sender).getLocation().getY());
				return null;
			}
		});

		return true;
	}

	private boolean hasRewardGrowAreaPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.reward.growarea")
				|| sender.hasPermission("nktotebag.user") || sender.hasPermission("nktotebag.reward.*") || sender.hasPermission("nktotebag.admin");
	}
}
