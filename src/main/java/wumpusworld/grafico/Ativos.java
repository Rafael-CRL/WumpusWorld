package wumpusworld.grafico;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

/**
 * Recursos gráficos da aplicação, criados uma única vez e liberados no fim.
 *
 * <p>Os únicos recursos externos usados no desenho são duas fontes TrueType
 * (DejaVu Sans, licença livre), guardadas em {@code assets/fontes}. Todo o
 * restante — o tabuleiro, o agente, os poços, o Wumpus e o ouro — é desenhado
 * por geometria em tempo de execução, o que mantém o pacote leve e faz a
 * imagem continuar nítida em qualquer resolução de janela.</p>
 */
public final class Ativos implements Disposable {

    /** Letras, números, pontuação e a acentuação do português. */
    private static final String CARACTERES =
            FreeTypeFontGenerator.DEFAULT_CHARS
            + "ÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇÑ"
            + "áàâãäéèêëíìîïóòôõöúùûüçñ"
            + "ºª°•–—→←↑↓“”‘’…−×÷±≤≥";

    public final BitmapFont fonteTitulo;
    public final BitmapFont fonteSubtitulo;
    public final BitmapFont fonteSecao;
    public final BitmapFont fonteTexto;
    public final BitmapFont fonteTextoForte;
    public final BitmapFont fonteValor;
    public final BitmapFont fontePequena;
    public final BitmapFont fonteMiuda;
    public final BitmapFont fonteBanner;
    public final Texture agente;
    public final Texture poco;
    public final Texture wumpus;
    public final Texture trofeu;
    public final Texture tesouro;

    public Ativos() {
        FreeTypeFontGenerator gerador = new FreeTypeFontGenerator(
                Gdx.files.internal("fontes/DejaVuSans.ttf"));

        try {
            fonteTitulo = gerar(gerador, 28, null);
            fonteBanner = gerar(gerador, 28, null);
            fonteValor = gerar(gerador, 20, null);
            fonteSecao = gerar(gerador, 15, null);
            fonteTextoForte = gerar(gerador, 15, null);
            fonteSubtitulo = gerar(gerador, 15, null);
            fonteTexto = gerar(gerador, 15, null);
            fontePequena = gerar(gerador, 15, null);
            fonteMiuda = gerar(gerador, 15, null);
            agente = carregarTextura("sprites/agente.png");
            poco = carregarTextura("sprites/poco.png");
            wumpus = carregarTextura("sprites/wumpus.png");
            trofeu = carregarTextura("sprites/trofeu.png");
            tesouro = carregarTextura("sprites/tesouro.png");
        } finally {
            gerador.dispose();
        }
    }

    private static Texture carregarTextura(String caminho) {
        Texture textura = new Texture(Gdx.files.internal(caminho));
        textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return textura;
    }

    private static BitmapFont gerar(FreeTypeFontGenerator gerador, int tamanho,
            Color cor) {
        FreeTypeFontParameter parametro = new FreeTypeFontParameter();
        parametro.size = tamanho;
        parametro.characters = CARACTERES;
        parametro.hinting = FreeTypeFontGenerator.Hinting.Full;
        parametro.minFilter = Texture.TextureFilter.Linear;
        parametro.magFilter = Texture.TextureFilter.Linear;
        parametro.spaceX = 0;
        parametro.genMipMaps = false;

        BitmapFont fonte = gerador.generateFont(parametro);
        fonte.setUseIntegerPositions(false);
        if (cor != null) {
            fonte.setColor(cor);
        }
        return fonte;
    }

    @Override
    public void dispose() {
        fonteTitulo.dispose();
        fonteSubtitulo.dispose();
        fonteSecao.dispose();
        fonteTexto.dispose();
        fonteTextoForte.dispose();
        fonteValor.dispose();
        fontePequena.dispose();
        fonteMiuda.dispose();
        fonteBanner.dispose();
        agente.dispose();
        poco.dispose();
        wumpus.dispose();
        trofeu.dispose();
        tesouro.dispose();
    }
}
