package wumpusworld.nucleo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * O mundo de Wumpus: a matriz do mapa e as regras que dependem dela.
 *
 * <p>Esta classe é a <strong>única dona da verdade</strong> sobre onde estão os
 * poços, o Wumpus e o ouro. Ela não imprime nada e não conhece a interface;
 * apenas responde perguntas. O agente só enxerga o mapa através de
 * {@link #percepcoesEm(Posicao)}, e a camada gráfica só revela uma casa depois
 * que ela é visitada — ou no fim da partida, quando o mapa completo é exibido.</p>
 */
public class Mundo {

    /** Quantidade de linhas e de colunas do mapa quadrado. */
    public static final int TAMANHO = 5;

    public static final char VAZIO = '.';
    public static final char POCO = 'P';
    public static final char WUMPUS = 'W';
    public static final char OURO = 'O';
    public static final char FLECHA = 'F';

    private final char[][] mapa;
    private final boolean[][] visitado;

    /** Cópia do mapa original, usada para redesenhar a fase quando reiniciada. */
    private final char[][] mapaOriginal;

    /** Cria a fase fixa utilizada nas aulas 1 a 7. */
    public Mundo() {
        mapa = new char[TAMANHO][TAMANHO];
        visitado = new boolean[TAMANHO][TAMANHO];
        criarMapa();
        mapaOriginal = copiarMatriz(mapa);
        visitado[0][0] = true;
    }

    /**
     * Cria um mundo a partir de um mapa já montado.
     *
     * <p>Visível apenas dentro do pacote: é usado por {@link #sortear(Random)},
     * por {@link #reiniciar()} e pelos testes automatizados, que precisam
     * montar cenários controlados.</p>
     */
    Mundo(char[][] mapaSorteado) {
        mapa = mapaSorteado;
        visitado = new boolean[TAMANHO][TAMANHO];
        mapaOriginal = copiarMatriz(mapa);
        visitado[0][0] = true;
    }

    /** Preenche a matriz e posiciona os elementos da fase original. */
    private void criarMapa() {
        for (int linha = 0; linha < TAMANHO; linha++) {
            for (int coluna = 0; coluna < TAMANHO; coluna++) {
                mapa[linha][coluna] = VAZIO;
            }
        }

        mapa[1][2] = POCO;
        mapa[3][1] = POCO;
        mapa[2][3] = WUMPUS;
        mapa[4][4] = OURO;
        mapa[0][3] = FLECHA;
    }

    // -----------------------------------------------------------------------
    //  Consultas básicas
    // -----------------------------------------------------------------------

    public boolean estaDentroDoMapa(int linha, int coluna) {
        return linha >= 0 && linha < TAMANHO
                && coluna >= 0 && coluna < TAMANHO;
    }

    public boolean estaDentroDoMapa(Posicao posicao) {
        return estaDentroDoMapa(posicao.linha(), posicao.coluna());
    }

    public char getElemento(int linha, int coluna) {
        return mapa[linha][coluna];
    }

    public char getElemento(Posicao posicao) {
        return mapa[posicao.linha()][posicao.coluna()];
    }

    public void removerElemento(int linha, int coluna) {
        mapa[linha][coluna] = VAZIO;
    }

    public void removerElemento(Posicao posicao) {
        removerElemento(posicao.linha(), posicao.coluna());
    }

    /** Registra uma posição na memória visual do mapa. */
    public void marcarVisitada(int linha, int coluna) {
        visitado[linha][coluna] = true;
    }

    public void marcarVisitada(Posicao posicao) {
        marcarVisitada(posicao.linha(), posicao.coluna());
    }

    /** Informa se a casa já foi pisada pelo agente. */
    public boolean foiVisitada(int linha, int coluna) {
        return visitado[linha][coluna];
    }

    // -----------------------------------------------------------------------
    //  Percepções
    // -----------------------------------------------------------------------

    /** Procura um elemento nas quatro posições vizinhas. */
    private boolean existeVizinho(int linha, int coluna, char procurado) {
        for (Direcao direcao : Direcao.TODAS) {
            int linhaVizinha = linha + direcao.getDeltaLinha();
            int colunaVizinha = coluna + direcao.getDeltaColuna();

            if (estaDentroDoMapa(linhaVizinha, colunaVizinha)
                    && mapa[linhaVizinha][colunaVizinha] == procurado) {
                return true;
            }
        }
        return false;
    }

    public boolean temBrisa(int linha, int coluna) {
        return existeVizinho(linha, coluna, POCO);
    }

    public boolean temFedor(int linha, int coluna) {
        return existeVizinho(linha, coluna, WUMPUS);
    }

    public boolean temBrilho(int linha, int coluna) {
        return mapa[linha][coluna] == OURO;
    }

    /**
     * Devolve, de uma só vez, tudo o que pode ser sentido na casa informada.
     * É a única porta de entrada legítima de informação para o agente.
     */
    public Percepcoes percepcoesEm(int linha, int coluna) {
        return new Percepcoes(
                temBrisa(linha, coluna),
                temFedor(linha, coluna),
                temBrilho(linha, coluna));
    }

    public Percepcoes percepcoesEm(Posicao posicao) {
        return percepcoesEm(posicao.linha(), posicao.coluna());
    }

    // -----------------------------------------------------------------------
    //  Flecha
    // -----------------------------------------------------------------------

    /**
     * Faz a flecha percorrer uma linha reta até sair do mapa.
     *
     * @return {@code true} quando o Wumpus estava no caminho.
     */
    public boolean atirarFlecha(int linha, int coluna, Direcao direcao) {
        Posicao flecha = new Posicao(linha, coluna).vizinha(direcao);

        while (estaDentroDoMapa(flecha)) {
            if (getElemento(flecha) == WUMPUS) {
                removerElemento(flecha);
                return true;
            }
            flecha = flecha.vizinha(direcao);
        }

        return false;
    }

    /**
     * Calcula até onde a flecha viaja, sem alterar o mapa.
     * Serve exclusivamente para animar o disparo na tela.
     */
    public Posicao calcularAlcanceDaFlecha(Posicao origem, Direcao direcao) {
        Posicao atual = origem;
        Posicao proxima = origem.vizinha(direcao);

        while (estaDentroDoMapa(proxima)) {
            atual = proxima;
            if (getElemento(atual) == WUMPUS) {
                return atual;
            }
            proxima = proxima.vizinha(direcao);
        }
        return atual;
    }

    // -----------------------------------------------------------------------
    //  Apoio à camada de apresentação
    // -----------------------------------------------------------------------

    /** Lista das casas ainda não visitadas, útil para métricas de exploração. */
    public int quantidadeDeCasasVisitadas() {
        int total = 0;
        for (int linha = 0; linha < TAMANHO; linha++) {
            for (int coluna = 0; coluna < TAMANHO; coluna++) {
                if (visitado[linha][coluna]) {
                    total++;
                }
            }
        }
        return total;
    }

    /** Recria a mesma fase do zero, mantendo o desenho original do mapa. */
    public Mundo reiniciar() {
        return new Mundo(copiarMatriz(mapaOriginal));
    }

    private static char[][] copiarMatriz(char[][] origem) {
        char[][] destino = new char[TAMANHO][TAMANHO];
        for (int linha = 0; linha < TAMANHO; linha++) {
            System.arraycopy(origem[linha], 0, destino[linha], 0, TAMANHO);
        }
        return destino;
    }

    // -----------------------------------------------------------------------
    //  Geração de fases sorteadas (recurso extra da versão gráfica)
    // -----------------------------------------------------------------------

    /**
     * Sorteia uma fase nova com dois poços, um Wumpus e um ouro.
     *
     * <p>As regras do jogo não mudam: apenas as posições. O sorteio só aceita
     * um mapa quando existe pelo menos um caminho seguro entre a casa inicial
     * e o ouro, de modo que a partida continue sendo vencível.</p>
     */
    public static Mundo sortear(Random sorteador) {
        while (true) {
            char[][] candidato = new char[TAMANHO][TAMANHO];
            for (char[] linha : candidato) {
                java.util.Arrays.fill(linha, VAZIO);
            }

            List<Posicao> livres = new ArrayList<>();
            for (int linha = 0; linha < TAMANHO; linha++) {
                for (int coluna = 0; coluna < TAMANHO; coluna++) {
                    Posicao posicao = new Posicao(linha, coluna);
                    // A casa inicial e suas vizinhas ficam livres de perigo,
                    // para que o agente não morra no primeiro passo.
                    if (posicao.distanciaAte(Posicao.INICIAL) > 1) {
                        livres.add(posicao);
                    }
                }
            }
            Collections.shuffle(livres, sorteador);

            Posicao primeiroPoco = livres.get(0);
            Posicao segundoPoco = livres.get(1);
            Posicao wumpus = livres.get(2);
            Posicao ouro = livres.get(3);
            Posicao flecha = livres.get(4);

            candidato[primeiroPoco.linha()][primeiroPoco.coluna()] = POCO;
            candidato[segundoPoco.linha()][segundoPoco.coluna()] = POCO;
            candidato[wumpus.linha()][wumpus.coluna()] = WUMPUS;
            candidato[ouro.linha()][ouro.coluna()] = OURO;
            candidato[flecha.linha()][flecha.coluna()] = FLECHA;

            if (existeCaminhoSeguro(candidato, ouro)) {
                return new Mundo(candidato);
            }
        }
    }

    /** Busca em largura: confirma que dá para ir da casa inicial até o ouro. */
    private static boolean existeCaminhoSeguro(char[][] candidato, Posicao destino) {
        boolean[][] alcancado = new boolean[TAMANHO][TAMANHO];
        Queue<Posicao> fila = new ArrayDeque<>();
        fila.add(Posicao.INICIAL);
        alcancado[0][0] = true;

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            if (atual.equals(destino)) {
                return true;
            }

            for (Direcao direcao : Direcao.TODAS) {
                Posicao vizinha = atual.vizinha(direcao);
                int linha = vizinha.linha();
                int coluna = vizinha.coluna();

                boolean dentro = linha >= 0 && linha < TAMANHO
                        && coluna >= 0 && coluna < TAMANHO;
                if (!dentro || alcancado[linha][coluna]) {
                    continue;
                }
                char conteudo = candidato[linha][coluna];
                if (conteudo == POCO || conteudo == WUMPUS) {
                    continue;
                }
                alcancado[linha][coluna] = true;
                fila.add(vizinha);
            }
        }
        return false;
    }
}
