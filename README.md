# Wumpus World

Jogo desktop em **Java e LibGDX**, com controle manual. Você escolhe cada movimento e a direção da flecha, usando brisa e fedor para encontrar o ouro e voltar à base.

![Interface do jogo com o mapa revelado](docs/interface.png)

Consulte a [documentação da versão gráfica](docs/VERSAO_GRAFICA.md) para conhecer as adições em relação à versão inicial, tecnologias, arquitetura, técnicas de computação gráfica e limitações.

## Executar

Requisitos: **JDK 17 ou superior**, ambiente gráfico e suporte a OpenGL. O Gradle Wrapper está incluído; o primeiro build baixa as dependências.

Linux / macOS:

```sh
./gradlew run
```

Windows:

```powershell
.\gradlew.bat run
```

No IntelliJ IDEA, importe `build.gradle.kts` como projeto Gradle, aguarde a sincronização e execute `application > run` no painel Gradle. Também é possível executar o método `main` de `wumpusworld.desktop.DesktopLauncher`. No macOS, a tarefa `run` configura `-XstartOnFirstThread`; acrescente essa opção à JVM ao executar a classe diretamente pela IDE.

## Como jogar

O personagem começa em `[0,0]`. Cada comando de movimento válido avança uma casa. **Nada se move enquanto você pensa.** As percepções e o diário ajudam a escolher o próximo passo.

| Ação | Teclado | Mouse |
| --- | --- | --- |
| Mover para cima | ↑ ou W | Cima |
| Mover para baixo | ↓ ou S | Baixo |
| Mover para a esquerda | ← ou A | Esquerda |
| Mover para a direita | → ou D | Direita |
| Preparar flecha | F | Preparar flecha |
| Disparar | Uma direção, com a mira ativa | Um botão direcional, com a mira ativa |
| Cancelar mira | Esc ou F | Cancelar mira |
| Nova partida | R | Nova partida |
| Revelar / ocultar mapa | V | Revelar / ocultar mapa |
| Consultar diário | — | Roda do mouse sobre o painel |

**Para atirar:** pressione F e depois escolha uma direção. Esse comando dispara sem mover o personagem e consome a única flecha, mesmo se errar. Apenas preparar ou cancelar a mira não custa pontos. Sentir fedor não provoca disparo automático.

O ouro é coletado ao entrar na casa onde está. Depois, **você escolhe o caminho de volta**; chegar à base com o ouro encerra a partida com vitória. Bater na borda do mapa não gasta movimentos nem pontos.

As casas visitadas recebem uma cor diferente. Ao terminar, o mapa é revelado e movimentos/disparos são bloqueados. **Nova partida** restaura posição, mapa, inventário, histórico, névoa e mira. Revelar o mapa durante o jogo funciona como ajuda visual.

## Regras

- Mapa fixo 5×5; base em `[0,0]`, poços em `[1,2]` e `[3,1]`, Wumpus em `[2,3]` e ouro em `[4,4]`.
- Brisa indica poço em um dos quatro vizinhos ortogonais. Fedor indica Wumpus em um desses vizinhos.
- A flecha percorre uma linha reta até acertar o Wumpus ou sair do mapa.
- Cair em um poço causa morte. Um encontro direto com o Wumpus mantém a regra original: sorteio com 50% de chance de sobrevivência; vencer elimina o Wumpus.
- A exploração termina em 180 movimentos sem ouro. Depois da coleta, o retorno pode ultrapassar esse limite.
- Movimento: **−1**; flecha: **−10**; ouro: **+100**; Wumpus abatido: **+50**; morte: **−100**; retorno com ouro: **+200**.
- Disparar não conta como movimento. O bônus de vitória é concedido uma única vez.

## Organização

```text
src/main/java/wumpusworld/
├── dominio/
│   ├── Mundo.java                 # Mapa, elementos, percepções e flecha
│   ├── Direcao.java               # Direções de movimento e disparo
│   └── AgenteInteligente.java     # Estado, movimento e estratégia autônoma original
├── aplicacao/
│   ├── Partida.java               # Ações, encontros, eventos, pontuação e resultado
│   ├── EstadoPartida.java         # Exploração, retorno e resultados
│   └── ControladorJogo.java       # Comandos manuais, mira e reinício
├── grafico/
│   ├── WumpusGame.java            # Ciclo de vida da aplicação LibGDX
│   ├── TelaPartida.java           # Layout e controles Scene2D
│   ├── TabuleiroActor.java        # Desenho e animação do tabuleiro
│   └── Tema.java                  # Cores, fontes e estilos
├── desktop/
│   └── DesktopLauncher.java       # Janela com backend LWJGL3
└── Main.java                      # Demonstração autônoma original no terminal
```

O domínio e a aplicação não importam LibGDX. A tela delega os comandos ao controlador; `Partida` aplica as regras. A renderização apenas atualiza a animação e desenha o estado, sem decidir movimentos. Os recursos gráficos são liberados em `dispose()`.

O tabuleiro usa primitivas geométricas. A fonte DejaVu Sans e sua licença estão em `src/main/resources/fonts/`.

### Demonstração autônoma no terminal

A estratégia anterior continua disponível separadamente:

```sh
./gradlew runConsole
```

Nessa demonstração, o agente usa memória, estimativas de risco e desempates aleatórios; dispara ao sentir fedor e retorna pelo histórico. A interface gráfica utiliza somente as ações manuais.

## Verificação

```sh
./gradlew test
```

Os testes cobrem movimentos manuais, bordas, mira, disparos, coleta, retorno escolhido pelo jogador, pontuação e bloqueio após o fim. Os testes da estratégia original também foram preservados, incluindo 300 partidas com sementes fixas.

Verificação gráfica opcional, com janela temporária que encerra automaticamente:

```sh
./gradlew verifyDesktop
```

Exercita mouse, WASD, setas, mira, disparo, coleta, retorno manual, vitória, reinício e redimensionamento. Verifica também que esperar não move o jogador. As capturas ficam em `build/desktop-validation/`. Requer ambiente gráfico e não faz parte do teste unitário padrão.

## Distribuição

```sh
./gradlew installDist
```

Execute `build/install/WumpusWorld/bin/WumpusWorld` (ou `WumpusWorld.bat` no Windows). Para gerar ZIP e TAR com scripts e dependências, use `./gradlew assembleDist`.

A distribuição requer Java instalado. Se um pacote gerado no Linux/Windows for usado no macOS, configure `JAVA_OPTS=-XstartOnFirstThread` antes de executar o script.

## Referências

LibGDX 1.14.2, [Scene2D UI](https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui), [FitViewport](https://libgdx.com/wiki/graphics/viewports) e backend LWJGL3. Consulte a [documentação de configuração](https://libgdx.com/wiki/start/project-generation).
