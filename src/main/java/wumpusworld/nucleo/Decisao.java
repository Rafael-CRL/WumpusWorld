package wumpusworld.nucleo;

/**
 * Resultado de uma deliberação do agente: para onde ele decidiu ir e por quê.
 *
 * <p>Devolver um objeto em vez de um texto pronto mantém a regra separada da
 * apresentação: o console e a janela gráfica formatam a mesma decisão cada um
 * do seu jeito.</p>
 *
 * @param direcao direção escolhida
 * @param nota    melhor nota calculada entre as casas vizinhas
 * @param risco   suspeita acumulada sobre a casa de destino
 * @param visitas quantas vezes a casa de destino já havia sido pisada
 */
public record Decisao(Direcao direcao, int nota, int risco, int visitas) {

    /** Linha de explicação usada no registro da partida. */
    public String explicacao() {
        return direcao.getRotulo()
                + "  |  risco=" + risco
                + "  |  visitas=" + visitas
                + "  |  nota=" + nota;
    }
}
