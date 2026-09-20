package wumpusworld;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import wumpusworld.grafico.TelaDaPartida;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuracao = new Lwjgl3ApplicationConfiguration();
        configuracao.setTitle("Mundo de Wumpus");
        configuracao.setWindowedMode(1000, 700);
        configuracao.setResizable(false);
        configuracao.setForegroundFPS(60);
        new Lwjgl3Application(new TelaDaPartida(), configuracao);
    }
}
