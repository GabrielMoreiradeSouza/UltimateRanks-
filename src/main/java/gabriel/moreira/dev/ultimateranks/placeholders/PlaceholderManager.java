package gabriel.moreira.dev.ultimateranks.placeholders;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;
import gabriel.moreira.dev.ultimateranks.stats.PlayerStats;
import gabriel.moreira.dev.ultimateranks.stats.StatsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

/**
 * Gerenciador de placeholders para PlaceholderAPI
 */
public class PlaceholderManager extends PlaceholderExpansion {
    private final UltimateRanks plugin;
    private final StatsManager statsManager;

    public PlaceholderManager(UltimateRanks plugin) {
        this.plugin = plugin;
        this.statsManager = plugin.getStatsManager();
    }

    @Override
    public String getIdentifier() {
        return "ultimateranks";
    }

    @Override
    public String getAuthor() {
        return "Gabriel Moreira";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        // Placeholders de top: %ultimateranks_top_<categoria>_<posicao>%
        if (identifier.startsWith("top_")) {
            String[] parts = identifier.split("_");
            if (parts.length >= 3) {
                String category = parts[1];
                try {
                    int position = Integer.parseInt(parts[2]);
                    return getTopPlaceholder(category, position);
                } catch (NumberFormatException e) {
                    return plugin.getMessageUtils().get("placeholders.not-found");
                }
            }
        }

        // Placeholders de posição: %ultimateranks_posicao_<categoria>_<player>%
        if (identifier.startsWith("posicao_")) {
            String[] parts = identifier.split("_");
            if (parts.length >= 3) {
                String category = parts[1];
                String playerName = parts[2];
                return getPositionPlaceholder(category, playerName);
            }
        }

        // Placeholders de valor: %ultimateranks_valor_<categoria>_<player>%
        if (identifier.startsWith("valor_")) {
            String[] parts = identifier.split("_");
            if (parts.length >= 3) {
                String category = parts[1];
                String playerName = parts[2];
                return getValuePlaceholder(category, playerName);
            }
        }

        return null;
    }

    /**
     * Retorna o jogador na posição X de uma categoria
     */
    private String getTopPlaceholder(String category, int position) {
        if (!plugin.getConfigManager().isCategoryEnabled(category)) {
            return plugin.getMessageUtils().get("placeholders.not-found");
        }

        var top = statsManager.getTop(category, position);
        if (top.size() >= position) {
            PlayerStats stats = top.get(position - 1);
            return stats.getName();
        }

        return plugin.getMessageUtils().get("placeholders.not-found");
    }

    /**
     * Retorna a posição de um jogador em uma categoria
     */
    private String getPositionPlaceholder(String category, String playerName) {
        if (!plugin.getConfigManager().isCategoryEnabled(category)) {
            return plugin.getMessageUtils().get("placeholders.not-found");
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return plugin.getMessageUtils().get("placeholders.not-found");
        }

        int position = statsManager.getPlayerPosition(category, player.getUniqueId());
        if (position > 0) {
            return String.valueOf(position);
        }

        return plugin.getMessageUtils().get("placeholders.not-found");
    }

    /**
     * Retorna o valor de uma estatística de um jogador
     */
    private String getValuePlaceholder(String category, String playerName) {
        if (!plugin.getConfigManager().isCategoryEnabled(category)) {
            return plugin.getMessageUtils().get("placeholders.not-found");
        }

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            return plugin.getMessageUtils().get("placeholders.not-found");
        }

        PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId(), player.getName());
        return getStatValue(stats, category);
    }

    /**
     * Obtém o valor de uma estatística específica
     */
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

    /**
     * Registra os placeholders no PlaceholderAPI
     */
    public boolean register() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            super.register();
            plugin.getLogger().info("Placeholders registrados com sucesso!");
            return true;
        }
        return false;
    }
} 