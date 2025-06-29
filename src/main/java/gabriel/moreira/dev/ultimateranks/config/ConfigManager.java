package gabriel.moreira.dev.ultimateranks.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;

/**
 * Gerenciador de configurações do plugin
 * Responsável por carregar e gerenciar todos os arquivos de configuração
 * 
 * @author Gabriel Moreira
 */
public class ConfigManager {
    
    private final UltimateRanks plugin;
    private final Logger logger;
    
    private FileConfiguration config;
    private FileConfiguration messages;
    private File configFile;
    private File messagesFile;
    
    // Configurações do banco de dados
    private String dbHost;
    private int dbPort;
    private String dbDatabase;
    private String dbUsername;
    private String dbPassword;
    private int dbMaxPoolSize;
    private int dbMinIdle;
    private int dbConnectionTimeout;
    private int dbIdleTimeout;
    private int dbMaxLifetime;
    
    // Configurações gerais
    private int saveInterval;
    private int topSize;
    private Map<String, Boolean> categories;
    
    // Configurações de performance
    private int cacheDuration;
    private boolean autoUpdateCache;
    private int slowQueryThreshold;
    
    // Configurações de debug
    private boolean debugEnabled;
    private boolean logSql;
    private boolean logEvents;
    
    // Configurações de atualização automática
    private boolean autoUpdateEnabled;
    private int autoUpdateInterval;
    private boolean smartUpdate;
    private boolean logUpdates;
    
    public ConfigManager(UltimateRanks plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.categories = new HashMap<>();
    }
    
    /**
     * Carrega todas as configurações do plugin
     */
    public void loadConfigs() {
        loadMainConfig();
        loadMessagesConfig();
        loadDatabaseConfig();
        loadGeneralConfig();
        loadPerformanceConfig();
        loadDebugConfig();
        loadAutoUpdateConfig();
    }
    
    /**
     * Carrega o arquivo de configuração principal
     */
    private void loadMainConfig() {
        // Salvar configuração padrão se não existir
        plugin.saveDefaultConfig();
        
        // Recarregar configuração
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        logger.info("Configuração principal carregada!");
    }
    
    /**
     * Carrega o arquivo de mensagens
     */
    private void loadMessagesConfig() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Atualizar mensagens se houver mudanças no arquivo padrão
        updateMessagesFile();
        
