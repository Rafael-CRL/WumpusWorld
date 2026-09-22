package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Posicao;

/**
 * Desenha a grade do mundo: as casas, os elementos conhecidos e o agente.
 *
 * <p>É o único ponto do projeto que converte a matriz em coordenadas de tela.
 * A matriz cresce para baixo e a tela cresce para cima, e essa inversão está
 * concentrada nos métodos {@link #x(int)} e {@link #y(int)}.</p>
 */
public class PainelTabuleiro {

    private static final float LADO = 100f;
    private static final float MARGEM_X = 50f;
    private static final float MARGEM_Y = 50f;

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

                if (conhecida) {
                    formas.setColor(0.75f, 0.75f, 0.75f, 1);
                } else {
                    formas.setColor(0.3f, 0.3f, 0.3f, 1);
                }
                formas.rect(x + 2, y + 2, LADO - 4, LADO - 4);

                if (conhecida) {
                    char elem = partida.mapaDeveSerRevelado()
                            ? mapaInicial.getElemento(l, c)
                            : partida.getMundo().getElemento(l, c);
                    float cx = x + LADO / 2;
                    float cy = y + LADO / 2;

                    if (elem == Mundo.POCO) {
                        formas.setColor(Color.BLACK);
                        formas.circle(cx, cy, 28);
                    } else if (elem == Mundo.WUMPUS) {
                        formas.setColor(Color.RED);
                        formas.circle(cx, cy, 28);
                    } else if (elem == Mundo.OURO) {
                        formas.setColor(Color.YELLOW);
                        formas.rect(cx - 20, cy - 20, 40, 40);
                    } else if (elem == Mundo.FLECHA) {
                        formas.setColor(Color.GREEN);
                        formas.rect(cx - 5, cy - 20, 10, 40);
                    }
                }
            }
        }

        Posicao pos = partida.getAgente().getPosicao();
        formas.setColor(Color.BLUE);
        formas.circle(x(pos.coluna()) + LADO / 2, y(pos.linha()) + LADO / 2, 20);
    }

    /** Numeração das colunas embaixo e das linhas à esquerda da grade. */
    public void desenharTextos(SpriteBatch lote, BitmapFont fonte) {
        fonte.getData().setScale(1.1f);

        for (int c = 0; c < Mundo.TAMANHO; c++) {
            fonte.draw(lote, String.valueOf(c), x(c) + LADO / 2 - 5, MARGEM_Y - 12);
        }
        for (int l = 0; l < Mundo.TAMANHO; l++) {
            fonte.draw(lote, String.valueOf(l), MARGEM_X - 25, y(l) + LADO / 2 + 7);
        }
    }
}
