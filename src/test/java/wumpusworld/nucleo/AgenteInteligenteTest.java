package wumpusworld.nucleo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifica a memória, a estimativa de risco e as decisões do agente. */
class AgenteInteligenteTest {

    private static final long SEMENTE = 7;

    @Test
    @DisplayName("o agente começa vivo, na casa inicial e com a flecha")
    void estadoInicial() {
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        assertEquals(Posicao.INICIAL, agente.getPosicao());
        assertTrue(agente.estaVivo());
        assertTrue(agente.possuiFlecha());
        assertFalse(agente.possuiOuro());
        assertEquals(0, agente.getPontuacao());
        assertEquals(0, agente.getQuantidadeDeMovimentos());
        assertEquals(1, agente.getCaminhoConhecido().size());
    }

    @Test
    @DisplayName("a percepção aumenta o risco das vizinhas ainda desconhecidas")
    void percepcaoAumentaORiscoDasVizinhas() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        agente.observar(mundo, new Percepcoes(true, false, false));

        assertEquals(1, agente.getRisco(0, 1));
        assertEquals(1, agente.getRisco(1, 0));
        assertEquals(0, agente.getRisco(2, 2), "casas distantes não são afetadas");
        assertEquals(0, agente.getRisco(0, 0), "a casa visitada não recebe risco");
    }

    @Test
    @DisplayName("brisa e fedor juntos pesam o dobro")
    void brisaEFedorSomamDois() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        agente.observar(mundo, new Percepcoes(true, true, false));

        assertEquals(2, agente.getRisco(0, 1));
        assertEquals(2, agente.getMaiorRisco());
    }

    @Test
    @DisplayName("a mesma percepção não é contada duas vezes na mesma casa")
    void percepcaoRepetidaNaoAcumula() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        agente.observar(mundo, new Percepcoes(true, false, false));
        agente.observar(mundo, new Percepcoes(true, false, false));
        agente.observar(mundo, new Percepcoes(true, true, false));

        assertEquals(1, agente.getRisco(0, 1));
    }

    @Test
    @DisplayName("mover custa um ponto e registra a casa na memória")
    void moverCustaUmPonto() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        Decisao decisao = agente.moverExplorando(mundo);

        assertEquals(1, agente.getQuantidadeDeMovimentos());
        assertEquals(AgenteInteligente.CUSTO_MOVIMENTO, agente.getPontuacao());
        assertNotEquals(Posicao.INICIAL, agente.getPosicao());
        assertEquals(agente.getPosicao(),
                Posicao.INICIAL.vizinha(decisao.direcao()));
        assertEquals(100, decisao.nota(), "casa nova vale o bônus cheio");
        assertEquals(2, agente.getCaminhoConhecido().size());
    }

    @Test
    @DisplayName("o agente nunca sai do mapa")
    void oAgenteNuncaSaiDoMapa() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        for (int passo = 0; passo < 400; passo++) {
            agente.moverExplorando(mundo);
            assertTrue(mundo.estaDentroDoMapa(agente.getPosicao()),
                    "posição inválida no passo " + passo);
        }
    }

    @Test
    @DisplayName("o caminho memorizado descarta as voltas em círculo")
    void caminhoMemorizadoDescartaVoltas() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        for (int passo = 0; passo < 60; passo++) {
            agente.moverExplorando(mundo);

            var caminho = agente.getCaminhoConhecido();
            assertEquals(Posicao.INICIAL, caminho.get(0),
                    "o caminho sempre começa na casa inicial");
            assertEquals(agente.getPosicao(), caminho.get(caminho.size() - 1),
                    "o caminho sempre termina onde o agente está");
            assertEquals(caminho.size(), new java.util.HashSet<>(caminho).size(),
                    "não pode haver casa repetida no caminho de retorno");

            for (int indice = 0; indice < caminho.size() - 1; indice++) {
                assertEquals(1,
                        caminho.get(indice).distanciaAte(caminho.get(indice + 1)),
                        "casas seguidas do caminho precisam ser vizinhas");
            }
        }
    }

    @Test
    @DisplayName("o retorno refaz o caminho até a casa inicial")
    void retornoRefazOCaminho() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        for (int passo = 0; passo < 25; passo++) {
            agente.moverExplorando(mundo);
        }

        int voltas = 0;
        while (agente.retornarPeloCaminho()) {
            voltas++;
            assertTrue(voltas < 100, "o retorno precisa terminar");
        }

        assertEquals(Posicao.INICIAL, agente.getPosicao());
        assertFalse(agente.retornarPeloCaminho(),
                "na casa inicial não há mais para onde voltar");
    }

    @Test
    @DisplayName("a flecha é apontada para a vizinha desconhecida mais suspeita")
    void flechaApontaParaAMaisSuspeita() {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente(SEMENTE);

        // Duas percepções seguidas deixam [0][1] com risco 2 e [1][0] com 1.
        agente.observar(mundo, new Percepcoes(true, true, false));

        assertEquals(2, agente.getRisco(0, 1));
        assertEquals(2, agente.getRisco(1, 0));

        Direcao alvo = agente.escolherDirecaoDaFlecha(mundo);
        assertTrue(alvo == Direcao.DIREITA || alvo == Direcao.BAIXO,
                "só existem duas vizinhas válidas na casa inicial");
    }

    @Test
    @DisplayName("a mesma semente produz exatamente a mesma partida")
    void mesmaSementeProduzMesmoPercurso() {
        StringBuilder primeiro = new StringBuilder();
        StringBuilder segundo = new StringBuilder();

        for (StringBuilder registro : new StringBuilder[] {primeiro, segundo}) {
            Mundo mundo = new Mundo();
            AgenteInteligente agente = new AgenteInteligente(99);
            for (int passo = 0; passo < 40; passo++) {
                agente.observar(mundo, mundo.percepcoesEm(agente.getPosicao()));
                registro.append(agente.moverExplorando(mundo).direcao());
            }
        }

        assertEquals(primeiro.toString(), segundo.toString());
    }
}
