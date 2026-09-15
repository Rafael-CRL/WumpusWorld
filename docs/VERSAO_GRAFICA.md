# Wumpus World — versão com computação gráfica

## 1. Objetivo e referência

Esta versão transforma a demonstração de terminal em um jogo desktop 2D com controle manual. O jogador explora o mapa, interpreta as percepções, decide quando disparar a flecha e escolhe o caminho de volta após coletar o ouro.

As mudanças descritas aqui são relativas ao commit `cc40a85` (Projeto inicial: Mundo de Wumpus em Gradle). A branch proposta para publicação é `feat/computacao-grafica`.

![Interface com o mapa revelado](interface.png)

## 2. O que foi adicionado

| Área | Versão inicial | Versão gráfica |
| --- | --- | --- |
| Apresentação | Mapa e mensagens no terminal | Janela com tabuleiro, painéis e botões |
| Movimento | Agente escolhe os passos automaticamente | Jogador escolhe cada direção por teclado ou mouse |
| Flecha | Estratégia dispara ao sentir fedor | Jogador ativa a mira e escolhe a direção |
| Retorno | Agente segue o histórico | Jogador decide o caminho até a base |
| Acompanhamento | Saída textual por turno | Pontuação, movimentos, inventário, percepções e diário com rolagem |
| Visibilidade | Mapa textual com casas desconhecidas | Casas ocultas, destaque das visitadas e opção de revelar o mapa |
| Reinício | Nova execução | Botão ou tecla R restaura a partida |
| Organização | Classes concentradas no pacote principal | Pacotes de domínio, aplicação, gráficos e inicialização desktop |

O modo autônomo permanece disponível pelo comando `runConsole`. As regras compartilhadas foram concentradas em `Partida`, utilizada pelos dois modos.

## 3. Tecnologias utilizadas

As versões abaixo correspondem à configuração deste repositório.

| Tecnologia | Uso |
| --- | --- |
| Java 17 | Linguagem e versão alvo da compilação |
| Gradle Wrapper 9.2.0 | Compilação, testes, execução e distribuição |
| Kotlin DSL | Configuração do build em `build.gradle.kts` |
| LibGDX 1.14.2 | Ciclo de vida, entrada, recursos e renderização gráfica |
| Backend LWJGL3 | Janela desktop e integração com OpenGL |
| Scene2D UI | Organização da tela com `Stage`, `Table`, textos, botões e rolagem |
| ShapeRenderer | Desenho das formas geométricas do tabuleiro e personagens |
| FitViewport | Preservação das proporções da interface no redimensionamento |
| gdx-freetype e DejaVu Sans | Geração das fontes usadas na interface |
| JUnit Jupiter, BOM 5.10.0 | Testes automatizados das regras e dos comandos |

Os módulos nativos desktop de LibGDX e FreeType também são incluídos em tempo de execução. A fonte e sua licença estão em `src/main/resources/fonts/`.

## 4. Computação gráfica aplicada

### Desenho 2D por primitivas

O tabuleiro e seus elementos são desenhados em código: retângulos representam casas e partes do explorador; círculos e elipses compõem personagens e poços; triângulos formam a base e o ouro. Cores distinguem perigos, tesouro, posição atual e locais visitados.

### Coordenadas e dimensionamento

O domínio usa linha e coluna com origem no canto superior esquerdo. `TabuleiroActor` converte essas posições para as coordenadas de desenho. O tamanho das casas é calculado a partir da área disponível, mantendo o tabuleiro quadrado.

A interface usa uma área virtual de 1240 × 820 com `FitViewport`. A janela inicia em 1280 × 820 e tem tamanho mínimo configurado de 960 × 640. A adaptação preserva a proporção, podendo deixar margens.

### Animação do movimento

A posição visual do explorador é interpolada entre origem e destino com `Interpolation.smooth`, com duração nominal de 0,16 segundo por transição. A regra de movimento é aplicada no comando; a animação representa visualmente o novo estado. O tempo entre quadros não decide ações do jogador.

### Visibilidade e interação

O desenho mostra os elementos das casas visitadas e oculta os demais. O jogador pode revelar o mapa como ajuda visual; ao encerrar a partida, o mapa é revelado automaticamente. Teclado e botões encaminham as ações ao mesmo controlador.

### Recursos gráficos

`Tema` centraliza cores, fontes e estilos. Os recursos de `Stage`, `Skin`, fontes, texturas e `ShapeRenderer` são liberados por seus responsáveis no encerramento da tela. A janela utiliza VSync e limite configurado de 60 FPS em primeiro plano.

## 5. Arquitetura

```text
DesktopLauncher → WumpusGame → TelaPartida → ControladorJogo → Partida
                                  │                            │
                           TabuleiroActor                 Mundo + AgenteInteligente
                                  │
                                 Tema

Main (terminal) → Partida.avancar() → estratégia autônoma
```

- `dominio/Mundo`: mapa, elementos, percepções, visitas e trajetória da flecha.
- `dominio/AgenteInteligente`: posição, inventário, pontuação, movimento e estratégia autônoma preservada.
- `dominio/Direcao`: direções e seus deslocamentos/comandos.
- `aplicacao/Partida`: ações, encontros, coleta, eventos, pontuação e término.
- `aplicacao/EstadoPartida`: exploração, retorno, vitória, morte e limite atingido.
- `aplicacao/ControladorJogo`: movimento manual, preparação/cancelamento de disparo e reinício.
- `grafico/TelaPartida`: composição da tela e tratamento da entrada.
- `grafico/TabuleiroActor`: desenho e animação do mapa.
- `grafico/Tema`: aparência compartilhada.
- `desktop/DesktopLauncher`: configuração e abertura da janela.

