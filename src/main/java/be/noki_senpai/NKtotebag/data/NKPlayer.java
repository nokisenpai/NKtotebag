package be.noki_senpai.NKtotebag.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import be.noki_senpai.NKtotebag.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import be.noki_senpai.NKtotebag.NKtotebag;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;

public class NKPlayer
{
	private int id;
	private UUID playerUUID;
	private String playerName;

	private List<RewardedItem> rewardedItems = new ArrayList<RewardedItem>();

	public NKPlayer(UUID UUID)
	{
		setPlayerUUID(UUID);
		setPlayerName(Bukkit.getOfflinePlayer(playerUUID).getName());

		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;
		Integer homeTp = null;
		try
		{
			bdd = DatabaseManager.getConnection();

			// Get 'id', 'uuid', 'name', 'amount' and 'home_tp' from database
			req = "SELECT id, name FROM " + DatabaseManager.common.PLAYERS + " WHERE uuid = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, getPlayerUUID().toString());

			resultat = ps.executeQuery();

			// If there is a result account exist
			if(resultat.next())
			{
				setId(resultat.getInt("id"));
				String tmpName = resultat.getString("name");
			}
			else
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while setting a player. (#1)");
			}
			ps.close();
			resultat.close();

			// ######################################
			// Load items data
			// ######################################

			req = "SELECT id, item FROM " + DatabaseManager.table.ITEMS + " WHERE player_id = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, getId());
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				YamlConfiguration restoreConfig = new YamlConfiguration();
				restoreConfig.loadFromString(resultat.getString("item"));
				rewardedItems.add(new RewardedItem(resultat.getInt("id"), restoreConfig.getItemStack("item")));
			}

			ps.close();
			resultat.close();


		}
		catch(SQLException | InvalidConfigurationException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while getting a player. (Error#data.Players.000)");
			e.printStackTrace();
		}
	}

	//######################################
	// Getters & Setters
	//######################################

	// Getter & Setter 'id'
	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	// Getter & Setter 'playerUUID'
	public UUID getPlayerUUID()
	{
		return playerUUID;
	}

	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
	}

	// Getter & Setter 'playerName'
	public String getPlayerName()
	{
		return playerName;
	}

	public void setPlayerName(String playerName)
	{
		this.playerName = playerName;
	}

	// ######################################
	//	check for lost rewarded items
	// ######################################

	public void checkLostRewardedItems(Inventory inventory)
	{
		List<Integer> ids = new ArrayList<Integer>();

		Iterator<RewardedItem> i = rewardedItems.iterator();

		while(i.hasNext())
		{
			RewardedItem item = i.next();
			if(inventory.firstEmpty() == -1)
			{
				break;
			}
			inventory.addItem(item.getItem());
			if(item.getId() != -1)
			{
				ids.add(item.getId());
			}
			i.remove();
		}

		if(!ids.isEmpty())
		{
			Connection bdd = null;
			PreparedStatement ps = null;
			String req = null;

			try
			{
				bdd = DatabaseManager.getConnection();
				req = "DELETE FROM " + DatabaseManager.table.ITEMS + " WHERE id IN (";
				for (int deleteId : ids)
				{
					req += deleteId + ",";
				}
				req = req.substring(0, req.length() - 1);
				req += ")";
				ps = bdd.prepareStatement(req);
				ps.execute();

				ps.close();
			}
			catch(SQLException e)
			{
				Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while deleting rewarded item.");
				e.printStackTrace();
			}
		}
	}

	public void saveRewardedItem()
	{
		if(rewardedItems.isEmpty())
		{
			return;
		}

		Connection bdd = null;
		PreparedStatement ps = null;
		String req = null;
		boolean ok = false;

		try
		{
			bdd = DatabaseManager.getConnection();
			req = "INSERT INTO " + DatabaseManager.table.ITEMS + " ( player_id , item ) VALUES ";
			for(RewardedItem rewardedItem : rewardedItems)
			{
				if(rewardedItem.getId() == -1)
				{
					YamlConfiguration itemConfig = new YamlConfiguration();
					itemConfig.set("item", rewardedItem.getItem());

					ok = true;
					req += "(" + getId() + ",'" + itemConfig.saveToString() + "'),";
				}
			}

			if(!ok)
			{
				return;
			}

			req = req.substring(0, req.length() - 1);
			ps = bdd.prepareStatement(req);
			ps.execute();

			ps.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while adding rewarded item.");
			e.printStackTrace();
		}
	}

	public void addRewardedItem(RewardedItem rewardedItem)
	{
		this.rewardedItems.add(rewardedItem);
	}
}
