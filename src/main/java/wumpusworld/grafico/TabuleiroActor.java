package wumpusworld.grafico;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;
import java.util.Locale;
import wumpusworld.aplicacao.ControladorJogo;
import wumpusworld.aplicacao.Disparo;
import wumpusworld.aplicacao.EstadoPartida;
import wumpusworld.aplicacao.Partida;
import wumpusworld.dominio.AgenteInteligente;
import wumpusworld.dominio.Mundo;

/**
 * Desenha apenas o estado visível e anima as mudanças. As coordenadas do domínio começam no alto
 * à esquerda. Efeitos como partículas e flash são disparados quando a animação do agente chega
 * à nova casa, sem interferir nas decisões.
 */
final class TabuleiroActor extends Widget implements Disposable {
    /** Fração do intervalo entre decisões usada para deslizar o agente até a nova casa. */
    private static final float FRACAO_DA_ANIMACAO = 0.7f;
    private static final float ALTURA_DO_PASSO = 0.06f;
    private static final float MARGEM_DA_CASA = 3;

    private final ShapeRenderer formas = new ShapeRenderer();
    private final ControladorJogo controlador;
    private final BitmapFont fonte;
    private final BitmapFont fonteDoResultado;
    private final Efeitos efeitos = new Efeitos();
    private boolean revelar;
    private float tempo;
    private float linhaVisual;
    private float colunaVisual;
    private float origemLinha;
    private float origemColuna;
    private int destinoLinha;
    private int destinoColuna;
    private float olhar;
    private float progresso = 1;
    private Disparo disparoExibido;
    private boolean ouroExibido;
    private EstadoPartida estadoExibido = EstadoPartida.EXPLORANDO;

    TabuleiroActor(ControladorJogo controlador, Tema tema) {
        this.controlador = controlador;
        fonte = tema.skin.getFont("pequena");
        fonteDoResultado = tema.skin.getFont("titulo");
    }

    void setRevelar(boolean revelar) { this.revelar = revelar; }

    void reiniciar() {
        revelar = false;
        linhaVisual = colunaVisual = origemLinha = origemColuna = 0;
        destinoLinha = destinoColuna = 0;
        olhar = 0;
        progresso = 1;
        disparoExibido = null;
        ouroExibido = false;
        estadoExibido = EstadoPartida.EXPLORANDO;
        efeitos.limpar();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        tempo += delta;
        acompanharAgente(delta);
        efeitos.atualizar(delta);
        notarEventos();
    }

    private void acompanharAgente(float delta) {
        AgenteInteligente agente = controlador.getPartida().getAgente();
        if (agente.getLinha() != destinoLinha || agente.getColuna() != destinoColuna) {
            origemLinha = linhaVisual;
            origemColuna = colunaVisual;
            olhar = Integer.signum(agente.getColuna() - destinoColuna);
            destinoLinha = agente.getLinha();
            destinoColuna = agente.getColuna();
            progresso = 0;
        }
        float duracao = controlador.getVelocidade().getIntervalo() * FRACAO_DA_ANIMACAO;
        progresso = Math.min(1, progresso + delta / duracao);
        linhaVisual = Interpolation.smooth.apply(origemLinha, destinoLinha, progresso);
        colunaVisual = Interpolation.smooth.apply(origemColuna, destinoColuna, progresso);
    }

    /** Converte mudanças do estado em efeitos: a flecha sai na hora; o resto, quando o agente chega. */
    private void notarEventos() {
        Partida partida = controlador.getPartida();
        Disparo disparo = partida.getDisparo();
        if (disparo != null && disparo != disparoExibido) {
            efeitos.disparou(disparo);
        }
        disparoExibido = disparo;
        if (progresso < 1) return;

        AgenteInteligente agente = partida.getAgente();
        if (agente.possuiOuro() && !ouroExibido) {
            efeitos.coletouOuro(agente.getLinha(), agente.getColuna());
        }
        ouroExibido = agente.possuiOuro();
        EstadoPartida estado = partida.getEstado();
        if (estado != estadoExibido) {
            estadoExibido = estado;
            anunciarResultado(estado, agente);
        }
    }

