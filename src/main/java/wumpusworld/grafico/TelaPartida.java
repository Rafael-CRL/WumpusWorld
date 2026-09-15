package wumpusworld.grafico;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.util.List;
import java.util.EnumMap;
import java.util.Map;
import wumpusworld.aplicacao.ControladorJogo;
import wumpusworld.aplicacao.EstadoPartida;
import wumpusworld.aplicacao.Partida;
import wumpusworld.dominio.AgenteInteligente;
import wumpusworld.dominio.Mundo;
import wumpusworld.dominio.Direcao;

/** Compõe a tela com Scene2D; ações do usuário são delegadas ao controlador. */
public final class TelaPartida extends ScreenAdapter {
    private final ControladorJogo controlador = new ControladorJogo();
    private final Tema tema = new Tema();
    private final Stage stage = new Stage(new FitViewport(1240, 820));
    private final TabuleiroActor tabuleiro = new TabuleiroActor(controlador, tema);
    private final Label estado = texto("", "normal", Tema.VERDE);
    private final Label pontuacao = texto("0", "numero", Tema.TEXTO);
    private final Label movimentos = texto("0", "numero", Tema.TEXTO);
    private final Label inventario = texto("", "normal", Tema.TEXTO);
    private final Label percepcoes = texto("", "normal", Tema.TEXTO);
    private final Label decisao = texto("", "pequena", Tema.SUAVE);
    private final Label objetivo = texto("", "normal", Tema.TEXTO);
    private final Label visibilidade = texto("", "pequena", Tema.SUAVE);
    private final Table registros = new Table();
    private final Map<Direcao, TextButton> direcoes = new EnumMap<>(Direcao.class);
    private final TextButton disparar;
    private final TextButton revelar;
    private int quantidadeRegistros = -1;
    private boolean mapaRevelado;

    public TelaPartida() {
        direcoes.put(Direcao.CIMA, botao("↑ Cima", "default", () -> controlador.escolherDirecao(Direcao.CIMA)));
        direcoes.put(Direcao.BAIXO, botao("↓ Baixo", "default", () -> controlador.escolherDirecao(Direcao.BAIXO)));
        direcoes.put(Direcao.ESQUERDA, botao("← Esquerda", "default", () -> controlador.escolherDirecao(Direcao.ESQUERDA)));
        direcoes.put(Direcao.DIREITA, botao("→ Direita", "default", () -> controlador.escolherDirecao(Direcao.DIREITA)));
        disparar = botao("Preparar flecha", "principal", controlador::alternarDisparo);
        revelar = botao("Revelar mapa", "default", this::alternarMapa);
        construirLayout();
        atualizarPainel();
    }

