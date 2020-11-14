package be.noki_senpai.NKtotebag.completers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class RewardCompleter implements TabCompleter
{
	private PlayerManager playerManager = null;
	private RewardManager rewardManager = null;

	List<String> COMMANDS = Arrays.asList("growArea", "effect");

	public RewardCompleter(PlayerManager playerManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.rewardManager = rewardManager;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if(sender instanceof Player)
		{
			if(args.length <= 1)
			{
				final List<String> completions = new ArrayList<>();
				org.bukkit.util.StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
				Collections.sort(completions);
				return completions;
			}
			else if(args.length <= 2)
			{
				if(args[0].equalsIgnoreCase("effect"))
				{
					final List<String> completions = new ArrayList<>();
					final List<String> tagNames = rewardManager.getEffectTagName(playerManager.getPlayer(sender.getName()).getId());
					if(tagNames != null)
					{
						org.bukkit.util.StringUtil.copyPartialMatches(args[1], tagNames, completions); Collections.sort(completions);
						return completions;
					}
				}
			}
		}
		return null;
	}
}