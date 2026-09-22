# Mundo de Wumpus — como o projeto funciona

Este documento percorre o projeto de ponta a ponta: quais classes existem, em que ordem são criadas, quem chama quem, e como uma matriz de caracteres `5×5` acaba virando uma janela com um tabuleiro desenhado.

> **Convenção:** nomes marcados **(libGDX)** vêm da biblioteca gráfica, dentro dos JARs que o Gradle baixa — você não encontra esses arquivos no repositório. Todos os outros nomes são arquivos `.java` do projeto.

---

## O retrato geral

O projeto tem catorze classes, divididas em dois pacotes com papéis opostos.

**`nucleo` — sabe jogar, não sabe desenhar.** Aqui está o jogo inteiro: o mapa, o agente, as regras de pontuação, as condições de vitória e derrota. Nenhum arquivo deste pacote importa a biblioteca gráfica. Se a janela desaparecesse, este pacote continuaria funcionando.

**`grafico` — sabe desenhar, não sabe jogar.** Uma tela, três painéis e duas classes de apoio — as cores e as fontes. Nenhuma delas decide nada sobre o jogo: leem o estado e o traduzem em retângulos, círculos e texto.

A ligação entre os dois é estreita de propósito. A tela empurra **um verbo só** para dentro das regras — `executarPasso()` — e recebe de volta apenas consultas: onde está o agente, qual a pontuação, o que há nesta casa, a partida acabou. É mão única: nada do que é desenhado volta para dentro das regras.

E aqui já está a resposta para a pergunta central. **A matriz continua sendo uma matriz.** O `Mundo` guarda um `char[][]` com `'P'` de poço, `'W'` de Wumpus, `'O'` de ouro. O que a camada gráfica acrescenta não é uma transformação do jogo — é um leitor: alguém que percorre essa matriz e, em vez de imprimir os caracteres em linhas de terminal, pinta um círculo preto onde encontra um `'P'`. A seção **Da matriz para a tela** mostra essa tradução em detalhe.

## O papel da biblioteca gráfica

A libGDX resolve o que o Java sozinho não faz: abrir uma janela do sistema operacional, obter acesso à placa de vídeo, desenhar formas e texto nela, e ler teclado e mouse. São três JARs declarados no `build.gradle.kts`.

O contrato de uso é este: **desenhar exige uma janela e um laço rodando a sessenta quadros por segundo, e quem é dono dos dois é a biblioteca.** Então o projeto não a chama num laço próprio. Ele entrega um objeto a ela e diz, na prática: "abra a janela, e chame este objeto a cada quadro". A partir daí quem manda no ritmo é ela. Esse arranjo tem nome — inversão de controle — e é tudo que você precisa saber sobre a biblioteca para entender o projeto.

## 1. A entrega do controle

`Main` faz três coisas e acaba. Cria um objeto de configuração da classe `Lwjgl3ApplicationConfiguration` **(libGDX)**, define título e janela de 1000×700 a 60 FPS, e executa a última linha do `main`:

```java
new Lwjgl3Application(new TelaDaPartida(), configuracao);
```

Essa linha merece atenção, porque é onde o controle sai do projeto.

Ela **cria dois objetos**, e nenhum recebe nome de variável — não existe no código nenhuma variável chamada `telaDaPartida`, e o objeto criado não é "do tipo `Lwjgl3Application`". São dois objetos independentes, de duas classes diferentes. Java avalia o argumento antes de invocar o construtor externo, então a ordem real é:

**Primeiro**, `new TelaDaPartida()` cria o objeto da tela. Roda o construtor padrão implícito — a classe não declara construtor. Nesse instante ela tem os três painéis, criados na declaração dos campos, mas nada mais: sem pincéis, sem fonte, sem partida. É uma casca.

