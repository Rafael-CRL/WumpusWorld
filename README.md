# Mundo de Wumpus — Interface Gráfica com libGDX

Trabalho Final de **Computação Gráfica**  
Universidade Federal do Pará · Campus Cametá · Faculdade de Sistemas de Informação  
**Professor:** Keventon Guimarães  

Aplicação gráfica interativa do **Mundo de Wumpus** desenvolvida em **Java** com a biblioteca **libGDX**. O mapa é desenhado em uma grade 5×5 e a simulação ocorre de forma automática a cada intervalo de tempo, exibindo a posição do agente, seus elementos visuais, percepções em tempo real e o histórico completo da partida.

---

## 1. Requisitos do Sistema

* **JDK (Java Development Kit)**: Versão 17 ou superior (testado com OpenJDK 17 e 25).
* **Gradle Wrapper**: Acompanha o projeto (não é necessário instalar o Gradle separadamente).
* **Sistema Operacional**: Linux, Windows ou macOS.

---

## 2. Passo a Passo para Execução (Tutorial Direto)

### Passo 1: Clonar ou Baixar o Repositório
Abra o terminal e navegue até a pasta do projeto:
```bash
cd WumpusWorld-main
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
    └── TelaDaPartida.java         # Renderizador LibGDX, gerenciamento de eventos e captura de clique/scroll
```

---

## 7. Regras e Tabela de Pontuação

| Evento | Pontuação |
|---|---|
| Cada Movimento realizado | -1 ponto |
| Coletar o Ouro | +100 pontos |
| Retornar à casa inicial `[0][0]` com o Ouro | +200 pontos |
| Acertar e matar o Wumpus | +50 pontos |
| Disparar uma Flecha | -10 pontos |
| Morrer (cair em poço ou ser devorado pelo Wumpus) | -100 pontos |

*A partida termina quando o agente morre, retorna à casa inicial `[0][0]` com o ouro, ou atinge o limite de 180 movimentos sem o ouro.*
