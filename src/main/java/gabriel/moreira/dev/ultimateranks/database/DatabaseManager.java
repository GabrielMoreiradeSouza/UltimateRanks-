package gabriel.moreira.dev.ultimateranks.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;

/**
 * Gerenciador de banco de dados do plugin
 * Utiliza HikariCP para pool de conexões
 * 
 * @author Gabriel Moreira
 */
public class DatabaseManager {
    
    private final UltimateRanks plugin;
    private final Logger logger;
    private HikariDataSource dataSource;
    private boolean initialized = false;
    
    public DatabaseManager(UltimateRanks plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * Inicializa a conexão com o banco de dados
     */
    public boolean initialize() {
        try {
            // Configurar HikariCP
            HikariConfig config = new HikariConfig();
            
            config.setJdbcUrl("jdbc:mysql://" + 
                plugin.getConfigManager().getDbHost() + ":" + 
                plugin.getConfigManager().getDbPort() + "/" + 
                plugin.getConfigManager().getDbDatabase() + 
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            
            config.setUsername(plugin.getConfigManager().getDbUsername());
            config.setPassword(plugin.getConfigManager().getDbPassword());
            
            // Configurações do pool
            config.setMaximumPoolSize(plugin.getConfigManager().getDbMaxPoolSize());
            config.setMinimumIdle(plugin.getConfigManager().getDbMinIdle());
            config.setConnectionTimeout(plugin.getConfigManager().getDbConnectionTimeout());
            config.setIdleTimeout(plugin.getConfigManager().getDbIdleTimeout());
            config.setMaxLifetime(plugin.getConfigManager().getDbMaxLifetime());
            
            // Configurações adicionais
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // Criar data source
            dataSource = new HikariDataSource(config);
            
            // Testar conexão
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    logger.info("Conexão com banco de dados estabelecida com sucesso!");
                    
                    // Criar tabelas se não existirem
                    createTables();
                    
                    initialized = true;
                    return true;
                } else {
                    logger.severe("Conexão com banco de dados inválida!");
                    return false;
                }
            }
            
        } catch (Exception e) {
            logger.severe("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cria as tabelas necessárias no banco de dados
     */
    private void createTables() {
        String createStatsTable = """
            CREATE TABLE IF NOT EXISTS player_stats (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid VARCHAR(36) NOT NULL,
                player_name VARCHAR(16) NOT NULL,
                andarblocos BIGINT DEFAULT 0,
                matarpvp INT DEFAULT 0,
                mortes INT DEFAULT 0,
                carrinho BIGINT DEFAULT 0,
                barco BIGINT DEFAULT 0,
                tempoonline BIGINT DEFAULT 0,
                mobs INT DEFAULT 0,
                quebrar BIGINT DEFAULT 0,
                colocar BIGINT DEFAULT 0,
                voar BIGINT DEFAULT 0,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                UNIQUE KEY unique_player (player_uuid)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
            """;
        
        String createIndexes = """
            CREATE INDEX IF NOT EXISTS idx_player_stats_andarblocos ON player_stats(andarblocos DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_matarpvp ON player_stats(matarpvp DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_mortes ON player_stats(mortes DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_carrinho ON player_stats(carrinho DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_barco ON player_stats(barco DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_tempoonline ON player_stats(tempoonline DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_mobs ON player_stats(mobs DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_quebrar ON player_stats(quebrar DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_colocar ON player_stats(colocar DESC);
            CREATE INDEX IF NOT EXISTS idx_player_stats_voar ON player_stats(voar DESC);
            """;
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Criar tabela principal
            statement.execute(createStatsTable);
            
            // Criar índices
            for (String indexSql : createIndexes.split(";")) {
                if (!indexSql.trim().isEmpty()) {
                    try {
                        statement.execute(indexSql);
                    } catch (SQLException e) {
                        // Índice já existe, ignorar erro
                        if (plugin.getConfigManager().isDebugEnabled()) {
                            logger.fine("Índice já existe: " + e.getMessage());
                        }
                    }
                }
            }
            
            logger.info("Tabelas do banco de dados criadas/verificadas com sucesso!");
            
        } catch (SQLException e) {
            logger.severe("Erro ao criar tabelas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Executa uma query de forma assíncrona
     */
    public CompletableFuture<ResultSet> executeQueryAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Definir parâmetros
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                
                // Log da query se debug estiver ativado
                if (plugin.getConfigManager().isLogSql()) {
                    logger.info("SQL: " + sql + " | Params: " + java.util.Arrays.toString(params));
                }
                
                return statement.executeQuery();
                
            } catch (SQLException e) {
                logger.severe("Erro ao executar query: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Executa uma atualização de forma assíncrona
     */
    public CompletableFuture<Integer> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Definir parâmetros
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                
                // Log da query se debug estiver ativado
                if (plugin.getConfigManager().isLogSql()) {
                    logger.info("SQL: " + sql + " | Params: " + java.util.Arrays.toString(params));
                }
                
                return statement.executeUpdate();
                
            } catch (SQLException e) {
                logger.severe("Erro ao executar update: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Executa uma query de forma síncrona
     */
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Definir parâmetros
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            // Log da query se debug estiver ativado
            if (plugin.getConfigManager().isLogSql()) {
                logger.info("SQL: " + sql + " | Params: " + java.util.Arrays.toString(params));
            }
            
            return statement.executeQuery();
        }
    }
    
    /**
     * Executa uma atualização de forma síncrona
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Definir parâmetros
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            // Log da query se debug estiver ativado
            if (plugin.getConfigManager().isLogSql()) {
                logger.info("SQL: " + sql + " | Params: " + java.util.Arrays.toString(params));
            }
            
            return statement.executeUpdate();
        }
    }
    
    /**
     * Obtém uma conexão do pool
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Recarrega as configurações do banco de dados
     */
    public void reload() {
        if (initialized) {
            shutdown();
        }
        initialize();
    }
    
    /**
     * Fecha todas as conexões e libera recursos
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Conexões do banco de dados fechadas!");
        }
        initialized = false;
    }
    
    /**
     * Verifica se o banco de dados está inicializado
     */
    public boolean isInitialized() {
        return initialized && dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * Obtém estatísticas do pool de conexões
     */
    public String getPoolStats() {
        if (dataSource == null) {
            return "DataSource não inicializado";
        }
        
        return String.format(
            "Pool Stats - Active: %d, Idle: %d, Total: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getTotalConnections()
        );
    }
} 