        logger.info("Arquivo de mensagens carregado!");
    }
    
    /**
     * Atualiza o arquivo de mensagens com novas mensagens se necessário
     */
    private void updateMessagesFile() {
        try {
            InputStream inputStream = plugin.getResource("messages.yml");
            if (inputStream != null) {
                YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)
                );
                
                boolean updated = false;
                for (String key : defaultMessages.getKeys(true)) {
                    if (!messages.contains(key)) {
                        messages.set(key, defaultMessages.get(key));
                        updated = true;
                    }
                }
                
                if (updated) {
                    messages.save(messagesFile);
                    logger.info("Arquivo de mensagens atualizado com novas mensagens!");
                }
            }
        } catch (IOException e) {
            logger.warning("Erro ao atualizar arquivo de mensagens: " + e.getMessage());
        }
    }
    
    /**
     * Carrega configurações do banco de dados
     */
    private void loadDatabaseConfig() {
        dbHost = config.getString("database.host", "localhost");
        dbPort = config.getInt("database.port", 3306);
        dbDatabase = config.getString("database.database", "ultimateranks");
        dbUsername = config.getString("database.username", "root");
        dbPassword = config.getString("database.password", "password");
        
        dbMaxPoolSize = config.getInt("database.connection-pool.maximum-pool-size", 10);
        dbMinIdle = config.getInt("database.connection-pool.minimum-idle", 5);
        dbConnectionTimeout = config.getInt("database.connection-pool.connection-timeout", 30000);
        dbIdleTimeout = config.getInt("database.connection-pool.idle-timeout", 600000);
        dbMaxLifetime = config.getInt("database.connection-pool.max-lifetime", 1800000);
        
        logger.info("Configurações do banco de dados carregadas!");
    }
    
    /**
     * Carrega configurações gerais
     */
    private void loadGeneralConfig() {
        saveInterval = config.getInt("settings.save-interval", 300);
        topSize = config.getInt("settings.top-size", 10);
        
        // Carregar categorias
        categories.clear();
        for (String category : getAvailableCategories()) {
            categories.put(category, config.getBoolean("settings.categories." + category, true));
        }
        
        logger.info("Configurações gerais carregadas!");
    }
    
    /**
     * Carrega configurações de performance
     */
    private void loadPerformanceConfig() {
        cacheDuration = config.getInt("performance.cache-duration", 60);
        autoUpdateCache = config.getBoolean("performance.auto-update-cache", true);
        slowQueryThreshold = config.getInt("performance.slow-query-threshold", 1000);
        
        logger.info("Configurações de performance carregadas!");
    }
    
    /**
     * Carrega configurações de debug
     */
    private void loadDebugConfig() {
        debugEnabled = config.getBoolean("debug.enabled", false);
        logSql = config.getBoolean("debug.log-sql", false);
        logEvents = config.getBoolean("debug.log-events", false);
        
        logger.info("Configurações de debug carregadas!");
    }
    
    /**
     * Carrega configurações de atualização automática
     */
    private void loadAutoUpdateConfig() {
        autoUpdateEnabled = config.getBoolean("auto-update.enabled", true);
        autoUpdateInterval = config.getInt("auto-update.interval", 60);
        smartUpdate = config.getBoolean("auto-update.smart-update", true);
        logUpdates = config.getBoolean("auto-update.log-updates", false);
        
        // Validar intervalo mínimo
        if (autoUpdateInterval < 30) {
            autoUpdateInterval = 30;
            logger.warning("Intervalo de atualização muito baixo! Definido para 30 segundos.");
        }
        
        logger.info("Configurações de atualização automática carregadas! Intervalo: " + autoUpdateInterval + "s");
    }
    
    /**
     * Retorna a lista de categorias disponíveis
     */
    public String[] getAvailableCategories() {
        return new String[]{
            "andarblocos", "matarpvp", "mortes", "carrinho", "barco",
            "tempoonline", "mobs", "quebrar", "colocar", "voar"
        };
    }
    
    /**
     * Verifica se uma categoria está habilitada
     */
    public boolean isCategoryEnabled(String category) {
        return categories.getOrDefault(category, false);
    }
    
    /**
     * Recarrega todas as configurações
     */
    public void reload() {
        loadConfigs();
    }
    
    // Getters para configurações do banco de dados
    
    public String getDbHost() {
        return dbHost;
    }
    
    public int getDbPort() {
        return dbPort;
    }
    
    public String getDbDatabase() {
        return dbDatabase;
    }
    
    public String getDbUsername() {
        return dbUsername;
    }
    
    public String getDbPassword() {
        return dbPassword;
    }
    
    public int getDbMaxPoolSize() {
        return dbMaxPoolSize;
    }
    
    public int getDbMinIdle() {
        return dbMinIdle;
    }
    
    public int getDbConnectionTimeout() {
        return dbConnectionTimeout;
    }
    
    public int getDbIdleTimeout() {
        return dbIdleTimeout;
    }
    
    public int getDbMaxLifetime() {
        return dbMaxLifetime;
    }
    
    // Getters para configurações gerais
    
    public int getSaveInterval() {
        return saveInterval;
    }
    
    public int getTopSize() {
        return topSize;
    }
    
    public Map<String, Boolean> categories() {
        return categories;
    }
    
    // Getters para configurações de performance
    
    public int getCacheDuration() {
        return cacheDuration;
    }
    
    public boolean isAutoUpdateCache() {
        return autoUpdateCache;
    }
    
    public int getSlowQueryThreshold() {
        return slowQueryThreshold;
    }
    
    // Getters para configurações de debug
    
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
    
    public boolean isLogSql() {
        return logSql;
    }
    
    public boolean isLogEvents() {
        return logEvents;
    }
    
    // Getters para configurações de atualização automática
    
    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }
    
    public int getAutoUpdateInterval() {
        return autoUpdateInterval;
    }
    
    public boolean isSmartUpdate() {
        return smartUpdate;
    }
    
    public boolean isLogUpdates() {
        return logUpdates;
    }
    
    // Getters para arquivos de configuração
    
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getMessages() {
        return messages;
    }
} 