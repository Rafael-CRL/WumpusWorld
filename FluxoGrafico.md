# A camada gráfica — do terminal à janela

Este documento trata só da apresentação. Ele parte do mapa impresso em caracteres, mostra o que foi feito para ele virar um tabuleiro desenhado, e percorre as classes que hoje cuidam disso. As regras do jogo não aparecem aqui — elas estão descritas em `FluxoGeral.md`.

> **Convenção:** nomes marcados **(libGDX)** vêm da biblioteca gráfica, dentro dos JARs que o Gradle baixa. Todos os outros nomes são arquivos `.java` do projeto.

---

## 1. O ponto de partida: o mapa impresso

Na versão de terminal, o mapa aparecia por um método do próprio `Mundo`. Reduzido ao essencial:

```java
for (int linha = 0; linha < TAMANHO; linha++) {
    for (int coluna = 0; coluna < TAMANHO; coluna++) {
        if (linha == agente.getLinha() && coluna == agente.getColuna()) {
            System.out.print(agente.estaVivo() ? "[A] " : "[X] ");
        } else if (revelarTudo) {
            System.out.print("[" + mapa[linha][coluna] + "] ");
        } else if (visitado[linha][coluna]) {
            System.out.print("[+] ");
        } else {
            System.out.print("[?] ");
        }
    }
    System.out.println();
}
```

Vale registrar três coisas que o terminal dava de graça, porque são exatamente as três que a janela cobraria depois:

- **Posição automática.** Ninguém calcula onde cada casa vai parar. O cursor anda sozinho a cada `print`, e o `println` no fim da linha interna quebra a fileira.
- **O desenho fica.** Cada rodada imprime um bloco novo abaixo do anterior. O histórico da partida é a rolagem do terminal — ninguém precisou construí-lo.
- **O tempo espera sozinho.** Entre uma rodada e outra, um `Thread.sleep(300)` no laço principal segura a partida. Nada mais depende daquela linha de execução, então dormir não custa nada.

## 2. A mesma travessia, agora em pixels

O laço duplo sobre a matriz continua sendo o mesmo laço. O que mudou foi o corpo dele:

```java
for (int l = 0; l < Mundo.TAMANHO; l++) {
    for (int c = 0; c < Mundo.TAMANHO; c++) {
        boolean conhecida = partida.getMundo().foiVisitada(l, c)
                || partida.mapaDeveSerRevelado();

        formas.setColor(conhecida ? claro : escuro);
        formas.rect(x(c) + 2, y(l) + 2, LADO - 4, LADO - 4);

        if (conhecida) {
            char elem = ...;
            if (elem == Mundo.POCO) { formas.setColor(BLACK); formas.circle(...); }
            else if (elem == Mundo.WUMPUS) { ... }
        }
    }
}
formas.circle(x(pos.coluna()) + LADO / 2, y(pos.linha()) + LADO / 2, 20);
```

A correspondência é quase termo a termo:

| Terminal | Janela |
|---|---|
| `[?]` — casa desconhecida | retângulo cinza-escuro |
| `[+]` — casa visitada e vazia | retângulo cinza-claro |
| `[P]`, `[W]`, `[O]`, `[F]` | círculo preto, círculo vermelho, quadrado amarelo, retângulo verde |
| `[A]` — agente, dentro do laço | círculo azul, **depois** do laço |

Essa última linha é o achado da travessia, e é um ganho, não uma dívida. **Texto não tem camadas:** no terminal, uma casa que imprime `[A]` não pode imprimir `[P]` também, então o agente precisava ser um `if` no meio da varredura, substituindo o conteúdo da casa. Pixels têm camadas. O agente passou a ser desenhado depois da grade inteira, e por isso **ele cobre o poço em que caiu** em vez de apagá-lo do quadro — a informação de que ali havia um poço continua ali, embaixo dele.

## 3. As três coisas que a janela obrigou a inventar

São exatamente as três facilidades listadas na seção 1, agora cobradas de volta.

**Coordenadas, porque não existe cursor.** Cada forma precisa saber onde começa. Some-se a isso que a matriz é indexada por linha crescendo para baixo, e a tela tem o eixo Y crescendo para cima. O `PainelTabuleiro` resolve os dois problemas em dois métodos privados:

