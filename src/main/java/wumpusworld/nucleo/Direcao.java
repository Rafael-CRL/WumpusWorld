package wumpusworld.nucleo;

public enum Direcao {

    CIMA(-1, 0, "CIMA"),
    BAIXO(1, 0, "BAIXO"),
    ESQUERDA(0, -1, "ESQUERDA"),
    DIREITA(0, 1, "DIREITA");

    public static final Direcao[] TODAS = values();

    private final int deltaLinha;
    private final int deltaColuna;
    private final String rotulo;

    Direcao(int deltaLinha, int deltaColuna, String rotulo) {
        this.deltaLinha = deltaLinha;
        this.deltaColuna = deltaColuna;
        this.rotulo = rotulo;
    }

    public int getDeltaLinha() {
        return deltaLinha;
    }

    public int getDeltaColuna() {
        return deltaColuna;
    }

    public String getRotulo() {
        return rotulo;
    }
}
