# Mundo de Wumpus — Interface Gráfica com libGDX

Aplicação gráfica interativa do **Mundo de Wumpus** desenvolvida em **Java** com a biblioteca **libGDX**. O mapa é desenhado em uma grade 5×5 e a simulação ocorre de forma automática a cada intervalo de tempo, exibindo a posição do agente, seus elementos visuais, percepções em tempo real e o histórico completo da partida.

---

## 1. Requisitos do Sistema

* **JDK (Java Development Kit)**: Versão 17 ou superior (testado com OpenJDK 21, 25 e 26).
* **Gradle Wrapper**: Acompanha o projeto (não é necessário instalar o Gradle separadamente).
* **Sistema Operacional**: Linux, Windows ou macOS.

---

## 2. Passo a Passo para Execução (Tutorial Direto)

### Passo 1: Clonar ou Baixar o Repositório
Abra o terminal e navegue até a pasta do projeto:
```bash
cd WumpusWorld
```

### Passo 2: Dar Permissão de Execução (Linux/macOS)
No Linux ou macOS, garanta que o script do Gradle Wrapper tenha permissão de execução:
```bash
chmod +x gradlew
```

### Passo 3: Executar a Aplicação

#### No Linux / macOS:
```bash
./gradlew run
```

#### No Windows (Prompt de Comando ou PowerShell):
```cmd
gradlew.bat run
```

> **Nota:** Na primeira execução, o Gradle baixará automaticamente as dependências da libGDX e do backend LWJGL 3.

---

## 3. Gerando e Executando um JAR Autônomo (Executable Jar)

Você pode compilar um arquivo `.jar` único que embute todas as dependências e bibliotecas nativas, dispensando o Gradle para rodar:

1. Gere o pacote completo:
   ```bash
   ./gradlew jarCompleto
   ```
2. Execute o arquivo gerado:
   ```bash
   java -jar build/libs/WumpusWorld-1.0-completo.jar
   ```

---

## 4. Executando em uma IDE (NetBeans / IntelliJ / Eclipse)

1. **Abrir o projeto**:
   * **Apache NetBeans**: *File → Open Project* e selecione a pasta do projeto.
   * **IntelliJ IDEA**: *File → Open* e selecione o arquivo `build.gradle.kts` (abrir como projeto Gradle).
   * **Eclipse**: *File → Import → Existing Gradle Project*.
2. **Classe Principal**: `wumpusworld.Main`
3. **Configuração de Execução no macOS**: Se for executar diretamente pela IDE no macOS, inclua o argumento de JVM `-XstartOnFirstThread`.

---

## 5. Funcionalidades da Interface Gráfica

* **Grade 5×5 e Névoa de Exploração**:
  * As casas não visitadas permanecem escuras.
  * À medida que o agente pisa nas casas, elas são reveladas.
  * Ao encerrar a partida (Vitória, Morte ou Limite), **o mapa completo é revelado**.
* **Representação Visual dos Elementos**:
  * **Agente**: Círculo azul.
  * **Wumpus**: Círculo vermelho.
  * **Poço**: Círculo preto.
  * **Ouro**: Bloco amarelo.
  * **Flecha (item)**: Marcador verde.
* **Painel de Informações (HUD)**:
  * Estado da Partida (Em andamento, Vitória, Fim de Jogo, Limite Atingido).
  * Pontuação acumulada e Quantidade de Movimentos.
  * Posse do Ouro e da Flecha.
  * Percepções atuais na casa do agente (**Brisa**, **Fedor** ou **Brilho**).
* **Histórico de Eventos Rolável**:
  * Registro detalhado de cada ação (movimento, percepções, disparos de flecha, coleta de itens).
  * Suporte a rolagem com o **scroll do mouse** para visualizar eventos anteriores.
* **Reinício Rápido**:
  * Botão interativo **"JOGAR NOVAMENTE"** exibido na tela ao final de cada partida para reiniciar a simulação instantaneamente.

---

## 6. Estrutura do Código-Fonte

