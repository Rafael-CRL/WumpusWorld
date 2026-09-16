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

    public Ativos() {
        FreeTypeFontGenerator geradorRegular = new FreeTypeFontGenerator(
                Gdx.files.internal("fontes/DejaVuSans.ttf"));
        FreeTypeFontGenerator geradorNegrito = new FreeTypeFontGenerator(
                Gdx.files.internal("fontes/DejaVuSans-Bold.ttf"));

        try {
            fonteTitulo = gerar(geradorNegrito, 30, 0f, null);
            fonteBanner = gerar(geradorNegrito, 46, 0f, null);
            fonteValor = gerar(geradorNegrito, 21, 0f, null);
            fonteSecao = gerar(geradorNegrito, 14, 1.4f, null);
            fonteTextoForte = gerar(geradorNegrito, 15, 0f, null);
            fonteSubtitulo = gerar(geradorRegular, 14, 0.4f, null);
            fonteTexto = gerar(geradorRegular, 15, 0f, null);
            fontePequena = gerar(geradorRegular, 13, 0f, null);
            fonteMiuda = gerar(geradorNegrito, 11, 0.8f, null);
        } finally {
            geradorRegular.dispose();
            geradorNegrito.dispose();
        }
    }

    private static BitmapFont gerar(FreeTypeFontGenerator gerador, int tamanho,
            float espacamento, Color cor) {
        FreeTypeFontParameter parametro = new FreeTypeFontParameter();
        parametro.size = tamanho;
        parametro.characters = CARACTERES;
        parametro.hinting = FreeTypeFontGenerator.Hinting.Full;
        parametro.minFilter = Texture.TextureFilter.Linear;
        parametro.magFilter = Texture.TextureFilter.Linear;
        parametro.spaceX = Math.round(espacamento);
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
        fonteBanner.dispose();    }
}
