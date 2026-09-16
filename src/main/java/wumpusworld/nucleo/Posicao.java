package wumpusworld.nucleo;

/**
 * Uma coordenada da matriz do mundo, sempre no formato linha/coluna.
 *
 * <p>Este é o único vocabulário de posição usado pelas regras. A conversão
 * para coordenadas de tela acontece somente na camada gráfica.</p>
 *
 * @param linha  índice da linha, contado de cima para baixo a partir de zero
 * @param coluna índice da coluna, contado da esquerda para a direita
 */
public record Posicao(int linha, int coluna) {

    /** Casa de partida e de retorno do agente. */
    public static final Posicao INICIAL = new Posicao(0, 0);

    /** Retorna a casa vizinha na direção informada. */
    public Posicao vizinha(Direcao direcao) {
        return new Posicao(linha + direcao.getDeltaLinha(),
                coluna + direcao.getDeltaColuna());
    }

    /** Distância de Manhattan, útil para métricas e depuração. */
    public int distanciaAte(Posicao outra) {
        return Math.abs(linha - outra.linha) + Math.abs(coluna - outra.coluna);
    }

    @Override
    public String toString() {
        return "[" + linha + "][" + coluna + "]";
    }
}
