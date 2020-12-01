package be.noki_senpai.NKtotebag.listeners;

import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.function.Function;

public class PlayerConnectionListener
{
	private PlayerManager playerManager = null;
	private QueueManager queueManager = null;

	public PlayerConnectionListener(PlayerManager playerManager, QueueManager queueManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
	}

	public void PlayerJoinEvent(PlayerJoinEvent event)
	{
		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				playerManager.addPlayer(event.getPlayer());
				return null;
			}
		});
	}

	public void onPlayerQuitEvent(PlayerQuitEvent event)
	{
		String playerName = event.getPlayer().getName();
		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				playerManager.getPlayer(playerName).saveRewardedItem();
				playerManager.delPlayer(event.getPlayer().getName());
				return null;
			}
		});
	}
}
