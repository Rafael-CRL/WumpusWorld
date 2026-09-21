package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import wumpusworld.aplicacao.Disparo;

/**
 * Efeitos passageiros do tabuleiro: partículas, trajetória da flecha, tremor, flash e faixa
 * de resultado. São puramente visuais; nunca alteram nem consultam as regras do jogo.
 * As partículas usam coordenadas de casa (linha, coluna), então acompanham o redimensionamento.
 */
final class Efeitos {
    private static final float VELOCIDADE_DA_FLECHA = 16f;
    private static final float CAUDA_DA_FLECHA = 1.1f;
    private static final float DESVANECER_DA_FLECHA = 1.5f;
    private static final float GRAVIDADE = 5f;
    private static final float DURACAO_DA_FAIXA = 3.2f;
    private static final float ENTRADA_DA_FAIXA = 0.35f;
    private static final float SAIDA_DA_FAIXA = 0.6f;

    private static final class Particula {
        float linha, coluna, velocidadeLinha, velocidadeColuna;
        float vida, duracao, tamanho;
        Color cor;
    }

    private final List<Particula> particulas = new ArrayList<>();
    private final Random sorteio = new Random();
    private float relogio;
    private float flash;
    private float tremor;
    private Disparo flecha;
    private float percurso;
    private boolean impactoRegistrado;
    private float tempoDaFaixa = -1;
    private String tituloDaFaixa = "";
    private Color corDaFaixa = Color.WHITE;

    void limpar() {
        particulas.clear();
        flash = 0;
        tremor = 0;
        flecha = null;
        tempoDaFaixa = -1;
    }

    void atualizar(float delta) {
        relogio += delta;
        flash = Math.max(0, flash - delta * 2.2f);
        tremor = Math.max(0, tremor - delta * 1.6f);
        for (int i = particulas.size() - 1; i >= 0; i--) {
            Particula p = particulas.get(i);
            p.vida -= delta;
            if (p.vida <= 0) {
                particulas.remove(i);
                continue;
            }
            p.velocidadeLinha += GRAVIDADE * delta;
            p.linha += p.velocidadeLinha * delta;
            p.coluna += p.velocidadeColuna * delta;
        }
        atualizarFlecha(delta);
        if (tempoDaFaixa >= 0) {
            tempoDaFaixa += delta;
            if (tempoDaFaixa > DURACAO_DA_FAIXA) tempoDaFaixa = -1;
        }
    }

    private void atualizarFlecha(float delta) {
        if (flecha == null) return;
        percurso += VELOCIDADE_DA_FLECHA * delta;
        float alvo = distanciaDaFlecha();
        if (percurso >= alvo && !impactoRegistrado) {
            impactoRegistrado = true;
            if (flecha.acertou()) {
                explodir(flecha.linha() + flecha.direcao().getVariacaoLinha() * flecha.alcance(),
                        flecha.coluna() + flecha.direcao().getVariacaoColuna() * flecha.alcance(),
                        Tema.PERIGO, 26, 3.2f);
            }
        }
        if (percurso >= alvo + DESVANECER_DA_FLECHA) flecha = null;
    }

    /** Distância percorrida em casas; se errou, a flecha segue até a borda do mapa. */
    private float distanciaDaFlecha() {
        return flecha.alcance() + (flecha.acertou() ? 0 : 0.5f);
    }

    void disparou(Disparo disparo) {
        flecha = disparo;
        percurso = 0;
        impactoRegistrado = false;
    }

    void coletouOuro(int linha, int coluna) {
        explodir(linha, coluna, Tema.OURO, 30, 3f);
    }

    void venceu(int linha, int coluna) {
        explodir(linha, coluna, Tema.OURO, 45, 4.5f);
        explodir(linha, coluna, Tema.VERDE, 35, 4f);
    }

    void morreu(int linha, int coluna) {
        flash = 1;
        tremor = 1;
        explodir(linha, coluna, Tema.PERIGO, 34, 3.5f);
    }

    void anunciar(String titulo, Color cor) {
        tituloDaFaixa = titulo;
        corDaFaixa = cor;
        tempoDaFaixa = 0;
    }

    private void explodir(float linha, float coluna, Color cor, int quantidade, float velocidade) {
        for (int i = 0; i < quantidade; i++) {
            Particula p = new Particula();
            float angulo = sorteio.nextFloat() * MathUtils.PI2;
            float impulso = velocidade * (0.35f + 0.65f * sorteio.nextFloat());
            p.linha = linha;
            p.coluna = coluna;
            p.velocidadeColuna = MathUtils.cos(angulo) * impulso;
            p.velocidadeLinha = MathUtils.sin(angulo) * impulso - velocidade * 0.4f;
            p.duracao = p.vida = 0.55f + 0.6f * sorteio.nextFloat();
            p.tamanho = 0.025f + 0.035f * sorteio.nextFloat();
            p.cor = cor;
            particulas.add(p);
        }
    }

