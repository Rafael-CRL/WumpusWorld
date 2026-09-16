package wumpusworld.nucleo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifica as regras do mapa: percepções, flecha e geração de fases. */
class MundoTest {

    // Fase das aulas: poços em [1][2] e [3][1], Wumpus em [2][3], ouro em [4][4].

    @Test
    @DisplayName("a brisa aparece somente ao lado de um poço")
    void brisaSoAoLadoDePoco() {
        Mundo mundo = new Mundo();

        assertTrue(mundo.temBrisa(0, 2), "[0][2] é vizinha do poço [1][2]");
        assertTrue(mundo.temBrisa(1, 1), "[1][1] é vizinha do poço [1][2]");
        assertFalse(mundo.temBrisa(0, 0), "a casa inicial não tem poço por perto");
        assertFalse(mundo.temBrisa(4, 4), "o canto do ouro não tem poço por perto");
    }

    @Test
    @DisplayName("o fedor aparece somente ao lado do Wumpus")
    void fedorSoAoLadoDoWumpus() {
        Mundo mundo = new Mundo();

        assertTrue(mundo.temFedor(2, 2));
        assertTrue(mundo.temFedor(1, 3));
        assertFalse(mundo.temFedor(0, 0));
    }

    @Test
    @DisplayName("o brilho aparece apenas na casa do ouro")
    void brilhoApenasNaCasaDoOuro() {
        Mundo mundo = new Mundo();

        assertTrue(mundo.percepcoesEm(4, 4).brilho());
        assertFalse(mundo.percepcoesEm(4, 3).brilho());
    }

    @Test
    @DisplayName("a flecha viaja em linha reta e mata o Wumpus no caminho")
    void flechaMataWumpusNoCaminho() {
        Mundo mundo = new Mundo();

        assertTrue(mundo.atirarFlecha(2, 0, Direcao.DIREITA),
                "de [2][0] para a direita a flecha passa por [2][3]");
        assertEquals(Mundo.VAZIO, mundo.getElemento(2, 3),
                "o Wumpus é retirado do mapa quando é atingido");
    }

    @Test
    @DisplayName("a flecha erra quando não há Wumpus na linha de tiro")
    void flechaErraQuandoNaoHaAlvo() {
        Mundo mundo = new Mundo();

        assertFalse(mundo.atirarFlecha(0, 0, Direcao.BAIXO));
        assertEquals(Mundo.WUMPUS, mundo.getElemento(2, 3),
                "um tiro errado não pode alterar o mapa");
    }

    @Test
    @DisplayName("o alcance calculado da flecha não altera o mapa")
    void alcanceDaFlechaNaoAlteraOMapa() {
        Mundo mundo = new Mundo();

        assertEquals(new Posicao(2, 3),
                mundo.calcularAlcanceDaFlecha(new Posicao(2, 0), Direcao.DIREITA));
        assertEquals(new Posicao(0, 4),
                mundo.calcularAlcanceDaFlecha(new Posicao(0, 0), Direcao.DIREITA));
        assertEquals(Mundo.WUMPUS, mundo.getElemento(2, 3),
                "calcular o alcance é apenas uma consulta");
    }

    @Test
    @DisplayName("os limites do mapa são respeitados")
    void limitesDoMapa() {
        Mundo mundo = new Mundo();

        assertTrue(mundo.estaDentroDoMapa(0, 0));
        assertTrue(mundo.estaDentroDoMapa(Mundo.TAMANHO - 1, Mundo.TAMANHO - 1));
        assertFalse(mundo.estaDentroDoMapa(-1, 0));
        assertFalse(mundo.estaDentroDoMapa(0, Mundo.TAMANHO));
    }

    @Test
    @DisplayName("só a casa inicial começa visitada")
    void apenasACasaInicialComecaVisitada() {
        Mundo mundo = new Mundo();

        assertEquals(1, mundo.quantidadeDeCasasVisitadas());
        assertTrue(mundo.foiVisitada(0, 0));

        mundo.marcarVisitada(new Posicao(2, 2));
        assertEquals(2, mundo.quantidadeDeCasasVisitadas());
    }

    @Test
    @DisplayName("reiniciar devolve a fase ao desenho original")
    void reiniciarRestauraAFase() {
        Mundo mundo = new Mundo();
        mundo.removerElemento(4, 4);
        mundo.marcarVisitada(3, 3);

        Mundo novo = mundo.reiniciar();

        assertEquals(Mundo.OURO, novo.getElemento(4, 4));
        assertEquals(1, novo.quantidadeDeCasasVisitadas());
    }

    @Test
    @DisplayName("toda fase sorteada é vencível e poupa a vizinhança do início")
    void faseSorteadaEhValida() {
        Random sorteador = new Random(2026);

        for (int tentativa = 0; tentativa < 120; tentativa++) {
            Mundo mundo = Mundo.sortear(sorteador);

            int pocos = 0;
            int wumpus = 0;
            int ouros = 0;
            Posicao posicaoDoOuro = null;

            for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
                for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                    char elemento = mundo.getElemento(linha, coluna);
                    Posicao posicao = new Posicao(linha, coluna);

                    if (elemento != Mundo.VAZIO) {
                        assertTrue(posicao.distanciaAte(Posicao.INICIAL) > 1,
                                "nada perigoso pode nascer colado à casa inicial");
                    }
                    switch (elemento) {
                        case Mundo.POCO -> pocos++;
                        case Mundo.WUMPUS -> wumpus++;
                        case Mundo.OURO -> {
                            ouros++;
                            posicaoDoOuro = posicao;
                        }
                        default -> {
                        }
                    }
                }
            }

            assertEquals(2, pocos);
            assertEquals(1, wumpus);
            assertEquals(1, ouros);
            assertTrue(existeCaminhoSeguro(mundo, posicaoDoOuro),
                    "o ouro precisa ser alcançável sem atravessar perigos");
        }
    }

    /** Busca em largura independente, para não confiar na do próprio Mundo. */
    private static boolean existeCaminhoSeguro(Mundo mundo, Posicao destino) {
        boolean[][] visto = new boolean[Mundo.TAMANHO][Mundo.TAMANHO];
        java.util.Deque<Posicao> fila = new java.util.ArrayDeque<>();
        fila.add(Posicao.INICIAL);
        visto[0][0] = true;

        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            if (atual.equals(destino)) {
                return true;
            }
            for (Direcao direcao : Direcao.TODAS) {
                Posicao vizinha = atual.vizinha(direcao);
                if (!mundo.estaDentroDoMapa(vizinha)
                        || visto[vizinha.linha()][vizinha.coluna()]) {
                    continue;
                }
                char elemento = mundo.getElemento(vizinha);
                if (elemento == Mundo.POCO || elemento == Mundo.WUMPUS) {
                    continue;
                }
                visto[vizinha.linha()][vizinha.coluna()] = true;
                fila.add(vizinha);
            }
        }
        return false;
    }
}
