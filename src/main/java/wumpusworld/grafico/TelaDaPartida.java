package wumpusworld.grafico;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Posicao;

import com.badlogic.gdx.ApplicationAdapter;

public class TelaDaPartida extends ApplicationAdapter {
    private SpriteBatch lote;
    private ShapeRenderer formas;
    private BitmapFont fonte;
    private Partida partida;
    private float tempo;
    private float scrollY = 0f;

    @Override
    public void create() {
        this.lote = new SpriteBatch();
        this.formas = new ShapeRenderer();
        this.fonte = new BitmapFont();
        this.fonte.getData().setScale(1.5f);
        this.partida = new Partida();
        
        Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (partida.getSituacao().encerrada()) {
                    float drawY = 700 - screenY;
                    if (screenX >= 200 && screenX <= 400 && drawY >= 300 && drawY <= 350) {
                        partida = new Partida();
                        tempo = 0f;
                        scrollY = 0f;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                // amountY costuma ser 1 (rolar para baixo) ou -1 (rolar para cima)
                scrollY += amountY * 40f; 
                if (scrollY < 0) scrollY = 0;
                return true;
            }
        });
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!partida.getSituacao().encerrada()) {
            tempo += delta;
            if (tempo > 0.5f) {
                partida.executarPasso();
                tempo = 0f;
            }
        }

        formas.begin(ShapeRenderer.ShapeType.Filled);
        desenharGradeEElementos();
        
        if (partida.getSituacao().encerrada()) {
            formas.setColor(0.2f, 0.6f, 0.2f, 1);
            formas.rect(200, 300, 200, 50);
        }
        
        formas.end();

        lote.begin();
        fonte.draw(lote, "Situação: " + partida.getSituacao().getTitulo(), 50, 680);
        fonte.draw(lote, "Pontuação: " + partida.getAgente().getPontuacao() + " | Movimentos: " + partida.getAgente().getQuantidadeDeMovimentos(), 50, 650);
        
        String inventario = "Ouro: " + (partida.getAgente().possuiOuro() ? "Sim" : "Não") + 
                            " | Flecha: " + (partida.getAgente().possuiFlecha() ? "Sim" : "Não");
        fonte.draw(lote, inventario, 50, 620);
        
        if (partida.getAgente().estaVivo()) {
            String percepcoesStr = partida.getPercepcoesAtuais().descricao();
            if (percepcoesStr.isEmpty()) percepcoesStr = "Nenhuma";
            fonte.draw(lote, "Percepções: " + percepcoesStr, 50, 590);
        }

        if (partida.getSituacao().encerrada()) {
            fonte.draw(lote, "JOGAR NOVAMENTE", 205, 332);
        }

        // Histórico de Eventos
        fonte.getData().setScale(1.1f);
        fonte.draw(lote, "HISTÓRICO:", 600, 680);
        java.util.List<String> registro = partida.getRegistro();
        
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        
        float currentY = 30 - scrollY; // Posição influenciada pela rolagem
        for (int i = registro.size() - 1; i >= 0; i--) {
            String texto = registro.get(i);
            layout.setText(fonte, texto, Color.WHITE, 380, -1, true);
            
            // O draw() usa o Y como o topo do texto, então somamos a altura
            float drawY = currentY + layout.height;
            if (drawY > 650) break; // Passou do limite superior (atingimos os mais velhos visíveis)
            
            if (drawY > 0) { // Só desenha se estiver dentro da tela (não caiu pra baixo)
                fonte.draw(lote, layout, 600, drawY);
            }
            
            currentY += (layout.height + 12); // Sobe para a próxima mensagem (mais antiga)
        }
        
        fonte.getData().setScale(1.5f); // reset para a próxima vez
        
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

                boolean conhecida = partida.getMundo().foiVisitada(l, c) || partida.getSituacao().encerrada();

                if (conhecida) {
                    formas.setColor(0.7f, 0.7f, 0.7f, 1);
                } else {
                    formas.setColor(0.3f, 0.3f, 0.3f, 1);
                }
                formas.rect(x + 2, y + 2, lado - 4, lado - 4);

                if (conhecida) {
                    char elem = partida.getMundo().getElemento(l, c);
                    float cx = x + lado / 2;
                    float cy = y + lado / 2;

                    if (elem == Mundo.POCO) {
                        formas.setColor(Color.BLACK);
                        formas.circle(cx, cy, 30);
                    } else if (elem == Mundo.WUMPUS) {
                        formas.setColor(Color.RED);
                        formas.circle(cx, cy, 30);
                    } else if (elem == Mundo.OURO) {
                        formas.setColor(Color.YELLOW);
                        formas.rect(cx - 20, cy - 20, 40, 40);
                    } else if (elem == Mundo.FLECHA) {
                        formas.setColor(Color.GREEN);
                        formas.rect(cx - 5, cy - 20, 10, 40); // Flecha desenhada como uma linha verde fina
                    }
                }
            }
        }

        // Desenhar Agente
        Posicao pos = partida.getAgente().getPosicao();
        float ax = margemX + pos.coluna() * lado + lado / 2;
        float ay = margemY + (tamanho - 1 - pos.linha()) * lado + lado / 2;
        formas.setColor(Color.BLUE);
        formas.circle(ax, ay, 20); // Agente como círculo azul
    }

    @Override
    public void dispose() {
        fonte.dispose();
        lote.dispose();
        formas.dispose();
    }
}
