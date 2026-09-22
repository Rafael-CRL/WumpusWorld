package wumpusworld.grafico;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;

/**
 * Conduz o quadro: mede o tempo, pede os passos e coordena os três painéis.
 *
 * <p>Esta classe não guarda nada sobre o jogo — posição, pontuação e mapa
 * vivem na {@link Partida}. É por isso que reiniciar consiste apenas em
 * trocar a partida por outra.</p>
 */
public class TelaDaPartida extends ApplicationAdapter {

    /** Dimensões da janela. O {@code Main} as usa para abri-la; o clique, para converter o eixo Y. */
    public static final int LARGURA_DA_JANELA = 1000;
    public static final int ALTURA_DA_JANELA = 700;

    private static final float INTERVALO_DO_PASSO = 0.5f;

    private final PainelTabuleiro painelTabuleiro = new PainelTabuleiro();
    private final PainelInformacoes painelInformacoes = new PainelInformacoes();
    private final PainelHistorico painelHistorico = new PainelHistorico();

    private SpriteBatch lote;
    private ShapeRenderer formas;
    private Fontes fontes;
    private Partida partida;

    /**
     * Cópia do mapa como ele era antes do primeiro passo. O ouro e a flecha
     * são apagados do mapa vivo ao serem recolhidos; esta cópia é o que
     * permite mostrá-los nos lugares originais na revelação final.
     */
    private Mundo mapaInicial;

    private float tempo;

    @Override
    public void create() {
        this.lote = new SpriteBatch();
        this.formas = new ShapeRenderer();
        this.fontes = new Fontes();
        iniciarNovaPartida();

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (!partida.getSituacao().encerrada()) {
                    return false;
                }
                // O mouse chega com o Y crescendo para baixo; o desenho usa o inverso.
                if (painelHistorico.cliqueNoBotao(screenX, ALTURA_DA_JANELA - screenY)) {
                    iniciarNovaPartida();
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                painelHistorico.rolar(amountY);
                return true;
            }
        });
    }

    private void iniciarNovaPartida() {
        this.partida = new Partida();
        this.mapaInicial = partida.getMundo().reiniciar();
        this.tempo = 0f;
        painelHistorico.reiniciarRolagem();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(Paleta.FUNDO);

        // A janela se redesenha a cada quadro; o jogo avança a cada meio segundo.
        if (!partida.getSituacao().encerrada()) {
            tempo += delta;
            if (tempo >= INTERVALO_DO_PASSO) {
                partida.executarPasso();
                tempo = 0f;
            }
        }

        // Os dois pincéis não podem estar abertos ao mesmo tempo: primeiro as
        // formas, depois os textos.
        formas.begin(ShapeRenderer.ShapeType.Filled);
        painelTabuleiro.desenharFormas(formas, partida, mapaInicial);
        painelInformacoes.desenharFormas(formas);
        painelHistorico.desenharFormas(formas, partida);
        formas.end();

        if (partida.getSituacao().encerrada()) {
            formas.begin(ShapeRenderer.ShapeType.Line);
            painelHistorico.desenharContornoDoBotao(formas);
            formas.end();
        }

        lote.begin();
        painelTabuleiro.desenharTextos(lote, fontes);
        painelInformacoes.desenharTextos(lote, fontes, partida);
        painelHistorico.desenharTextos(lote, fontes, partida);
        lote.end();
    }

    @Override
    public void dispose() {
        fontes.dispose();
        lote.dispose();
        formas.dispose();
    }
}
