package gabriel.moreira.dev.ultimateranks.stats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;
import gabriel.moreira.dev.ultimateranks.database.DatabaseManager;

/**
 * Gerencia as estatísticas dos jogadores (cache + banco de dados)
 */
public class StatsManager {
    private final UltimateRanks plugin;
    private final DatabaseManager db;
    private final Logger logger;
    private final Map<UUID, PlayerStats> statsCache = new ConcurrentHashMap<>();
    private final Map<String, String> categoryColumnMap = new HashMap<>();

    public StatsManager(UltimateRanks plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabaseManager();
        this.logger = plugin.getLogger();
        // Mapeamento de categoria -> coluna do banco
        categoryColumnMap.put("andarblocos", "andarblocos");
        categoryColumnMap.put("matarpvp", "matarpvp");
        categoryColumnMap.put("mortes", "mortes");
        categoryColumnMap.put("carrinho", "carrinho");
        categoryColumnMap.put("barco", "barco");
        categoryColumnMap.put("tempoonline", "tempoonline");
        categoryColumnMap.put("mobs", "mobs");
        categoryColumnMap.put("quebrar", "quebrar");
        categoryColumnMap.put("colocar", "colocar");
        categoryColumnMap.put("voar", "voar");
    }

    /** Inicializa o cache e tarefas periódicas */
    public void initialize() {
        // Carregar dados do banco se necessário
        // Agendar salvamento periódico
    }

    /** Recarrega dados e cache */
    public void reload() {
        statsCache.clear();
        // Recarregar do banco se necessário
    }

    /** Salva todos os dados do cache no banco */
    public void saveAllData() {
        for (PlayerStats stats : statsCache.values()) {
            savePlayerStats(stats);
        }
    }

    /** Salva as estatísticas de um jogador no banco */
    public void savePlayerStats(PlayerStats stats) {
        String sql = "INSERT INTO player_stats (player_uuid, player_name, andarblocos, matarpvp, mortes, carrinho, barco, tempoonline, mobs, quebrar, colocar, voar) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE player_name=?, andarblocos=?, matarpvp=?, mortes=?, carrinho=?, barco=?, tempoonline=?, mobs=?, quebrar=?, colocar=?, voar=?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, stats.getUuid().toString());
            ps.setString(2, stats.getName());
            ps.setLong(3, stats.andarBlocos);
            ps.setInt(4, stats.matarPvp);
            ps.setInt(5, stats.mortes);
            ps.setLong(6, stats.carrinho);
            ps.setLong(7, stats.barco);
            ps.setLong(8, stats.tempoOnline);
            ps.setInt(9, stats.mobs);
            ps.setLong(10, stats.quebrar);
            ps.setLong(11, stats.colocar);
            ps.setLong(12, stats.voar);
            // Update
            ps.setString(13, stats.getName());
            ps.setLong(14, stats.andarBlocos);
            ps.setInt(15, stats.matarPvp);
            ps.setInt(16, stats.mortes);
            ps.setLong(17, stats.carrinho);
            ps.setLong(18, stats.barco);
            ps.setLong(19, stats.tempoOnline);
            ps.setInt(20, stats.mobs);
            ps.setLong(21, stats.quebrar);
            ps.setLong(22, stats.colocar);
            ps.setLong(23, stats.voar);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("Erro ao salvar estatísticas do jogador: " + stats.getName() + " - " + e.getMessage());
        }
    }

    /** Obtém as estatísticas de um jogador do cache ou banco */
    public PlayerStats getPlayerStats(UUID uuid, String name) {
        return statsCache.computeIfAbsent(uuid, id -> loadPlayerStats(id, name));
    }

    /** Carrega as estatísticas de um jogador do banco */
    private PlayerStats loadPlayerStats(UUID uuid, String name) {
        String sql = "SELECT * FROM player_stats WHERE player_uuid = ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PlayerStats stats = new PlayerStats(uuid, name);
                    stats.andarBlocos = rs.getLong("andarblocos");
                    stats.matarPvp = rs.getInt("matarpvp");
                    stats.mortes = rs.getInt("mortes");
                    stats.carrinho = rs.getLong("carrinho");
                    stats.barco = rs.getLong("barco");
                    stats.tempoOnline = rs.getLong("tempoonline");
                    stats.mobs = rs.getInt("mobs");
                    stats.quebrar = rs.getLong("quebrar");
                    stats.colocar = rs.getLong("colocar");
                    stats.voar = rs.getLong("voar");
                    return stats;
                }
            }
        } catch (SQLException e) {
            logger.warning("Erro ao carregar estatísticas do jogador: " + name + " - " + e.getMessage());
        }
        return new PlayerStats(uuid, name);
    }

    /** Retorna o top N jogadores de uma categoria */
    public List<PlayerStats> getTop(String category, int limit) {
        List<PlayerStats> top = new ArrayList<>();
        String column = categoryColumnMap.get(category);
        if (column == null) return top;
        String sql = "SELECT * FROM player_stats ORDER BY " + column + " DESC LIMIT ?";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                    String name = rs.getString("player_name");
                    PlayerStats stats = new PlayerStats(uuid, name);
                    stats.andarBlocos = rs.getLong("andarblocos");
                    stats.matarPvp = rs.getInt("matarpvp");
                    stats.mortes = rs.getInt("mortes");
                    stats.carrinho = rs.getLong("carrinho");
                    stats.barco = rs.getLong("barco");
                    stats.tempoOnline = rs.getLong("tempoonline");
                    stats.mobs = rs.getInt("mobs");
                    stats.quebrar = rs.getLong("quebrar");
                    stats.colocar = rs.getLong("colocar");
                    stats.voar = rs.getLong("voar");
                    top.add(stats);
                }
            }
        } catch (SQLException e) {
            logger.warning("Erro ao buscar top " + category + ": " + e.getMessage());
        }
        return top;
    }

    /** Retorna a posição de um jogador em uma categoria */
    public int getPlayerPosition(String category, UUID uuid) {
        String column = categoryColumnMap.get(category);
        if (column == null) return -1;
        String sql = "SELECT COUNT(*)+1 AS pos FROM player_stats WHERE " + column + 
                     "> (SELECT " + column + " FROM player_stats WHERE player_uuid = ?)";
        try (Connection conn = db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("pos");
                }
            }
        } catch (SQLException e) {
            logger.warning("Erro ao buscar posição do jogador: " + e.getMessage());
        }
        return -1;
    }
} 