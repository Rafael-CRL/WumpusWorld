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
        configuracao.setWindowedMode(TelaDaPartida.LARGURA_DA_JANELA, TelaDaPartida.ALTURA_DA_JANELA);
        configuracao.setResizable(false);
        configuracao.setForegroundFPS(60);
        // 4 amostras por pixel: suaviza a borda dos círculos.
        configuracao.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        new Lwjgl3Application(new TelaDaPartida(), configuracao);
    }
}
