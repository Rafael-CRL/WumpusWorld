package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Primitivas de desenho reaproveitadas por todos os painéis.
 *
 * <p>A libGDX oferece retângulos e círculos; o que a interface precisa são
 * cantos arredondados, sombras suaves, anéis e polígonos preenchidos. Esta
 * classe constrói essas formas a partir das primitivas básicas, sempre com o
 * {@link ShapeRenderer} já aberto no modo {@code Filled}.</p>
 */
public final class Desenho {

    private static final GlyphLayout LAYOUT = new GlyphLayout();

    private Desenho() {
    }

    // -----------------------------------------------------------------------
    //  Formas
    // -----------------------------------------------------------------------

    /**
     * Retângulo de cantos arredondados, preenchido.
     *
     * <p>Os três retângulos e os quatro quartos de círculo são recortados de
     * modo a <strong>não se sobrepor</strong>. Isso é essencial: com cores
     * translúcidas, qualquer sobreposição seria misturada duas vezes e
     * apareceria como manchas escuras nos cantos.</p>
     */
    public static void caixa(ShapeRenderer formas, float x, float y,
            float largura, float altura, float raio, Color cor) {
        formas.setColor(cor);
        float r = Math.min(raio, Math.min(largura, altura) / 2f);

        if (r <= 0.5f) {
            formas.rect(x, y, largura, altura);
            return;
        }

        formas.rect(x + r, y, largura - r * 2f, altura);
        formas.rect(x, y + r, r, altura - r * 2f);
        formas.rect(x + largura - r, y + r, r, altura - r * 2f);

        formas.arc(x + r, y + r, r, 180f, 90f, 10);
        formas.arc(x + largura - r, y + r, r, 270f, 90f, 10);
        formas.arc(x + largura - r, y + altura - r, r, 0f, 90f, 10);
        formas.arc(x + r, y + altura - r, r, 90f, 90f, 10);
    }

    /**
     * Apenas a moldura de um retângulo arredondado, com espessura constante.
     * Como não há preenchimento no miolo, funciona bem com cores translúcidas.
     */
    public static void contorno(ShapeRenderer formas, float x, float y,
            float largura, float altura, float raio, float espessura,
            Color cor) {
        formas.setColor(cor);
        float r = Math.min(raio, Math.min(largura, altura) / 2f);
        float e = Math.min(espessura, r);

        formas.rect(x + r, y, largura - r * 2f, e);
        formas.rect(x + r, y + altura - e, largura - r * 2f, e);
        formas.rect(x, y + r, e, altura - r * 2f);
        formas.rect(x + largura - e, y + r, e, altura - r * 2f);

        arcoDeAnel(formas, x + r, y + r, r - e, r, 180f, 90f, cor);
        arcoDeAnel(formas, x + largura - r, y + r, r - e, r, 270f, 90f, cor);
        arcoDeAnel(formas, x + largura - r, y + altura - r, r - e, r, 0f, 90f, cor);
        arcoDeAnel(formas, x + r, y + altura - r, r - e, r, 90f, 90f, cor);
    }

    public static void contorno(ShapeRenderer formas, Area area, float raio,
            float espessura, Color cor) {
        contorno(formas, area.x(), area.y(), area.largura(), area.altura(),
                raio, espessura, cor);
    }

    /** Trecho de anel entre dois raios, usado nos cantos das molduras. */
    public static void arcoDeAnel(ShapeRenderer formas, float centroX,
            float centroY, float raioInterno, float raioExterno,
            float anguloInicial, float abertura, Color cor) {
        formas.setColor(cor);
        int segmentos = Math.max(4, (int) (abertura / 9f));
        float inicio = anguloInicial * MathUtils.degreesToRadians;
        float passo = (abertura * MathUtils.degreesToRadians) / segmentos;

        for (int indice = 0; indice < segmentos; indice++) {
            float anguloA = inicio + indice * passo;
            float anguloB = anguloA + passo;

            float internoAx = centroX + MathUtils.cos(anguloA) * raioInterno;
            float internoAy = centroY + MathUtils.sin(anguloA) * raioInterno;
            float internoBx = centroX + MathUtils.cos(anguloB) * raioInterno;
            float internoBy = centroY + MathUtils.sin(anguloB) * raioInterno;
            float externoAx = centroX + MathUtils.cos(anguloA) * raioExterno;
            float externoAy = centroY + MathUtils.sin(anguloA) * raioExterno;
            float externoBx = centroX + MathUtils.cos(anguloB) * raioExterno;
            float externoBy = centroY + MathUtils.sin(anguloB) * raioExterno;

            formas.triangle(internoAx, internoAy, externoAx, externoAy,
                    externoBx, externoBy);
            formas.triangle(internoAx, internoAy, externoBx, externoBy,
                    internoBx, internoBy);
        }
    }

