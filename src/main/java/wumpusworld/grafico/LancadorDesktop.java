package wumpusworld.grafico;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Ponto de entrada da versão gráfica (backend LWJGL 3, para Windows, Linux e macOS).
 *
 * <p>A janela abre num tamanho proporcional ao monitor do usuário, nunca maior
 * que a área disponível, e pode ser redimensionada à vontade: a
 * {@code FitViewport} da tela reescala todo o layout sem deformá-lo.</p>
 */
public final class LancadorDesktop {

    private static final int LARGURA_DESEJADA = 1400;
    private static final int ALTURA_DESEJADA = 900;
    private static final int LARGURA_MINIMA = 900;
    private static final int ALTURA_MINIMA = 580;

    private LancadorDesktop() {
    }

    public static void iniciar() {
        Lwjgl3ApplicationConfiguration configuracao =
                new Lwjgl3ApplicationConfiguration();

        configuracao.setTitle("Mundo de Wumpus — UFPA Cametá · Computação Gráfica");
        configuracao.setWindowedMode(largura(), altura());
        configuracao.setWindowSizeLimits(LARGURA_MINIMA, ALTURA_MINIMA, -1, -1);
        configuracao.setForegroundFPS(60);
        configuracao.useVsync(true);

        // 4 amostras por pixel: bordas suaves nos ícones desenhados por geometria.
        configuracao.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);

        configuracao.setWindowIcon(Files.FileType.Internal,
                "icone/icone-128.png", "icone/icone-64.png", "icone/icone-32.png");

        new Lwjgl3Application(new JogoWumpus(), configuracao);
    }

    private static int largura() {
        int disponivel = Lwjgl3ApplicationConfiguration.getDisplayMode().width;
        return Math.max(LARGURA_MINIMA,
                Math.min(LARGURA_DESEJADA, (int) (disponivel * 0.92f)));
    }

    private static int altura() {
        int disponivel = Lwjgl3ApplicationConfiguration.getDisplayMode().height;
        return Math.max(ALTURA_MINIMA,
                Math.min(ALTURA_DESEJADA, (int) (disponivel * 0.88f)));
    }
}
