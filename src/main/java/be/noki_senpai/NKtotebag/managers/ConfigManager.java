package be.noki_senpai.NKtotebag.managers;

import be.noki_senpai.NKmanager.data.DBAccess;
import be.noki_senpai.NKmanager.data.NKServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import be.noki_senpai.NKtotebag.NKtotebag;

public class ConfigManager
{
	private ConsoleCommandSender console = null;
	private FileConfiguration config = null;

	private static DBAccess dbAccess = new DBAccess();

	public static String PREFIX = null;
	public static NKServer SERVERNAME = null;
	public static Boolean SQLRESETFLYTIME = false;

	// Constructor
	public ConfigManager(FileConfiguration config)
	{
		this.console = Bukkit.getConsoleSender();
		this.config = config;
	}

	public boolean loadConfig()
	{
		// Check if "use-mysql" is to true. Plugin only use MySQL database.
		if(config.getBoolean("auto-config-mysql", true))
		{
			// Get database access informations
			dbAccess = NKtotebag.getNKmanagerAPI().getDBAccess();
		}
		else
		{
			// Get database access informations
			dbAccess.setHost(config.getString("host"));
			dbAccess.setPort(config.getInt("port"));
			dbAccess.setDbName(config.getString("dbName"));
			dbAccess.setUser(config.getString("user"));
			dbAccess.setPassword(config.getString("password"));
		}

		// Get prefix used for table name on database
		PREFIX = config.getString("table-prefix", "NKtotebag_");

		// Get server name gave to bungeecord config
		SERVERNAME = NKtotebag.getNKmanagerAPI().getNKServer();

		// Get server name gave to bungeecord config
		SQLRESETFLYTIME = config.getBoolean("sql-reset-flytime", false);

		return true;
	}

	// ######################################
	// Getters (only)
	// ######################################

	public static DBAccess getDbAccess()
	{
		return dbAccess;
	}
}
