package wumpusworld.nucleo;

/**
 * Descrição de um disparo, guardada para que a interface possa animá-lo.
 *
 * @param origem  casa de onde o agente atirou
 * @param destino casa onde a flecha parou (o Wumpus ou a borda do mapa)
 * @param direcao direção do voo
 * @param acertou se o Wumpus estava no caminho
 */
public record DisparoDeFlecha(Posicao origem, Posicao destino,
        Direcao direcao, boolean acertou) {
}
