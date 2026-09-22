package wumpusworld.grafico;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wumpusworld.nucleo.Partida;

/**
 * Coluna da direita: o registro rolante, o desfecho e o botão de reinício.
 *
 * <p>O botão mora aqui, e não numa classe própria, porque divide o espaço com
 * a lista: quando a partida termina, o histórico encolhe por cima para abrir
 * lugar ao desfecho e ao botão. As duas metades da conta estão lado a lado.</p>
 *
 * <p>O deslocamento da rolagem é o único estado guardado por um painel — é
 * estado da leitura, não da partida.</p>
 */
public class PainelHistorico {

    private static final float BOTAO_X = 670f;
    private static final float BOTAO_Y = 20f;
    private static final float BOTAO_LARGURA = 230f;
    private static final float BOTAO_ALTURA = 50f;

    private static final float COLUNA_X = 600f;
    private static final float LARGURA_DO_TEXTO = 370f;
    private static final float Y_DO_RESULTADO = 495f;
    private static final float Y_DO_TITULO = 530f;

    /** Limites da lista: a primeira coluna vale com o botão na tela; a segunda, sem. */
    private static final float BASE_COM_BOTAO = 90f;
    private static final float BASE_SEM_BOTAO = 30f;
    private static final float TOPO_COM_BOTAO = 410f;
    private static final float TOPO_SEM_BOTAO = 500f;
    private static final float CORTE_COM_BOTAO = 85f;
    private static final float CORTE_SEM_BOTAO = 0f;

    private static final float PASSO_DA_ROLAGEM = 40f;
    private static final float ESPACO_ENTRE_ENTRADAS = 10f;

    private float scrollY = 0f;

    public void rolar(float quantidade) {
        scrollY += quantidade * PASSO_DA_ROLAGEM;
        if (scrollY < 0) {
            scrollY = 0;
        }
    }

    public void reiniciarRolagem() {
        scrollY = 0f;
    }

    /** Recebe o ponto já convertido para o eixo do desenho. */
    public boolean cliqueNoBotao(float x, float y) {
        return x >= BOTAO_X && x <= BOTAO_X + BOTAO_LARGURA
                && y >= BOTAO_Y && y <= BOTAO_Y + BOTAO_ALTURA;
    }

    public void desenharFormas(ShapeRenderer formas, Partida partida) {
        if (partida.getSituacao().encerrada()) {
            formas.setColor(0.15f, 0.55f, 0.25f, 1f);
            formas.rect(BOTAO_X, BOTAO_Y, BOTAO_LARGURA, BOTAO_ALTURA);
        }
    }

    /** Chamado na passagem de contorno, separada da passagem preenchida. */
    public void desenharContornoDoBotao(ShapeRenderer formas) {
        formas.setColor(0.35f, 0.85f, 0.45f, 1f);
        formas.rect(BOTAO_X, BOTAO_Y, BOTAO_LARGURA, BOTAO_ALTURA);
    }

    public void desenharTextos(SpriteBatch lote, BitmapFont fonte, Partida partida) {
        boolean encerrada = partida.getSituacao().encerrada();

        if (encerrada) {
            fonte.getData().setScale(1.1f);
            GlyphLayout resultado = new GlyphLayout(fonte,
                    partida.getDescricaoResultado(), Color.WHITE, LARGURA_DO_TEXTO, -1, true);
            fonte.draw(lote, resultado, COLUNA_X, Y_DO_RESULTADO);
        }

        if (encerrada) {
            fonte.getData().setScale(1.15f);
            GlyphLayout layoutBotao = new GlyphLayout(fonte, "JOGAR NOVAMENTE");
            float tx = BOTAO_X + (BOTAO_LARGURA - layoutBotao.width) / 2f;
            float ty = BOTAO_Y + (BOTAO_ALTURA + layoutBotao.height) / 2f;
            fonte.draw(lote, layoutBotao, tx, ty);
        }

        fonte.getData().setScale(1.1f);
        fonte.draw(lote, "HISTÓRICO DE EVENTOS:", COLUNA_X, Y_DO_TITULO);

        List<String> registro = partida.getRegistro();
        float topo = encerrada ? TOPO_COM_BOTAO : TOPO_SEM_BOTAO;
        float corte = encerrada ? CORTE_COM_BOTAO : CORTE_SEM_BOTAO;

        GlyphLayout layout = new GlyphLayout();
        float currentY = (encerrada ? BASE_COM_BOTAO : BASE_SEM_BOTAO) - scrollY;

        // As entradas mais recentes ficam embaixo, então a lista é montada de trás para frente.
        for (int i = registro.size() - 1; i >= 0; i--) {
            layout.setText(fonte, registro.get(i), Color.WHITE, LARGURA_DO_TEXTO, -1, true);

            float drawY = currentY + layout.height;
            if (drawY > topo) {
                break;
            }

            if (currentY >= corte) {
                fonte.draw(lote, layout, COLUNA_X, drawY);
            }

            currentY += (layout.height + ESPACO_ENTRE_ENTRADAS);
        }
    }
}
