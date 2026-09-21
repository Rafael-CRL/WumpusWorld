package wumpusworld.aplicacao;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControladorJogoTest {
    private static int movimentos(ControladorJogo controlador) {
        return controlador.getPartida().getAgente().getQuantidadeDeMovimentos();
    }

    @Test
    void agenteSoAvancaQuandoOIntervaloCompleta() {
        ControladorJogo controlador = new ControladorJogo();
        float intervalo = controlador.getVelocidade().getIntervalo();
        controlador.atualizar(intervalo / 2);
        assertEquals(0, movimentos(controlador));
        controlador.atualizar(intervalo / 2);
        assertEquals(1, movimentos(controlador));
    }

    @Test
    void quadroLongoExecutaNoMaximoUmaDecisao() {
        ControladorJogo controlador = new ControladorJogo();
        controlador.atualizar(10);
        assertEquals(1, movimentos(controlador));
    }

    @Test
    void pausaCongelaAPartidaEContinuarRetomaOMovimento() {
        ControladorJogo controlador = new ControladorJogo();
        float intervalo = controlador.getVelocidade().getIntervalo();
        controlador.alternarPausa();
        assertTrue(controlador.estaPausado());
        controlador.atualizar(intervalo * 5);
        assertEquals(0, movimentos(controlador));
        controlador.alternarPausa();
        controlador.atualizar(intervalo);
        assertEquals(1, movimentos(controlador));
    }

    @Test
    void velocidadeAlternaEmCicloComIntervalosDecrescentes() {
        ControladorJogo controlador = new ControladorJogo();
        assertEquals(Velocidade.NORMAL, controlador.getVelocidade());
        controlador.alternarVelocidade();
        assertEquals(Velocidade.RAPIDA, controlador.getVelocidade());
        controlador.alternarVelocidade();
        assertEquals(Velocidade.LENTA, controlador.getVelocidade());
        assertTrue(Velocidade.LENTA.getIntervalo() > Velocidade.NORMAL.getIntervalo());
        assertTrue(Velocidade.NORMAL.getIntervalo() > Velocidade.RAPIDA.getIntervalo());
    }

    @Test
    void partidaTerminaSozinhaEBloqueiaNovasDecisoes() {
        ControladorJogo controlador = new ControladorJogo();
        int quadros = 0;
        while (!controlador.getPartida().getEstado().terminou()) {
            controlador.atualizar(controlador.getVelocidade().getIntervalo());
            assertTrue(++quadros <= 204, "A partida deve terminar dentro do limite.");
        }
        int movimentos = movimentos(controlador);
        int pontuacao = controlador.getPartida().getAgente().getPontuacao();
        controlador.atualizar(10);
        controlador.alternarPausa();
        assertFalse(controlador.estaPausado(), "Não se pausa uma partida encerrada.");
        assertEquals(movimentos, movimentos(controlador));
        assertEquals(pontuacao, controlador.getPartida().getAgente().getPontuacao());
    }

    @Test
    void novaPartidaRestauraEstadoInicialEDespausa() {
        ControladorJogo controlador = new ControladorJogo();
        Partida anterior = controlador.getPartida();
        controlador.atualizar(10);
        controlador.alternarPausa();
        controlador.reiniciar();
        assertNotSame(anterior, controlador.getPartida());
        assertFalse(controlador.estaPausado());
        var agente = controlador.getPartida().getAgente();
        assertEquals(0, agente.getQuantidadeDeMovimentos());
        assertEquals(0, agente.getPontuacao());
        assertEquals(0, agente.getLinha());
        assertEquals(0, agente.getColuna());
        assertTrue(agente.possuiFlecha());
        assertFalse(agente.possuiOuro());
    }
}
