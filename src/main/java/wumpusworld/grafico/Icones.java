package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import wumpusworld.nucleo.Direcao;

/** Ícones geométricos planos para os elementos do mundo. */
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

    public static void agente(ShapeRenderer formas, float x, float y, float lado,
            Direcao olhar, boolean vivo, boolean comOuro, float tempo) {
        if (!vivo) {
            caveira(formas, x, y, lado);
            return;
        }
        float raio = lado * .23f;
        float angulo = anguloDe(olhar) * MathUtils.degreesToRadians;
        float dx = MathUtils.cos(angulo);
        float dy = MathUtils.sin(angulo);

        formas.setColor(Paleta.AGENTE_ESCURO);
        formas.circle(x - dx * raio * .25f, y - dy * raio * .25f,
                raio * 1.08f, 20);
        formas.setColor(Paleta.AGENTE);
        formas.circle(x, y, raio, 20);
        formas.setColor(Paleta.TEXTO);
        formas.circle(x + dx * raio * .22f, y + dy * raio * .22f,
                raio * .38f, 16);
        formas.setColor(Paleta.AGENTE_BRILHO);
        formas.triangle(x + dx * raio * 1.42f, y + dy * raio * 1.42f,
                x - dy * raio * .42f, y + dx * raio * .42f,
                x + dy * raio * .42f, y - dx * raio * .42f);
        if (comOuro) {
            formas.setColor(Paleta.OURO);
            formas.circle(x + raio * .82f, y - raio * .82f, raio * .3f, 12);
        }
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

    public static void poco(ShapeRenderer formas, float x, float y, float lado,
            float tempo) {
        float raio = lado * .31f;
        formas.setColor(Paleta.POCO_BORDA);
        formas.circle(x, y, raio, 24);
        formas.setColor(Paleta.PAINEL_INTERNO);
        formas.circle(x, y - raio * .05f, raio * .76f, 24);
        formas.setColor(Paleta.POCO);
        formas.circle(x, y - raio * .12f, raio * .55f, 24);
        formas.setColor(Paleta.FUNDO_BAIXO);
        formas.circle(x + raio * .12f, y - raio * .18f, raio * .25f, 16);
    }

    public static void wumpus(ShapeRenderer formas, float x, float y, float lado,
            float tempo) {
        float raio = lado * .25f;
        formas.setColor(Paleta.WUMPUS_ESCURO);
        formas.triangle(x - raio * .82f, y + raio * .35f,
                x - raio * .35f, y + raio * .64f, x - raio * .56f, y + raio * 1.1f);
        formas.triangle(x + raio * .82f, y + raio * .35f,
                x + raio * .35f, y + raio * .64f, x + raio * .56f, y + raio * 1.1f);
        formas.setColor(Paleta.WUMPUS);
        formas.circle(x, y, raio * 1.08f, 22);
        formas.setColor(Paleta.PERIGO);
        formas.circle(x - raio * .35f, y + raio * .2f, raio * .14f, 10);
        formas.circle(x + raio * .35f, y + raio * .2f, raio * .14f, 10);
        formas.setColor(Paleta.POCO);
        formas.rect(x - raio * .52f, y - raio * .42f, raio * 1.04f, raio * .28f);
        formas.setColor(Paleta.TEXTO);
        for (int dente = 0; dente < 3; dente++) {
            float denteX = x - raio * .42f + dente * raio * .34f;
            formas.triangle(denteX, y - raio * .14f,
                    denteX + raio * .18f, y - raio * .14f,
                    denteX + raio * .09f, y - raio * .42f);
        }
    }

    public static void ouro(ShapeRenderer formas, float x, float y, float lado,
            float tempo) {
        float raio = lado * .25f;
        formas.setColor(Paleta.OURO);
        formas.triangle(x, y + raio, x + raio, y, x, y - raio);
        formas.triangle(x, y + raio, x, y - raio, x - raio, y);
    }

    /** Troféu dourado apoiado em uma pilha de moedas para a tela de vitória. */
    public static void trofeuComOuro(ShapeRenderer formas, float x, float y,
            float lado) {
        float raio = lado * .22f;
        formas.setColor(Paleta.OURO);
        formas.circle(x - raio * .55f, y - raio * 1.25f, raio * .34f, 14);
        formas.circle(x, y - raio * 1.32f, raio * .38f, 14);
        formas.circle(x + raio * .55f, y - raio * 1.25f, raio * .34f, 14);
        formas.rect(x - raio * .22f, y - raio * .9f, raio * .44f, raio * .48f);
        formas.rect(x - raio * .54f, y - raio * 1.02f, raio * 1.08f, raio * .18f);
        formas.triangle(x - raio * .72f, y + raio * .7f,
                x + raio * .72f, y + raio * .7f, x + raio * .38f, y - raio * .35f);
        formas.triangle(x - raio * .72f, y + raio * .7f,
                x + raio * .38f, y - raio * .35f, x - raio * .38f, y - raio * .35f);
        formas.rect(x - raio * 1.02f, y + raio * .18f, raio * .28f, raio * .16f);
        formas.rect(x + raio * .74f, y + raio * .18f, raio * .28f, raio * .16f);
    }

    public static void saida(ShapeRenderer formas, float x, float y, float lado,
            boolean destacada, float tempo) {
        float raio = lado * .24f;
        formas.setColor(Paleta.INICIO);
        formas.rect(x - raio, y - raio, raio * 2f, raio * 2f);
        formas.setColor(Paleta.PAINEL_INTERNO);
        formas.rect(x - raio * .45f, y - raio, raio * .9f, raio * 1.25f);
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
