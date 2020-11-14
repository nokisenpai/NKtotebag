package be.noki_senpai.NKtotebag.listeners;

import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import be.noki_senpai.NKtotebag.NKtotebag;
import org.bukkit.scheduler.BukkitRunnable;;

public class PlayerConnectionListener implements Listener
{
	private PlayerManager playerManager = null;
	private RewardManager rewardManager = null;

	public PlayerConnectionListener(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.rewardManager = rewardManager;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void PlayerJoinEvent(final PlayerJoinEvent event) 
	{
		new BukkitRunnable()
		{
			@Override public void run()
			{
				playerManager.addPlayer(event.getPlayer());

			}
		}.runTaskLaterAsynchronously(NKtotebag.getPlugin(), 20);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuitEvent(final PlayerQuitEvent event) 
	{
		String playerName = event.getPlayer().getName();
		new BukkitRunnable()
		{
			@Override public void run()
			{
				playerManager.getPlayer(playerName).saveRewardedItem();
				playerManager.delPlayer(event.getPlayer().getName());
			}
		}.runTaskAsynchronously(NKtotebag.getPlugin());
	}
}