    public static void caixa(ShapeRenderer formas, Area area, float raio, Color cor) {
        caixa(formas, area.x(), area.y(), area.largura(), area.altura(), raio, cor);
    }

    /**
     * Painel completo: primeiro o preenchimento, depois a moldura por cima.
     * Nessa ordem a borda nunca é misturada com o fundo, o que preserva o
     * contraste mesmo quando as duas cores são translúcidas.
     */
    public static void painel(ShapeRenderer formas, Area area, float raio,
            Color corDoFundo, Color corDaBorda, float espessura) {
        caixa(formas, area, raio, corDoFundo);
        contorno(formas, area, raio, espessura, corDaBorda);
    }

    /** Sombra difusa desenhada em camadas translúcidas abaixo do painel. */
    public static void sombra(ShapeRenderer formas, Area area, float raio,
            float intensidade) {
        int camadas = 6;
        for (int camada = camadas; camada >= 1; camada--) {
            float expansao = camada * 2.2f;
            float alfa = intensidade / (camada * 2.6f);
            caixa(formas,
                    area.x() - expansao,
                    area.y() - expansao - 2f,
                    area.largura() + expansao * 2f,
                    area.altura() + expansao * 2f,
                    raio + expansao,
                    new Color(0f, 0f, 0f, alfa));
        }
    }

    /** Retângulo com degradê vertical entre duas cores. */
    public static void degradeVertical(ShapeRenderer formas, float x, float y,
            float largura, float altura, Color corDeBaixo, Color corDeCima) {
        formas.rect(x, y, largura, altura,
                corDeBaixo, corDeBaixo, corDeCima, corDeCima);
    }

    /** Anel preenchido entre dois raios. */
    public static void anel(ShapeRenderer formas, float centroX, float centroY,
            float raioInterno, float raioExterno, Color cor, int segmentos) {
        formas.setColor(cor);
        float passo = MathUtils.PI2 / segmentos;

        for (int indice = 0; indice < segmentos; indice++) {
            float anguloA = indice * passo;
            float anguloB = anguloA + passo;

            float internoAx = centroX + MathUtils.cos(anguloA) * raioInterno;
            float internoAy = centroY + MathUtils.sin(anguloA) * raioInterno;
            float internoBx = centroX + MathUtils.cos(anguloB) * raioInterno;
            float internoBy = centroY + MathUtils.sin(anguloB) * raioInterno;
            float externoAx = centroX + MathUtils.cos(anguloA) * raioExterno;
            float externoAy = centroY + MathUtils.sin(anguloA) * raioExterno;
            float externoBx = centroX + MathUtils.cos(anguloB) * raioExterno;
            float externoBy = centroY + MathUtils.sin(anguloB) * raioExterno;

            formas.triangle(internoAx, internoAy, externoAx, externoAy,
                    externoBx, externoBy);
            formas.triangle(internoAx, internoAy, externoBx, externoBy,
                    internoBx, internoBy);
        }
    }

    /**
     * Polígono convexo preenchido, montado como um leque de triângulos a
     * partir do centro. Os vértices vêm em pares {@code x, y}.
     */
    public static void leque(ShapeRenderer formas, float centroX, float centroY,
            float[] vertices, Color cor) {
        formas.setColor(cor);
        int quantidade = vertices.length / 2;

        for (int indice = 0; indice < quantidade; indice++) {
            int proximo = (indice + 1) % quantidade;
            formas.triangle(centroX, centroY,
                    vertices[indice * 2], vertices[indice * 2 + 1],
                    vertices[proximo * 2], vertices[proximo * 2 + 1]);
        }
    }

