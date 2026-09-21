package wumpusworld.aplicacao;

/**
 * Faz o agente avançar em intervalos regulares, sem bloquear a janela.
 * Recebe apenas o tempo decorrido entre quadros; quem decide a ação é {@link Partida}.
 */
public final class ControladorJogo {
    private Partida partida = new Partida();
    private Velocidade velocidade = Velocidade.NORMAL;
    private boolean pausado;
    private float acumulado;

    /** Acumula o tempo e executa, no máximo, uma decisão por chamada. */
    public void atualizar(float segundos) {
        if (pausado || partida.getEstado().terminou()) return;
        acumulado += segundos;
        if (acumulado >= velocidade.getIntervalo()) {
            acumulado -= velocidade.getIntervalo();
            partida.avancar();
        }
    }

    public void alternarPausa() {
        if (!partida.getEstado().terminou()) {
            pausado = !pausado;
        }
    }

    public void alternarVelocidade() {
        velocidade = velocidade.proxima();
    }

    public void reiniciar() {
        partida = new Partida();
        pausado = false;
        acumulado = 0;
    }

    public Partida getPartida() { return partida; }
    public Velocidade getVelocidade() { return velocidade; }
    public boolean estaPausado() { return pausado; }
}
