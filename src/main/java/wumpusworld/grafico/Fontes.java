package wumpusworld.grafico;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;

/**
 * As três fontes da janela, geradas a partir dos arquivos em {@code assets/fontes}.
 *
 * <p>Cada fonte é rasterizada já no tamanho em que será usada. Ampliar uma
 * fonte pronta com {@code setScale} borra as letras; gerá-la no tamanho certo,
 * não.</p>
 */
public final class Fontes implements Disposable {

    private static final String REGULAR = "fontes/DejaVuSans.ttf";
    private static final String NEGRITO = "fontes/DejaVuSans-Bold.ttf";

    /** Estado da partida, histórico e desfecho. */
    public final BitmapFont texto;
    /** Títulos e o texto do botão. */
    public final BitmapFont destaque;
    /** Numeração dos eixos e rótulos da legenda. */
    public final BitmapFont pequena;

    public Fontes() {
        FreeTypeFontGenerator regular = new FreeTypeFontGenerator(Gdx.files.internal(REGULAR));
        FreeTypeFontGenerator negrito = new FreeTypeFontGenerator(Gdx.files.internal(NEGRITO));
        try {
            texto = gerar(regular, 16, Paleta.TEXTO);
            destaque = gerar(negrito, 16, Paleta.TEXTO);
            pequena = gerar(regular, 14, Paleta.TEXTO_SUAVE);
        } finally {
            regular.dispose();
            negrito.dispose();
        }
    }

    private static BitmapFont gerar(FreeTypeFontGenerator gerador, int tamanho, Color cor) {
        FreeTypeFontParameter parametro = new FreeTypeFontParameter();
        parametro.size = tamanho;
        parametro.minFilter = TextureFilter.Linear;
        parametro.magFilter = TextureFilter.Linear;
        BitmapFont fonte = gerador.generateFont(parametro);
        fonte.setColor(cor);
        return fonte;
    }

    @Override
    public void dispose() {
        texto.dispose();
        destaque.dispose();
        pequena.dispose();
    }
}