```
x = margem + coluna × lado
y = margem + (TAMANHO - 1 - linha) × lado
```

O `lado` vale `500 / TAMANHO`: a grade ocupa sempre o mesmo quadrado, e as formas dentro de cada casa são frações do lado. A inversão `TAMANHO - 1 - linha` é o que faz a linha 0 aparecer no alto da janela, como aparecia no topo da impressão.

**Repintura, porque não existe rolagem.** A janela não acumula: cada quadro apaga tudo e redesenha do zero, sessenta vezes por segundo. Isso tem uma consequência que não é óbvia — **o histórico teve de ser construído à mão**. O que no terminal era um efeito colateral gratuito (o texto antigo ficava acima) virou uma lista de mensagens guardada pela `Partida` e um painel que a desenha de baixo para cima, com rolagem própria.

**Um relógio, porque dormir passou a custar caro.** A linha de execução que roda o jogo é a mesma que repinta a janela e atende o mouse. Um `Thread.sleep` ali congelaria as três coisas: a janela pararia de responder e o sistema a daria por travada. Em vez de dormir, a tela conta. A cada quadro ela soma o tempo decorrido a um contador e, quando ele passa de meio segundo, pede um passo. O laço nunca para; só o jogo espera.

## 4. Quem faz o quê

Sete classes cuidam da apresentação.

| Classe | Responsabilidade |
|---|---|
| `Main` | Configura a janela e entrega a tela à biblioteca |
| `TelaDaPartida` | Conduz o quadro: relógio, ordem de desenho, delegação do mouse |
| `PainelTabuleiro` | Converte a matriz em coordenadas e desenha a grade |
| `PainelInformacoes` | Escreve o estado atual e explica os símbolos |
| `PainelHistorico` | Registro rolante, desfecho e botão de reinício |
| `Paleta` | Todas as cores da janela; tabuleiro e legenda leem as mesmas |
| `Fontes` | Gera as três fontes a partir dos arquivos TTF |

Vale notar de onde cada painel veio. No terminal, quem imprimia o mapa era o próprio `Mundo`, e quem imprimia pontuação e percepções era o `main` — ou seja, a regra e a apresentação moravam juntas. Hoje o `Mundo` não imprime nada: ele só responde perguntas, e quem pergunta é o painel. É a mesma informação, com a decisão de *como mostrá-la* movida para fora das regras.

## 5. Como a biblioteca entra

A libGDX resolve o que o Java sozinho não faz: abrir uma janela do sistema operacional, obter acesso à placa de vídeo, desenhar formas e texto, e ler teclado e mouse.

O contrato de uso é este: desenhar exige uma janela e um laço a sessenta quadros por segundo, e quem é dono dos dois é a biblioteca. Então o projeto não a chama num laço próprio — ele entrega a `TelaDaPartida` a ela, na última linha do `main`, e diz na prática: "abra a janela, e chame este objeto a cada quadro". A partir daí a tela é chamada de fora, não executada de dentro.

*A mecânica dessa entrega — por que o construtor da biblioteca nunca retorna, e como ela consegue chamar uma classe que não conhece — está dissecada na seção 1 do `FluxoGeral.md`. O que interessa aqui é o que a tela monta quando é chamada pela primeira vez.*

Essa primeira chamada é o `create()`, e é nele que a tela se equipa, e não no construtor: os pincéis precisam do contexto gráfico, que só existe depois que a janela abre. São quatro providências:

1. **Os pincéis e as fontes.** `ShapeRenderer` desenha formas, `SpriteBatch` desenha texto, três `BitmapFont` **(libGDX)** fornecem as letras — geradas pela classe `Fontes` a partir do DejaVuSans, já no tamanho de uso, em vez de ampliar uma fonte pronta e borrá-la. São a totalidade das ferramentas de desenho do projeto — não há arquivo de imagem nenhum.
2. **A partida**, que é o jogo em si.
3. **Uma cópia congelada do mapa.** Durante o jogo, o ouro e a flecha são apagados da matriz ao serem recolhidos. Se a revelação final lesse o mapa vivo, mostraria casas vazias onde estavam os tesouros. Esta cópia guarda o mapa como ele era no primeiro quadro, e serve só para essa exibição — é o equivalente ao `revelarTudo` da impressão do terminal, com uma diferença que o obrigou a existir: lá a impressão era imediata e o mapa ainda estava intacto; aqui a revelação acontece no fim, quando a matriz já mudou.
4. **O tratamento de mouse**, descrito na seção 8.

