package be.noki_senpai.NKtotebag.managers;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.*;
import be.noki_senpai.NKtotebag.listeners.TotebagListener;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.material.CocoaPlant;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RewardManager
{
	private ConsoleCommandSender console = null;
	private Map<Integer, Map<String, PotionEffect>> effect = new HashMap<>();
	private Map<Integer, FlyTime> flyTime = new HashMap<>();
	private Map<Integer, GrowArea> growArea = new HashMap<>();
	private List<Integer> fallProtection = new ArrayList<>();
	private Timer timer = new Timer();

	public enum rewardType
	{
		EFFECT("EFFECT"), FALL_PROTECTION("FALL_PROTECTION"), FLY("FLY"), GROW_AREA("GROW_AREA"), INFINITE_ITEM("INFINITE_ITEM");

		private String name = "";

		rewardType(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		public static int size()
		{
			return rewardType.values().length;
		}
	}

	public RewardManager()
	{
		console = Bukkit.getConsoleSender();
	}

	// **************************************
	// **************************************
	// Load all data
	// **************************************
	// **************************************

	public void loadData()
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				alterFlyTime();
			}
		}.runTaskTimerAsynchronously(NKtotebag.getPlugin(), 0, 20);

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				loadGrowArea();
			}
		}.runTaskTimerAsynchronously(NKtotebag.getPlugin(), 0, 60*20);
		scheduleResetFlyTimer();
	}

	public void loadData(NKPlayer nkPlayer)
	{
		Player player = Bukkit.getPlayer(nkPlayer.getPlayerUUID());

		loadEffect(nkPlayer.getId());

		loadFallProtection(nkPlayer.getId());

		loadFly(nkPlayer.getId(), nkPlayer.getPlayerUUID());

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				applyEffect(nkPlayer.getId(), player);
			}
		}.runTask(NKtotebag.getPlugin());
	}

	public void unloadData()
	{
		effect.clear();
		flyTime.clear();
		growArea.clear();
		fallProtection.clear();
		timer.cancel();
		timer.purge();
	}

	// **************************************
	// Load effect
	// **************************************

	public void loadEffect(int id)
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT * FROM " + DatabaseManager.table.REWARDS + " WHERE player_id = ? AND type = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.setString(2, rewardType.EFFECT.toString());
			resultat = ps.executeQuery();

			if(!effect.containsKey(id))
			{
				effect.put(id, new HashMap<>());
			}
			YamlConfiguration restoreConfig = new YamlConfiguration();
			while(resultat.next())
			{
				try
				{
					restoreConfig.loadFromString(resultat.getString("data"));
				}
				catch(InvalidConfigurationException e)
				{
					e.printStackTrace();
				}

				if(!effect.get(id).containsValue((PotionEffect) restoreConfig.get("potionEffect")))
				{
					effect.get(id).put(resultat.getString("tag_name"), (PotionEffect) restoreConfig.get("potionEffect"));
				}
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while getting players effect.");
			e.printStackTrace();
		}
	}

	public void unloadEffect(int id)
	{
		effect.remove(id);
	}

	// **************************************
	// Load fallprotection
	// **************************************

	public void loadFallProtection(int id)
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT * FROM " + DatabaseManager.table.REWARDS + " WHERE player_id = ? AND type = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.setString(2, rewardType.FALL_PROTECTION.toString());
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				fallProtection.add(id);
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while getting players fall protection.");
			e.printStackTrace();
		}
	}

	public void unloadFallProtection(int id)
	{
		fallProtection.remove((Integer) id);
	}

	public void loadFly(int id, UUID uuid)
	{
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT * FROM " + DatabaseManager.table.REWARDS + " WHERE player_id = ? AND type = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.setString(2, rewardType.FLY.toString());
			resultat = ps.executeQuery();

			if(resultat.next())
			{
				String[] args = resultat.getString("data").split("#");
				int currentTime = Integer.parseInt(args[0]);
				int maxTime = Integer.parseInt(args[1]);

				Player player = Bukkit.getPlayer(uuid);
				if(player != null)
				{
					boolean fly = false;
					if(player.getGameMode().equals(GameMode.SURVIVAL) && !TotebagListener.hasByPassFlyTimePermissions(player)
							&& checkFlyTimeRemaining(id) > 0)
					{
						fly = true;
					}
					flyTime.put(id, new FlyTime(maxTime, currentTime, player, fly));
				}
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while getting players effect.");
			e.printStackTrace();
		}
	}

	public void unloadFly(int id)
	{
		flyTime.remove(id);
	}

	public void loadGrowArea()
	{
		growArea.clear();
		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT * FROM " + DatabaseManager.table.REWARDS + " WHERE type = ?";
			ps = bdd.prepareStatement(req);
			ps.setString(1, rewardType.GROW_AREA.toString());
			resultat = ps.executeQuery();

			while(resultat.next())
			{
				String[] args = resultat.getString("data").split("#");
				String worldName = args[1];
				if(ConfigManager.SERVERNAME.getName().equalsIgnoreCase(args[0]) && !worldName.equalsIgnoreCase("null"))
				{
					int x = Integer.parseInt(args[2]);
					int z = Integer.parseInt(args[3]);
					growArea.put(resultat.getInt("player_id"), new GrowArea(x, z, worldName));
				}
				else
				{
					growArea.put(resultat.getInt("player_id"), null);
				}
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while getting players effect.");
			e.printStackTrace();
		}
	}

	public void unloadGrowArea(int id)
	{
		growArea.clear();
	}

	// **************************************
	// **************************************
	// Apply
	// **************************************
	// **************************************

	// **************************************
	// Apply effect
	// **************************************

	public void applyEffect(int id, Player player)
	{
		if(effect.containsKey(id))
		{
			for(Map.Entry<String, PotionEffect> potionEffect : effect.get(id).entrySet())
			{
				for(PotionEffect activePotionEffect : player.getActivePotionEffects())
				{
					if(activePotionEffect.getType().equals(potionEffect.getValue().getType())
							&& activePotionEffect.getAmplifier() < potionEffect.getValue().getAmplifier())
					{
						player.removePotionEffect(potionEffect.getValue().getType());
					}
				}
				player.addPotionEffect(potionEffect.getValue());
				player.sendMessage(ChatColor.GREEN + "Votre effet " + ChatColor.AQUA + potionEffect.getKey() + ChatColor.GREEN + " est actif.");
			}
		}
	}

	public void alterEffect(int id, String tagName, Player player)
	{
		if(player.hasPotionEffect(effect.get(id).get(tagName).getType()))
		{
			player.removePotionEffect(effect.get(id).get(tagName).getType());
			player.sendMessage(ChatColor.GREEN + "Votre effect " + ChatColor.AQUA + tagName + ChatColor.GREEN + " est maintenant inactif.");
		}
		else
		{
			player.addPotionEffect(effect.get(id).get(tagName));
			player.sendMessage(ChatColor.GREEN + "Votre effet " + ChatColor.AQUA + tagName + ChatColor.GREEN + " est maintenant actif.");
		}
	}

	public List<String> getEffectTagName(int id)
	{
		if(effect.containsKey(id))
		{
			return new ArrayList<>(effect.get(id).keySet());
		}
		return null;
	}

	public boolean checkEffectTagName(int id, String tagName)
	{
		if(effect.containsKey(id))
		{
			return effect.get(id).containsKey(tagName);
		}
		return false;
	}

	public boolean checkFallProtection(int id)
	{
		return fallProtection.contains(id);
	}

	public boolean checkFlyTime(int id)
	{
		return flyTime.containsKey(id);
	}

	public int checkFlyTimeRemaining(int id)
	{
		if(flyTime.containsKey(id))
		{
			return flyTime.get(id).getMaxTime() - flyTime.get(id).getCurrentTime();
		}
		return 0;
	}

	public boolean checkGrowArea(int id)
	{
		if(growArea.containsKey(id))
		{
			return true;
		}
		boolean exist = false;

		Connection bdd = null;
		ResultSet resultat = null;
		PreparedStatement ps = null;
		String req = null;

		try
		{
			bdd = DatabaseManager.getConnection();

			req = "SELECT * FROM " + DatabaseManager.table.REWARDS + " WHERE player_id = ? AND type = ?";
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.setString(2, rewardType.GROW_AREA.toString());
			resultat = ps.executeQuery();

			if(resultat.next())
			{
				exist = true;
			}

			ps.close();
			resultat.close();
		}
		catch(SQLException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + NKtotebag.PNAME + " Error while getting players effect.");
			e.printStackTrace();
		}
		return exist;
	}

	public boolean isGrowArea(Chunk chunk)
	{
		return growArea.containsValue(new GrowArea(chunk.getX(), chunk.getZ(), chunk.getWorld().getName()));
	}

	public void removeGrowArea(int x, int z, int id)
	{
		if(growArea.containsKey(id))
		{
			growArea.replace(id, null);
		}
	}

	public void setFly(int id, boolean fly)
	{
		flyTime.get(id).setFly(fly);
	}

	public void insertReward(int id, String rewardType)
	{
		insertReward(id, rewardType, "", "");
	}

	public void insertReward(int id, String rewardType, String data)
	{
		insertReward(id, rewardType, "", data);
	}

	public void insertReward(int id, String rewardType, String tagName, String data)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = "INSERT INTO " + DatabaseManager.table.REWARDS + " ( player_id , type , tag_name , data )" + " VALUES ( ? , ? , ? , ? )"
				+ " ON DUPLICATE KEY UPDATE player_id = VALUES(player_id)";
		try
		{
			bdd = DatabaseManager.getConnection();
			ps = bdd.prepareStatement(req);
			ps.setInt(1, id);
			ps.setString(2, rewardType);
			ps.setString(3, tagName);
			ps.setString(4, data);

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void updateReward(int id, String rewardType, String data)
	{
		updateReward(id, rewardType, "", data);
	}

	public void updateReward(int id, String rewardType, String tagName, String data)
	{
		Connection bdd = null;
		PreparedStatement ps = null;
		String req = "UPDATE " + DatabaseManager.table.REWARDS + " SET data = ? " + "WHERE player_id = ? AND type = ? AND tag_name = ? ";
		try
		{
			bdd = DatabaseManager.getConnection();
			ps = bdd.prepareStatement(req);
			ps.setString(1, data);
			ps.setInt(2, id);
			ps.setString(3, rewardType);
			ps.setString(4, tagName);

			ps.executeUpdate();
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	public void updateGrowArea(int id, int x, int z, String worldName)
	{
		if(growArea.containsKey(id))
		{
			growArea.replace(id, new GrowArea(x, z, worldName));
		}
		updateReward(id, rewardType.GROW_AREA.toString(), ConfigManager.SERVERNAME + "#" + worldName + "#" + x + "#" + z);
	}

	public void insertRewardEffect(int id, String tagName, String effectType, int effectLevel)
	{
		PotionEffect potionEffect = new PotionEffect(PotionEffectType.getByName(effectType), Integer.MAX_VALUE, effectLevel, false, false, true);

		YamlConfiguration itemConfig = new YamlConfiguration();
		itemConfig.set("potionEffect", potionEffect);

		if(!effect.containsKey(id))
		{
			effect.put(id, new HashMap<>());
		}
		effect.get(id).put(tagName, potionEffect);

		insertReward(id, rewardType.EFFECT.toString(), tagName, itemConfig.saveToString());
	}

	public void insertRewardFallProtection(int id)
	{
		if(!fallProtection.contains(id))
		{
			fallProtection.add(id);
		}

		insertReward(id, rewardType.FALL_PROTECTION.toString());
	}

	public void insertRewardFly(int id, int duration, Player player)
	{
		if(!flyTime.containsKey(id))
		{
			boolean fly = false;
			if(player.getGameMode().equals(GameMode.SURVIVAL) && !TotebagListener.hasByPassFlyTimePermissions(player)
					&& checkFlyTimeRemaining(id) > 0)
			{
				fly = true;
			}

			flyTime.put(id, new FlyTime(duration, 0, player, fly));
			insertReward(id, rewardType.FLY.toString(), "0#" + String.valueOf(duration));
		}
		else
		{
			int totalDuration = duration + flyTime.get(id).getMaxTime();
			flyTime.get(id).alterMaxTime(duration);
			updateReward(id, rewardType.FLY.toString(), flyTime.get(id).getCurrentTime() + "#" + String.valueOf(totalDuration));
		}
	}

	public void insertRewardGrowArea(int id)
	{
		if(!growArea.containsKey(id))
		{
			growArea.put(id, new GrowArea(0, 0, null));
			insertReward(id, rewardType.GROW_AREA.toString(), "null#null#0#0");
		}
	}

	public void saveFlyTime(int id)
	{
		if(flyTime.containsKey(id))
		{
			updateReward(id, rewardType.FLY.toString(), flyTime.get(id).getCurrentTime() + "#" + flyTime.get(id).getMaxTime());
		}
	}

	public void alterFlyTime()
	{
		for(FlyTime value : flyTime.values())
		{
			if(value.isFlying())
			{
				value.alterCurrentTime(1);
				value.setProgressBar();
				value.displayBar();
				value.count = 0;
				if(value.getMaxTime() - value.getCurrentTime() <= 0)
				{
					value.disableFly();
				}
				if(value.getMaxTime() - value.getCurrentTime() == 10)
				{
					value.warningFly();
				}
			}
			else
			{
				if(value.count < 3)
				{
					value.count++;
				}
			}
			if(value.count >= 3)
			{
				value.hideBar();
			}
		}
	}

	public void scheduleResetFlyTimer()
	{
		Calendar date = Calendar.getInstance();
		date.set(Calendar.HOUR_OF_DAY, 1);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		timer.schedule(new FlyTimerTask(this), date.getTime(), TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
	}

	public void resetFlyTimer()
	{
		for(FlyTime value : flyTime.values())
		{
			value.resetCurrentTime();
			value.setProgressBar();
		}
		if(ConfigManager.SQLRESETFLYTIME)
		{
			Connection bdd = null;
			PreparedStatement ps = null;
			String req = "UPDATE " + DatabaseManager.table.REWARDS + " SET data = CONCAT('0#', SUBSTRING_INDEX(data,'#',-1)) "
					+ "WHERE type = ? AND tag_name = ? ";
			try
			{
				bdd = DatabaseManager.getConnection();
				ps = bdd.prepareStatement(req);
				ps.setString(1, rewardType.FLY.toString());
				ps.setString(2, "");

				ps.executeUpdate();
				ps.close();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void grow(Block block)
	{
		int age = -1;
		switch(block.getBlockData().getMaterial().toString())
		{
			case "WHEAT":
			case "CARROTS":
			case "POTATOES":
				age = 7;
				break;
			case "BEETROOTS":
			case "SWEET_BERRY_BUSH":
				age = 3;
				break;
			case "COCOA":
				age = 2;
				break;
			default:
				break;
		}

		if(age != -1)
		{
			BlockData data = block.getBlockData();
			if(data instanceof Ageable)
			{
				Ageable ag = (Ageable) data;
				ag.setAge(age);
				block.setBlockData(ag);
				return;
			}
		}

		Material material = null;
		Material material2 = null;

		switch(block.getBlockData().getMaterial().toString())
		{
			case "MELON_STEM":
				material = Material.MELON;
				material2 = Material.ATTACHED_MELON_STEM;
				break;
			case "PUMPKIN_STEM":
				material = Material.PUMPKIN;
				material2 = Material.ATTACHED_PUMPKIN_STEM;
				break;
			default:
				break;
		}

		if(material != null)
		{
			Block floor = block.getRelative(BlockFace.EAST).getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				Block grow = block.getRelative(BlockFace.EAST);
				if(grow.getType() == Material.AIR)
				{
					grow.setType(material);

					block.setType(material2);
					Directional d = ((Directional) block.getBlockData());
					d.setFacing(BlockFace.EAST);
					block.setBlockData(d);
					return;
				}
			}
			floor = block.getRelative(BlockFace.NORTH).getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				Block grow = block.getRelative(BlockFace.NORTH);
				if(grow.getType() == Material.AIR)
				{
					grow.setType(material);
					block.setType(material2);
					Directional d = ((Directional) block.getBlockData());
					d.setFacing(BlockFace.NORTH);
					block.setBlockData(d);
					return;
				}
			}
			floor = block.getRelative(BlockFace.WEST).getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				Block grow = block.getRelative(BlockFace.WEST);
				if(grow.getType() == Material.AIR)
				{
					grow.setType(material);
					block.setType(material2);
					Directional d = ((Directional) block.getBlockData());
					d.setFacing(BlockFace.WEST);
					block.setBlockData(d);
					return;
				}
			}
			floor = block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				Block grow = block.getRelative(BlockFace.SOUTH);
				if(grow.getType() == Material.AIR)
				{
					grow.setType(material);
					block.setType(material2);
					Directional d = ((Directional) block.getBlockData());
					d.setFacing(BlockFace.SOUTH);
					block.setBlockData(d);
					return;
				}
			}
			return;
		}

		switch(block.getBlockData().getMaterial().toString())
		{
			case "CACTUS":
				material = Material.CACTUS;
				break;
			case "SUGAR_CANE":
				material = Material.SUGAR_CANE;
				break;
			default:
				break;
		}

		if(material != null)
		{
			if(block.getRelative(BlockFace.DOWN).getType() == material)
			{
				return;
			}
			Block grow = block.getRelative(BlockFace.UP);
			if(grow.getType() == Material.AIR)
			{
				grow.setType(material);
				grow = grow.getRelative(BlockFace.UP);
				if(grow.getType() == Material.AIR)
				{
					grow.setType(material);
				}
			}
			return;
		}

		TreeType treeType = null;

		switch(block.getBlockData().getMaterial().toString())
		{
			case "OAK_SAPLING":
				material = Material.OAK_SAPLING;
				treeType = TreeType.TREE;
				break;
			default:
				break;
		}

		if(treeType != null)
		{
			block.setType(Material.AIR);

			if(!block.getWorld().generateTree(block.getLocation(), treeType))
			{
				block.setType(material);
			}
			return;
		}

		switch(block.getBlockData().getMaterial().toString())
		{
			case "BROWN_MUSHROOM":
				material = Material.BROWN_MUSHROOM;
				break;
			case "RED_MUSHROOM":
				material = Material.RED_MUSHROOM;
				break;
			default:
				break;
		}

		if(material != null)
		{
			Block spread = null;

			spread = block.getRelative(BlockFace.NORTH_EAST);
			Block floor = spread.getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				if(spread.getType() == Material.AIR)
				{
					spread.setType(material);
				}
			}

			spread = block.getRelative(BlockFace.NORTH_WEST);
			floor = spread.getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				if(spread.getType() == Material.AIR)
				{
					spread.setType(material);
				}
			}

			spread = block.getRelative(BlockFace.SOUTH_EAST);
			floor = spread.getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				if(spread.getType() == Material.AIR)
				{
					spread.setType(material);
				}
			}

			spread = block.getRelative(BlockFace.SOUTH_WEST);
			floor = spread.getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				if(spread.getType() == Material.AIR)
				{
					spread.setType(material);
				}
			}
			return;
		}

		if(block.getType() == Material.VINE)
		{
			Block vine = block.getRelative(BlockFace.DOWN);

			while(vine.getType() == Material.AIR)
			{
				vine.setType(block.getType());
				vine.setBlockData(block.getBlockData());
				vine = vine.getRelative(BlockFace.DOWN);
			}
			return;
		}
	}

	public void reGrow(Block block)
	{
		int age = -1;
		switch(block.getBlockData().getMaterial().toString())
		{
			case "SWEET_BERRY_BUSH":
				age = 3;
				break;
			default:
				break;
		}

		if(age != -1)
		{
			BlockData data = block.getBlockData();
			if(data instanceof Ageable)
			{
				Ageable ag = (Ageable) data;
				ag.setAge(age);
				block.setBlockData(ag);
				return;
			}
		}

		Material material = null;
		Material material2 = null;

		switch(block.getBlockData().getMaterial().toString())
		{
			case "MELON":
				material = Material.MELON;
				material2 = Material.ATTACHED_MELON_STEM;
				break;
			case "PUMPKIN":
				material = Material.PUMPKIN;
				material2 = Material.ATTACHED_PUMPKIN_STEM;
				break;
			default:
				break;
		}

		if(material != null)
		{
			Block floor = block.getRelative(BlockFace.DOWN);
			if(!(floor.isLiquid() || !floor.getType().isSolid() || floor.getType() == Material.AIR))
			{
				Block stem = block.getRelative(BlockFace.EAST);
				if(stem.getType() == material2)
				{
					Directional d = ((Directional) stem.getBlockData());
					if(d.getFacing() == BlockFace.WEST)
					{
						Block finalStem = stem;
						Material finalMaterial = material2;
						Material finalMaterial1 = material;
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								block.getLocation().getBlock().setType(finalMaterial1);
								finalStem.setType(finalMaterial);
								d.setFacing(BlockFace.WEST);
								finalStem.setBlockData(d);
							}
						}.runTaskLater(NKtotebag.getPlugin(), 0);
						return;
					}
				}

				stem = block.getRelative(BlockFace.NORTH);
				if(stem.getType() == material2)
				{
					Directional d = ((Directional) stem.getBlockData());
					if(d.getFacing() == BlockFace.SOUTH)
					{
						Block finalStem = stem;
						Material finalMaterial = material2;
						Material finalMaterial1 = material;
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								block.getLocation().getBlock().setType(finalMaterial1);
								finalStem.setType(finalMaterial);
								d.setFacing(BlockFace.SOUTH);
								finalStem.setBlockData(d);
							}
						}.runTaskLater(NKtotebag.getPlugin(), 0);
						return;
					}
				}

				stem = block.getRelative(BlockFace.WEST);
				if(stem.getType() == material2)
				{
					Directional d = ((Directional) stem.getBlockData());
					if(d.getFacing() == BlockFace.EAST)
					{
						Block finalStem = stem;
						Material finalMaterial = material2;
						Material finalMaterial1 = material;
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								block.getLocation().getBlock().setType(finalMaterial1);
								finalStem.setType(finalMaterial);
								d.setFacing(BlockFace.EAST);
								finalStem.setBlockData(d);
							}
						}.runTaskLater(NKtotebag.getPlugin(), 0);
						return;
					}
				}

				stem = block.getRelative(BlockFace.SOUTH);
				if(stem.getType() == material2)
				{
					Directional d = ((Directional) stem.getBlockData());
					if(d.getFacing() == BlockFace.NORTH)
					{
						Block finalStem = stem;
						Material finalMaterial = material2;
						Material finalMaterial1 = material;
						new BukkitRunnable()
						{
							@Override
							public void run()
							{
								block.getLocation().getBlock().setType(finalMaterial1);
								finalStem.setType(finalMaterial);
								d.setFacing(BlockFace.NORTH);
								finalStem.setBlockData(d);
							}
						}.runTaskLater(NKtotebag.getPlugin(), 0);
						return;
					}
				}
			}
			return;
		}

		switch(block.getBlockData().getMaterial().toString())
		{
			case "CACTUS":
				material = Material.CACTUS;
				break;
			case "SUGAR_CANE":
				material = Material.SUGAR_CANE;
				break;
			default:
				break;
		}

		if(material != null)
		{
			Block down = block.getRelative(BlockFace.DOWN);
			if(down.getType() != material)
			{
				return;
			}

			down = down.getRelative(BlockFace.DOWN);
			Material finalMaterial2 = material;
			if(down.getType() == material)
			{
				if(down.getRelative(BlockFace.DOWN).getType() == material)
				{
					return;
				}

				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						block.setType(finalMaterial2);
					}
				}.runTaskLater(NKtotebag.getPlugin(), 0);

				return;
			}
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					block.setType(finalMaterial2);
					Block top = block.getRelative(BlockFace.UP);
					if(top.getType() != Material.AIR)
					{
						top.breakNaturally();
						top.setType(finalMaterial2);
					}
				}
			}.runTaskLater(NKtotebag.getPlugin(), 0);

			return;
		}

		if(block.getType() == Material.VINE)
		{
			if(block.getRelative(BlockFace.UP).getType() == Material.VINE)
			{
				final BlockData blockData = block.getBlockData();
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						Block vine = block;
						while(vine.getType() == Material.AIR)
						{
							vine.setType(Material.VINE);
							vine.setBlockData(blockData);
							vine = vine.getRelative(BlockFace.DOWN);
						}
					}
				}.runTaskLater(NKtotebag.getPlugin(), 0);
			}
			return;
		}

	}

	public void displayGrowArea(World world, int _x, int _z, double _y)
	{
		for(int i = 0; i < 25; i++)
		{
			double speed = 0.1 + (Math.random() * ((0.9) - (0)));

			double x = _x * 16 + 0.5;
			x += 2 + Math.random() * 13;

			double y = _y;
			y += Math.random();

			double z = _z * 16 + 0.5;
			z += 2 + Math.random() * 11;

			world.spawnParticle(Particle.VILLAGER_HAPPY, x, y + 1, z, 10, speed, speed + 1, speed);
		}
	}
}