**Segundo**, essa casca é passada ao construtor de `Lwjgl3Application` **(libGDX)**, cujo parâmetro **não é declarado como `TelaDaPartida`**, e sim como `ApplicationListener` **(libGDX)** — uma interface, isto é, só uma lista de seis assinaturas: `create`, `resize`, `render`, `pause`, `resume`, `dispose`. A biblioteca enxerga o objeto apenas através dessa lista. Ela não sabe que a classe se chama `TelaDaPartida`, nem que existe um `Mundo`.

A tela cumpre esse contrato porque declara `extends ApplicationAdapter` **(libGDX)** — uma classe abstrata que implementa as seis assinaturas com corpos vazios. Herdando dela, a tela ganha os seis métodos prontos e sobrescreve só os três que lhe interessam: `create`, `render` e `dispose`.

**Terceiro**, o construtor de `Lwjgl3Application` executa — e **não retorna enquanto a janela existir**. O programa inteiro vive dentro dele. Ele inicializa o sistema de janelas, abre a janela nativa, cria o contexto gráfico, e então **chama `create()`** no objeto que recebeu. Não é magia do Java: é uma chamada de método comum, escrita no código-fonte da biblioteca, que por acaso mora dentro de um construtor. Por isso você não encontra nenhuma chamada a `create()` escrita no projeto — só a definição dela. Feito isso, o construtor entra no laço infinito e passa a chamar `render()` cerca de sessenta vezes por segundo.

## 2. O que o `create()` monta

É aqui, e não no construtor, que a tela se equipa — os pincéis precisam do contexto gráfico, que só passou a existir um passo antes. São quatro providências:

1. **Os dois pincéis e as fontes.** `ShapeRenderer` desenha formas geométricas, `SpriteBatch` desenha texto, e três `BitmapFont` **(libGDX)** fornecem as letras. Quem as cria é a classe `Fontes`, que rasteriza o DejaVuSans de `assets/fontes` já no tamanho de uso de cada uma. São a totalidade das ferramentas de desenho do projeto.
2. **A partida.** `new Partida()` monta o jogo: dentro dela nascem um `Mundo` e um `AgenteInteligente`.
3. **Uma cópia congelada do mapa.** Ela existe por um motivo concreto: durante o jogo, o ouro e a flecha são *removidos* da matriz quando o agente os recolhe. Se a revelação final lesse o mapa vivo, mostraria casas vazias onde estavam os tesouros. A cópia preserva o mapa como ele era no primeiro quadro, e serve só para essa exibição.
4. **O tratamento de mouse.** Um objeto de subclasse anônima de `InputAdapter` **(libGDX)**, escrito dentro do próprio arquivo da tela. Ele trata o clique no botão de reinício e a rolagem do histórico, repassando os dois ao painel que cuida daquela região. O laço da biblioteca o chama antes do render do quadro, quando houver evento.

## 3. Quem é quem

| Classe | O que é | Responsabilidade |
|---|---|---|
| `Main` | classe | Configura a janela e entrega a tela à biblioteca |
| `TelaDaPartida` | classe | Conduz o quadro: relógio, ordem de desenho, delegação do mouse |
| `PainelTabuleiro` | classe | Converte a matriz em coordenadas e desenha a grade |
| `PainelInformacoes` | classe | Escreve o estado atual e explica os símbolos |
| `PainelHistorico` | classe | Registro rolante, desfecho e botão de reinício |
| `Paleta` | classe | Todas as cores da janela; tabuleiro e legenda leem as mesmas |
| `Fontes` | classe | Gera as três fontes a partir dos arquivos TTF |
| `Partida` | classe | Árbitro: conduz o passo e julga o fim |
| `Mundo` | classe | Dona da verdade: a matriz do mapa e o que se sente nela |
| `AgenteInteligente` | classe | Decide para onde ir, com memória própria |
| `Posicao` | record | Uma coordenada linha/coluna |
| `Percepcoes` | record | Brisa, fedor, brilho — o que se sente numa casa |
| `Direcao` | enum | Cima, baixo, esquerda, direita, com seus deslocamentos |
| `Situacao` | enum | Em andamento, vitória, morte, limite atingido |

