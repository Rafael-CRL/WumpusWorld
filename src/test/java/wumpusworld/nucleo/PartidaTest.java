package wumpusworld.nucleo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifica o árbitro da simulação em cenários montados de propósito, de modo
 * que cada desfecho possível seja exercitado sem depender de sorte.
 */
class PartidaTest {

    /** Monta um mapa vazio e posiciona os elementos pedidos. */
    private static Mundo mapaCom(char[]... linhas) {
        char[][] mapa = new char[Mundo.TAMANHO][Mundo.TAMANHO];
        for (char[] linha : mapa) {
            Arrays.fill(linha, Mundo.VAZIO);
        }
        for (int linha = 0; linha < linhas.length; linha++) {
            System.arraycopy(linhas[linha], 0, mapa[linha], 0, Mundo.TAMANHO);
        }
        return new Mundo(mapa);
    }

    private static Mundo mapaVazio() {
        return mapaCom();
    }

    private static Partida executarAteOFim(Partida partida) {
        int guarda = 0;
        while (partida.executarPasso()) {
            assertTrue(++guarda < 1000, "a partida precisa terminar");
        }
        return partida;
    }

    @Test
    @DisplayName("a partida nasce em andamento, com o mapa oculto")
    void estadoInicial() {
        Partida partida = new Partida();

        assertSame(Situacao.EM_ANDAMENTO, partida.getSituacao());
        assertEquals(0, partida.getTurno());
        assertFalse(partida.mapaDeveSerRevelado());
        assertEquals(1, partida.getRegistro().size(),
                "o início da partida já fica registrado");
        assertEquals(Posicao.INICIAL, partida.getOrigemDoPasso());
    }

    @Test
    @DisplayName("cada passo avança um turno e registra a decisão")
    void cadaPassoAvancaUmTurno() {
        Partida partida = new Partida();

        assertTrue(partida.executarPasso());

        assertEquals(1, partida.getTurno());
        assertEquals(1, partida.getAgente().getQuantidadeDeMovimentos());
        assertTrue(partida.getEventosDoUltimoPasso().stream()
                .anyMatch(evento -> evento.tipo() == TipoEvento.DECISAO));
        assertEquals(partida.getAgente().getPosicao(),
                partida.getDestinoDoPasso());
        assertTrue(partida.agenteAndouNoUltimoPasso());
    }

    @Test
    @DisplayName("com o ouro ao lado, o agente vence e ganha o bônus de retorno")
    void vitoriaComOuroVizinho() {
        Mundo mundo = mapaCom(new char[] {
            Mundo.VAZIO, Mundo.OURO, Mundo.VAZIO, Mundo.VAZIO, Mundo.VAZIO});

        Partida partida = executarAteOFim(
                new Partida(mundo, new AgenteInteligente(11)));

        assertSame(Situacao.VITORIA, partida.getSituacao());
        assertTrue(partida.mapaDeveSerRevelado());

        var agente = partida.getAgente();
        assertTrue(agente.possuiOuro());
        assertEquals(Posicao.INICIAL, agente.getPosicao());
        assertEquals(
                AgenteInteligente.BONUS_OURO + AgenteInteligente.BONUS_VITORIA
                - agente.getQuantidadeDeMovimentos(),
                agente.getPontuacao(),
                "sem perigos, a pontuação é ouro + vitória - movimentos");
    }

    @Test
    @DisplayName("cercado de poços, o agente morre e é penalizado")
    void morteQuandoSoHaPocos() {
        Mundo mundo = mapaCom(
                new char[] {Mundo.VAZIO, Mundo.POCO, Mundo.VAZIO,
                    Mundo.VAZIO, Mundo.VAZIO},
                new char[] {Mundo.POCO, Mundo.VAZIO, Mundo.VAZIO,
                    Mundo.VAZIO, Mundo.VAZIO});

        Partida partida = executarAteOFim(
                new Partida(mundo, new AgenteInteligente(3)));

        assertSame(Situacao.MORTE, partida.getSituacao());
        assertTrue(partida.mapaDeveSerRevelado());
        assertFalse(partida.getAgente().estaVivo());
        assertEquals(
                AgenteInteligente.CUSTO_MOVIMENTO
                + AgenteInteligente.PENALIDADE_MORTE,
                partida.getAgente().getPontuacao());
        assertTrue(partida.getRegistro().stream()
                .anyMatch(evento -> evento.tipo() == TipoEvento.PERIGO));
    }

