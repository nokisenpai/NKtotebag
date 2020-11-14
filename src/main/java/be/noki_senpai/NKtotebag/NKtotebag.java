package be.noki_senpai.NKtotebag;

import be.noki_senpai.NKtotebag.cmd.*;
import be.noki_senpai.NKtotebag.completers.GiveRewardCompleter;
import be.noki_senpai.NKtotebag.completers.RewardCompleter;
import be.noki_senpai.NKtotebag.listeners.PlayerConnectionListener;
import be.noki_senpai.NKtotebag.listeners.ProtectedItemListener;
import be.noki_senpai.NKtotebag.listeners.TotebagListener;
import be.noki_senpai.NKtotebag.managers.Manager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class NKtotebag extends JavaPlugin
{
	public final static String PNAME = "[NKtotebag]";
	private Manager manager = null;
	private ConsoleCommandSender console = null;
	private static NKtotebag plugin = null;

	// Fired when plugin is first enabled
	@Override public void onEnable()
	{
		plugin = this;
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "WARN");
		this.saveDefaultConfig();

		console = Bukkit.getConsoleSender();
		manager = new Manager(this);

		if(!checkNKmanager())
		{
			console.sendMessage(ChatColor.DARK_RED + PNAME + " NKmanager in not enabled !");
			disablePlugin();
			return;
		}

		// Load configuration
		if(!manager.getConfigManager().loadConfig())
		{
			disablePlugin();
			return;
		}

		// Load database connection (with check)
		if(!manager.getDatabaseManager().loadDatabase())
		{
			disablePlugin();
			return;
		}

		manager.getRewardManager().loadData();

		// Load homes for online players
		manager.getPlayerManager().loadPlayer();



		// Register listeners
		getServer().getPluginManager().registerEvents(new PlayerConnectionListener(manager.getPlayerManager(), manager.getQueueManager(), manager.getRewardManager()), this);
		getServer().getPluginManager().registerEvents(new TotebagListener(manager.getPlayerManager(), manager.getRewardManager(), manager.getQueueManager()), this);
		getServer().getPluginManager().registerEvents(new ProtectedItemListener(manager.getPlayerManager()), this);

		// Set tabulation completers
		getCommand("givereward").setTabCompleter(new GiveRewardCompleter(manager.getPlayerManager()));
		getCommand("reward").setTabCompleter(new RewardCompleter(manager.getPlayerManager(), manager.getRewardManager()));

		// Register commands
		getCommand("givereward").setExecutor(new GiveRewardCmd(manager.getPlayerManager(), manager.getQueueManager(), manager.getRewardManager()));
		getCommand("reward").setExecutor(new RewardCmd(manager.getPlayerManager(), manager.getQueueManager(), manager.getRewardManager()));

		console.sendMessage(ChatColor.WHITE + "     .--. ");
		console.sendMessage(ChatColor.WHITE + "     |   '.   " + ChatColor.GREEN + PNAME + " by NoKi_senpai - successfully enabled !");
		console.sendMessage(ChatColor.WHITE + "'-..____.-'");
	}

	// Fired when plugin is disabled
	@Override public void onDisable()
	{
		manager.getDatabaseManager().unloadDatabase();
		manager.getPlayerManager().unloadPlayer();
		console.sendMessage(ChatColor.GREEN + PNAME + " has been disable.");
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Getter 'plugin'
	public static NKtotebag getPlugin()
	{
		return plugin;
	}

	// ######################################
	// Disable this plugin
	// ######################################

	public void disablePlugin()
	{
		getServer().getPluginManager().disablePlugin(this);
	}

	// ######################################
	// Check if NKmanager is enabled
	// ######################################

	public boolean checkNKmanager()
	{
		return getServer().getPluginManager().getPlugin("NKmanager").isEnabled();
	}
}
