package wumpusworld.grafico;

import com.badlogic.gdx.Game;

/** Dono do ciclo de vida da tela e de seus recursos gráficos. */
public final class WumpusGame extends Game {
    @Override
    public void create() {
        setScreen(new TelaPartida());
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
