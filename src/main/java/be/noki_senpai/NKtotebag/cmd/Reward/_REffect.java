package be.noki_senpai.NKtotebag.cmd.Reward;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import be.noki_senpai.NKtotebag.utils.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class _REffect
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;
	RewardManager rewardManager = null;

	public _REffect(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	// /reward effect <tagName>
	public boolean effect(CommandSender sender, String[] args)
	{
		String tagName = null;
		String effectName = null;
		int effectLevel = 1;

		// Check if sender has permission
		if(!hasRewardEffectPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if(args.length < 2)
		{
			sender.sendMessage(ChatColor.GREEN + "/reward effect <tagName>");
			return true;
		}

		tagName = args[1];

		NKPlayer nkPlayer = playerManager.getPlayer(sender.getName());
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Vous semblez ne pas exister. Veuillez contacter un Alpha.");
			return true;
		}

		if(!rewardManager.checkEffectTagName(nkPlayer.getId(), tagName))
		{
			sender.sendMessage(ChatColor.RED + "Vous ne possédez pas d'effet sous ce nom.");
			return true;
		}

		rewardManager.alterEffect(nkPlayer.getId(), tagName, (Player) sender);

		return true;
	}

	private boolean hasRewardEffectPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.reward.effect")
				|| sender.hasPermission("nktotebag.user") || sender.hasPermission("nktotebag.reward.*") || sender.hasPermission("nktotebag.admin");
	}
}
