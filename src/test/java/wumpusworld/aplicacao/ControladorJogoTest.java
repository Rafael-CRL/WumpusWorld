package wumpusworld.aplicacao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static wumpusworld.dominio.Direcao.*;

class ControladorJogoTest {
    @Test
    void direcaoMoveUmaCasaSemAcaoAutomatica() {
        ControladorJogo controlador = new ControladorJogo();
        controlador.escolherDirecao(BAIXO);
        var agente = controlador.getPartida().getAgente();
        assertEquals(1, agente.getLinha());
        assertEquals(0, agente.getColuna());
        assertEquals(1, agente.getQuantidadeDeMovimentos());
        assertEquals(-1, agente.getPontuacao());
    }

    @Test
    void miraDisparaSemMoverEConsomeUmaUnicaFlecha() {
        ControladorJogo controlador = new ControladorJogo();
        controlador.alternarDisparo();
        assertTrue(controlador.estaPreparandoDisparo());
        controlador.escolherDirecao(DIREITA);
        var agente = controlador.getPartida().getAgente();
        assertEquals(0, agente.getQuantidadeDeMovimentos());
        assertEquals(-10, agente.getPontuacao());
        assertFalse(agente.possuiFlecha());
        assertFalse(controlador.estaPreparandoDisparo());
        controlador.alternarDisparo();
        assertFalse(controlador.estaPreparandoDisparo());
        controlador.escolherDirecao(DIREITA);
        assertEquals(1, agente.getColuna());
    }

    @Test
    void cancelarMiraPermiteMoverSemGastarFlecha() {
        ControladorJogo controlador = new ControladorJogo();
        controlador.alternarDisparo();
        controlador.cancelarDisparo();
        controlador.escolherDirecao(BAIXO);
        assertTrue(controlador.getPartida().getAgente().possuiFlecha());
        assertEquals(1, controlador.getPartida().getAgente().getLinha());
        controlador.alternarDisparo();
        controlador.alternarDisparo();
        assertFalse(controlador.estaPreparandoDisparo());
    }

    @Test
    void novaPartidaRestauraPosicaoInventarioEMira() {
        ControladorJogo controlador = new ControladorJogo();
        Partida anterior = controlador.getPartida();
        controlador.escolherDirecao(BAIXO);
        controlador.alternarDisparo();
        controlador.reiniciar();
        assertNotSame(anterior, controlador.getPartida());
        assertFalse(controlador.estaPreparandoDisparo());
        var agente = controlador.getPartida().getAgente();
        assertEquals(0, agente.getQuantidadeDeMovimentos());
        assertEquals(0, agente.getPontuacao());
        assertEquals(0, agente.getLinha());
        assertTrue(agente.possuiFlecha());
        assertFalse(agente.possuiOuro());
    }
}
