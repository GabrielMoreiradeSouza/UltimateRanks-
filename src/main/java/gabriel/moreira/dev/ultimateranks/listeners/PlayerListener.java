package gabriel.moreira.dev.ultimateranks.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import gabriel.moreira.dev.ultimateranks.UltimateRanks;
import gabriel.moreira.dev.ultimateranks.stats.PlayerStats;
import gabriel.moreira.dev.ultimateranks.stats.StatsManager;

/**
 * Listener centralizado para atualizar estatísticas dos jogadores
 */
public class PlayerListener implements Listener {
    private final UltimateRanks plugin;
    private final StatsManager statsManager;
    // Para controle de tempo online
    private final Map<UUID, Long> joinTimes = new HashMap<>();
    // Para controle de voo
    private final Map<UUID, Long> flyStartTimes = new HashMap<>();

    public PlayerListener(UltimateRanks plugin) {
        this.plugin = plugin;
        this.statsManager = plugin.getStatsManager();
        // Agendar atualização periódica do tempo online
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateOnlineTime(player);
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60); // a cada 60s
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        joinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        // Carregar stats para o cache
        statsManager.getPlayerStats(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        updateOnlineTime(player);
        joinTimes.remove(player.getUniqueId());
        // Salvar stats ao sair
        PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId(), player.getName());
        statsManager.savePlayerStats(stats);
    }

    private void updateOnlineTime(Player player) {
        Long join = joinTimes.get(player.getUniqueId());
        if (join != null) {
            long minutes = (System.currentTimeMillis() - join) / 1000 / 60;
            PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId(), player.getName());
            stats.tempoOnline += minutes;
            joinTimes.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId(), player.getName());
        // Andar blocos
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
            event.getFrom().getBlockY() != event.getTo().getBlockY() ||
            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            stats.andarBlocos++;
            statsManager.markCategoryChanged("andarblocos");
        }
        // Carrinho de mina
        Entity vehicle = player.getVehicle();
        if (vehicle != null && vehicle instanceof Vehicle && vehicle.getType().toString().contains("MINECART")) {
            stats.carrinho++;
            statsManager.markCategoryChanged("carrinho");
        }
        // Barco
        if (vehicle != null && vehicle.getType().toString().contains("BOAT")) {
            stats.barco++;
            statsManager.markCategoryChanged("barco");
        }
        // Voo (elytra ou fly)
        if (player.isFlying() || player.isGliding()) {
            stats.voar++;
            statsManager.markCategoryChanged("voar");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId(), player.getName());
        stats.quebrar++;
        statsManager.markCategoryChanged("quebrar");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId(), player.getName());
        stats.colocar++;
        statsManager.markCategoryChanged("colocar");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player) return; // Não contar players
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            PlayerStats stats = statsManager.getPlayerStats(killer.getUniqueId(), killer.getName());
            stats.mobs++;
            statsManager.markCategoryChanged("mobs");
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        PlayerStats statsVictim = statsManager.getPlayerStats(victim.getUniqueId(), victim.getName());
        statsVictim.mortes++;
        statsManager.markCategoryChanged("mortes");
        
        Player killer = victim.getKiller();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            PlayerStats statsKiller = statsManager.getPlayerStats(killer.getUniqueId(), killer.getName());
            statsKiller.matarPvp++;
            statsManager.markCategoryChanged("matarpvp");
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (event.isFlying()) {
            flyStartTimes.put(player.getUniqueId(), System.currentTimeMillis());
        } else {
            Long start = flyStartTimes.remove(player.getUniqueId());
            if (start != null) {
                long minutes = (System.currentTimeMillis() - start) / 1000 / 60;
                PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId(), player.getName());
                stats.voar += minutes;
            }
        }
    }
} 