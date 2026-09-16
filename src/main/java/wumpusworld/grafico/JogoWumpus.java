package wumpusworld.grafico;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Aplicação libGDX: cria os recursos compartilhados e abre a tela da partida.
 *
 * <p>O {@link SpriteBatch} (textos) e o {@link ShapeRenderer} (formas) são
 * criados uma única vez e emprestados a todos os painéis, evitando o custo de
 * recriá-los a cada quadro.</p>
 */
public final class JogoWumpus extends Game {

    private Ativos ativos;
    private SpriteBatch lote;
    private ShapeRenderer formas;

    @Override
    public void create() {
        ativos = new Ativos();
        lote = new SpriteBatch();
        formas = new ShapeRenderer();
        formas.setAutoShapeType(false);

        setScreen(new TelaDaPartida(ativos, lote, formas));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
        formas.dispose();
        lote.dispose();
        ativos.dispose();
    }
}
