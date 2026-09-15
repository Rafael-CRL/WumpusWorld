package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Disposable;
import wumpusworld.aplicacao.ControladorJogo;
import wumpusworld.dominio.AgenteInteligente;
import wumpusworld.dominio.Mundo;

/** Desenha apenas o estado visível. As coordenadas do domínio começam no alto à esquerda. */
final class TabuleiroActor extends Widget implements Disposable {
    private static final Color OCULTA = Color.valueOf("192736");
    private static final Color VISITADA = Color.valueOf("203C43");
    private final ShapeRenderer formas = new ShapeRenderer();
    private final ControladorJogo controlador;
    private final BitmapFont fonte;
    private boolean revelar;
    private float linhaVisual;
    private float colunaVisual;
    private float origemLinha;
    private float origemColuna;
    private int destinoLinha;
    private int destinoColuna;
    private float progresso = 1;

    TabuleiroActor(ControladorJogo controlador, Tema tema) {
        this.controlador = controlador;
        fonte = tema.skin.getFont("pequena");
    }

    void setRevelar(boolean revelar) { this.revelar = revelar; }

    void reiniciar() {
        revelar = false;
        linhaVisual = colunaVisual = origemLinha = origemColuna = 0;
        destinoLinha = destinoColuna = 0;
        progresso = 1;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        AgenteInteligente agente = controlador.getPartida().getAgente();
        if (agente.getLinha() != destinoLinha || agente.getColuna() != destinoColuna) {
            origemLinha = linhaVisual;
            origemColuna = colunaVisual;
            destinoLinha = agente.getLinha();
            destinoColuna = agente.getColuna();
            progresso = 0;
        }
        progresso = Math.min(1, progresso + delta / 0.16f);
        linhaVisual = Interpolation.smooth.apply(origemLinha, destinoLinha, progresso);
        colunaVisual = Interpolation.smooth.apply(origemColuna, destinoColuna, progresso);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        Mundo mundo = controlador.getPartida().getMundo();
        AgenteInteligente agente = controlador.getPartida().getAgente();
        boolean mapaAberto = revelar || controlador.getPartida().getEstado().terminou();
        float lado = Math.min(getWidth(), getHeight());
        float passo = lado / Mundo.TAMANHO;
        float x = getX() + (getWidth() - lado) / 2;
        float y = getY() + (getHeight() - lado) / 2;
        batch.end();
        formas.setProjectionMatrix(batch.getProjectionMatrix());
        formas.setTransformMatrix(batch.getTransformMatrix());
        formas.begin(ShapeRenderer.ShapeType.Filled);
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                float cx = x + coluna * passo + 3;
                float cy = y + (Mundo.TAMANHO - 1 - linha) * passo + 3;
                float tamanho = passo - 6;
                boolean visitada = mundo.foiVisitada(linha, coluna);
                formas.setColor(visitada ? VISITADA : OCULTA);
                formas.rect(cx, cy, tamanho, tamanho);
                if (linha == agente.getLinha() && coluna == agente.getColuna()) {
                    formas.setColor(agente.estaVivo() ? Tema.VERDE : Tema.PERIGO);
                    formas.rect(cx, cy, tamanho, 3);
                }
                if (linha == 0 && coluna == 0) {
                    base(cx + tamanho / 2, cy + tamanho / 2, passo);
                }
                if (mapaAberto || visitada) {
                    elemento(mundo.getElemento(linha, coluna), cx + tamanho / 2, cy + tamanho / 2, passo);
                } else {
                    formas.setColor(Tema.BORDA);
                    formas.circle(cx + tamanho / 2, cy + tamanho / 2, 3, 12);
                }
            }
        }
        float ax = x + (colunaVisual + 0.5f) * passo;
        float ay = y + (Mundo.TAMANHO - 0.5f - linhaVisual) * passo;
        explorador(ax, ay, passo, agente.estaVivo(), agente.possuiOuro());
        formas.end();
        batch.begin();
        fonte.setColor(Tema.SUAVE);
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                fonte.draw(batch, linha + "," + coluna, x + coluna * passo + 11,
                        y + (Mundo.TAMANHO - linha) * passo - 10);
            }
        }
        fonte.setColor(Color.WHITE);
    }

    private void base(float x, float y, float passo) {
        float r = passo * 0.22f;
        formas.setColor(Tema.BORDA);
        formas.triangle(x - r, y - r * 0.6f, x + r, y - r * 0.6f, x, y + r);
        formas.setColor(Tema.VERDE);
        formas.rect(x - 3, y - r * 0.6f, 6, r);
    }

    private void elemento(char elemento, float x, float y, float passo) {
        float r = passo * 0.18f;
        switch (elemento) {
            case Mundo.POCO -> {
                formas.setColor(Tema.BORDA);
                formas.ellipse(x - r * 1.3f, y - r * 0.7f, r * 2.6f, r * 1.4f, 32);
                formas.setColor(Tema.FUNDO);
                formas.ellipse(x - r, y - r * 0.5f, r * 2, r, 32);
            }
            case Mundo.OURO -> {
                formas.setColor(Tema.OURO);
                formas.triangle(x - r, y, x, y + r * 1.3f, x, y - r * 1.3f);
                formas.setColor(Color.valueOf("BE8C38"));
                formas.triangle(x, y + r * 1.3f, x + r, y, x, y - r * 1.3f);
            }
            case Mundo.WUMPUS -> {
                formas.setColor(Tema.PERIGO);
                formas.circle(x, y, r, 24);
                formas.triangle(x - r, y, x - r * 1.2f, y + r * 1.5f, x - r * 0.2f, y + r * 0.7f);
                formas.triangle(x + r, y, x + r * 1.2f, y + r * 1.5f, x + r * 0.2f, y + r * 0.7f);
                formas.setColor(Tema.FUNDO);
                formas.circle(x - r * 0.4f, y + 2, 3, 12);
                formas.circle(x + r * 0.4f, y + 2, 3, 12);
            }
            default -> { }
        }
    }

    private void explorador(float x, float y, float passo, boolean vivo, boolean ouro) {
        float r = passo * 0.17f;
        formas.setColor(Tema.FUNDO);
        formas.circle(x, y, r * 1.35f, 32);
        formas.setColor(vivo ? Tema.VERDE : Tema.PERIGO);
        if (!vivo) {
            formas.rectLine(x - r * 0.6f, y - r * 0.6f, x + r * 0.6f, y + r * 0.6f, 5);
            formas.rectLine(x - r * 0.6f, y + r * 0.6f, x + r * 0.6f, y - r * 0.6f, 5);
            return;
        }
        formas.circle(x, y + r * 0.4f, r * 0.7f, 24);
        formas.rect(x - r * 0.7f, y - r * 0.9f, r * 1.4f, r * 0.9f);
        formas.setColor(Tema.FUNDO);
        formas.rect(x - r * 0.4f, y + r * 0.25f, r * 0.8f, r * 0.25f);
        formas.setColor(ouro ? Tema.OURO : Tema.TEXTO);
        formas.circle(x, y + r, 3, 12);
    }

    @Override
    public float getPrefWidth() { return 500; }
    @Override
    public float getPrefHeight() { return 500; }
    @Override
    public void dispose() { formas.dispose(); }
}