Domínio e aplicação não importam LibGDX. Essa separação permite testar as regras sem abrir uma janela ou inicializar OpenGL.

## 6. Controles e regras importantes

| Comando | Ação |
| --- | --- |
| Setas ou WASD | Mover uma casa; com a mira ativa, disparar na direção escolhida |
| F | Preparar ou cancelar a mira |
| Esc | Cancelar a mira |
| R | Reiniciar |
| V | Alternar a revelação do mapa |
| Botões da tela | Movimentos, mira, reinício e revelação |
| Roda do mouse sobre o diário | Consultar eventos |

O mapa fixo tem 5 × 5 casas: base em `[0,0]`, poços em `[1,2]` e `[3,1]`, Wumpus em `[2,3]` e ouro em `[4,4]`. Brisa e fedor consideram os quatro vizinhos ortogonais.

- Ouro é coletado ao entrar na casa; voltar à base com ele concede vitória.
- A única flecha percorre uma linha reta. Acertar ou errar consome a flecha, sem mover o personagem.
- Preparar/cancelar a mira e tentar mover além da borda não custam pontos.
- Poços matam. O encontro direto com Wumpus preserva o sorteio original de 50% de chance de sobrevivência; vencer elimina o monstro.
- O limite de exploração é de 180 movimentos sem ouro. A coleta permite continuar o retorno além desse limite.
- Ao terminar, movimentos e disparos são bloqueados. Reiniciar restaura mapa, posição, inventário, histórico, visibilidade e mira.

| Evento | Pontos |
| --- | ---: |
| Movimento válido | −1 |
| Disparo | −10 |
| Coleta do ouro | +100 |
| Wumpus abatido | +50 |
| Morte | −100 |
| Retorno à base com ouro | +200 |

## 7. Execução e distribuição

Requisitos: JDK 17 ou superior, ambiente gráfico e suporte a OpenGL. O primeiro build precisa baixar as dependências.

```sh
./gradlew run             # Jogo gráfico manual
./gradlew runConsole      # Demonstração autônoma no terminal
./gradlew test            # Testes sem janela
./gradlew verifyDesktop   # Verificação com janela temporária
./gradlew installDist     # Distribuição com scripts e dependências
./gradlew assembleDist    # Pacotes ZIP e TAR
```

No Windows, use `.\gradlew.bat` no lugar de `./gradlew`. No IntelliJ IDEA, importe o projeto Gradle e execute a tarefa `run` ou a classe `wumpusworld.desktop.DesktopLauncher`.

A distribuição fica em `build/install/WumpusWorld/`; o inicializador está em `bin/`. Java deve estar instalado na máquina de destino. No macOS, a tarefa `run` configura `-XstartOnFirstThread`; para execução direta pela IDE, adicione essa opção. Ao levar uma distribuição gerada em Linux/Windows para macOS, configure `JAVA_OPTS=-XstartOnFirstThread`.

## 8. Verificação

A suíte tem 17 testes JUnit, cobrindo percepções, flecha, memória visual, movimentos, bordas, mira, reinício, coleta, retorno manual, morte, pontuação e término. Um dos testes percorre 300 partidas autônomas com sementes fixas; outro verifica a possibilidade de retornar quando o ouro é coletado no último movimento permitido.

`verifyDesktop` exercita entrada por mouse e teclado, espera sem movimento automático, disparo, coleta, retorno, vitória, reinício e redimensionamento. Salva capturas em `build/desktop-validation/` e encerra a janela automaticamente. Essa verificação é separada dos testes JUnit e depende de ambiente gráfico.

### Resultado da preparação em 15/09/2026

Ambiente: Linux com OpenJDK 25.0.4; o build mantém compilação com `--release 17`. A configuração local do IntelliJ referencia um SDK chamado `25`; em outra máquina, selecione um JDK instalado nas configurações da IDE.

- `./gradlew test verifyDesktop --rerun-tasks`: compilação e verificações executadas novamente com sucesso; 17 testes JUnit, sem falhas, erros ou testes ignorados.
- A verificação desktop encerrou com a mensagem `VERIFICAÇÃO DESKTOP OK`, cobrindo controle manual, mira, flecha, ouro, retorno, vitória e redimensionamento.
- `./gradlew test installDist`: execução concluída com sucesso, incluindo a geração da distribuição. Nesse comando os testes estavam atualizados no cache; a execução forçada acima confirmou o resultado.
- A JVM e o LWJGL emitiram avisos sobre acesso nativo, APIs depreciadas e versão JNI durante a execução. Eles não impediram a conclusão das verificações neste ambiente; a compatibilidade com outras máquinas e sistemas não foi validada nesta preparação.

## 9. Escopo e limitações atuais

- Jogo desktop 2D com uma fase fixa; reiniciar não gera um mapa novo.
- A interface gráfica é manual. O modo autônomo está disponível somente pelo terminal.
- A estratégia autônoma usa regras, memória e estimativas de risco; pode perder a partida.
- Não há persistência de partidas, multiplayer ou áudio; o áudio está desativado no inicializador.
- Revelar o mapa é uma ajuda disponível ao jogador e não modifica as regras nem a pontuação.
- O build inclui dependências nativas, mas não inclui uma instalação de Java.

## 10. Conteúdo da entrega

A versão reúne código do jogo, configuração Gradle, recursos de fonte com licença, testes, captura da interface, README de uso e esta documentação técnica. Diretórios gerados como `build/` e `.gradle/` permanecem fora do versionamento.
