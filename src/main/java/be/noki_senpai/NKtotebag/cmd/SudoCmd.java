package be.noki_senpai.NKtotebag.cmd;

import be.noki_senpai.NKtotebag.NKtotebag;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SudoCmd implements CommandExecutor
{
	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
	{
		//
		if(this.hasSudoPermissions(sender))
		{
			// Vérification si il y a au moins 2 arguments
			if(args.length < 2)
			{
				sender.spigot().sendMessage(new TextComponent(ChatColor.RED + "Il faut préciser au minimum un joueur et une commande"));
				return true;
			}
			// Récupération du joueur
			Player player = Bukkit.getServer().getPlayer(args[0]);
			// Vérification si le joueur existe
			if(player == null)
			{
				sender.spigot().sendMessage(new TextComponent(ChatColor.RED + "Ce joueur n'existe pas ou n'est pas connecté"));
				return false;
			}

			String cmd = "";
			// Récupération des argument pour reformer la commande
			for(int i = 1; i < args.length; ++i)
			{
				cmd = cmd + args[i] + " ";
			}
			cmd = cmd.substring(0, cmd.length() - 1);

			// Test si la commande viens de bukkit ou bungee
			Command commandSend = Bukkit.getServer().getPluginCommand(args[1]);

			if(commandSend != null)
			{
				// command bukkit
				player.performCommand(cmd);
			}
			else
			{
				// command bungee
				ByteArrayDataOutput out = ByteStreams.newDataOutput();

				ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
				DataOutputStream msgout = new DataOutputStream(msgbytes);
				try
				{
					msgout.writeUTF(player.getName() + "|" + cmd);
				}
				catch(IOException exception)
				{
					exception.printStackTrace();
				}

				out.writeShort(msgbytes.toByteArray().length);
				out.write(msgbytes.toByteArray());

				Player firstPlayer = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

				firstPlayer.sendPluginMessage(NKtotebag.getPlugin(), "lulu:tktotebagbc", out.toByteArray());
			}
		}
		return false;
	}

	private boolean hasSudoPermissions(CommandSender sender)
	{
		return sender.hasPermission("nktotebag.reward") || sender.hasPermission("nktotebag.user") || sender.hasPermission("nktotebag.admin");
	}
}
