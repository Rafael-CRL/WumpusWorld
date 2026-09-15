package wumpusworld.dominio;

/** Direções ortogonais compartilhadas pelas ações de movimento e disparo. */
public enum Direcao {
    CIMA(-1, 0, 'W', "cima"),
    BAIXO(1, 0, 'S', "baixo"),
    ESQUERDA(0, -1, 'A', "esquerda"),
    DIREITA(0, 1, 'D', "direita");

    private final int variacaoLinha;
    private final int variacaoColuna;
    private final char comando;
    private final String nome;

    Direcao(int variacaoLinha, int variacaoColuna, char comando, String nome) {
        this.variacaoLinha = variacaoLinha;
        this.variacaoColuna = variacaoColuna;
        this.comando = comando;
        this.nome = nome;
    }

    public int getVariacaoLinha() { return variacaoLinha; }
    public int getVariacaoColuna() { return variacaoColuna; }
    public char getComando() { return comando; }
    public String getNome() { return nome; }

    public static Direcao peloComando(char comando) {
        for (Direcao direcao : values()) {
            if (direcao.comando == comando) return direcao;
        }
        throw new IllegalArgumentException("Direção inválida: " + comando);
    }
}