    private void construirLayout() {
        Table raiz = new Table();
        raiz.setFillParent(true);
        raiz.pad(24);
        stage.addActor(raiz);

        Table cabecalho = new Table();
        Table titulo = new Table();
        titulo.add(texto("WUMPUS WORLD", "titulo", Tema.TEXTO)).left().row();
        titulo.add(texto("EXPEDIÇÃO MANUAL  /  VOCÊ ESCOLHE O PRÓXIMO PASSO",
                "pequena", Tema.SUAVE)).left().padTop(7);
        cabecalho.add(titulo).expandX().left();
        cabecalho.add(estado).right();
        raiz.add(cabecalho).growX().height(74).padBottom(16).row();

        Table corpo = new Table();
        Table mapa = painel();
        Table tituloMapa = new Table();
        tituloMapa.add(texto("01  /  O MUNDO", "normal", Tema.TEXTO)).expandX().left();
        tituloMapa.add(texto("5 × 5  •  BASE [0, 0]", "pequena", Tema.VERDE)).right();
        mapa.add(tituloMapa).growX().padBottom(5).row();
        mapa.add(visibilidade).left().padBottom(10).row();
        mapa.add(tabuleiro).grow().row();
        Table legenda = new Table();
        legenda.add(texto("● Agente", "pequena", Tema.VERDE)).padRight(18);
        legenda.add(texto("◆ Ouro", "pequena", Tema.OURO)).padRight(18);
        legenda.add(texto("W Wumpus", "pequena", Tema.PERIGO)).padRight(18);
        legenda.add(texto("○ Poço", "pequena", Tema.SUAVE));
        mapa.add(legenda).padTop(12).left();
        corpo.add(mapa).grow().padRight(16);

        Table lateral = new Table();
        Table telemetria = painel();
        telemetria.add(texto("02  /  TELEMETRIA", "normal", Tema.TEXTO)).left().colspan(2).padBottom(14).row();
        telemetria.add(texto("PONTUAÇÃO", "pequena", Tema.SUAVE)).left().expandX();
        telemetria.add(texto("MOVIMENTOS", "pequena", Tema.SUAVE)).left().expandX().row();
        telemetria.add(pontuacao).left().padTop(5);
        telemetria.add(movimentos).left().padTop(5).row();
        telemetria.add(inventario).left().colspan(2).padTop(14);
        lateral.add(telemetria).growX().padBottom(12).row();

        Table sensores = painel();
        sensores.add(texto("03  /  PERCEPÇÕES", "normal", Tema.TEXTO)).left().padBottom(12).row();
        sensores.add(percepcoes).left().growX().row();
        objetivo.setWrap(true);
        sensores.add(objetivo).growX().padTop(12).row();
        decisao.setWrap(true);
        sensores.add(decisao).growX().padTop(10);
        lateral.add(sensores).growX().padBottom(12).row();

        Table diario = painel();
        diario.add(texto("04  /  DIÁRIO DA EXPEDIÇÃO", "normal", Tema.TEXTO)).left().padBottom(10).row();
        registros.top().left();
        ScrollPane scroll = new ScrollPane(registros, tema.skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        diario.add(scroll).grow().minHeight(110);
        lateral.add(diario).grow();
        corpo.add(lateral).width(420).growY();
        raiz.add(corpo).grow().row();

        Table controles = new Table();
        controles.defaults().height(46).padRight(10);
        for (Direcao direcao : Direcao.values()) {
            controles.add(direcoes.get(direcao)).width(118);
        }
        controles.add(disparar).width(176);
        controles.add(botao("Nova partida", "default", this::reiniciar)).width(148);
        controles.add().expandX();
        controles.add(revelar).width(166).padRight(0);
        raiz.add(controles).growX().padTop(16).row();
        raiz.add(texto("SETAS / WASD  mover     F  preparar flecha + direção     ESC  cancelar mira     R  nova partida     V  revelar mapa",
                "pequena", Tema.SUAVE)).left().padTop(14);
    }

    private Table painel() {
        Table painel = new Table();
        painel.setBackground(tema.fundo(Tema.PAINEL));
        painel.pad(20);
        painel.top().left();
        return painel;
    }

    private Label texto(String conteudo, String estilo, Color cor) {
        return tema.texto(conteudo, estilo, cor);
    }

    private TextButton botao(String nome, String estilo, Runnable acao) {
        TextButton botao = new TextButton(nome, tema.skin, estilo);
        botao.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                acao.run();
            }
        });
        return botao;
    }

    private void reiniciar() {
        controlador.reiniciar();
        mapaRevelado = false;
        quantidadeRegistros = -1;
        tabuleiro.reiniciar();
    }

    private void alternarMapa() {
        mapaRevelado = !mapaRevelado;
        tabuleiro.setRevelar(mapaRevelado);
    }

    private void atualizarPainel() {
        Partida partida = controlador.getPartida();
        AgenteInteligente agente = partida.getAgente();
        Mundo mundo = partida.getMundo();
        EstadoPartida fase = partida.getEstado();
        boolean terminou = fase.terminou();
        estado.setText(terminou ? fase.getTitulo()
                : controlador.estaPreparandoDisparo() ? "Escolha a direção da flecha" : "Sua vez  /  " + fase.getTitulo());
        estado.setColor(fase == EstadoPartida.MORTE ? Tema.PERIGO : Tema.VERDE);
        pontuacao.setText(Integer.toString(agente.getPontuacao()));
        movimentos.setText(agente.getQuantidadeDeMovimentos() + " / 180");
        inventario.setText("Ouro: " + (agente.possuiOuro() ? "coletado" : "a encontrar")
                + "   •   Flecha: " + (agente.possuiFlecha() ? "1" : "0"));
        boolean brisa = mundo.temBrisa(agente.getLinha(), agente.getColuna());
        boolean fedor = mundo.temFedor(agente.getLinha(), agente.getColuna());
        percepcoes.setText(!agente.estaVivo() ? "Sensores inativos"
                : (brisa ? "BRISA" : "Sem brisa") + "   /   " + (fedor ? "FEDOR" : "Sem fedor"));
        percepcoes.setColor(agente.estaVivo() && (brisa || fedor) ? Tema.OURO : Tema.VERDE);
        objetivo.setText(switch (fase) {
            case EXPLORANDO -> "Use as setas ou WASD para explorar e encontrar o ouro.";
            case RETORNANDO -> "Ouro coletado! Escolha o caminho de volta à base [0, 0].";
            case VITORIA -> "Ouro entregue! A expedição foi concluída. Vitória: +200 pontos.";
            case MORTE -> "A expedição terminou em um perigo. Inicie uma nova partida para tentar novamente.";
            case LIMITE_ATINGIDO -> "Os 180 movimentos de exploração acabaram antes de encontrar o ouro.";
        });
        if (controlador.estaPreparandoDisparo()) {
            objetivo.setText("Mira ativa: escolha uma direção para disparar. F ou Esc cancela.");
        }
        decisao.setText(partida.getUltimaDecisao());
        direcoes.values().forEach(botao -> botao.setDisabled(terminou));
        disparar.setDisabled(terminou || !agente.possuiFlecha());
        disparar.setText(controlador.estaPreparandoDisparo() ? "Cancelar mira" : "Preparar flecha");
        revelar.setDisabled(terminou);
        revelar.setText(mapaRevelado ? "Ocultar mapa" : "Revelar mapa");
        visibilidade.setText(terminou ? "Mapa completo  •  expedição encerrada"
                : mapaRevelado ? "Mapa revelado  •  modo de ajuda visual"
                : "Névoa ativa  •  casas visitadas ficam visíveis");
        List<String> historico = partida.getHistorico();
        if (historico.size() != quantidadeRegistros) {
            quantidadeRegistros = historico.size();
            registros.clearChildren();
            for (int i = historico.size() - 1; i >= 0; i--) {
                Label registro = texto(historico.get(i), "pequena", i == historico.size() - 1 ? Tema.TEXTO : Tema.SUAVE);
                registro.setWrap(true);
                registro.setAlignment(Align.left);
                registros.add(registro).width(352).padBottom(12).left().row();
            }
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.UP, Input.Keys.W -> controlador.escolherDirecao(Direcao.CIMA);
                    case Input.Keys.DOWN, Input.Keys.S -> controlador.escolherDirecao(Direcao.BAIXO);
                    case Input.Keys.LEFT, Input.Keys.A -> controlador.escolherDirecao(Direcao.ESQUERDA);
                    case Input.Keys.RIGHT, Input.Keys.D -> controlador.escolherDirecao(Direcao.DIREITA);
                    case Input.Keys.F -> controlador.alternarDisparo();
                    case Input.Keys.ESCAPE -> controlador.cancelarDisparo();
                    case Input.Keys.R -> reiniciar();
                    case Input.Keys.V -> alternarMapa();
                    default -> { return false; }
                }
                return true;
            }
        }));
    }

    @Override
    public void render(float delta) {
        atualizarPainel();
        ScreenUtils.clear(Tema.FUNDO);
        stage.getViewport().apply();
        stage.act(Math.min(delta, 0.1f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() { controlador.cancelarDisparo(); }

    @Override
    public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        hide();
        stage.dispose();
        tabuleiro.dispose();
        tema.dispose();
    }
}
