package wumpusworld.grafico;

import wumpusworld.dominio.Mundo;

/**
 * Posição do tabuleiro na tela. Converte linha e coluna do domínio (origem no alto,
 * à esquerda) para coordenadas de desenho, cujo eixo Y cresce para cima.
 *
 * @param x     canto esquerdo
 * @param y     canto inferior
 * @param passo lado de uma casa
 */
record Grade(float x, float y, float passo) {
    float lado() { return passo * Mundo.TAMANHO; }

    /** Centro horizontal da coluna; aceita valores fracionários para animações. */
    float centroX(float coluna) { return x + (coluna + 0.5f) * passo; }

    /** Centro vertical da linha; aceita valores fracionários para animações. */
    float centroY(float linha) { return y + (Mundo.TAMANHO - 0.5f - linha) * passo; }
}