As quatro últimas não executam trabalho: são o vocabulário tipado com que as outras conversam.

## 4. O quadro: o que acontece sessenta vezes por segundo

O laço chama `render()` na tela, e ela faz duas coisas bem separadas.

**Mede o tempo e talvez avance o jogo.** A tela pergunta à biblioteca quanto tempo real passou desde o quadro anterior e soma isso a um contador próprio. Quando o contador passa de meio segundo, ela pede **um** passo à `Partida` e zera. São dois ritmos independentes: a janela se redesenha sessenta vezes por segundo, e o jogo avança duas vezes por segundo. É o que torna a partida acompanhável a olho nu sem travar a janela.

**Pinta o quadro inteiro, do zero.** Não há memória de desenho entre quadros: a tela limpa tudo e repinta. Como os dois pincéis não podem estar abertos ao mesmo tempo, o quadro sai em passagens — primeiro todas as formas, depois todos os textos. Em cada passagem a tela percorre os três painéis na mesma ordem, e cada um desenha a sua região:

- **`PainelTabuleiro`** — a grade, os elementos conhecidos, o agente e a numeração dos eixos.
- **`PainelInformacoes`** — acima da grade, o estado atual em texto; ao lado, a legenda que explica cada forma.
- **`PainelHistorico`** — a coluna da direita: o registro dos acontecimentos, o desfecho e o botão.

A tela não sabe *o que* cada painel desenha; sabe apenas em que ordem chamá-los e quando abrir e fechar cada pincel. Essa é toda a responsabilidade dela.

## 5. O passo do agente: seis etapas

Quando a tela chama `executarPasso()`, o controle entra na `Partida` e não toca em nada gráfico até voltar. A sequência é sempre esta:

1. **Perceber.** A `Partida` pergunta ao `Mundo` o que se sente na casa onde o agente está. O `Mundo` devolve um `Percepcoes` — brisa, fedor, brilho — e **nada além disso**.
2. **Deduzir.** A `Partida` entrega essa percepção ao agente, que aumenta a suspeita de perigo sobre as casas vizinhas que ainda não pisou. É dedução, não leitura: ele nunca vê o conteúdo de casa nenhuma.
3. **Reagir.** Se havia fedor e o agente possui flecha, ele aponta para a vizinha desconhecida mais suspeita e atira. A `Partida` pede ao `Mundo` que resolva o disparo e registra se houve grito.
4. **Mover.** O agente pontua cada vizinho — casa nunca pisada vale muito, suspeita desconta bastante, repetição desconta um pouco — e caminha para o melhor. Se já tem o ouro, ignora tudo isso e refaz de trás para frente o caminho que memorizou até a casa inicial.
5. **Resolver a casa.** A `Partida` pergunta ao `Mundo` o que há onde o agente parou e aplica a consequência: poço e Wumpus matam, ouro e flecha são recolhidos e apagados do mapa.
6. **Julgar.** Morte encerra a partida. Ouro na casa inicial é vitória, com bônus. Exploração longa demais sem achar o ouro encerra por limite. Cada acontecimento das seis etapas vira uma linha de texto no registro.

**O porquê desse desenho.** O `Mundo` é a *verdade*: sabe exatamente onde está cada coisa, mas é passivo — só responde perguntas, e a única pergunta que o agente pode fazer é "o que se sente aqui?". O agente é a *crença*: três matrizes de memória sobre visitas, suspeitas e percepções já registradas, mais a lista do caminho percorrido. Os dois nunca conversam diretamente; a `Partida` é quem os aproxima, e é ela quem decide o que cada um fica sabendo. É essa separação que faz o agente ser genuinamente cego, e não um programa que trapaceia lendo o gabarito.

## 6. Da matriz para a tela

Aqui acontece a tradução que dá nome ao projeto gráfico, e ela cabe inteira no `PainelTabuleiro`.

**A conversão de coordenadas.** A matriz é indexada por linha crescendo para baixo; a tela tem o eixo Y crescendo para cima. O painel resolve isso em dois métodos privados, `x(coluna)` e `y(linha)`, usados por todo o resto da classe:

