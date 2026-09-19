package wumpusworld.grafico;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import wumpusworld.nucleo.AgenteInteligente;
import wumpusworld.nucleo.DisparoDeFlecha;
import wumpusworld.nucleo.EventoJogo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Posicao;
import wumpusworld.nucleo.Situacao;
import wumpusworld.nucleo.TipoEvento;

/**
 * Tela principal: monta o layout, controla o relógio da simulação e desenha.
 *
 * <h2>Movimento automático</h2>
 * A janela nunca é bloqueada esperando o agente. A cada quadro o tempo real
 * decorrido é somado a um relógio; quando ele atinge o intervalo configurado,
 * a tela pede <em>um</em> passo à {@link Partida} e reinicia a contagem. Entre
 * dois passos, a posição desenhada do agente é interpolada entre a casa de
 * origem e a de destino, de modo que cada movimento possa ser acompanhado com
 * os olhos. É o equivalente, em libGDX, ao {@code javax.swing.Timer} sugerido
 * no enunciado.
 *
 * <h2>Separação de responsabilidades</h2>
 * Esta classe lê o estado da partida e o traduz em pixels. Ela não consulta o
 * conteúdo das casas para decidir nada pelo agente: desenha apenas o que já é
 * conhecido, e só revela o mapa inteiro quando a partida termina.
 */
public final class TelaDaPartida extends ScreenAdapter {

    /** Resolução de projeto: todo o layout é descrito nestas unidades. */
    public static final float LARGURA_VIRTUAL = 1400f;
    public static final float ALTURA_VIRTUAL = 900f;

    private static final float MARGEM = 24f;
    private static final float ESPACO = 16f;
    private static final float ALTURA_DA_BARRA = 78f;
    private static final float ALTURA_DO_RODAPE = 100f;
    private static final float LARGURA_DO_TABULEIRO = 660f;

    /** Intervalo padrão entre dois passos do agente, em segundos. */
    private static final float INTERVALO_BASE = 0.62f;

    private static final float[] VELOCIDADES =
        {0.25f, 0.5f, 1f, 1.5f, 2f, 3f, 4f};
    private static final int VELOCIDADE_PADRAO = 2;

    private final Ativos ativos;
    private final SpriteBatch lote;
    private final ShapeRenderer formas;
    private final Viewport viewport;
    private final OrthographicCamera camera;

    private final PainelTabuleiro painelTabuleiro;
    private final PainelStatus painelStatus;
    private final PainelRegistro painelRegistro;
    private final PainelRodape painelRodape;
    private final BarraSuperior barraSuperior;
    private final CamadaResultado camadaResultado;
    private final Efeitos efeitos = new Efeitos();

    private final Random sorteador = new Random();
    private Partida partida = new Partida();

    private float tempo;
    private float relogioDoPasso;
    private float tempoDesdeOPasso = 999f;
    private int indiceDaVelocidade = VELOCIDADE_PADRAO;
    private EstadoDoJogo estado = EstadoDoJogo.PARADO;
    private boolean mostrarMapaDeRisco = true;

    /** Partículas de poeira que dão profundidade ao fundo. */
    private final float[] poeiraX = new float[46];
    private final float[] poeiraY = new float[46];
    private final float[] poeiraRaio = new float[46];
    private final float[] poeiraVelocidade = new float[46];
    private final float[] poeiraAlfa = new float[46];

