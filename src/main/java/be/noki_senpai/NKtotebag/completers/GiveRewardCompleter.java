package be.noki_senpai.NKtotebag.completers;

import be.noki_senpai.NKtotebag.managers.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GiveRewardCompleter implements TabCompleter
{

	private PlayerManager playerManager = null;
	List<String> COMMANDS = Arrays.asList("effect", "fly", "growArea", "infiniteItem", "spawner", "fallProtection");

	public GiveRewardCompleter(PlayerManager playerManager)
	{
		this.playerManager = playerManager;
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
		}
		return null;
	}

}
