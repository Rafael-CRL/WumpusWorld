package wumpusworld.aplicacao;

import wumpusworld.dominio.Direcao;

/** Traduz comandos manuais em ações. Nenhuma ação depende do tempo entre quadros. */
public final class ControladorJogo {
    private Partida partida = new Partida();
    private boolean preparandoDisparo;

    public void escolherDirecao(Direcao direcao) {
        if (partida.getEstado().terminou()) return;
        if (preparandoDisparo) {
            partida.atirar(direcao);
            preparandoDisparo = false;
        } else {
            partida.mover(direcao);
        }
    }

    public void alternarDisparo() {
        if (!partida.getEstado().terminou() && partida.getAgente().possuiFlecha()) {
            preparandoDisparo = !preparandoDisparo;
        }
    }

    public void cancelarDisparo() { preparandoDisparo = false; }

    public void reiniciar() {
        partida = new Partida();
        preparandoDisparo = false;
    }

    public Partida getPartida() { return partida; }
    public boolean estaPreparandoDisparo() { return preparandoDisparo; }
}