    private void anunciarResultado(EstadoPartida estado, AgenteInteligente agente) {
        if (!estado.terminou()) return;
        String titulo = estado.getTitulo().toUpperCase(Locale.ROOT);
        switch (estado) {
            case VITORIA -> {
                efeitos.venceu(0, 0);
                efeitos.anunciar(titulo, Tema.VERDE);
            }
            case MORTE -> {
                efeitos.morreu(agente.getLinha(), agente.getColuna());
                efeitos.anunciar(titulo, Tema.PERIGO);
            }
            default -> efeitos.anunciar(titulo, Tema.OURO);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        Mundo mundo = controlador.getPartida().getMundo();
        AgenteInteligente agente = controlador.getPartida().getAgente();
        float lado = Math.min(getWidth(), getHeight());
        float passo = lado / Mundo.TAMANHO;
        Grade grade = new Grade(getX() + (getWidth() - lado) / 2 + efeitos.tremorX(passo),
                getY() + (getHeight() - lado) / 2 + efeitos.tremorY(passo), passo);
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        formas.setProjectionMatrix(batch.getProjectionMatrix());
        formas.setTransformMatrix(batch.getTransformMatrix());
        formas.begin(ShapeRenderer.ShapeType.Filled);
        desenharCasas(mundo, agente, grade);
        desenharAgente(agente, grade);
        efeitos.desenharPartes(formas, grade);
        efeitos.desenharCobertura(formas, grade);
        formas.end();
        batch.begin();
        desenharRotulos(batch, grade);
        efeitos.desenharTexto(batch, fonteDoResultado, grade);
    }

    private void desenharCasas(Mundo mundo, AgenteInteligente agente, Grade grade) {
        boolean mapaAberto = revelar || estadoExibido.terminou();
        boolean chamando = ouroExibido && !estadoExibido.terminou();
        float tamanho = grade.passo() - 2 * MARGEM_DA_CASA;
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                float x = grade.centroX(coluna) - grade.passo() / 2 + MARGEM_DA_CASA;
                float y = grade.centroY(linha) - grade.passo() / 2 + MARGEM_DA_CASA;
                float cx = x + tamanho / 2;
                float cy = y + tamanho / 2;
                boolean visitada = mundo.foiVisitada(linha, coluna);
                Sprites.casa(formas, x, y, tamanho, visitada);
                if (linha == agente.getLinha() && coluna == agente.getColuna()) {
                    formas.setColor(agente.estaVivo() ? Tema.VERDE : Tema.PERIGO);
                    formas.rect(x, y, tamanho, 3);
                }
                if (linha == 0 && coluna == 0) {
                    Sprites.base(formas, cx, cy, grade.passo(), tempo, chamando);
                }
                if (mapaAberto || visitada) {
                    desenharElemento(mundo.getElemento(linha, coluna), cx, cy, grade.passo());
                } else {
                    Sprites.nevoa(formas, cx, cy);
                }
            }
        }
    }

    private void desenharElemento(char elemento, float x, float y, float passo) {
        switch (elemento) {
            case Mundo.POCO -> Sprites.poco(formas, x, y, passo, tempo);
            case Mundo.OURO -> Sprites.ouro(formas, x, y, passo, tempo);
            case Mundo.WUMPUS -> Sprites.wumpus(formas, x, y, passo, tempo);
            default -> { }
        }
    }

    private void desenharAgente(AgenteInteligente agente, Grade grade) {
        boolean vivo = agente.estaVivo() || progresso < 1;
        float andando = progresso < 1 ? MathUtils.sin(progresso * MathUtils.PI) : 0;
        float respiracao = 0.15f + 0.15f * MathUtils.sin(tempo * 3f);
        float salto = vivo ? (andando + respiracao * (1 - andando)) * grade.passo() * ALTURA_DO_PASSO : 0;
        Sprites.explorador(formas, grade.centroX(colunaVisual), grade.centroY(linhaVisual), grade.passo(),
                vivo, ouroExibido, salto, olhar);
    }

    private void desenharRotulos(Batch batch, Grade grade) {
        fonte.setColor(Tema.SUAVE);
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                fonte.draw(batch, linha + "," + coluna, grade.centroX(coluna) - grade.passo() / 2 + 11,
                        grade.centroY(linha) + grade.passo() / 2 - 10);
            }
        }
        fonte.setColor(Color.WHITE);
    }

    @Override
    public float getPrefWidth() { return 500; }
    @Override
    public float getPrefHeight() { return 500; }
    @Override
    public void dispose() { formas.dispose(); }
}
