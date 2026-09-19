package wumpusworld.grafico;

/**
 * Fotografia do momento da animação, repassada aos painéis a cada quadro.
 *
 * @param tempo               segundos decorridos desde a abertura da janela
 * @param progressoDoPasso    0 a 1: o quanto o agente já andou entre duas casas
 * @param progressoDoDisparo  0 a 1: o quanto a flecha já voou (1 = sem disparo)
 * @param mostrarMapaDeRisco  desenha as suspeitas que o agente memorizou
 */
public record EstadoDaAnimacao(float tempo, float progressoDoPasso,
        float progressoDoDisparo, boolean mostrarMapaDeRisco) {
}
