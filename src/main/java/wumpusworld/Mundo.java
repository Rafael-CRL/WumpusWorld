package wumpusworld;

/**
 * A classe Mundo cria e mostra o mapa.
 * Cada posição da matriz possui uma linha e uma coluna.
 */
public class Mundo {

    public static final int TAMANHO = 5;

    public static final char VAZIO = '.';
    public static final char POCO = 'P';
    public static final char WUMPUS = 'W';
    public static final char OURO = 'O';

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

    /** Mostra os sinais existentes ao redor do agente. */
    public void mostrarPercepcoes(AgenteInteligente agente) {
        int linha = agente.getLinha();
        int coluna = agente.getColuna();
        boolean percebeuAlgo = false;

        System.out.print("Percepções: ");

        if (temBrisa(linha, coluna)) {
            System.out.print("BRISA  ");
            percebeuAlgo = true;
        }

        if (temFedor(linha, coluna)) {
            System.out.print("FEDOR  ");
            percebeuAlgo = true;
        }

        if (mapa[linha][coluna] == OURO) {
            System.out.print("BRILHO  ");
            percebeuAlgo = true;
        }

        if (!percebeuAlgo) {
            System.out.print("NENHUMA");
        }

        System.out.println();
    }

    /**
     * Durante o jogo, ? representa uma posição desconhecida e + uma posição
     * visitada. No final, revelarTudo permite discutir o mapa real com a turma.
     */
    public void mostrar(AgenteInteligente agente, boolean revelarTudo) {
        System.out.print("      ");
        for (int coluna = 0; coluna < TAMANHO; coluna++) {
            System.out.print(coluna + "   ");
        }
        System.out.println("  COLUNAS");

        for (int linha = 0; linha < TAMANHO; linha++) {
            System.out.print("  " + linha + "  ");

            for (int coluna = 0; coluna < TAMANHO; coluna++) {
                if (linha == agente.getLinha()
                        && coluna == agente.getColuna()) {
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
        System.out.println("LINHAS");
    }
}
