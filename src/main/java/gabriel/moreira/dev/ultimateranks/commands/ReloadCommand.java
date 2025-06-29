package gabriel.moreira.dev.ultimateranks.commands;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final UltimateRanks plugin;

    public ReloadCommand(UltimateRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimateranks.reload")) {
            sender.sendMessage(plugin.getMessageUtils().get("general.no-permission"));
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            sender.sendMessage(plugin.getMessageUtils().get("general.reload-success"));
            return true;
        }
        sender.sendMessage(plugin.getMessageUtils().get("commands.ur.usage"));
        return true;
    }
} 