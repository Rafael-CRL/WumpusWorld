package wumpusworld.grafico;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Posicao;

public class TelaDaPartida extends ApplicationAdapter {

    private SpriteBatch lote;
    private ShapeRenderer formas;
    private BitmapFont fonte;
    private Partida partida;
    private Mundo mapaInicial;
    private float tempo;
    private float scrollY = 0f;

    private static final float BOTAO_X = 670f;
    private static final float BOTAO_Y = 20f;
    private static final float BOTAO_LARGURA = 230f;
    private static final float BOTAO_ALTURA = 50f;

    @Override
    public void create() {
        this.lote = new SpriteBatch();
        this.formas = new ShapeRenderer();
        this.fonte = new BitmapFont();
        this.fonte.getData().setScale(1.3f);
        this.partida = new Partida();
        this.mapaInicial = partida.getMundo().reiniciar();

        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (partida.getSituacao().encerrada()) {
                    float drawY = 700 - screenY;
                    if (screenX >= BOTAO_X && screenX <= BOTAO_X + BOTAO_LARGURA
                            && drawY >= BOTAO_Y && drawY <= BOTAO_Y + BOTAO_ALTURA) {
                        partida = new Partida();
                        mapaInicial = partida.getMundo().reiniciar();
                        tempo = 0f;
                        scrollY = 0f;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                scrollY += amountY * 40f;
                if (scrollY < 0) {
                    scrollY = 0;
                }
                return true;
            }
        });
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0.18f, 0.18f, 0.18f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!partida.getSituacao().encerrada()) {
            tempo += delta;
            if (tempo >= 0.5f) {
                partida.executarPasso();
                tempo = 0f;
            }
        }

        formas.begin(ShapeRenderer.ShapeType.Filled);
        desenharGradeEElementos();
        desenharLegenda();

        if (partida.getSituacao().encerrada()) {
            formas.setColor(0.15f, 0.55f, 0.25f, 1f);
            formas.rect(BOTAO_X, BOTAO_Y, BOTAO_LARGURA, BOTAO_ALTURA);
        }
        formas.end();

        if (partida.getSituacao().encerrada()) {
            formas.begin(ShapeRenderer.ShapeType.Line);
            formas.setColor(0.35f, 0.85f, 0.45f, 1f);
            formas.rect(BOTAO_X, BOTAO_Y, BOTAO_LARGURA, BOTAO_ALTURA);
            formas.end();
        }

        lote.begin();

        Posicao posAgente = partida.getAgente().getPosicao();
        fonte.getData().setScale(1.2f);
        fonte.draw(lote, "Situação: " + partida.getSituacao().getTitulo(), 50, 680);
        fonte.draw(lote, "Posição Atual: " + posAgente + " | Movimentos: " + partida.getAgente().getQuantidadeDeMovimentos() + " | Pontos: " + partida.getAgente().getPontuacao(), 50, 650);

        String inventario = "Ouro: " + (partida.getAgente().possuiOuro() ? "Sim" : "Não")
                + " | Flecha: " + (partida.getAgente().possuiFlecha() ? "Sim" : "Não");
        fonte.draw(lote, inventario, 50, 620);

        if (partida.getAgente().estaVivo()) {
            String percepcoesStr = partida.getPercepcoesAtuais().descricao();
            if (percepcoesStr.isEmpty()) {
                percepcoesStr = "Nenhuma";
            }
            fonte.draw(lote, "Percepções: " + percepcoesStr, 50, 590);
        }

        fonte.getData().setScale(1.1f);
        int tamanho = Mundo.TAMANHO;
        float lado = 100f;
        float margemX = 50f;
        float margemY = 50f;

        for (int c = 0; c < tamanho; c++) {
            float cx = margemX + c * lado + lado / 2 - 5;
            fonte.draw(lote, String.valueOf(c), cx, margemY - 12);
        }
        for (int l = 0; l < tamanho; l++) {
            float ly = margemY + (tamanho - 1 - l) * lado + lado / 2 + 7;
            fonte.draw(lote, String.valueOf(l), margemX - 25, ly);
        }

        desenharTextosDaLegenda();

        if (partida.getSituacao().encerrada()) {
            fonte.getData().setScale(1.1f);
            GlyphLayout resultado = new GlyphLayout(fonte,
                    partida.getDescricaoResultado(), Color.WHITE, 370, -1, true);
            fonte.draw(lote, resultado, 600, 495);
        }

        if (partida.getSituacao().encerrada()) {
            fonte.getData().setScale(1.15f);
            GlyphLayout layoutBotao = new GlyphLayout(fonte, "JOGAR NOVAMENTE");
            float tx = BOTAO_X + (BOTAO_LARGURA - layoutBotao.width) / 2f;
            float ty = BOTAO_Y + (BOTAO_ALTURA + layoutBotao.height) / 2f;
            fonte.draw(lote, layoutBotao, tx, ty);
        }

        fonte.getData().setScale(1.1f);
        fonte.draw(lote, "HISTÓRICO DE EVENTOS:", 600, 530);
        java.util.List<String> registro = partida.getRegistro();

        GlyphLayout layout = new GlyphLayout();
        float currentY = (partida.getSituacao().encerrada() ? 90 : 30) - scrollY;
        for (int i = registro.size() - 1; i >= 0; i--) {
            String texto = registro.get(i);
            layout.setText(fonte, texto, Color.WHITE, 370, -1, true);

            float drawY = currentY + layout.height;
            if (drawY > (partida.getSituacao().encerrada() ? 410 : 500)) {
                break;
            }

            if (currentY >= (partida.getSituacao().encerrada() ? 85 : 0)) {
                fonte.draw(lote, layout, 600, drawY);
            }

            currentY += (layout.height + 10);
        }

        lote.end();
    }

    private void desenharGradeEElementos() {
        int tamanho = Mundo.TAMANHO;
        float lado = 100f;
        float margemX = 50f;
        float margemY = 50f;

        for (int l = 0; l < tamanho; l++) {
            for (int c = 0; c < tamanho; c++) {
                float x = margemX + c * lado;
                float y = margemY + (tamanho - 1 - l) * lado;

                boolean conhecida = partida.getMundo().foiVisitada(l, c) || partida.mapaDeveSerRevelado();

                if (conhecida) {
                    formas.setColor(0.75f, 0.75f, 0.75f, 1);
                } else {
                    formas.setColor(0.3f, 0.3f, 0.3f, 1);
                }
                formas.rect(x + 2, y + 2, lado - 4, lado - 4);

                if (conhecida) {
                    char elem = partida.mapaDeveSerRevelado()
                            ? mapaInicial.getElemento(l, c)
                            : partida.getMundo().getElemento(l, c);
                    float cx = x + lado / 2;
                    float cy = y + lado / 2;

                    if (elem == Mundo.POCO) {
                        formas.setColor(Color.BLACK);
                        formas.circle(cx, cy, 28);
                    } else if (elem == Mundo.WUMPUS) {
                        formas.setColor(Color.RED);
                        formas.circle(cx, cy, 28);
                    } else if (elem == Mundo.OURO) {
                        formas.setColor(Color.YELLOW);
                        formas.rect(cx - 20, cy - 20, 40, 40);
                    } else if (elem == Mundo.FLECHA) {
                        formas.setColor(Color.GREEN);
                        formas.rect(cx - 5, cy - 20, 10, 40);
                    }
                }
            }
        }

        Posicao pos = partida.getAgente().getPosicao();
        float ax = margemX + pos.coluna() * lado + lado / 2;
        float ay = margemY + (tamanho - 1 - pos.linha()) * lado + lado / 2;
        formas.setColor(Color.BLUE);
        formas.circle(ax, ay, 20);
    }

    private void desenharLegenda() {
        float baseX = 600f;
        float baseY = 560f;

        formas.setColor(0.25f, 0.25f, 0.25f, 1);
        formas.rect(baseX, baseY, 370, 120);

        formas.setColor(Color.BLUE);
        formas.circle(baseX + 25, baseY + 95, 10);

        formas.setColor(Color.BLACK);
        formas.circle(baseX + 25, baseY + 60, 10);

        formas.setColor(Color.RED);
        formas.circle(baseX + 25, baseY + 25, 10);

        formas.setColor(Color.YELLOW);
        formas.rect(baseX + 195, baseY + 85, 18, 18);

        formas.setColor(Color.GREEN);
        formas.rect(baseX + 200, baseY + 50, 8, 20);
    }

    private void desenharTextosDaLegenda() {
        float baseX = 600f;
        float baseY = 560f;

        fonte.getData().setScale(1.0f);
        fonte.draw(lote, "Agente", baseX + 45, baseY + 100);
        fonte.draw(lote, "Poço", baseX + 45, baseY + 65);
        fonte.draw(lote, "Wumpus", baseX + 45, baseY + 30);

        fonte.draw(lote, "Ouro", baseX + 225, baseY + 100);
        fonte.draw(lote, "Flecha", baseX + 225, baseY + 65);
    }

    @Override
    public void dispose() {
        fonte.dispose();
        lote.dispose();
        formas.dispose();
    }
}

