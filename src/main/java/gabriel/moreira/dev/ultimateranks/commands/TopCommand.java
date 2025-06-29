package gabriel.moreira.dev.ultimateranks.commands;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;
import gabriel.moreira.dev.ultimateranks.stats.PlayerStats;
import gabriel.moreira.dev.ultimateranks.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TopCommand implements CommandExecutor, TabCompleter {
    private final UltimateRanks plugin;
    private final StatsManager statsManager;

    public TopCommand(UltimateRanks plugin) {
        this.plugin = plugin;
        this.statsManager = plugin.getStatsManager();
        Bukkit.getPluginCommand("top").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ultimateranks.top")) {
            sender.sendMessage(plugin.getMessageUtils().get("general.no-permission"));
            return true;
        }
        if (args.length == 0) {
            // Listar categorias disponíveis
            sender.sendMessage(plugin.getMessageUtils().get("commands.top.categories.title"));
            for (String cat : plugin.getConfigManager().getAvailableCategories()) {
                if (plugin.getConfigManager().isCategoryEnabled(cat)) {
                    sender.sendMessage(plugin.getMessageUtils().get("commands.top.categories." + cat));
                }
            }
            return true;
        }
        String category = args[0].toLowerCase(Locale.ROOT);
        if (!plugin.getConfigManager().isCategoryEnabled(category)) {
            sender.sendMessage(plugin.getMessageUtils().get("errors.category-not-enabled"));
            return true;
        }
        // Atualizar ranking da categoria antes de mostrar
        plugin.getStatsManager().forceUpdateRanking(category);
        int topSize = plugin.getConfigManager().getTopSize();
        List<PlayerStats> top = statsManager.getTop(category, topSize);
        if (top.isEmpty()) {
            sender.sendMessage(plugin.getMessageUtils().get("ranking.no-data"));
            return true;
        }
        String title = plugin.getMessageUtils().get("ranking.title").replace("{size}", String.valueOf(topSize)).replace("{category}", plugin.getMessageUtils().get("categories." + category + ".name"));
        sender.sendMessage(title);
        String suffix = plugin.getMessageUtils().get("categories." + category + ".suffix");
        int pos = 1;
        for (PlayerStats stats : top) {
            String line = plugin.getMessageUtils().get("ranking.position-with-suffix")
                    .replace("{position}", String.valueOf(pos))
                    .replace("{player}", stats.getName())
                    .replace("{value}", getStatValue(stats, category))
                    .replace("{suffix}", suffix);
            sender.sendMessage(line);
            pos++;
        }
        // Se for jogador, mostrar posição dele
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int playerPos = statsManager.getPlayerPosition(category, player.getUniqueId());
            if (playerPos > 0) {
                sender.sendMessage(plugin.getMessageUtils().get("ranking.player-position")
                        .replace("{position}", String.valueOf(playerPos))
                        .replace("{value}", getStatValue(statsManager.getPlayerStats(player.getUniqueId(), player.getName()), category)));
            } else {
                sender.sendMessage(plugin.getMessageUtils().get("ranking.player-not-ranked"));
            }
        }
        return true;
    }

    private String getStatValue(PlayerStats stats, String category) {
        switch (category) {
            case "andarblocos": return String.valueOf(stats.andarBlocos);
            case "matarpvp": return String.valueOf(stats.matarPvp);
            case "mortes": return String.valueOf(stats.mortes);
            case "carrinho": return String.valueOf(stats.carrinho);
            case "barco": return String.valueOf(stats.barco);
            case "tempoonline": return String.valueOf(stats.tempoOnline);
            case "mobs": return String.valueOf(stats.mobs);
            case "quebrar": return String.valueOf(stats.quebrar);
            case "colocar": return String.valueOf(stats.colocar);
            case "voar": return String.valueOf(stats.voar);
            default: return "0";
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            for (String cat : plugin.getConfigManager().getAvailableCategories()) {
                if (plugin.getConfigManager().isCategoryEnabled(cat)) {
                    options.add(cat);
                }
            }
            return options;
        }
        return Collections.emptyList();
    }
} 