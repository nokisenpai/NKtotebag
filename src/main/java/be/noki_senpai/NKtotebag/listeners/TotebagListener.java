package be.noki_senpai.NKtotebag.listeners;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class TotebagListener implements Listener
{
	private PlayerManager playerManager = null;
	private RewardManager rewardManager = null;
	private QueueManager queueManager = null;

	public TotebagListener(PlayerManager playerManager, RewardManager rewardManager, QueueManager queueManager)
	{
		this.playerManager = playerManager;
		this.rewardManager = rewardManager;
		this.queueManager = queueManager;
	}

	// ######################################
	// on player respawn (regive effects)
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerRespawn(final PlayerRespawnEvent e)
	{
		Player player = e.getPlayer();

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				rewardManager.applyEffect(playerManager.getPlayer(player.getName()).getId(), player);
			}
		}.runTaskLater(NKtotebag.getPlugin(), 5);
	}

	// ######################################
	// on player fall damage
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onFallDamage(final EntityDamageEvent e)
	{
		if(!(e.getEntity() instanceof Player))
		{
			return;
		}
		Player p = (Player) e.getEntity();
		if(e.getCause() == EntityDamageEvent.DamageCause.FALL)
		{
			if(rewardManager.checkFallProtection(playerManager.getPlayer(p.getName()).getId()))
			{
				e.setCancelled(true);
			}
		}
	}

	// ######################################
	// on Spawner place / Infinite item place
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}
		Block block = event.getBlock();
		if(event.getItemInHand().getType() == Material.SPAWNER)
		{
			BlockStateMeta blockMeta = (BlockStateMeta) event.getItemInHand().getItemMeta();
			CreatureSpawner spawner = (CreatureSpawner) blockMeta.getBlockState();

			CreatureSpawner state = ((CreatureSpawner) block.getState());
			state.setSpawnedType(spawner.getSpawnedType());
			state.setDelay(state.getMaxSpawnDelay() / 2);
			state.setMaxNearbyEntities(state.getMaxNearbyEntities() * 2);
			state.update(true);
			return;
		}
		else
		{
			ItemMeta blockMeta = event.getItemInHand().getItemMeta();

			if(blockMeta.getPersistentDataContainer().has(new NamespacedKey(NKtotebag.getPlugin(), "NKinfinite"), PersistentDataType.INTEGER))
			{
				event.getPlayer().getInventory().setItemInMainHand(event.getItemInHand());
			}

			if(rewardManager.isGrowArea(block.getChunk()))
			{
				rewardManager.grow(block);
			}

		}
	}

	// ######################################
	// on block break
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}
		Block block = event.getBlock();

		if(rewardManager.isGrowArea(block.getChunk()))
		{
			rewardManager.reGrow(block);
		}
	}

	// ######################################
	// Infinite item use
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerUse(PlayerInteractEvent event)
	{
		if(event.isCancelled())
		{
			if(event.useItemInHand() == Event.Result.ALLOW)
			{
				return;
			}
		}
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR)
		{
			ItemStack itemStack = event.getItem();
			if(itemStack != null)
			{
				if(itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(NKtotebag.getPlugin(), "NKinfinite"), PersistentDataType.INTEGER))
				{
					ItemStack item = event.getItem().clone();
					new BukkitRunnable()
					{
						@Override
						public void run()
						{
							event.getPlayer().getInventory().setItemInMainHand(item);
						}
					}.runTaskLater(NKtotebag.getPlugin(), 0);
				}
			}
		}
	}

	// ######################################
	// Infinite item consume
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerConsume(PlayerItemConsumeEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		ItemMeta blockMeta = event.getItem().getItemMeta();

		if(blockMeta.getPersistentDataContainer().has(new NamespacedKey(NKtotebag.getPlugin(), "NKinfinite"), PersistentDataType.INTEGER))
		{
			ItemStack item = event.getItem().clone();
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					event.getPlayer().getInventory().setItemInMainHand(item);
				}
			}.runTaskLater(NKtotebag.getPlugin(), 0);
		}
	}

	// ######################################
	// On player fly toggle
	// ######################################

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerFlyToggle(PlayerToggleFlightEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		Player player = (Player) event.getPlayer();
		if(!player.getGameMode().equals(GameMode.SURVIVAL))
		{
			return;
		}

		if(hasByPassFlyTimePermissions(player))
		{
			return;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(player.getName());
		if(nkPlayer == null)
		{
			return;
		}

		if(!rewardManager.checkFlyTime(nkPlayer.getId()))
		{
			return;
		}

		if(event.isFlying())
		{
			if(rewardManager.checkFlyTimeRemaining(nkPlayer.getId()) <= 0)
			{
				event.setCancelled(true);
				return;
			}
			rewardManager.setFly(nkPlayer.getId(), true);
		}
		else
		{
			rewardManager.setFly(nkPlayer.getId(), false);
		}
	}

	// ######################################
	// Display growArea
	// ######################################

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if(event.isCancelled())
		{
			return;
		}

		if(!event.getFrom().getChunk().equals(event.getTo().getChunk()))
		{
			if(rewardManager.isGrowArea(event.getTo().getChunk()))
			{
				Chunk chunk = event.getTo().getChunk();
				queueManager.addToQueue(new Function()
				{
					@Override
					public Object apply(Object o)
					{
						rewardManager.displayGrowArea(chunk.getWorld(), chunk.getX(), chunk.getZ(), event.getPlayer().getLocation().getY());
						return null;
					}
				});
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractAtEntityEvent event)
	{
		if(event.getPlayer().getInventory().getItemInMainHand().getType() == Material.NAME_TAG)
		{
			Entity entity = event.getRightClicked();
			if(entity instanceof Creature)
			{
				entity.setPersistent(true);
			}
		}
	}

	public static boolean hasByPassFlyTimePermissions(Player player)
	{
		return player.hasPermission("*") || player.hasPermission("nktotebag.*") || player.hasPermission("nktotebag.bypass.flytime")
				|| player.hasPermission("nktotebag.bypass.*");
	}
}
