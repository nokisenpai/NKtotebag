package be.noki_senpai.NKtotebag.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import be.noki_senpai.NKtotebag.NKtotebag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import be.noki_senpai.NKtotebag.data.NKPlayer;
import org.bukkit.entity.Player;

public class PlayerManager
{
	// Players datas
	private Map<String, NKPlayer> players = null;
	private ConsoleCommandSender console = null;
	private RewardManager rewardManager = null;

	public PlayerManager(RewardManager rewardManager)
	{
		this.players = new TreeMap<String, NKPlayer>(String.CASE_INSENSITIVE_ORDER);
		this.console = Bukkit.getConsoleSender();
		this.rewardManager = rewardManager;
	}

	public void loadPlayer()
	{
		// Get all connected players
		Bukkit.getOnlinePlayers().forEach(player -> addPlayer(player));
	}

	public void unloadPlayer()
	{
		players.clear();
	}

	// ######################################
	// Getters & Setters
	// ######################################

	// getPlayer
	public NKPlayer getPlayer(String playerName)
	{
		if(players.containsKey(playerName))
		{
			return players.get(playerName);
		}
		else
		{
			Connection bdd = null;
			ResultSet resultat = null;
			PreparedStatement ps = null;
			String req = null;

			try
			{
				bdd = DatabaseManager.getConnection();

				req = "SELECT id, name, uuid FROM " + DatabaseManager.common.PLAYERS + " WHERE name = ?";
				ps = bdd.prepareStatement(req);
				ps.setString(1, playerName);
				resultat = ps.executeQuery();

				if(resultat.next())
				{
					return new NKPlayer(UUID.fromString(resultat.getString("uuid")));
				}
			}
			catch(SQLException e1)
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while getting a player.");
			}
		}
		return null;
	}

	public void addPlayer(Player player)
	{
		NKPlayer nkPlayer = new NKPlayer(player.getUniqueId());
		players.put(player.getName(), nkPlayer);
		rewardManager.loadData(nkPlayer);
	}

	public void delPlayer(String playerName)
	{
		int playerId = getPlayer(playerName).getId();
		rewardManager.unloadEffect(playerId);
		rewardManager.unloadFallProtection(playerId);
		rewardManager.saveFlyTime(playerId);
		rewardManager.unloadFly(playerId);
		players.remove(playerName);
	}

	// Check if a player is connected in other server
	public String getOtherServer(String playername)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		ResultSet resultat = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT server FROM " + DatabaseManager.common.PLAYERS + " WHERE name = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, playername);

			resultat = ps.executeQuery();

			if(resultat.next())
			{
				String server = resultat.getString("server");

				resultat.close();
				ps.close();

				return server;
			}
			resultat.close();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
