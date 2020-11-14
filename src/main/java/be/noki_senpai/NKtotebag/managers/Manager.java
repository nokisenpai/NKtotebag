package be.noki_senpai.NKtotebag.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import be.noki_senpai.NKtotebag.NKtotebag;

public class Manager
{
	private ConsoleCommandSender console = null;
	private ConfigManager configManager = null;
	private DatabaseManager databaseManager = null;
	private PlayerManager playerManager = null;
	private QueueManager queueManager = null;
	private RewardManager rewardManager = null;

	public Manager(NKtotebag instance)
	{
		console = Bukkit.getConsoleSender();
		configManager = new ConfigManager(instance.getConfig());
		databaseManager = new DatabaseManager(configManager);
		rewardManager = new RewardManager();
		playerManager = new PlayerManager(rewardManager);
		queueManager = new QueueManager();
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// Console
	public ConsoleCommandSender getConsole()
	{
		return console;
	}

	// PluginManager
	public ConfigManager getConfigManager()
	{
		return configManager;
	}

	// DatabaseManager
	public DatabaseManager getDatabaseManager()
	{
		return databaseManager;
	}

	// PlayerManager
	public PlayerManager getPlayerManager()
	{
		return playerManager;
	}

	// QueueManager
	public QueueManager getQueueManager()
	{
		return queueManager;
	}

	// RewardManager
	public RewardManager getRewardManager()
	{
		return rewardManager;
	}
}
