package wumpusworld.grafico;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Verificação optativa com OpenGL real: movimento automático, cliques, atalhos e capturas. */
public final class VerificacaoDesktop extends ApplicationAdapter {
    private static final int QUADROS_SEM_MOVER = 90;
    private static final int LIMITE_DE_QUADROS = 6000;
    private static final Pattern MOVIMENTOS = Pattern.compile("(\\d+) / 180");

    private final WumpusGame jogo = new WumpusGame();
    private int frame;
    private int movimentosAoPausar;
    private int fimDaPartida;
    private Throwable falha;

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Wumpus World — verificação automática");
        config.setWindowedMode(1280, 820);
        config.setForegroundFPS(60);
        config.setIdleFPS(60);
        config.disableAudio(true);
        VerificacaoDesktop verificacao = new VerificacaoDesktop();
        new Lwjgl3Application(verificacao, config);
        if (verificacao.falha != null) {
            throw new AssertionError("Falha na verificação desktop", verificacao.falha);
        }
    }

    @Override
    public void create() { jogo.create(); }

    @Override
    public void resize(int width, int height) { jogo.resize(width, height); }

    @Override
    public void render() {
        try {
            jogo.render();
            frame++;
            switch (frame) {
                case 15 -> {
                    exigir(movimentos() == 0, "Estado inicial");
                    exigir(textos().contains("Posição: [0, 0]"), "Posição inicial exibida");
                    exigir(botao("Pausar") != null, "Partida inicia em execução");
                    capturar("inicial");
                }
                case 120 -> {
                    exigir(movimentos() > 0, "O agente se move sozinho, sem entrada do usuário");
                    clicar("Pausar");
                }
                case 125 -> {
                    exigir(botao("Continuar") != null, "Pausa ativada");
                    movimentosAoPausar = movimentos();
                }
                case 125 + QUADROS_SEM_MOVER -> {
                    exigir(movimentos() == movimentosAoPausar, "Pausa congela o agente");
                    clicar("Continuar");
                }
                case 130 + QUADROS_SEM_MOVER -> {
                    exigir(botao("Pausar") != null, "Movimento retomado");
                    clicar("Velocidade: Normal");
                }
                case 135 + QUADROS_SEM_MOVER -> {
                    exigir(botao("Velocidade: Rápida") != null, "Velocidade alterada");
                    clicar("Revelar mapa");
                }
                case 140 + QUADROS_SEM_MOVER -> {
                    exigir(botao("Ocultar mapa") != null, "Revelação visual");
                    capturar("revelado");
                    clicar("Ocultar mapa");
                }
                default -> {
                    if (frame > 140 + QUADROS_SEM_MOVER) aguardarFimDaPartida();
                }
            }
        } catch (Throwable erro) {
            falha = erro;
            Gdx.app.exit();
        }
    }

    private void aguardarFimDaPartida() {
        if (fimDaPartida == 0) {
            exigir(frame < LIMITE_DE_QUADROS, "A partida deve terminar sozinha");
            if (!partidaTerminou()) return;
            fimDaPartida = frame;
            exigir(botao("Pausar").isDisabled(), "Fim bloqueia a pausa");
            capturar("fim");
            int movimentos = movimentos();
            exigir(movimentos > 0, "Partida encerrada com movimentos");
            movimentosAoPausar = movimentos;
        } else if (frame == fimDaPartida + 30) {
            capturar("resultado");
        } else if (frame == fimDaPartida + 60) {
            exigir(movimentos() == movimentosAoPausar, "Fim impede decisões adicionais");
            tecla(Input.Keys.R);
        } else if (frame == fimDaPartida + 65) {
            exigir(movimentos() == 0, "Reinício por teclado");
            exigir(textos().contains("Posição: [0, 0]"), "Posição reiniciada");
            Gdx.graphics.setWindowedMode(960, 640);
        } else if (frame == fimDaPartida + 95) {
            capturar("janela-menor");
            System.out.println("VERIFICAÇÃO DESKTOP OK: movimento automático, pausa, velocidade, fim de partida, reinício e redimensionamento.");
            Gdx.app.exit();
        }
    }

    private boolean partidaTerminou() {
        List<String> textos = textos();
        return List.of("Missão cumprida", "Agente perdido", "Exploração encerrada").stream()
                .anyMatch(textos::contains);
    }

    private int movimentos() {
        for (String texto : textos()) {
            Matcher m = MOVIMENTOS.matcher(texto);
            if (m.matches()) return Integer.parseInt(m.group(1));
        }
        throw new AssertionError("Contador de movimentos não encontrado");
    }

    private Stage stage() {
        return (Stage) ((InputMultiplexer) Gdx.input.getInputProcessor()).getProcessors().first();
    }

    private List<Actor> atores() {
        List<Actor> atores = new ArrayList<>();
        coletar(stage().getRoot(), atores);
        return atores;
    }

    private void coletar(Group grupo, List<Actor> atores) {
        for (Actor ator : grupo.getChildren()) {
            atores.add(ator);
            if (ator instanceof Group filho) coletar(filho, atores);
        }
    }

    private List<String> textos() {
        return atores().stream().filter(Label.class::isInstance).map(Label.class::cast)
                .map(label -> label.getText().toString()).toList();
    }

    private TextButton botao(String texto) {
        return atores().stream().filter(TextButton.class::isInstance).map(TextButton.class::cast)
                .filter(botao -> botao.getText().toString().equals(texto)).findFirst().orElseThrow();
    }

    private void clicar(String texto) {
        TextButton botao = botao(texto);
        Vector2 posicao = botao.localToStageCoordinates(new Vector2(botao.getWidth() / 2, botao.getHeight() / 2));
        stage().stageToScreenCoordinates(posicao);
        Gdx.input.getInputProcessor().touchDown((int) posicao.x, (int) posicao.y, 0, Input.Buttons.LEFT);
        Gdx.input.getInputProcessor().touchUp((int) posicao.x, (int) posicao.y, 0, Input.Buttons.LEFT);
    }

    private void tecla(int codigo) {
        Gdx.input.getInputProcessor().keyDown(codigo);
        Gdx.input.getInputProcessor().keyUp(codigo);
    }

    private void capturar(String nome) {
        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0,
                Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        try {
            PixmapIO.writePNG(Gdx.files.local("build/desktop-validation/" + nome + ".png"), pixmap, -1, true);
        } finally {
            pixmap.dispose();
        }
    }

    private void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new AssertionError(mensagem);
    }

    @Override
    public void dispose() { jogo.dispose(); }
}