    @Test
    @DisplayName("sem ouro no mapa, a partida acaba no limite de exploração")
    void limiteDeExploracao() {
        Partida partida = executarAteOFim(
                new Partida(mapaVazio(), new AgenteInteligente(5)));

        assertSame(Situacao.LIMITE_ATINGIDO, partida.getSituacao());
        assertEquals(Partida.LIMITE_DE_EXPLORACAO,
                partida.getAgente().getQuantidadeDeMovimentos());
        assertEquals(-Partida.LIMITE_DE_EXPLORACAO,
                partida.getAgente().getPontuacao(),
                "num mapa vazio a única despesa é o custo de andar");
    }

    @Test
    @DisplayName("o fedor faz o agente gastar a flecha uma única vez")
    void oFedorGastaAFlechaUmaVezSo() {
        Mundo mundo = mapaCom(
                new char[] {Mundo.VAZIO, Mundo.VAZIO, Mundo.VAZIO,
                    Mundo.VAZIO, Mundo.VAZIO},
                new char[] {Mundo.WUMPUS, Mundo.VAZIO, Mundo.VAZIO,
                    Mundo.VAZIO, Mundo.VAZIO});

        Partida partida = new Partida(mundo, new AgenteInteligente(13));
        partida.executarPasso();

        assertFalse(partida.getAgente().possuiFlecha(),
                "havia fedor na casa inicial, então a flecha foi usada");
        assertTrue(partida.getRegistro().stream()
                .anyMatch(evento -> evento.tipo() == TipoEvento.FLECHA));

        executarAteOFim(partida);
        long disparos = partida.getRegistro().stream()
                .filter(evento -> evento.tipo() == TipoEvento.FLECHA)
                .filter(evento -> evento.mensagem().contains("disparada"))
                .count();
        assertEquals(1, disparos, "o agente só tem uma flecha");
    }

    @Test
    @DisplayName("a flecha certeira rende o bônus do Wumpus")
    void flechaCerteiraRendeBonus() {
        Mundo mundo = mapaCom(
                new char[] {Mundo.VAZIO, Mundo.VAZIO, Mundo.VAZIO,
                    Mundo.VAZIO, Mundo.VAZIO},
                new char[] {Mundo.WUMPUS, Mundo.VAZIO, Mundo.VAZIO,
                    Mundo.VAZIO, Mundo.VAZIO});

        Partida partida = new Partida(mundo, new AgenteInteligente(13));
        partida.executarPasso();

        DisparoDeFlecha disparo = partida.getUltimoDisparo();
        assertTrue(disparo != null, "o fedor deveria ter provocado um disparo");

        if (disparo.acertou()) {
            assertEquals(Mundo.VAZIO, mundo.getElemento(1, 0));
            assertEquals(new Posicao(1, 0), disparo.destino());
        }
    }

    @Test
    @DisplayName("qualquer partida termina em um estado válido e coerente")
    void todasAsPartidasTerminamBem() {
        java.util.Random sorteador = new java.util.Random(2026);

        for (int rodada = 0; rodada < 150; rodada++) {
            Partida partida = executarAteOFim(Partida.comFaseSorteada(sorteador));
            var agente = partida.getAgente();

            assertTrue(partida.getSituacao().encerrada());
            assertTrue(partida.mapaDeveSerRevelado());
            assertTrue(partida.getMundo().estaDentroDoMapa(agente.getPosicao()));

            switch (partida.getSituacao()) {
                case VITORIA -> {
                    assertTrue(agente.possuiOuro());
                    assertTrue(agente.estaVivo());
                    assertEquals(Posicao.INICIAL, agente.getPosicao());
                }
                case MORTE -> assertFalse(agente.estaVivo());
                case LIMITE_ATINGIDO -> {
                    assertFalse(agente.possuiOuro());
                    assertEquals(Partida.LIMITE_DE_EXPLORACAO,
                            agente.getQuantidadeDeMovimentos());
                }
                default -> throw new AssertionError("estado inesperado");
            }
        }
    }

    @Test
    @DisplayName("depois de encerrada, a partida não aceita mais passos")
    void partidaEncerradaNaoAceitaMaisPassos() {
        Partida partida = executarAteOFim(
                new Partida(mapaVazio(), new AgenteInteligente(1)));

        int turnoFinal = partida.getTurno();
        assertFalse(partida.executarPasso());
        assertEquals(turnoFinal, partida.getTurno());
    }
}
