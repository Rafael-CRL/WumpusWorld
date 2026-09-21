package wumpusworld.dominio;

/**
 * Representa o mapa e calcula as percepções do ambiente.
 * Cada posição da matriz possui uma linha e uma coluna.
 */
public class Mundo {

    public static final int TAMANHO = 5;

    public static final char VAZIO = '.';
    public static final char POCO = 'P';
    public static final char WUMPUS = 'W';
    public static final char OURO = 'O';
    public static final char DESCONHECIDO = '?';

    private final char[][] mapa;
    private final boolean[][] visitado;

    public Mundo() {
        mapa = new char[TAMANHO][TAMANHO];
        visitado = new boolean[TAMANHO][TAMANHO];
        criarMapa();
        visitado[0][0] = true;
    }

    /** Preenche a matriz e posiciona os elementos da fase. */
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
    }

    public boolean estaDentroDoMapa(int linha, int coluna) {
        return linha >= 0 && linha < TAMANHO
                && coluna >= 0 && coluna < TAMANHO;
    }

    public char getElemento(int linha, int coluna) {
        return mapa[linha][coluna];
    }

    /**
     * Elemento que a interface pode mostrar: o conteúdo real só é devolvido
     * para casas visitadas ou quando o mapa está revelado.
     */
    public char getElementoVisivel(int linha, int coluna, boolean revelarTudo) {
        return revelarTudo || visitado[linha][coluna] ? mapa[linha][coluna] : DESCONHECIDO;
    }

    public void removerElemento(int linha, int coluna) {
        mapa[linha][coluna] = VAZIO;
    }

    /**
     * Faz a flecha percorrer uma linha reta até sair do mapa.
     * Retorna true quando o Wumpus estava no caminho.
     */
    public boolean atirarFlecha(int linha, int coluna, char direcao) {
        int variacaoLinha = 0;
        int variacaoColuna = 0;

        switch (direcao) {
            case 'W':
                variacaoLinha = -1;
                break;
            case 'S':
                variacaoLinha = 1;
                break;
            case 'A':
                variacaoColuna = -1;
                break;
            case 'D':
                variacaoColuna = 1;
                break;
            default:
                return false;
        }

        int linhaDaFlecha = linha + variacaoLinha;
        int colunaDaFlecha = coluna + variacaoColuna;

        while (estaDentroDoMapa(linhaDaFlecha, colunaDaFlecha)) {
            if (mapa[linhaDaFlecha][colunaDaFlecha] == WUMPUS) {
                removerElemento(linhaDaFlecha, colunaDaFlecha);
                return true;
            }

            linhaDaFlecha = linhaDaFlecha + variacaoLinha;
            colunaDaFlecha = colunaDaFlecha + variacaoColuna;
        }

        return false;
    }

    /** Registra uma posição na memória visual do mapa. */
    public void marcarVisitada(int linha, int coluna) {
        visitado[linha][coluna] = true;
    }

    /** Procura um elemento nas quatro posições vizinhas. */
    private boolean existeVizinho(int linha, int coluna, char procurado) {
        int[][] direcoes = {
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
        };

        for (int[] direcao : direcoes) {
            int linhaVizinha = linha + direcao[0];
            int colunaVizinha = coluna + direcao[1];

            if (estaDentroDoMapa(linhaVizinha, colunaVizinha)
                    && mapa[linhaVizinha][colunaVizinha] == procurado) {
                return true;
            }
        }

        return false;
    }

    /** Métodos usados pelo agente para consultar suas percepções atuais. */
    public boolean temBrisa(int linha, int coluna) {
        return existeVizinho(linha, coluna, POCO);
    }

    public boolean temFedor(int linha, int coluna) {
        return existeVizinho(linha, coluna, WUMPUS);
    }

    public boolean foiVisitada(int linha, int coluna) {
        return visitado[linha][coluna];
    }
}
