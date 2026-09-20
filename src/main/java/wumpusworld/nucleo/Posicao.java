package wumpusworld.nucleo;

public record Posicao(int linha, int coluna) {

    public static final Posicao INICIAL = new Posicao(0, 0);

    public Posicao vizinha(Direcao direcao) {
        return new Posicao(linha + direcao.getDeltaLinha(),
                coluna + direcao.getDeltaColuna());
    }

    public int distanciaAte(Posicao outra) {
        return Math.abs(linha - outra.linha) + Math.abs(coluna - outra.coluna);
    }

    @Override
    public String toString() {
        return "[" + linha + "][" + coluna + "]";
    }
}
