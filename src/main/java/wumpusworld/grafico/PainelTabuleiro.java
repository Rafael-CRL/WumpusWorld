package wumpusworld.grafico;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Posicao;

/**
 * Desenha a grade do mundo: as casas, os elementos conhecidos e o agente.
 *
 * <p>É o único ponto do projeto que converte a matriz em coordenadas de tela.
 * A matriz cresce para baixo e a tela cresce para cima, e essa inversão está
 * concentrada nos métodos {@link #x(int)} e {@link #y(int)}.</p>
 *
 * <p>A grade ocupa sempre o mesmo quadrado de {@value #AREA} pixels. O lado de
 * cada casa, e o tamanho de tudo que vai dentro dela, é derivado de
 * {@link Mundo#TAMANHO}: mudar o tamanho da matriz não exige mexer aqui.</p>
 */
public class PainelTabuleiro {

    private static final float AREA = 500f;
    private static final float LADO = AREA / Mundo.TAMANHO;
    private static final float MARGEM_X = 50f;
    private static final float MARGEM_Y = 50f;

    /** Folga entre casas vizinhas. */
    private static final float ESPACO = 2f;

    private static final float RAIO_DO_ELEMENTO = 0.28f * LADO;
    private static final float RAIO_DO_AGENTE = 0.20f * LADO;
    private static final float MEIO_LADO_DO_OURO = 0.20f * LADO;
    private static final float MEIA_LARGURA_DA_FLECHA = 0.05f * LADO;
    private static final float MEIA_ALTURA_DA_FLECHA = 0.20f * LADO;

    /** Acima do padrão da libGDX, para os círculos não parecerem polígonos. */
    private static final int SEGMENTOS = 48;

    /** Borda esquerda da casa que está na coluna informada. */
    private float x(int coluna) {
        return MARGEM_X + coluna * LADO;
    }

    /** Borda inferior da casa que está na linha informada. */
    private float y(int linha) {
        return MARGEM_Y + (Mundo.TAMANHO - 1 - linha) * LADO;
    }

    public void desenharFormas(ShapeRenderer formas, Partida partida, Mundo mapaInicial) {
        for (int l = 0; l < Mundo.TAMANHO; l++) {
            for (int c = 0; c < Mundo.TAMANHO; c++) {
                float x = x(c);
                float y = y(l);

                boolean conhecida = partida.getMundo().foiVisitada(l, c) || partida.mapaDeveSerRevelado();

                formas.setColor(conhecida ? Paleta.CASA_CONHECIDA : Paleta.CASA_DESCONHECIDA);
                formas.rect(x + ESPACO, y + ESPACO, LADO - 2 * ESPACO, LADO - 2 * ESPACO);

                if (conhecida) {
                    char elem = partida.mapaDeveSerRevelado()
                            ? mapaInicial.getElemento(l, c)
                            : partida.getMundo().getElemento(l, c);
                    float cx = x + LADO / 2;
                    float cy = y + LADO / 2;

                    if (elem == Mundo.POCO) {
                        formas.setColor(Paleta.POCO);
                        formas.circle(cx, cy, RAIO_DO_ELEMENTO, SEGMENTOS);
                    } else if (elem == Mundo.WUMPUS) {
                        formas.setColor(Paleta.WUMPUS);
                        formas.circle(cx, cy, RAIO_DO_ELEMENTO, SEGMENTOS);
                    } else if (elem == Mundo.OURO) {
                        formas.setColor(Paleta.OURO);
                        formas.rect(cx - MEIO_LADO_DO_OURO, cy - MEIO_LADO_DO_OURO,
                                2 * MEIO_LADO_DO_OURO, 2 * MEIO_LADO_DO_OURO);
                    } else if (elem == Mundo.FLECHA) {
                        formas.setColor(Paleta.FLECHA);
                        formas.rect(cx - MEIA_LARGURA_DA_FLECHA, cy - MEIA_ALTURA_DA_FLECHA,
                                2 * MEIA_LARGURA_DA_FLECHA, 2 * MEIA_ALTURA_DA_FLECHA);
                    }
                }
            }
        }

        Posicao pos = partida.getAgente().getPosicao();
        formas.setColor(Paleta.AGENTE);
        formas.circle(x(pos.coluna()) + LADO / 2, y(pos.linha()) + LADO / 2, RAIO_DO_AGENTE, SEGMENTOS);
    }

    /** Numeração das colunas embaixo e das linhas à esquerda, centrada em cada casa. */
    public void desenharTextos(SpriteBatch lote, Fontes fontes) {
        BitmapFont fonte = fontes.pequena;
        float meiaAltura = fonte.getCapHeight() / 2;

        for (int c = 0; c < Mundo.TAMANHO; c++) {
            fonte.draw(lote, String.valueOf(c), x(c), MARGEM_Y - 12, LADO, Align.center, false);
        }
        for (int l = 0; l < Mundo.TAMANHO; l++) {
            fonte.draw(lote, String.valueOf(l), MARGEM_X - 40, y(l) + LADO / 2 + meiaAltura,
                    30, Align.right, false);
        }
    }
}