```
x = margem + coluna × lado
y = margem + (TAMANHO - 1 - linha) × lado
```

O `lado` não é fixo: vale `500 / TAMANHO`, então a grade ocupa sempre o mesmo quadrado na tela, com casas maiores ou menores conforme o tamanho da matriz. A inversão `TAMANHO - 1 - linha` é o que faz a linha 0 aparecer no alto da janela. O caminho de volta também existe, do outro lado da tela: o mouse chega com o Y invertido, então o tratamento de clique faz `ALTURA_DA_JANELA - screenY` antes de perguntar ao `PainelHistorico` se o ponto caiu dentro do botão. Mesma conversão, sentido oposto.

**A decisão do que mostrar.** Para cada casa, o painel pergunta se ela já foi visitada, ou se a partida terminou. Se nenhum dos dois, pinta cinza-escuro e para aí — é assim que a caverna permanece desconhecida enquanto o agente não a explora. Se a casa é conhecida, ele escolhe de qual matriz ler: com a partida encerrada usa a cópia congelada, para o mapa completo aparecer íntegro; durante o jogo usa a matriz viva.

**A tradução de caractere em forma.** Lido o caractere, o desenho é direto: `'P'` vira círculo preto, `'W'` vira círculo vermelho, `'O'` vira quadrado amarelo, `'F'` vira um retângulo verde estreito. O agente é um círculo azul desenhado por cima de tudo, na sua casa atual — por isso ele cobre o poço em que caiu, em vez de sumir atrás dele. A legenda do `PainelInformacoes` repete exatamente essas mesmas formas em escala menor, para que o significado seja lido sem adivinhação.

**Os números viram texto.** O relatório que no terminal sairia em linhas está distribuído em duas regiões: o `PainelInformacoes` escreve situação, posição, movimentos, pontos, inventário, percepções e perigo estimado; o `PainelHistorico` empilha o registro, com as entradas mais recentes embaixo. São os mesmos valores que o núcleo produz — a diferença é que agora alguém os posiciona em coordenadas em vez de imprimi-los em sequência.

## 7. O fim e o recomeço

Quando a `Situacao` passa a um estado final, três coisas mudam no desenho: o mapa inteiro é revelado, a descrição do desfecho aparece, e um botão verde surge no canto inferior. O relógio para de pedir passos, e o histórico encolhe por cima para abrir lugar ao desfecho e ao botão — por isso as duas coisas moram no mesmo painel: dividem o mesmo espaço e a mesma conta.

O clique no botão faz uma coisa só: **descarta a `Partida` atual e cria outra**, junto com uma nova cópia congelada, zerando o contador de tempo e a rolagem.

Vale reparar no que esse gesto prova. Nenhum painel precisa "se resetar", porque nenhum deles guarda algo sobre o jogo — nem posição, nem pontuação, nem mapa. Eles recebem a partida como argumento a cada quadro e a devolvem. O único estado que um painel guarda é o deslocamento da rolagem, e isso é estado da leitura, não da partida. Jogar fora a partida e pedir outra basta, e é a demonstração prática de que a separação entre as duas camadas é real, e não apenas uma organização de pastas.

Ao fechar a janela, o laço chama `dispose()` na tela, que libera as fontes e os dois pincéis. Só então o construtor da última linha do `main` — aquele que nunca tinha retornado — finalmente retorna, e o programa termina.

---

**Em uma frase:** o `Main` abre a janela entregando a `TelaDaPartida` à biblioteca gráfica, que passa a chamá-la sessenta vezes por segundo; a cada meio segundo a tela pede um passo à `Partida`, que conduz as regras tendo o `Mundo` como verdade e o `AgenteInteligente` como crença; nos demais quadros a tela apenas manda os três painéis lerem esse estado e traduzirem a matriz em formas. A tela conhece a partida; a partida não sabe que existe uma tela.
