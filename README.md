# Wumpus World

Visualização desktop em **Java e LibGDX** do Mundo de Wumpus. O agente autônomo decide sozinho cada movimento e disparo, em intervalos regulares, e a janela mostra a cada passo o estado do agente e do ambiente.

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

No NetBeans (com suporte a Gradle) ou no IntelliJ IDEA, abra/importe o projeto pela pasta ou por `build.gradle.kts`, aguarde a sincronização e execute a tarefa `application > run`. Também é possível executar o método `main` de `wumpusworld.desktop.DesktopLauncher`. No macOS, a tarefa `run` configura `-XstartOnFirstThread`; acrescente essa opção à JVM ao executar a classe diretamente pela IDE.

## Como funciona

A partida começa sozinha: o agente sai de `[0,0]` e, a cada intervalo, observa brisa e fedor, decide o próximo passo (e dispara a flecha ao sentir fedor) e a tela atualiza posição, percepções, movimentos, pontuação, ouro e flecha. Nada exige digitação para avançar. O intervalo é medido pelo tempo entre quadros, sem `sleep`, então a janela nunca trava.

| Controle | Teclado | Mouse |
| --- | --- | --- |
| Pausar / continuar | Espaço | Pausar / Continuar |
| Trocar velocidade (lenta 1,0 s, normal 0,6 s, rápida 0,25 s por passo) | — | Velocidade |
| Nova partida | R | Nova partida |
| Revelar / ocultar mapa (só ajuda visual; o agente não o usa) | V | Revelar mapa |
| Consultar diário | — | Roda do mouse sobre o painel |

O ouro é coletado ao entrar na casa onde está e o agente volta pelo caminho percorrido; chegar à base com o ouro encerra a partida com vitória. As casas visitadas recebem uma cor diferente. Ao terminar (vitória, morte ou limite de exploração), o mapa completo é revelado.

## Linhas e colunas na tela

O domínio usa `[linha, coluna]` com origem no canto superior esquerdo. `TabuleiroActor` calcula o lado do tabuleiro pelo menor lado da área disponível e divide por 5 (`passo`); `Grade` faz a conversão. A coluna vira o deslocamento horizontal, `x + coluna * passo`; a linha vira o vertical, `y + (5 - 1 - linha) * passo`, porque o eixo Y do LibGDX cresce para cima. Cada casa mostra seu rótulo `linha,coluna`.

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
│   └── AgenteInteligente.java     # Estado, movimento e estratégia autônoma
├── aplicacao/
│   ├── Partida.java               # Decisão autônoma, encontros, eventos, pontuação e resultado
│   ├── EstadoPartida.java         # Exploração, retorno e resultados
│   ├── Disparo.java               # Registro imutável da flecha, para a interface animá-la
│   ├── Velocidade.java            # Intervalos entre decisões
│   └── ControladorJogo.java       # Temporização, pausa, velocidade e reinício
├── grafico/
│   ├── WumpusGame.java            # Ciclo de vida da aplicação LibGDX
│   ├── TelaPartida.java           # Layout, painéis e atalhos Scene2D
│   ├── TabuleiroActor.java        # Desenho e animação do tabuleiro
│   ├── Sprites.java               # Desenho procedural de casas, agente, poço, ouro e Wumpus
│   ├── Efeitos.java               # Partículas, flecha, tremor, flash e faixa de resultado
│   ├── Grade.java                 # Conversão de linha/coluna para coordenadas de tela
│   └── Tema.java                  # Cores, fontes e estilos
├── desktop/
│   └── DesktopLauncher.java       # Janela com backend LWJGL3
└── Main.java                      # Demonstração autônoma no terminal
```

O domínio e a aplicação não importam LibGDX. A tela repassa o tempo decorrido ao controlador, que aciona `Partida` em intervalos regulares; `Partida` aplica as regras. A renderização apenas anima e desenha o estado, sem decidir movimentos nem consultar posições ocultas para o agente. Os recursos gráficos são liberados em `dispose()`.

O tabuleiro é desenhado por primitivas geométricas, degradês e transparência, sem imagens externas. A fonte DejaVu Sans e sua licença estão em `src/main/resources/fonts/`.

### Demonstração no terminal

A mesma partida pode ser executada sem janela:

```sh
./gradlew runConsole
```

## Verificação

```sh
./gradlew test
```

Os testes cobrem a temporização do controlador (intervalo, pausa, velocidade, fim e reinício), as regras do mundo e 300 partidas autônomas com sementes fixas, verificando movimentos, pontuação e término.

Verificação gráfica opcional, com janela temporária que encerra automaticamente:

```sh
./gradlew verifyDesktop
```

Confere que o agente se move sozinho, que a pausa o congela, a troca de velocidade, o fim da partida (que bloqueia novas decisões), o reinício e o redimensionamento. As capturas ficam em `build/desktop-validation/`. Requer ambiente gráfico e não faz parte do teste unitário padrão.

## Distribuição

```sh
./gradlew installDist
```

Execute `build/install/WumpusWorld/bin/WumpusWorld` (ou `WumpusWorld.bat` no Windows). Para gerar ZIP e TAR com scripts e dependências, use `./gradlew assembleDist`.

A distribuição requer Java instalado. Se um pacote gerado no Linux/Windows for usado no macOS, configure `JAVA_OPTS=-XstartOnFirstThread` antes de executar o script.

## Referências

LibGDX 1.14.2, [Scene2D UI](https://libgdx.com/wiki/graphics/2d/scene2d/scene2d-ui), [FitViewport](https://libgdx.com/wiki/graphics/viewports) e backend LWJGL3. Consulte a [documentação de configuração](https://libgdx.com/wiki/start/project-generation).
