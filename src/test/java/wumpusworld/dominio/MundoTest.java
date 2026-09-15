package wumpusworld.dominio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MundoTest {
    @Test
    void percepcoesConsideramSomenteVizinhosOrtogonais() {
        Mundo mundo = new Mundo();
        assertTrue(mundo.temBrisa(1, 1));
        assertFalse(mundo.temBrisa(0, 1));
        assertTrue(mundo.temFedor(2, 2));
        assertFalse(mundo.temFedor(1, 2));
        assertFalse(mundo.temBrisa(0, 0));
        assertFalse(mundo.temFedor(0, 0));
    }

    @Test
    void flechaPercorreRetaEEliminaWumpusEFedor() {
        Mundo mundo = new Mundo();
        assertFalse(mundo.atirarFlecha(0, 0, 'D'));
        assertEquals(Mundo.WUMPUS, mundo.getElemento(2, 3));
        assertTrue(mundo.atirarFlecha(2, 0, 'D'));
        assertEquals(Mundo.VAZIO, mundo.getElemento(2, 3));
        assertFalse(mundo.temFedor(2, 2));
        assertFalse(mundo.atirarFlecha(2, 0, 'D'));
        assertFalse(mundo.atirarFlecha(0, 0, '?'));
    }

    @Test
    void memoriaVisualComecaApenasNaBase() {
        Mundo mundo = new Mundo();
        assertTrue(mundo.foiVisitada(0, 0));
        assertFalse(mundo.foiVisitada(4, 4));
        mundo.marcarVisitada(0, 1);
        assertTrue(mundo.foiVisitada(0, 1));
        assertFalse(mundo.estaDentroDoMapa(-1, 0));
        assertFalse(mundo.estaDentroDoMapa(0, 5));
    }
}
