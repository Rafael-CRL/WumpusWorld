package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import wumpusworld.nucleo.Direcao;

/** Marcas geométricas auxiliares que não possuem um sprite próprio. */
public final class Icones {

    private Icones() {
    }

    private static float anguloDe(Direcao direcao) {
        return switch (direcao) {
            case DIREITA -> 0f;
            case CIMA -> 90f;
            case ESQUERDA -> 180f;
            case BAIXO -> 270f;
        };
    }

    public static void caveira(ShapeRenderer formas, float x, float y, float lado) {
        float raio = lado * .2f;
        formas.setColor(Paleta.PERIGO);
        formas.circle(x, y + raio * .2f, raio, 18);
        formas.rect(x - raio * .5f, y - raio, raio, raio * .7f);
        formas.setColor(Paleta.FUNDO_BAIXO);
        formas.circle(x - raio * .35f, y + raio * .25f, raio * .2f, 10);
        formas.circle(x + raio * .35f, y + raio * .25f, raio * .2f, 10);
    }

    public static void seloDeBrisa(ShapeRenderer formas, float x, float y,
            float tamanho, float alfa) {
        formas.setColor(Paleta.TEXTO_SUAVE);
        for (int indice = -1; indice <= 1; indice++) {
            formas.rect(x - tamanho / 2f, y + indice * tamanho * .3f,
                    tamanho, Math.max(1f, tamanho * .12f));
        }
    }

    public static void seloDeFedor(ShapeRenderer formas, float x, float y,
            float tamanho, float alfa) {
        formas.setColor(Paleta.TEXTO_SUAVE);
        formas.circle(x - tamanho * .25f, y, tamanho * .18f, 10);
        formas.circle(x + tamanho * .15f, y + tamanho * .2f, tamanho * .22f, 10);
    }

    public static void seloDeBrilho(ShapeRenderer formas, float x, float y,
            float tamanho, float alfa) {
        formas.setColor(Paleta.OURO);
        formas.triangle(x, y + tamanho * .4f, x + tamanho * .4f, y,
                x, y - tamanho * .4f);
        formas.triangle(x, y + tamanho * .4f, x, y - tamanho * .4f,
                x - tamanho * .4f, y);
    }

    public static void flecha(ShapeRenderer formas, float x, float y,
            Direcao direcao, float comprimento, Color cor) {
        float angulo = anguloDe(direcao) * MathUtils.degreesToRadians;
        float dx = MathUtils.cos(angulo);
        float dy = MathUtils.sin(angulo);
        Desenho.linha(formas, x - dx * comprimento / 2f, y - dy * comprimento / 2f,
                x + dx * comprimento / 2f, y + dy * comprimento / 2f, 2f, cor);
    }
}
