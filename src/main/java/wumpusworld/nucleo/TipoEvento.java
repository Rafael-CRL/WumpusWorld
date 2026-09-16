package wumpusworld.nucleo;

/**
 * Categoria de um acontecimento da partida.
 *
 * <p>Serve para que o registro seja legível: o console escolhe um prefixo e a
 * janela gráfica escolhe uma cor a partir do mesmo tipo.</p>
 */
public enum TipoEvento {

    /** Abertura da partida e avisos gerais. */
    SISTEMA("SISTEMA"),
    /** Sinais sentidos pelo agente na casa atual. */
    PERCEPCAO("PERCEPÇÃO"),
    /** Explicação da regra que determinou o movimento. */
    DECISAO("DECISÃO"),
    /** Disparo da flecha e seu resultado. */
    FLECHA("FLECHA"),
    /** Encontro com o ouro. */
    TESOURO("TESOURO"),
    /** Encontro com um perigo, com ou sem morte. */
    PERIGO("PERIGO"),
    /** Retorno bem-sucedido com o ouro. */
    VITORIA("VITÓRIA"),
    /** Fim de jogo sem vitória. */
    DERROTA("FIM DE JOGO");

    private final String rotulo;

    TipoEvento(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
