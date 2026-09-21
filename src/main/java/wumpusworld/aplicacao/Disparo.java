package wumpusworld.aplicacao;

import wumpusworld.dominio.Direcao;

/**
 * Registro imutável de uma flecha já disparada, para a interface representá-la.
 *
 * @param linha   linha de onde a flecha saiu
 * @param coluna  coluna de onde a flecha saiu
 * @param direcao direção do disparo
 * @param alcance casas percorridas: até o Wumpus, se acertou, ou até a borda do mapa
 * @param acertou se o Wumpus foi atingido
 */
public record Disparo(int linha, int coluna, Direcao direcao, int alcance, boolean acertou) { }