```
src/main/java/wumpusworld/
├── Main.java                      # Ponto de entrada (Configura a janela LibGDX 1000x700 a 60 FPS)
│
├── nucleo/                        # Regras e modelo de domínio (Sem dependência gráfica)
│   ├── Mundo.java                 # Matriz 5x5, posições dos elementos e verificação de percepções
│   ├── AgenteInteligente.java     # Lógica de tomada de decisão, navegação e risco
│   ├── Partida.java               # Árbitro que executa os passos da simulação
│   ├── Posicao.java               # Coordenadas (linha, coluna)
│   ├── Direcao.java               # Enum das direções (CIMA, BAIXO, ESQUERDA, DIREITA)
│   ├── Percepcoes.java            # Registro de sinais (Brisa, Fedor, Brilho)
│   └── Situacao.java              # Estado do jogo (EM_ANDAMENTO, VITORIA, MORTE, LIMITE_ATINGIDO)
│
└── grafico/                       # Renderização e Interface de Usuário
    ├── TelaDaPartida.java         # Conduz o quadro: relógio da simulação, ordem de desenho e captura de clique/scroll
    ├── PainelTabuleiro.java       # Converte a matriz em coordenadas de tela e desenha a grade, os elementos e o agente
    ├── PainelInformacoes.java     # Painel de estado (HUD) e legenda dos símbolos
    ├── PainelHistorico.java       # Histórico rolável, mensagem de resultado e botão "JOGAR NOVAMENTE"
    ├── Paleta.java                # Todas as cores da janela, compartilhadas por tabuleiro e legenda
    └── Fontes.java                # Gera as fontes a partir de assets/fontes (DejaVuSans) via gdx-freetype
```

Cada painel expõe dois métodos, `desenharFormas` e `desenharTextos`, porque o `ShapeRenderer` e o `SpriteBatch` da libGDX não podem ficar abertos ao mesmo tempo: a `TelaDaPartida` abre um pincel de cada vez e percorre os três painéis em cada passagem. Nenhum painel guarda estado do jogo — todos recebem a `Partida` como argumento a cada quadro.

---

## 7. Mapeamento de Coordenadas (Matriz → Tela)

No projeto, o sistema de coordenadas do mundo (matriz $5 \times 5$, onde `[0][0]` é o canto superior esquerdo da matriz) é convertido para as coordenadas de renderização bidimensional da libGDX (onde o eixo Y cresce de baixo para cima) da seguinte forma:

```java
float x = margemX + coluna * tamanhoCelula;
float y = margemY + (tamanhoMatriz - 1 - linha) * tamanhoCelula;
```

* **Eixo X**: `x = 50 + coluna * 100` (deslocamento horizontal com base na coluna).
* **Eixo Y**: `y = 50 + (4 - linha) * 100` (inversão do eixo vertical para que a linha `0` fique posicionada no topo da grade na tela).
* **Parâmetros**: `tamanhoCelula = 500 / tamanhoMatriz` (100px na matriz 5×5), `margemX = 50px`, `margemY = 50px`. A grade ocupa sempre o mesmo quadrado de 500px; mudar `Mundo.TAMANHO` redimensiona as casas sem mexer no layout.

A conversão está concentrada nos métodos `x(coluna)` e `y(linha)` de `PainelTabuleiro`, usados por todo o desenho da grade. A conversão inversa aparece uma única vez, no tratamento do clique em `TelaDaPartida`: o mouse chega com o eixo Y invertido, e o ponto é convertido com `ALTURA_DA_JANELA - screenY` antes de ser testado contra o botão.

---

## 8. Documentação Complementar

Dois documentos detalham o funcionamento interno do projeto:

* **`FluxoGeral.md`** — o fluxo de execução de ponta a ponta: a ordem em que as classes são criadas, quem chama quem, o passo do agente em seis etapas e a fronteira entre as regras e a interface.
* **`FluxoGrafico.md`** — a camada gráfica em detalhe, partindo do mapa impresso em caracteres da versão de terminal e mostrando o que foi necessário para transformá-lo em um tabuleiro desenhado.

---

## 9. Verificação do checklist

- [x] Regras de risco, pontuação e retorno preservadas: a interface chama `Partida.executarPasso()` e consulta o núcleo, sem alterar suas decisões.
- [x] Movimento automático sem espera bloqueante: `render()` acumula `Gdx.graphics.getDeltaTime()` e executa um passo a cada 0,5 segundo. Esse mecanismo do ciclo da libGDX equivale à atualização periódica solicitada; não utiliza `Thread.sleep`.
- [x] Mensagens permanentes de morte, vitória (retorno à casa inicial com ouro) e limite de exploração, fora do histórico rolável.
- [x] Mapa completo revelado nos três resultados, com posições originais de poços, Wumpus e ouro. Essa cópia visual não restaura elementos no jogo.
- [x] Interface Java com libGDX e instruções de execução nas seções 2 a 4.
- [x] Conversão de linha e coluna para coordenadas da tela explicada na seção 7.

O limite é de 180 movimentos de exploração sem ouro. Após coletar o ouro, o agente retorna pelo caminho conhecido, conforme as regras do núcleo. O botão de reinício fica à direita, sem cobrir o mapa revelado.
