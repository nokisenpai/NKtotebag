package be.noki_senpai.NKtotebag.data;

import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;

import java.util.Map;
import java.util.TimerTask;

public class FlyTimerTask extends TimerTask
{
	RewardManager rewardManager = null;
	public FlyTimerTask(RewardManager rewardManager)
	{
		this.rewardManager = rewardManager;
	}

	public void run()
	{
		rewardManager.resetFlyTimer();
	}
}
