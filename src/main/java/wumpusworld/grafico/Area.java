package wumpusworld.grafico;

/**
 * Retângulo de layout em coordenadas do mundo virtual da tela.
 *
 * <p>Toda a interface é montada sobre uma resolução virtual fixa. A
 * {@code FitViewport} se encarrega de escalar esse retângulo para o tamanho
 * real da janela, de modo que o desenho fique idêntico em qualquer monitor.</p>
 *
 * @param x       borda esquerda
 * @param y       borda inferior (em libGDX o eixo Y cresce para cima)
 * @param largura largura em unidades virtuais
 * @param altura  altura em unidades virtuais
 */
public record Area(float x, float y, float largura, float altura) {

    public float direita() {
        return x + largura;
    }

    public float topo() {
        return y + altura;
    }

    public float centroX() {
        return x + largura / 2f;
    }

    public float centroY() {
        return y + altura / 2f;
    }

    /** Devolve a mesma área reduzida igualmente nos quatro lados. */
    public Area encolher(float margem) {
        return new Area(x + margem, y + margem,
                largura - margem * 2f, altura - margem * 2f);
    }
}