    /** Losango (gema) preenchido. */
    public static void losango(ShapeRenderer formas, float centroX, float centroY,
            float meiaLargura, float meiaAltura, Color cor) {
        formas.setColor(cor);
        formas.triangle(centroX, centroY + meiaAltura,
                centroX - meiaLargura, centroY,
                centroX + meiaLargura, centroY);
        formas.triangle(centroX, centroY - meiaAltura,
                centroX - meiaLargura, centroY,
                centroX + meiaLargura, centroY);
    }

    /** Segmento de reta com espessura, com as pontas arredondadas. */
    public static void linha(ShapeRenderer formas, float x1, float y1,
            float x2, float y2, float espessura, Color cor) {
        formas.setColor(cor);
        formas.rectLine(x1, y1, x2, y2, espessura);
        formas.circle(x1, y1, espessura / 2f, 10);
        formas.circle(x2, y2, espessura / 2f, 10);
    }

    /** Linha tracejada, usada para indicar a rota de retorno memorizada. */
    public static void linhaTracejada(ShapeRenderer formas, float x1, float y1,
            float x2, float y2, float espessura, float traco, float vao,
            Color cor) {
        formas.setColor(cor);
        float comprimento = (float) Math.hypot(x2 - x1, y2 - y1);
        if (comprimento < 0.001f) {
            return;
        }
        float direcaoX = (x2 - x1) / comprimento;
        float direcaoY = (y2 - y1) / comprimento;

        float percorrido = 0f;
        while (percorrido < comprimento) {
            float fim = Math.min(percorrido + traco, comprimento);
            formas.rectLine(
                    x1 + direcaoX * percorrido, y1 + direcaoY * percorrido,
                    x1 + direcaoX * fim, y1 + direcaoY * fim,
                    espessura);
            percorrido = fim + vao;
        }
    }

    // -----------------------------------------------------------------------
    //  Texto
    // -----------------------------------------------------------------------

    /** Escreve o texto com a base da primeira linha em {@code y}. */
    public static void texto(SpriteBatch lote, BitmapFont fonte, String conteudo,
            float x, float y, Color cor) {
        fonte.setColor(cor);
        fonte.draw(lote, conteudo, x, y);
    }

    /** Escreve o texto centralizado horizontalmente em torno de {@code centroX}. */
    public static void textoCentralizado(SpriteBatch lote, BitmapFont fonte,
            String conteudo, float centroX, float y, Color cor) {
        LAYOUT.setText(fonte, conteudo);
        fonte.setColor(cor);
        fonte.draw(lote, conteudo, centroX - LAYOUT.width / 2f, y);
    }

    /** Escreve o texto alinhado pela borda direita em {@code direita}. */
    public static void textoDireita(SpriteBatch lote, BitmapFont fonte,
            String conteudo, float direita, float y, Color cor) {
        LAYOUT.setText(fonte, conteudo);
        fonte.setColor(cor);
        fonte.draw(lote, conteudo, direita - LAYOUT.width, y);
    }

    /** Largura que o texto ocuparia com a fonte informada. */
    public static float largura(BitmapFont fonte, String conteudo) {
        LAYOUT.setText(fonte, conteudo);
        return LAYOUT.width;
    }

    // -----------------------------------------------------------------------
    //  Interpolações de apoio
    // -----------------------------------------------------------------------

    /** Suavização clássica: começa devagar, acelera e desacelera no fim. */
    public static float suavizar(float fator) {
        float f = MathUtils.clamp(fator, 0f, 1f);
        return f * f * (3f - 2f * f);
    }

    /** Oscilação entre 0 e 1 no tempo, para pulsações e respiros. */
    public static float pulsar(float tempo, float velocidade) {
        return 0.5f + 0.5f * MathUtils.sin(tempo * velocidade);
    }
}
