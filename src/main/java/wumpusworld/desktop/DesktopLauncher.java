package wumpusworld.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import wumpusworld.grafico.WumpusGame;

/** Configuração específica da janela desktop (LWJGL3). */
public final class DesktopLauncher {
    private DesktopLauncher() { }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Wumpus World | Agente autônomo");
        config.setWindowedMode(1280, 820);
        config.setWindowSizeLimits(960, 640, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.disableAudio(true);
        new Lwjgl3Application(new WumpusGame(), config);
    }
}
