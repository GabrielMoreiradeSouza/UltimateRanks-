# UltimateRanks

Plugin de rankings para servidores **Spigot/Paper** com compatibilidade da versão 1.16.4 até 1.21.6.

## 📋 Funcionalidades

O plugin rastreia e exibe o **Top 10 jogadores** nas seguintes categorias:

1. **Blocos Andados** (`/top andarblocos`) - Jogador que mais caminhou em blocos
2. **Kills PvP** (`/top matarpvp`) - Jogador que mais matou outros jogadores
3. **Mortes** (`/top mortes`) - Jogador que mais morreu
4. **Carrinho de Mina** (`/top carrinho`) - Maior distância com carrinho de mina
5. **Barco** (`/top barco`) - Maior distância com barco
6. **Tempo Online** (`/top tempoonline`) - Mais tempo online no servidor
7. **Mobs Mortos** (`/top mobs`) - Mais mobs mortos
8. **Blocos Quebrados** (`/top quebrar`) - Mais blocos quebrados
9. **Blocos Colocados** (`/top colocar`) - Mais blocos colocados
10. **Tempo Voando** (`/top voar`) - Mais tempo voando com elytra/fly

## 🚀 Instalação

### Requisitos
- **Java 21** ou superior
- **Spigot/Paper** 1.16.4 até 1.21.6
- **MySQL/MariaDB** (recomendado) ou SQLite

### Passos
1. Baixe o arquivo `.jar` do plugin
2. Coloque na pasta `plugins/` do seu servidor
3. Configure o banco de dados no `config.yml`
4. Reinicie o servidor

## ⚙️ Configuração

### Banco de Dados (config.yml)
```yaml
database:
  host: "localhost"
  port: 3306
  database: "ultimateranks"
  username: "root"
  password: "sua_senha"
```

### Categorias
Você pode ativar/desativar categorias no `config.yml`:
```yaml
settings:
  categories:
    andarblocos: true
    matarpvp: true
    mortes: true
    # ... outras categorias
```

## 📝 Comandos

### `/top [categoria]`
Exibe o ranking de uma categoria específica.

**Exemplos:**
- `/top` - Lista todas as categorias disponíveis
- `/top andarblocos` - Top 10 jogadores que mais andaram
- `/top matarpvp` - Top 10 jogadores com mais kills PvP

### `/ur reload`
Recarrega todas as configurações do plugin (apenas para administradores).

## 🔧 Permissões

- `ultimateranks.admin` - Acesso total ao plugin
- `ultimateranks.reload` - Pode recarregar o plugin
- `ultimateranks.top` - Pode usar comandos de ranking
- `ultimateranks.top.<categoria>` - Pode ver ranking específico

## 🎯 PlaceholderAPI

O plugin integra com **PlaceholderAPI** e fornece os seguintes placeholders:

### Top Rankings
- `%ultimateranks_top_andarblocos_1%` - 1º lugar em blocos andados
- `%ultimateranks_top_matarpvp_5%` - 5º lugar em kills PvP
- `%ultimateranks_top_mortes_10%` - 10º lugar em mortes

### Posições de Jogadores
- `%ultimateranks_posicao_andarblocos_PlayerName%` - Posição do jogador em blocos andados
- `%ultimateranks_posicao_matarpvp_PlayerName%` - Posição do jogador em kills PvP

### Valores de Estatísticas
- `%ultimateranks_valor_andarblocos_PlayerName%` - Quantos blocos o jogador andou
- `%ultimateranks_valor_matarpvp_PlayerName%` - Quantas kills PvP o jogador tem

## 🏗️ Compatibilidade

### Plugins Testados
- ✅ **PlaceholderAPI** - Placeholders funcionais
- ✅ **DecentHolograms** - Compatível para exibir rankings
- ✅ **EssentialsX** - Compatível com comandos `/seen`

### Versões Suportadas
- **Mínima**: 1.16.4
- **Máxima**: 1.21.6
- **Java**: 21+

## 📊 Performance

- **Cache em memória** para consultas rápidas
- **Pool de conexões** (HikariCP) para MySQL
- **Salvamento assíncrono** para não impactar o servidor
- **Índices otimizados** no banco de dados

## 🐛 Troubleshooting

### Erro de Conexão com Banco
1. Verifique se o MySQL está rodando
2. Confirme as credenciais no `config.yml`
3. Verifique se o banco `ultimateranks` existe

### Estatísticas Não Atualizando
1. Verifique se as categorias estão ativadas
2. Use `/ur reload` para recarregar
3. Verifique os logs do servidor

### Placeholders Não Funcionando
1. Certifique-se de que o PlaceholderAPI está instalado
2. Reinicie o servidor após instalar o PlaceholderAPI
3. Verifique se os placeholders estão escritos corretamente

## 📝 Logs

O plugin registra informações importantes no console:
- Conexão com banco de dados
- Criação de tabelas
- Registro de placeholders
- Erros de configuração

## 🤝 Suporte

Para suporte, reporte bugs ou sugestões:
- **GitHub**: [Issues](https://github.com/gabrielmoreira/UltimateRanks/issues)
- **Discord**: Entre em contato com o desenvolvedor

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

---

**Desenvolvido por Gabriel Moreira** 🚀
