package wumpusworld.aplicacao;

public enum EstadoPartida {
    EXPLORANDO("Explorando"),
    RETORNANDO("Voltando à base"),
    VITORIA("Missão cumprida"),
    MORTE("Agente perdido"),
    LIMITE_ATINGIDO("Exploração encerrada");

    private final String titulo;

    EstadoPartida(String titulo) {
        this.titulo = titulo;
    }

    public String getTitulo() {
        return titulo;
    }

    public boolean terminou() {
        return this == VITORIA || this == MORTE || this == LIMITE_ATINGIDO;
    }
}
