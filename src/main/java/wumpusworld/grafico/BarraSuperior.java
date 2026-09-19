package wumpusworld.grafico;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Cabeçalho simples com o título da aplicação. */
public final class BarraSuperior {

    private static final float MARGEM = 20f;

    private final Ativos ativos;
    private Area area = new Area(0f, 0f, 1f, 1f);

    public BarraSuperior(Ativos ativos) {
        this.ativos = ativos;
    }

    public void definirArea(Area novaArea) {
        area = novaArea;
    }

    public void desenharFormas(ShapeRenderer formas) {
        Desenho.painel(formas, area, 4f, Paleta.PAINEL, Paleta.BORDA, 1f);
    }

    public void desenharTextos(SpriteBatch lote) {
        Desenho.texto(lote, ativos.fonteTitulo, "Mundo de Wumpus",
                area.x() + MARGEM, area.centroY() + 7f, Paleta.TEXTO);
    }
}
