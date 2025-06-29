package gabriel.moreira.dev.ultimateranks;

import gabriel.moreira.dev.ultimateranks.commands.ReloadCommand;
import gabriel.moreira.dev.ultimateranks.commands.TopCommand;
import gabriel.moreira.dev.ultimateranks.config.ConfigManager;
import gabriel.moreira.dev.ultimateranks.database.DatabaseManager;
import gabriel.moreira.dev.ultimateranks.listeners.PlayerListener;
import gabriel.moreira.dev.ultimateranks.placeholders.PlaceholderManager;
import gabriel.moreira.dev.ultimateranks.stats.StatsManager;
import gabriel.moreira.dev.ultimateranks.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Classe principal do plugin UltimateRanks
 * Plugin de rankings para servidores Spigot/Paper
 * 
 * @author Gabriel Moreira
 * @version 1.0.0
 */
public class UltimateRanks extends JavaPlugin {
    
    private static UltimateRanks instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private StatsManager statsManager;
    private MessageUtils messageUtils;
    private PlaceholderManager placeholderManager;
    
    @Override
    public void onEnable() {
        instance = this;
        Logger logger = getLogger();
        
        logger.info("=== UltimateRanks v" + getDescription().getVersion() + " ===");
        logger.info("Iniciando plugin...");
        
        try {
            // Inicializar gerenciadores
            initializeManagers();
            
            // Registrar comandos
            registerCommands();
            
            // Registrar listeners
            registerListeners();
            
            // Registrar placeholders (se PlaceholderAPI estiver disponível)
            registerPlaceholders();
            
            // Inicializar banco de dados
            if (!databaseManager.initialize()) {
                logger.severe("Falha ao conectar com o banco de dados! Plugin será desabilitado.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            // Inicializar estatísticas
            statsManager.initialize();
            
            logger.info("Plugin iniciado com sucesso!");
            
        } catch (Exception e) {
            logger.severe("Erro ao iniciar o plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        Logger logger = getLogger();
        logger.info("Desabilitando UltimateRanks...");
        
        try {
            // Salvar dados pendentes
            if (statsManager != null) {
                statsManager.saveAllData();
            }
            
            // Fechar conexões do banco
            if (databaseManager != null) {
                databaseManager.shutdown();
            }
            
            logger.info("Plugin desabilitado com sucesso!");
            
        } catch (Exception e) {
            logger.severe("Erro ao desabilitar o plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Inicializa todos os gerenciadores do plugin
     */
    private void initializeManagers() {
        // Configurações
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Utilitários de mensagem
        messageUtils = new MessageUtils(this);
        
        // Banco de dados
        databaseManager = new DatabaseManager(this);
        
        // Estatísticas
        statsManager = new StatsManager(this);
        
        // Placeholders
        placeholderManager = new PlaceholderManager(this);
    }
    
    /**
     * Registra todos os comandos do plugin
     */
    private void registerCommands() {
        // Comando principal /ur
        getCommand("ur").setExecutor(new ReloadCommand(this));
        
        // Comando de ranking /top
        getCommand("top").setExecutor(new TopCommand(this));
    }
    
    /**
     * Registra todos os listeners do plugin
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    /**
     * Registra placeholders se PlaceholderAPI estiver disponível
     */
    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderManager.register();
            getLogger().info("PlaceholderAPI encontrado! Placeholders registrados.");
        } else {
            getLogger().info("PlaceholderAPI não encontrado. Placeholders não estarão disponíveis.");
        }
    }
    
    /**
     * Recarrega todas as configurações do plugin
     */
    public void reloadPlugin() {
        try {
            // Recarregar configurações
            configManager.loadConfigs();
            
            // Recarregar mensagens
            messageUtils.reloadMessages();
            
            // Recarregar banco de dados
            databaseManager.reload();
            
            // Recarregar estatísticas
            statsManager.reload();
            
            getLogger().info("Plugin recarregado com sucesso!");
            
        } catch (Exception e) {
            getLogger().severe("Erro ao recarregar o plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters para acessar os gerenciadores
    
    public static UltimateRanks getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public StatsManager getStatsManager() {
        return statsManager;
    }
    
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
} 