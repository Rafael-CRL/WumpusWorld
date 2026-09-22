package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Posicao;

/**
 * Espelha o estado da partida em texto e explica os símbolos da grade.
 *
 * <p>São duas leituras do mesmo quadro: em cima da grade, o que está
 * acontecendo agora; ao lado, o que cada forma desenhada significa. Nenhuma
 * das duas tem interação.</p>
 */
public class PainelInformacoes {

    private static final float TEXTO_X = 50f;
    private static final float Y_SITUACAO = 680f;
    private static final float Y_ESTADO = 650f;
    private static final float Y_INVENTARIO = 620f;
    private static final float Y_PERCEPCOES = 590f;

    private static final float LEGENDA_X = 600f;
    private static final float LEGENDA_Y = 560f;
    private static final float LEGENDA_LARGURA = 370f;
    private static final float LEGENDA_ALTURA = 120f;

    /** A caixa da legenda e os símbolos dentro dela, nas mesmas cores da grade. */
    public void desenharFormas(ShapeRenderer formas) {
        formas.setColor(0.25f, 0.25f, 0.25f, 1);
        formas.rect(LEGENDA_X, LEGENDA_Y, LEGENDA_LARGURA, LEGENDA_ALTURA);

        formas.setColor(Color.BLUE);
        formas.circle(LEGENDA_X + 25, LEGENDA_Y + 95, 10);

        formas.setColor(Color.BLACK);
        formas.circle(LEGENDA_X + 25, LEGENDA_Y + 60, 10);

        formas.setColor(Color.RED);
        formas.circle(LEGENDA_X + 25, LEGENDA_Y + 25, 10);

        formas.setColor(Color.YELLOW);
        formas.rect(LEGENDA_X + 195, LEGENDA_Y + 85, 18, 18);

        formas.setColor(Color.GREEN);
        formas.rect(LEGENDA_X + 200, LEGENDA_Y + 50, 8, 20);
    }

    public void desenharTextos(SpriteBatch lote, BitmapFont fonte, Partida partida) {
        Posicao posAgente = partida.getAgente().getPosicao();

        fonte.getData().setScale(1.2f);
        fonte.draw(lote, "Situação: " + partida.getSituacao().getTitulo(), TEXTO_X, Y_SITUACAO);
        fonte.draw(lote, "Posição Atual: " + posAgente
                + " | Movimentos: " + partida.getAgente().getQuantidadeDeMovimentos()
                + " | Pontos: " + partida.getAgente().getPontuacao(), TEXTO_X, Y_ESTADO);

        String inventario = "Ouro: " + (partida.getAgente().possuiOuro() ? "Sim" : "Não")
                + " | Flecha: " + (partida.getAgente().possuiFlecha() ? "Sim" : "Não");
        fonte.draw(lote, inventario, TEXTO_X, Y_INVENTARIO);

        if (partida.getAgente().estaVivo()) {
            String percepcoesStr = partida.getPercepcoesAtuais().descricao();
            if (percepcoesStr.isEmpty()) {
                percepcoesStr = "Nenhuma";
            }
            int perigo = partida.getAgente().getRisco(posAgente.linha(), posAgente.coluna());
            fonte.draw(lote, "Percepções: " + percepcoesStr + " | Perigo: " + perigo,
                    TEXTO_X, Y_PERCEPCOES);
        }

        desenharRotulosDaLegenda(lote, fonte);
    }

    private void desenharRotulosDaLegenda(SpriteBatch lote, BitmapFont fonte) {
        fonte.getData().setScale(1.0f);
        fonte.draw(lote, "Agente", LEGENDA_X + 45, LEGENDA_Y + 100);
        fonte.draw(lote, "Poço", LEGENDA_X + 45, LEGENDA_Y + 65);
        fonte.draw(lote, "Wumpus", LEGENDA_X + 45, LEGENDA_Y + 30);

        fonte.draw(lote, "Ouro", LEGENDA_X + 225, LEGENDA_Y + 100);
        fonte.draw(lote, "Flecha", LEGENDA_X + 225, LEGENDA_Y + 65);
    }
}
