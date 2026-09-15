package wumpusworld.aplicacao;

import java.util.EnumSet;
import java.util.Random;
import org.junit.jupiter.api.Test;
import wumpusworld.dominio.AgenteInteligente;
import wumpusworld.dominio.Mundo;
import static org.junit.jupiter.api.Assertions.*;

class PartidaTest {
    @Test
    void partidasCompletasRespeitamMovimentosPontuacaoETermino() {
        var resultados = EnumSet.noneOf(EstadoPartida.class);
        for (int seed = 0; seed < 300; seed++) {
            Partida partida = new Partida(new Random(seed));
            AgenteInteligente agente = partida.getAgente();
            int turnos = 0;
            while (!partida.getEstado().terminou()) {
                int linha = agente.getLinha();
                int coluna = agente.getColuna();
                boolean retornando = agente.possuiOuro();
                partida.avancar();
                assertEquals(1, Math.abs(linha - agente.getLinha()) + Math.abs(coluna - agente.getColuna()));
                assertTrue(partida.getMundo().foiVisitada(agente.getLinha(), agente.getColuna()));
                if (retornando) {
                    assertTrue(agente.estaVivo(), "O retorno deve seguir casas já percorridas.");
                }
                assertTrue(++turnos <= 204, "Exploração mais retorno sem ciclos deve terminar.");
            }
            resultados.add(partida.getEstado());
            int esperado = -agente.getQuantidadeDeMovimentos();
            if (!agente.possuiFlecha()) esperado -= 10;
            if (partida.getMundo().getElemento(2, 3) == Mundo.VAZIO) esperado += 50;
            if (agente.possuiOuro()) esperado += 100;
            if (!agente.estaVivo()) esperado -= 100;
            if (partida.getEstado() == EstadoPartida.VITORIA) {
                esperado += 200;
                assertEquals(0, agente.getLinha());
                assertEquals(0, agente.getColuna());
                assertTrue(agente.possuiOuro());
            }
            assertEquals(esperado, agente.getPontuacao());
            var historico = partida.getHistorico();
            partida.avancar();
            assertEquals(esperado, agente.getPontuacao(), "Não se pode conceder bônus novamente.");
            assertEquals(turnos, agente.getQuantidadeDeMovimentos());
            assertEquals(historico, partida.getHistorico());
        }
        assertTrue(resultados.contains(EstadoPartida.VITORIA));
        assertTrue(resultados.contains(EstadoPartida.MORTE));
    }

    @Test
    void limiteEncerraExploracao() {
        Partida partida = new Partida(new Mundo(), new AgenteInteligente(new Random(0)), 1);
        partida.avancar();
        assertEquals(EstadoPartida.LIMITE_ATINGIDO, partida.getEstado());
        assertEquals(-1, partida.getAgente().getPontuacao());
    }

    @Test
    void ouroColetadoNoUltimoTurnoAindaPermiteRetornoCompleto() {
        for (int seed = 0; seed < 300; seed++) {
            Partida referencia = new Partida(new Random(seed));
            while (!referencia.getEstado().terminou() && !referencia.getAgente().possuiOuro()) {
                referencia.avancar();
            }
            if (!referencia.getAgente().possuiOuro()) continue;
            int limite = referencia.getAgente().getQuantidadeDeMovimentos();
            Partida partida = new Partida(new Mundo(), new AgenteInteligente(new Random(seed)), limite);
            for (int i = 0; i < limite; i++) partida.avancar();
            assertEquals(EstadoPartida.RETORNANDO, partida.getEstado());
            int retorno = 0;
            while (!partida.getEstado().terminou()) {
                partida.avancar();
                assertTrue(++retorno <= 24);
            }
            assertEquals(EstadoPartida.VITORIA, partida.getEstado());
            assertTrue(partida.getAgente().getQuantidadeDeMovimentos() > limite);
            return;
        }
        fail("Nenhuma partida de referência encontrou o ouro.");
    }

    @Test
    void historicoNaoPodeSerModificadoPelaInterface() {
        Partida partida = new Partida();
        assertThrows(UnsupportedOperationException.class, () -> partida.getHistorico().clear());
    }
}
