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

/** Verificação optativa com OpenGL real, cliques, atalhos e capturas da janela. */
public final class VerificacaoDesktop extends ApplicationAdapter {
    private final WumpusGame jogo = new WumpusGame();
    private int frame;
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
                    exigir(textos().contains("0 / 180"), "Estado inicial");
                    capturar("inicial");
                }
                case 60 -> {
                    exigir(textos().contains("0 / 180"), "Esperar não move o jogador");
                    clicar("↑ Cima");
                }
                case 65 -> {
                    exigir(textos().contains("0 / 180"), "Parede não consome movimento");
                    clicar("↓ Baixo");
                }
                case 70 -> {
                    exigir(textos().contains("1 / 180"), "Movimento por clique");
                    tecla(Input.Keys.D);
                }
                case 75 -> {
                    exigir(textos().contains("2 / 180"), "Movimento por WASD");
                    clicar("Preparar flecha");
                }
                case 80 -> {
                    exigir(botao("Cancelar mira") != null, "Mira ativada");
                    tecla(Input.Keys.ESCAPE);
                }
                case 85 -> {
                    exigir(botao("Preparar flecha") != null, "Mira cancelada");
                    tecla(Input.Keys.F);
                }
                case 90 -> clicar("→ Direita");
                case 95 -> {
                    exigir(textos().contains("2 / 180"), "Flecha não move o jogador");
                    exigir(textos().contains("-12"), "Custo de disparo");
                    exigir(botao("Preparar flecha").isDisabled(), "Flecha consumida");
                    tecla(Input.Keys.R);
                }
                case 100 -> {
                    exigir(textos().contains("0 / 180"), "Reinício por teclado");
                    clicar("Revelar mapa");
                }
                case 105 -> {
                    exigir(botao("Ocultar mapa") != null, "Revelação visual");
                    capturar("revelado");
                    tecla(Input.Keys.DOWN);
                    tecla(Input.Keys.DOWN);
                    tecla(Input.Keys.F);
                    tecla(Input.Keys.RIGHT);
                }
                case 110 -> {
                    exigir(textos().contains("38"), "Flecha abate o Wumpus escolhido");
                    tecla(Input.Keys.DOWN);
                    tecla(Input.Keys.DOWN);
                    for (int i = 0; i < 4; i++) tecla(Input.Keys.RIGHT);
                }
                case 115 -> {
                    exigir(textos().contains("8 / 180"), "Percurso manual até o ouro");
                    exigir(textos().stream().anyMatch(t -> t.startsWith("Ouro: coletado")), "Coleta do ouro");
                }
                case 175 -> {
                    exigir(textos().contains("8 / 180"), "Retorno não é automático");
                    tecla(Input.Keys.UP);
                }
                case 180 -> {
                    exigir(textos().contains("9 / 180"), "Jogador escolhe o retorno");
                    tecla(Input.Keys.DOWN);
                    for (int i = 0; i < 4; i++) tecla(Input.Keys.LEFT);
                    for (int i = 0; i < 4; i++) tecla(Input.Keys.UP);
                }
                case 195 -> {
                    exigir(botao("↑ Cima").isDisabled(), "Fim bloqueia os movimentos");
                    exigir(textos().contains("Missão cumprida"), "Vitória manual");
                    exigir(textos().contains("322"), "Pontuação final");
                    capturar("fim");
                    tecla(Input.Keys.RIGHT);
                    tecla(Input.Keys.F);
                }
                case 200 -> {
                    exigir(textos().contains("18 / 180"), "Fim impede turnos adicionais");
                    clicar("Nova partida");
                    Gdx.graphics.setWindowedMode(960, 640);
                }
                case 230 -> {
                    exigir(textos().contains("0 / 180"), "Reinício por clique");
                    capturar("janela-menor");
                    clicar("→ Direita");
                }
                case 235 -> {
                    exigir(textos().contains("1 / 180"), "Clique após redimensionamento");
                    System.out.println("VERIFICAÇÃO DESKTOP OK: controle manual, mira, flecha, ouro, retorno, vitória e redimensionamento.");
                    Gdx.app.exit();
                }
                default -> { }
            }
        } catch (Throwable erro) {
            falha = erro;
            Gdx.app.exit();
        }
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
