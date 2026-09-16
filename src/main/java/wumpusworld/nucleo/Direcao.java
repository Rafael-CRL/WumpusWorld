package wumpusworld.nucleo;

/**
 * As quatro direções possíveis dentro da matriz do mundo.
 *
 * <p>A ordem de declaração é significativa: ela reproduz exatamente a ordem
 * usada nas aulas ({@code cima, baixo, esquerda, direita}), preservando o
 * comportamento do agente na hora de comparar notas e desempatar.</p>
 *
 * <p>Este enum substitui os vetores paralelos de deslocamentos e de nomes que
 * existiam na versão de console. A vantagem prática é que fica impossível
 * combinar o deslocamento de uma direção com o rótulo de outra.</p>
 */
public enum Direcao {

    CIMA(-1, 0, "CIMA"),
    BAIXO(1, 0, "BAIXO"),
    ESQUERDA(0, -1, "ESQUERDA"),
    DIREITA(0, 1, "DIREITA");

    /** Lista reaproveitável, para evitar alocações a cada varredura. */
    public static final Direcao[] TODAS = values();

    private final int deltaLinha;
    private final int deltaColuna;
    private final String rotulo;

    Direcao(int deltaLinha, int deltaColuna, String rotulo) {
        this.deltaLinha = deltaLinha;
        this.deltaColuna = deltaColuna;
        this.rotulo = rotulo;
    }

    /** Quanto a linha varia ao andar nesta direção. */
    public int getDeltaLinha() {
        return deltaLinha;
    }

    /** Quanto a coluna varia ao andar nesta direção. */
    public int getDeltaColuna() {
        return deltaColuna;
    }

    /** Nome exibido nos relatórios e no registro da partida. */
    public String getRotulo() {
        return rotulo;
    }
}
