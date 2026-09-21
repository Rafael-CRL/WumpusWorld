package wumpusworld.aplicacao;

/** Intervalo, em segundos, entre duas decisões consecutivas do agente. */
public enum Velocidade {
    LENTA("Lenta", 1.0f),
    NORMAL("Normal", 0.6f),
    RAPIDA("Rápida", 0.25f);

    private final String titulo;
    private final float intervalo;

    Velocidade(String titulo, float intervalo) {
        this.titulo = titulo;
        this.intervalo = intervalo;
    }

    public String getTitulo() { return titulo; }
    public float getIntervalo() { return intervalo; }

    /** Próxima velocidade, voltando à primeira depois da última. */
    public Velocidade proxima() {
        Velocidade[] todas = values();
        return todas[(ordinal() + 1) % todas.length];
    }
}
