package wumpusworld.aplicacao;

import org.junit.jupiter.api.Test;
import wumpusworld.dominio.Mundo;
import static org.junit.jupiter.api.Assertions.*;
import static wumpusworld.dominio.Direcao.*;

class PartidaManualTest {
    @Test
    void bordaNaoMoveNemPenaliza() {
        Partida partida = new Partida();
        assertFalse(partida.mover(CIMA));
        assertFalse(partida.mover(ESQUERDA));
        assertEquals(0, partida.getAgente().getQuantidadeDeMovimentos());
        assertEquals(0, partida.getAgente().getPontuacao());
        assertEquals(0, partida.getAgente().getLinha());
        assertEquals(0, partida.getAgente().getColuna());
        assertTrue(partida.getUltimaDecisao().contains("Parede"));
    }

    @Test
    void fedorNaoDisparaFlechaAutomaticamente() {
        Partida partida = new Partida();
        partida.mover(BAIXO);
        partida.mover(BAIXO);
        partida.mover(DIREITA);
        partida.mover(DIREITA);
        assertTrue(partida.getMundo().temFedor(2, 2));
        assertTrue(partida.getAgente().possuiFlecha());
        partida.mover(ESQUERDA);
        assertTrue(partida.getAgente().possuiFlecha());
        assertEquals(Mundo.WUMPUS, partida.getMundo().getElemento(2, 3));
    }

    @Test
    void disparoAcertaSemMoverESemPermitirSegundaFlecha() {
        Partida partida = new Partida();
        partida.mover(BAIXO);
        partida.mover(BAIXO);
        assertTrue(partida.atirar(DIREITA));
        assertEquals(Mundo.VAZIO, partida.getMundo().getElemento(2, 3));
        assertEquals(38, partida.getAgente().getPontuacao());
        assertEquals(2, partida.getAgente().getQuantidadeDeMovimentos());
        assertEquals(0, partida.getAgente().getColuna());
        assertFalse(partida.atirar(CIMA));
        assertEquals(38, partida.getAgente().getPontuacao());
    }

    @Test
    void tiroErradoTambemConsomeFlecha() {
        Partida partida = new Partida();
        assertTrue(partida.atirar(CIMA));
        assertEquals(-10, partida.getAgente().getPontuacao());
        assertFalse(partida.getAgente().possuiFlecha());
        assertEquals(Mundo.WUMPUS, partida.getMundo().getElemento(2, 3));
    }

    @Test
    void ouroNaoImpedeEscolherOCaminhoDeRetorno() {
        Partida partida = new Partida();
        for (int i = 0; i < 4; i++) partida.mover(BAIXO);
        for (int i = 0; i < 4; i++) partida.mover(DIREITA);
        assertTrue(partida.getAgente().possuiOuro());
        assertEquals(EstadoPartida.RETORNANDO, partida.getEstado());
        assertEquals(92, partida.getAgente().getPontuacao());
        // A casa de cima não é o retorno pelo histórico (que seria à esquerda).
        partida.mover(CIMA);
        assertEquals(3, partida.getAgente().getLinha());
        assertEquals(4, partida.getAgente().getColuna());
        partida.mover(BAIXO);
        for (int i = 0; i < 4; i++) partida.mover(ESQUERDA);
        for (int i = 0; i < 4; i++) partida.mover(CIMA);
        assertEquals(EstadoPartida.VITORIA, partida.getEstado());
        assertEquals(282, partida.getAgente().getPontuacao());
        assertFalse(partida.mover(DIREITA));
        assertFalse(partida.atirar(DIREITA));
        assertEquals(282, partida.getAgente().getPontuacao());
    }

    @Test
    void pocoMataEBloqueiaNovasAcoes() {
        Partida partida = new Partida();
        partida.mover(DIREITA);
        partida.mover(DIREITA);
        partida.mover(BAIXO);
        assertEquals(EstadoPartida.MORTE, partida.getEstado());
        assertEquals(-103, partida.getAgente().getPontuacao());
        assertFalse(partida.mover(CIMA));
        assertFalse(partida.atirar(DIREITA));
        assertEquals(-103, partida.getAgente().getPontuacao());
    }
}
