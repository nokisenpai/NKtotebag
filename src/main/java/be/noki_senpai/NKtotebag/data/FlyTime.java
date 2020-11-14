package be.noki_senpai.NKtotebag.data;

import be.noki_senpai.NKtotebag.cmd.GiveReward._Fly;
import be.noki_senpai.NKtotebag.listeners.TotebagListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class FlyTime
{
	private int maxTime = 0;
	private int currentTime = 0;
	private boolean fly = false;
	private BossBar bossBar = null;
	public int count = 0;
	private Player player = null;

	public FlyTime(int maxTime, int currentTime, Player player, boolean fly)
	{
		this.maxTime = maxTime;
		this.currentTime = currentTime;
		if(fly)
		{
			this.fly = player.isFlying();
		}

		BossBar bossBar = Bukkit.getServer().createBossBar(
				"Temps de vol restant : " + getTimeRemaining(), BarColor.valueOf("PURPLE"), BarStyle.SOLID);
		bossBar.setVisible(false);
		bossBar.addPlayer(player);
		if((double) currentTime / maxTime > 1 || (double) currentTime / maxTime < 0)
		{
			bossBar.setProgress(0.0);
		}
		else
		{
			bossBar.setProgress((double) currentTime / maxTime);
		}

		this.bossBar = bossBar;
		this.player = player;
	}

	public int getCurrentTime()
	{
		return currentTime;
	}

	public int getMaxTime()
	{
		return maxTime;
	}

	public void alterMaxTime(int amount)
	{
		maxTime += amount;
	}

	public void alterCurrentTime(int amount)
	{
		currentTime += amount;
	}

	public String getTimeRemaining()
	{
		int timeRemaining = maxTime - currentTime;
		return String.format("%02dh %02dm %02ds", timeRemaining / 3600, (timeRemaining % 3600) / 60, timeRemaining % 60);
	}

	public boolean isFlying()
	{
		return fly;
	}

	public void setProgressBar()
	{
		this.bossBar.setTitle("Temps de vol restant : " + getTimeRemaining());
		if((double) currentTime / maxTime > 1 || (double) currentTime / maxTime < 0)
		{
			this.bossBar.setProgress(0.0);
		}
		else
		{
			this.bossBar.setProgress((double) currentTime / maxTime);
		}
	}

	public void displayBar()
	{
		this.bossBar.setVisible(true);
	}

	public void hideBar()
	{
		this.bossBar.setVisible(false);
	}

	public void setFly(boolean fly)
	{
		this.fly = fly;
	}

	public void disableFly()
	{
		player.setFlying(false);
		fly = false;
	}

	public void warningFly()
	{
		player.sendTitle(ChatColor.RED + "/!\\ 10 secondes de fly /!\\", "", 10, 40, 10);
	}

	public void resetCurrentTime()
	{
		currentTime = 0;
		player.sendMessage(ChatColor.GREEN + "Votre temps de fly a été réinitialisé !");
	}
}
