package gabriel.moreira.dev.ultimateranks.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;

/**
 * Utilitário para mensagens customizadas do plugin
 */
public class MessageUtils {
    private final UltimateRanks plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessageUtils(UltimateRanks plugin) {
        this.plugin = plugin;
        reloadMessages();
    }

    public void reloadMessages() {
        this.messages = plugin.getConfigManager().getMessages();
        this.prefix = color(messages.getString("prefix", ""));
    }

    /**
     * Busca uma mensagem pelo caminho e aplica prefixo e cores
     */
    public String get(String path) {
        String msg = messages.getString(path);
        if (msg == null) return prefix + ChatColor.RED + "Mensagem não encontrada: " + path;
        return prefix + color(msg);
    }

    /**
     * Aplica cores do Minecraft (&)
     */
    public String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
} 