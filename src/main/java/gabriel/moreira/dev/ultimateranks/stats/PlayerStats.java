package gabriel.moreira.dev.ultimateranks.stats;

import java.util.UUID;

/**
 * Representa as estatísticas de um jogador.
 */
public class PlayerStats {
    private final UUID uuid;
    private String name;
    public long andarBlocos;
    public int matarPvp;
    public int mortes;
    public long carrinho;
    public long barco;
    public long tempoOnline;
    public int mobs;
    public long quebrar;
    public long colocar;
    public long voar;

    public PlayerStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Getters e setters para cada estatística podem ser adicionados conforme necessário
} 