## 6. O quadro por dentro

A cada chamada de `render()`, a `TelaDaPartida` faz duas coisas.

Primeiro **mede o tempo**, e eventualmente pede um passo à partida, como descrito na seção 3.

Depois **pinta o quadro inteiro**. Os dois pincéis não podem estar abertos ao mesmo tempo, então o quadro sai em passagens: abre o `ShapeRenderer`, deixa os três painéis desenharem suas formas, fecha; abre o `SpriteBatch`, deixa os três desenharem seus textos, fecha. É por isso que cada painel expõe exatamente dois métodos, `desenharFormas` e `desenharTextos` — a divisão não é estética, é exigência da ferramenta.

A ordem das chamadas dentro de cada passagem importa, porque o que vem depois fica por cima. É toda a responsabilidade da tela: ela não sabe *o que* cada painel desenha, só em que ordem chamá-los e quando abrir e fechar cada pincel.

## 7. As três regiões

**`PainelTabuleiro`** é o tradutor da matriz, descrito nas seções 2 e 3. Além da grade, ele desenha a numeração das colunas embaixo e das linhas à esquerda — os mesmos rótulos que a impressão do terminal punha na primeira linha e na margem.

**`PainelInformacoes`** tem duas metades. Acima da grade fica o bloco de estado — situação, posição, movimentos, pontos, ouro, flecha, percepções e perigo estimado —, que é a versão posicionada daquelas linhas de `System.out.println` que vinham depois do mapa. Ao lado fica a legenda, e essa não tem equivalente no terminal: `[P]` carregava a própria pista na letra, mas um círculo preto não diz nada sozinho. Trocar caracteres por formas custou essa caixa explicativa, e ela desenha os símbolos com as mesmas chamadas da grade, para que a legenda nunca discorde do tabuleiro.

**`PainelHistorico`** é a rolagem reconstruída. Ele lê a lista de mensagens da partida, monta as entradas de baixo para cima — as mais recentes embaixo, como no terminal — e para quando o espaço acaba. Guarda o deslocamento da rolagem, que é o único estado que um painel mantém: estado da leitura, não da partida. O botão de reinício mora aqui porque divide espaço com a lista: quando a partida termina, o histórico encolhe por cima para abrir lugar ao desfecho e ao botão.

## 8. O mouse: a conversão de volta

O terminal não tinha entrada nenhuma: o laço rodava até o fim e pronto. A janela abriu essa possibilidade, e com ela a conversão inversa de coordenadas.

A biblioteca entrega o clique com o Y crescendo para baixo — a mesma convenção da matriz, e o oposto da usada no desenho. Então a tela converte antes de perguntar qualquer coisa:

```java
painelHistorico.cliqueNoBotao(screenX, ALTURA_DA_JANELA - screenY)
```

É a mesma inversão da seção 3, no sentido contrário. O painel recebe o ponto já no eixo do desenho e só precisa testar se ele caiu dentro do retângulo do botão — não precisa saber que existe um mouse.

O clique faz uma coisa só: descarta a partida atual e cria outra, com nova cópia congelada, zerando o contador de tempo e a rolagem. Nenhum painel precisa se reiniciar, porque nenhum deles guarda algo sobre o jogo. Eles recebem a partida como argumento a cada quadro e a devolvem.

## 9. O fecho

Ao fechar a janela, o laço chama `dispose()` na tela, que libera as fontes e os dois pincéis. Só então o construtor da última linha do `main` — aquele que nunca tinha retornado — finalmente retorna, e o programa termina.

---

**Em uma frase:** o laço duplo sobre a matriz é o mesmo do terminal; trocou-se `System.out.print(mapa[l][c])` por `formas.circle(...)`, e com isso vieram três obrigações que o terminal cumpria sozinho — calcular coordenadas, repintar a cada quadro e contar o tempo em vez de dormir. As classes de `grafico` existem para dar conta dessas três obrigações, e nenhuma delas decide nada sobre o jogo.
