package be.noki_senpai.NKtotebag.cmd.GiveReward;

import be.noki_senpai.NKtotebag.NKtotebag;
import be.noki_senpai.NKtotebag.data.NKPlayer;
import be.noki_senpai.NKtotebag.data.RewardedItem;
import be.noki_senpai.NKtotebag.managers.PlayerManager;
import be.noki_senpai.NKtotebag.managers.QueueManager;
import be.noki_senpai.NKtotebag.managers.RewardManager;
import be.noki_senpai.NKtotebag.utils.CheckType;
import org.bukkit.*;
import org.bukkit.block.Beacon;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

public class _Effect
{
	PlayerManager playerManager = null;
	QueueManager queueManager = null;
	RewardManager rewardManager = null;

	public _Effect(PlayerManager playerManager, QueueManager queueManager, RewardManager rewardManager)
	{
		this.playerManager = playerManager;
		this.queueManager = queueManager;
		this.rewardManager = rewardManager;
	}

	// /givereward effect <playerName> <tagName> <effectName> [effectLevel]
	public boolean effect(CommandSender sender, String[] args)
	{
		String tagName = null;
		String effectName = null;
		int effectLevel = 1;
		// Check if sender has permission
		if(!hasGiveRewardEffectPermissions(sender))
		{
			sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if(args.length < 4)
		{
			sender.sendMessage(ChatColor.GREEN + "/givereward effect <playerName> <tagName> <effectName> [effectLevel]");
			return true;
		}

		tagName = args[2];

		if(!isValidEffectType(args[3]))
		{
			sender.sendMessage(ChatColor.RED + "'" + args[3] + "' n'est pas un type d'effet valide.");
			return true;
		}

		effectName = args[3];

		if(CheckType.isNumber(args[4]))
		{
			effectLevel = Integer.parseInt(args[4]);
			if(effectLevel == 0)
			{
				effectLevel = 1;
			}
		}
		else
		{
			sender.sendMessage(ChatColor.RED + "Le niveau de l'effet doit être un nombre");
			return true;
		}

		NKPlayer nkPlayer = playerManager.getPlayer(args[1]);
		if(nkPlayer == null)
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
			return true;
		}

		if(rewardManager.checkEffectTagName(nkPlayer.getId(), tagName))
		{
			sender.sendMessage(ChatColor.RED + "Ce joueur possède déjà un effet avec ce tagName !");
			return true;
		}



		String finalTagName = tagName;
		int finalEffectLevel = effectLevel;
		String finalEffectName = effectName;
		queueManager.addToQueue(new Function()
		{
			@Override
			public Object apply(Object o)
			{
				rewardManager.insertRewardEffect(nkPlayer.getId(), finalTagName, finalEffectName, finalEffectLevel);

				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						rewardManager.applyEffect(nkPlayer.getId(), Bukkit.getPlayer(nkPlayer.getPlayerUUID()));
					}
				}.runTaskLater(NKtotebag.getPlugin(), 0);
				Bukkit.getPlayer(nkPlayer.getPlayerUUID()).sendMessage(ChatColor.GREEN + "Vous avez reçu l'effet " + ChatColor.AQUA + finalTagName + ChatColor.GREEN + ".");
				return null;
			}
		});
		return true;
	}

	private boolean hasGiveRewardEffectPermissions(CommandSender sender)
	{
		return sender.hasPermission("*") || sender.hasPermission("nktotebag.*") || sender.hasPermission("nktotebag.givereward.effect")
				|| sender.hasPermission("nktotebag.givereward.*") || sender.hasPermission("nktotebag.admin");
	}

	public boolean isValidEffectType(String effectType)
	{
		for(PotionEffectType entityType : PotionEffectType.values())
		{
			if(entityType.getName().equalsIgnoreCase(effectType))
			{
				return true;
			}
		}
		return false;
	}
}