    public TelaDaPartida(Ativos ativos, SpriteBatch lote, ShapeRenderer formas) {
        this.ativos = ativos;
        this.lote = lote;
        this.formas = formas;

        camera = new OrthographicCamera();
        viewport = new FitViewport(LARGURA_VIRTUAL, ALTURA_VIRTUAL, camera);

        painelTabuleiro = new PainelTabuleiro(ativos);
        painelStatus = new PainelStatus(ativos);
        painelRegistro = new PainelRegistro(ativos);
        painelRodape = new PainelRodape(ativos);
        barraSuperior = new BarraSuperior(ativos);
        camadaResultado = new CamadaResultado(ativos);

        lote.setBlendFunctionSeparate(
                GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
                GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

        prepararLayout();
        prepararPoeira();
        Gdx.input.setInputProcessor(criarControles());
    }

    // -----------------------------------------------------------------------
    //  Layout
    // -----------------------------------------------------------------------

    /**
     * Divide a resolução virtual entre os cinco blocos da interface:
     * cabeçalho, tabuleiro, informações, registro e rodapé.
     */
    private void prepararLayout() {
        float larguraUtil = LARGURA_VIRTUAL - MARGEM * 2f;

        Area barra = new Area(MARGEM, ALTURA_VIRTUAL - MARGEM - ALTURA_DA_BARRA,
                larguraUtil, ALTURA_DA_BARRA);
        Area rodape = new Area(MARGEM, MARGEM, larguraUtil, ALTURA_DO_RODAPE);

        float baseDoMeio = rodape.topo() + ESPACO;
        float alturaDoMeio = barra.y() - ESPACO - baseDoMeio;

        Area tabuleiro = new Area(MARGEM, baseDoMeio,
                LARGURA_DO_TABULEIRO, alturaDoMeio);

        float xDaLateral = tabuleiro.direita() + ESPACO;
        float larguraDaLateral = LARGURA_VIRTUAL - MARGEM - xDaLateral;
        float alturaDoStatus = 310f;

        Area status = new Area(xDaLateral,
                baseDoMeio + alturaDoMeio - alturaDoStatus,
                larguraDaLateral, alturaDoStatus);
        Area registro = new Area(xDaLateral, baseDoMeio,
                larguraDaLateral, alturaDoMeio - alturaDoStatus - ESPACO);

        barraSuperior.definirArea(barra);
        painelTabuleiro.definirArea(tabuleiro);
        painelStatus.definirArea(status);
        painelRegistro.definirArea(registro);
        painelRodape.definirArea(rodape);

        float alturaDoCartao = 300f;
        Area cartao = new Area(xDaLateral + 8f,
                baseDoMeio + (alturaDoMeio - alturaDoCartao) / 2f,
                larguraDaLateral - 16f, alturaDoCartao);
        camadaResultado.definirArea(cartao, LARGURA_VIRTUAL, ALTURA_VIRTUAL);
    }

    private void prepararPoeira() {
        for (int indice = 0; indice < poeiraX.length; indice++) {
            poeiraX[indice] = MathUtils.random(0f, LARGURA_VIRTUAL);
            poeiraY[indice] = MathUtils.random(0f, ALTURA_VIRTUAL);
            poeiraRaio[indice] = MathUtils.random(0.8f, 2.4f);
            poeiraVelocidade[indice] = MathUtils.random(3f, 14f);
            poeiraAlfa[indice] = MathUtils.random(0.05f, 0.18f);
        }
    }

    // -----------------------------------------------------------------------
    //  Controles de teclado (recurso extra; a simulação anda sozinha)
    // -----------------------------------------------------------------------

    private InputAdapter criarControles() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int tecla) {
                switch (tecla) {
                    case Input.Keys.SPACE -> alternarPausa();
                    case Input.Keys.ENTER, Input.Keys.NUMPAD_ENTER -> {
                        if (estado == EstadoDoJogo.JOGANDO
                                || estado == EstadoDoJogo.PAUSADO) {
                            estado = EstadoDoJogo.PAUSADO;
                            executarPasso();
                        }
                    }
                    case Input.Keys.PLUS, Input.Keys.EQUALS,
                            Input.Keys.NUMPAD_ADD -> indiceDaVelocidade =
                            Math.min(VELOCIDADES.length - 1, indiceDaVelocidade + 1);
                    case Input.Keys.MINUS, Input.Keys.NUMPAD_SUBTRACT ->
                            indiceDaVelocidade = Math.max(0, indiceDaVelocidade - 1);
                    case Input.Keys.R -> reiniciar();
                    case Input.Keys.H -> mostrarMapaDeRisco = !mostrarMapaDeRisco;
                    case Input.Keys.ESCAPE -> Gdx.app.exit();
                    default -> {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean touchDown(int x, int y, int ponteiro, int botao) {
                Vector2 ponto = viewport.unproject(new Vector2(x, y));
                switch (barraSuperior.acaoNoPonto(ponto.x, ponto.y, estado)) {
                    case JOGAR -> estado = EstadoDoJogo.JOGANDO;
                    case PAUSAR -> alternarPausa();
                    case REINICIAR -> reiniciar();
                    case NENHUMA -> {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    private void alternarPausa() {
        if (estado == EstadoDoJogo.JOGANDO) {
            estado = EstadoDoJogo.PAUSADO;
        } else if (estado == EstadoDoJogo.PAUSADO) {
            estado = EstadoDoJogo.JOGANDO;
        }
    }

    /** Sorteia uma nova fase e aguarda o início pelo botão Jogar. */
    private void reiniciar() {
        partida = Partida.comFaseSorteada(sorteador);

        relogioDoPasso = 0f;
        tempoDesdeOPasso = 999f;
        estado = EstadoDoJogo.PARADO;
        efeitos.limpar();
        camadaResultado.reiniciar();
        prepararLayout();
    }

    // -----------------------------------------------------------------------
    //  Ciclo de atualização e desenho
    // -----------------------------------------------------------------------

    @Override
    public void render(float delta) {
        float passoDeTempo = Math.min(delta, 1f / 20f);
        atualizar(passoDeTempo);
        desenhar();
    }

    private void atualizar(float delta) {
        tempo += delta;
        tempoDesdeOPasso += delta;
        efeitos.atualizar(delta);
        atualizarPoeira(delta);

        camadaResultado.atualizar(delta, estado == EstadoDoJogo.FIM);

        if (estado == EstadoDoJogo.JOGANDO) {
            relogioDoPasso += delta;
            if (relogioDoPasso >= intervaloAtual()) {
                relogioDoPasso = 0f;
                executarPasso();
            }
        }

        painelRegistro.atualizar(partida.getRegistro());
    }

    private void atualizarPoeira(float delta) {
        for (int indice = 0; indice < poeiraX.length; indice++) {
            poeiraY[indice] += poeiraVelocidade[indice] * delta;
            if (poeiraY[indice] > ALTURA_VIRTUAL) {
                poeiraY[indice] = -4f;
                poeiraX[indice] = MathUtils.random(0f, LARGURA_VIRTUAL);
            }
        }
    }

    private float intervaloAtual() {
        return INTERVALO_BASE / VELOCIDADES[indiceDaVelocidade];
    }

    private float duracaoDoDisparo() {
        return partida.getUltimoDisparo() == null
                ? 0f : Math.min(0.40f, intervaloAtual() * 0.50f);
    }

    private float duracaoDoMovimento() {
        return Math.min(0.30f, intervaloAtual() * 0.45f);
    }

    private void executarPasso() {
        if (!partida.executarPasso()) {
            return;
        }
        tempoDesdeOPasso = 0f;
        reagirAoPasso();
        if (partida.getSituacao().encerrada()) {
            estado = EstadoDoJogo.FIM;
        }
    }

    /** Traduz os acontecimentos do passo em efeitos visuais. */
    private void reagirAoPasso() {
        Posicao destino = partida.getDestinoDoPasso();
        float centroX = painelTabuleiro.centroX(destino.coluna());
        float centroY = painelTabuleiro.centroY(destino.linha());
        float lado = painelTabuleiro.getLado();

        for (EventoJogo evento : partida.getEventosDoUltimoPasso()) {
            if (evento.tipo() == TipoEvento.TESOURO) {
                efeitos.clarao(Paleta.OURO, 0.22f);
                efeitos.onda(centroX, centroY, lado * 1.5f, Paleta.OURO, 0.9f);
                efeitos.textoFlutuante("+" + AgenteInteligente.BONUS_OURO,
                        centroX, centroY + lado * 0.4f, Paleta.OURO);
            }
        }

        DisparoDeFlecha disparo = partida.getUltimoDisparo();
        if (disparo != null && disparo.acertou()) {
            float alvoX = painelTabuleiro.centroX(disparo.destino().coluna());
            float alvoY = painelTabuleiro.centroY(disparo.destino().linha());
            efeitos.tremer(4.5f);
            efeitos.onda(alvoX, alvoY, lado * 1.3f, Paleta.ALERTA, 0.7f);
            efeitos.textoFlutuante("+" + AgenteInteligente.BONUS_WUMPUS,
                    alvoX, alvoY + lado * 0.4f, Paleta.ALERTA);
        }

        switch (partida.getSituacao()) {
            case MORTE -> {
                efeitos.tremer(11f);
                efeitos.clarao(Paleta.PERIGO, 0.45f);
                efeitos.onda(centroX, centroY, lado * 2.0f, Paleta.PERIGO, 1.1f);
                efeitos.textoFlutuante(
                        String.valueOf(AgenteInteligente.PENALIDADE_MORTE),
                        centroX, centroY + lado * 0.4f, Paleta.PERIGO);
            }
            case VITORIA -> {
                efeitos.clarao(Paleta.SUCESSO, 0.32f);
                efeitos.onda(centroX, centroY, lado * 2.2f, Paleta.SUCESSO, 1.2f);
                efeitos.textoFlutuante("+" + AgenteInteligente.BONUS_VITORIA,
                        centroX, centroY + lado * 0.4f, Paleta.SUCESSO);
            }
            default -> {
                // Passo comum: nenhum efeito especial.
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Desenho
    // -----------------------------------------------------------------------

    private void desenhar() {
        EstadoDaAnimacao animacao = montarEstadoDaAnimacao();

        Gdx.gl.glClearColor(Paleta.FUNDO_BAIXO.r, Paleta.FUNDO_BAIXO.g,
                Paleta.FUNDO_BAIXO.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        // Passagem 1: todas as formas, com o tremor aplicado à câmera.
        habilitarMistura();
        posicionarCamera(efeitos.getDeslocamentoX(), efeitos.getDeslocamentoY());
        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);

        desenharFundo();
        barraSuperior.desenharFormas(formas, estado);
        painelTabuleiro.desenharFormas(formas, partida, animacao);
        painelStatus.desenharFormas(formas, partida, animacao);
        painelRegistro.desenharFormas(formas);
        painelRodape.desenharFormas(formas, tempo);
        efeitos.desenharOndas(formas);

        formas.end();

        // Passagem 2: todos os textos.
        lote.setProjectionMatrix(camera.combined);
        lote.begin();

        barraSuperior.desenharTextos(lote, estado);
        painelTabuleiro.desenharTextos(lote, partida, animacao);
        painelStatus.desenharTextos(lote, partida);
        painelRegistro.desenharTextos(lote, partida.getRegistro().size());
        painelRodape.desenharTextos(lote);
        efeitos.desenharTextos(lote, ativos.fonteValor);

        lote.end();

        // Passagem 3: clarão e cartão de encerramento, já sem tremor.
        habilitarMistura();
        posicionarCamera(0f, 0f);
        formas.setProjectionMatrix(camera.combined);
        formas.begin(ShapeRenderer.ShapeType.Filled);
        efeitos.desenharClarao(formas, LARGURA_VIRTUAL, ALTURA_VIRTUAL);
        camadaResultado.desenharFormas(formas, partida, tempo);
        formas.end();

        if (camadaResultado.estaVisivel()) {
            lote.setProjectionMatrix(camera.combined);
            lote.begin();
            camadaResultado.desenharTextos(lote, partida);
            lote.end();
        }
    }

    private EstadoDaAnimacao montarEstadoDaAnimacao() {
        float duracaoDoDisparo = duracaoDoDisparo();
        float progressoDoDisparo = duracaoDoDisparo <= 0f ? 1f
                : MathUtils.clamp(tempoDesdeOPasso / duracaoDoDisparo, 0f, 1f);
        float progressoDoPasso = MathUtils.clamp(
                (tempoDesdeOPasso - duracaoDoDisparo) / duracaoDoMovimento(),
                0f, 1f);

        return new EstadoDaAnimacao(tempo, progressoDoPasso, progressoDoDisparo,
                mostrarMapaDeRisco);
    }

    /**
     * Liga a mistura de transparência com a função padrão de composição.
     *
     * <p>É preciso repetir isso antes de cada passagem do {@link ShapeRenderer}:
     * o {@code SpriteBatch} desliga o {@code GL_BLEND} ao terminar, e sem ele
     * todas as cores translúcidas seriam escritas como se fossem opacas.</p>
     */
    private void habilitarMistura() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Cor: composição normal. Alfa: a função separada mantém o quadro
        // opaco, o que evita véus translúcidos "furarem" a imagem final.
        Gdx.gl.glBlendFuncSeparate(
                GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA,
                GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void posicionarCamera(float deslocamentoX, float deslocamentoY) {
        camera.position.set(LARGURA_VIRTUAL / 2f + deslocamentoX,
                ALTURA_VIRTUAL / 2f + deslocamentoY, 0f);
        camera.update();
    }

    /** Degradê da caverna, poeira em suspensão e vinheta nas bordas. */
    private void desenharFundo() {
        Desenho.degradeVertical(formas, 0f, 0f, LARGURA_VIRTUAL, ALTURA_VIRTUAL,
                Paleta.FUNDO_BAIXO, Paleta.FUNDO_ALTO);

        for (int indice = 0; indice < poeiraX.length; indice++) {
            formas.setColor(Paleta.comAlfa(Paleta.AGENTE_BRILHO,
                    poeiraAlfa[indice]));
            formas.circle(poeiraX[indice], poeiraY[indice],
                    poeiraRaio[indice], 10);
        }

        Color opaco = new Color(0f, 0f, 0f, 0.55f);
        Color transparente = new Color(0f, 0f, 0f, 0f);
        float faixa = 130f;
        formas.rect(0f, 0f, LARGURA_VIRTUAL, faixa,
                opaco, opaco, transparente, transparente);
        formas.rect(0f, ALTURA_VIRTUAL - faixa, LARGURA_VIRTUAL, faixa,
                transparente, transparente, opaco, opaco);
        formas.rect(0f, 0f, faixa, ALTURA_VIRTUAL,
                opaco, transparente, transparente, opaco);
        formas.rect(LARGURA_VIRTUAL - faixa, 0f, faixa, ALTURA_VIRTUAL,
                transparente, opaco, opaco, transparente);
    }

    @Override
    public void resize(int largura, int altura) {
        viewport.update(largura, altura, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    /** Situação atual, exposta para testes e para a barra de título da janela. */
    public Situacao getSituacao() {
        return partida.getSituacao();
    }
}
