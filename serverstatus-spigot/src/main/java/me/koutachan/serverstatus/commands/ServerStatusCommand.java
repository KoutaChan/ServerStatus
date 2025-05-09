package me.koutachan.serverstatus.commands;

import me.koutachan.serverstatus.ServerStatusSpigot;
import me.koutachan.serverstatus.ServerStatusInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServerStatusCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandName, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "プレイヤーから実行する必要があります");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "不明な引数です");
            return true;
        }

        Player player = (Player) sender;
        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "remove":
                handleRemoveCommand(player);
                break;
            case "add":
                handleAddCommand(player, args);
                break;
            case "cancel":
                handleCancelCommand(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "不明なコマンドです: " + subCommand);
                break;
        }
        return true;
    }

    private void handleRemoveCommand(Player player) {
        ServerStatusSpigot.INSTANCE.queueRemove(player);
        player.sendMessage(ChatColor.GREEN + "NPCをクリックしてください。");
    }

    private void handleAddCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "サーバー名を指定してください");
            return;
        }

        String serverName = args[1];
        ServerStatusSpigot.INSTANCE.queueAdd(player, serverName);
        player.sendMessage(ChatColor.GREEN + "NPCをクリックしてください。");
    }

    private void handleCancelCommand(Player player) {
        ServerStatusSpigot.INSTANCE.queue.remove(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "キャンセルしました。");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String commandName, String[] args) {
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("add", "remove", "cancel");
            return subCommands.stream()
                    .filter(subCommand -> subCommand.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            if (ServerStatusSpigot.INSTANCE.lastStatus != null) {
                return ServerStatusSpigot.INSTANCE.lastStatus
                        .stream()
                        .map(ServerStatusInfo::getServerName)
                        .collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}