    /** Deslocamento horizontal do tremor, em unidades de tela. */
    float tremorX(float passo) { return MathUtils.sin(relogio * 70f) * tremor * passo * 0.06f; }

    /** Deslocamento vertical do tremor, em unidades de tela. */
    float tremorY(float passo) { return MathUtils.cos(relogio * 83f) * tremor * passo * 0.05f; }

    /** Camada sobre os elementos: partículas e flecha. */
    void desenharPartes(ShapeRenderer f, Grade grade) {
        for (Particula p : particulas) {
            f.setColor(p.cor.r, p.cor.g, p.cor.b, MathUtils.clamp(p.vida / p.duracao, 0, 1));
            f.circle(grade.centroX(p.coluna), grade.centroY(p.linha), p.tamanho * grade.passo(), 10);
        }
        if (flecha != null) desenharFlecha(f, grade);
    }

    private void desenharFlecha(ShapeRenderer f, Grade grade) {
        float alvo = distanciaDaFlecha();
        float cabeca = Math.min(percurso, alvo);
        float cauda = Math.max(0, cabeca - CAUDA_DA_FLECHA);
        float alfa = percurso <= alvo ? 1 : MathUtils.clamp(1 - (percurso - alvo) / DESVANECER_DA_FLECHA, 0, 1);
        float dx = flecha.direcao().getVariacaoColuna();
        float dy = -flecha.direcao().getVariacaoLinha();
        float passo = grade.passo();
        float ox = grade.centroX(flecha.coluna());
        float oy = grade.centroY(flecha.linha());
        float ponta = 9;
        float hx = ox + dx * cabeca * passo;
        float hy = oy + dy * cabeca * passo;
        f.setColor(Tema.TEXTO.r, Tema.TEXTO.g, Tema.TEXTO.b, alfa);
        f.rectLine(ox + dx * cauda * passo, oy + dy * cauda * passo, hx - dx * ponta, hy - dy * ponta, 3);
        f.triangle(hx, hy, hx - dx * ponta - dy * ponta * 0.6f, hy - dy * ponta + dx * ponta * 0.6f,
                hx - dx * ponta + dy * ponta * 0.6f, hy - dy * ponta - dx * ponta * 0.6f);
    }

    /** Camada mais alta: flash de morte e faixa do resultado. */
    void desenharCobertura(ShapeRenderer f, Grade grade) {
        if (flash > 0) {
            f.setColor(Tema.PERIGO.r, Tema.PERIGO.g, Tema.PERIGO.b, flash * 0.35f);
            f.rect(grade.x(), grade.y(), grade.lado(), grade.lado());
        }
        if (tempoDaFaixa < 0) return;
        float alfa = alfaDaFaixa();
        float altura = grade.passo() * 0.9f * Interpolation.smooth.apply(alfa);
        float y = grade.y() + (grade.lado() - altura) / 2;
        f.setColor(Tema.FUNDO.r, Tema.FUNDO.g, Tema.FUNDO.b, 0.85f * alfa);
        f.rect(grade.x(), y, grade.lado(), altura);
        f.setColor(corDaFaixa.r, corDaFaixa.g, corDaFaixa.b, alfa);
        f.rect(grade.x(), y, grade.lado(), 3);
        f.rect(grade.x(), y + altura - 3, grade.lado(), 3);
    }

    /** Texto do resultado, desenhado depois de todas as formas. */
    void desenharTexto(Batch batch, BitmapFont fonte, Grade grade) {
        if (tempoDaFaixa < 0) return;
        float alfa = alfaDaFaixa();
        fonte.setColor(corDaFaixa.r, corDaFaixa.g, corDaFaixa.b, alfa);
        float y = grade.y() + grade.lado() / 2 + fonte.getCapHeight() / 2;
        fonte.draw(batch, tituloDaFaixa, grade.x(), y, grade.lado(), Align.center, false);
        fonte.setColor(Color.WHITE);
    }

    private float alfaDaFaixa() {
        return Math.min(1, tempoDaFaixa / ENTRADA_DA_FAIXA)
                * MathUtils.clamp((DURACAO_DA_FAIXA - tempoDaFaixa) / SAIDA_DA_FAIXA, 0, 1);
    }
}
