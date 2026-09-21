package wumpusworld.grafico;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;

/** Paleta, tipografia e estilos compartilhados. Todos os recursos pertencem à Skin. */
final class Tema implements Disposable {
    static final Color FUNDO = Color.valueOf("0B1420");
    static final Color PAINEL = Color.valueOf("121F2E");
    static final Color BORDA = Color.valueOf("263B4D");
    static final Color TEXTO = Color.valueOf("E5EDF5");
    static final Color SUAVE = Color.valueOf("92A9BC");
    static final Color VERDE = Color.valueOf("59DEC3");
    static final Color OURO = Color.valueOf("F5C76C");
    static final Color PERIGO = Color.valueOf("EF8292");
    final Skin skin = new Skin();

    Tema() {
        Pixmap pixel = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixel.setColor(Color.WHITE);
        pixel.fill();
        skin.add("pixel", new Texture(pixel));
        pixel.dispose();
        FreeTypeFontGenerator gerador = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/DejaVuSans.ttf"));
        try {
            fonte(gerador, "normal", 17);
            fonte(gerador, "pequena", 13);
            fonte(gerador, "titulo", 30);
            fonte(gerador, "numero", 26);
        } finally {
            gerador.dispose();
        }
        for (String nome : new String[]{"normal", "pequena", "titulo", "numero"}) {
            skin.add(nome, new Label.LabelStyle(skin.getFont(nome), TEXTO));
        }
        estiloBotao("default", BORDA, TEXTO);
        estiloBotao("principal", VERDE, FUNDO);
        ScrollPane.ScrollPaneStyle scroll = new ScrollPane.ScrollPaneStyle();
        scroll.vScroll = fundo(PAINEL);
        scroll.vScrollKnob = fundo(BORDA);
        skin.add("default", scroll);
    }

    private void fonte(FreeTypeFontGenerator gerador, String nome, int tamanho) {
        FreeTypeFontGenerator.FreeTypeFontParameter parametros = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametros.size = tamanho;
        parametros.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "•–—×●◆○▲";
        parametros.minFilter = Texture.TextureFilter.Linear;
        parametros.magFilter = Texture.TextureFilter.Linear;
        BitmapFont fonte = gerador.generateFont(parametros);
        skin.add(nome, fonte);
    }

    private void estiloBotao(String nome, Color cor, Color texto) {
        TextButton.TextButtonStyle estilo = new TextButton.TextButtonStyle();
        estilo.font = skin.getFont("normal");
        estilo.fontColor = texto;
        estilo.up = fundo(cor);
        estilo.over = fundo(new Color(cor).lerp(Color.WHITE, 0.12f));
        estilo.down = fundo(new Color(cor).lerp(Color.BLACK, 0.2f));
        estilo.disabled = fundo(PAINEL);
        estilo.disabledFontColor = SUAVE;
        skin.add(nome, estilo);
    }

    Drawable fundo(Color cor) { return skin.newDrawable("pixel", cor); }

    Label texto(String texto, String estilo, Color cor) {
        Label label = new Label(texto, skin, estilo);
        label.setColor(cor);
        return label;
    }

    @Override
    public void dispose() { skin.dispose(); }
}
