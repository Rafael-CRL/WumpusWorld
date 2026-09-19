package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import wumpusworld.nucleo.Direcao;

/** Ícones geométricos simples, sem animações, brilho ou transparência. */
public final class Icones {
    private Icones() { }

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
        if (!vivo) { caveira(formas, x, y, lado); return; }
        float r = lado * .22f;
        formas.setColor(Paleta.AGENTE);
        formas.circle(x, y, r, 20);
        float a = anguloDe(olhar) * MathUtils.degreesToRadians;
        float dx = MathUtils.cos(a), dy = MathUtils.sin(a);
        formas.setColor(Paleta.TEXTO);
        formas.triangle(x + dx * r * 1.45f, y + dy * r * 1.45f,
                x - dy * r * .45f, y + dx * r * .45f,
                x + dy * r * .45f, y - dx * r * .45f);
        if (comOuro) { formas.setColor(Paleta.OURO); formas.circle(x + r*.8f, y-r*.8f, r*.3f, 12); }
    }

    public static void caveira(ShapeRenderer formas, float x, float y, float lado) {
        float r = lado * .2f;
        formas.setColor(Paleta.PERIGO); formas.circle(x, y + r*.2f, r, 18);
        formas.rect(x-r*.5f, y-r, r, r*.7f);
        formas.setColor(Paleta.FUNDO_BAIXO);
        formas.circle(x-r*.35f, y+r*.25f, r*.2f, 10);
        formas.circle(x+r*.35f, y+r*.25f, r*.2f, 10);
    }

    public static void poco(ShapeRenderer formas, float x, float y, float lado, float tempo) {
        formas.setColor(Paleta.POCO_BORDA); formas.circle(x, y, lado*.3f, 20);
        formas.setColor(Paleta.POCO); formas.circle(x, y, lado*.22f, 20);
    }

    public static void wumpus(ShapeRenderer formas, float x, float y, float lado, float tempo) {
        float r = lado*.25f;
        formas.setColor(Paleta.WUMPUS); formas.circle(x, y, r, 20);
        formas.setColor(Paleta.PERIGO);
        formas.circle(x-r*.35f, y+r*.2f, r*.12f, 10);
        formas.circle(x+r*.35f, y+r*.2f, r*.12f, 10);
        formas.setColor(Paleta.FUNDO_BAIXO); formas.rect(x-r*.45f, y-r*.35f, r*.9f, r*.15f);
    }

    public static void ouro(ShapeRenderer formas, float x, float y, float lado, float tempo) {
        float r = lado*.25f; formas.setColor(Paleta.OURO);
        formas.triangle(x, y+r, x+r, y, x, y-r);
        formas.triangle(x, y+r, x, y-r, x-r, y);
    }

    public static void saida(ShapeRenderer formas, float x, float y, float lado,
            boolean destacada, float tempo) {
        float r = lado*.24f; formas.setColor(Paleta.INICIO); formas.rect(x-r, y-r, r*2f, r*2f);
        formas.setColor(Paleta.PAINEL_INTERNO); formas.rect(x-r*.45f, y-r, r*.9f, r*1.25f);
    }

    public static void seloDeBrisa(ShapeRenderer formas, float x, float y, float t, float alfa) {
        formas.setColor(Paleta.TEXTO_SUAVE);
        for (int i = -1; i <= 1; i++) formas.rect(x-t/2f, y+i*t*.3f, t, Math.max(1f, t*.12f));
    }

    public static void seloDeFedor(ShapeRenderer formas, float x, float y, float t, float alfa) {
        formas.setColor(Paleta.TEXTO_SUAVE); formas.circle(x-t*.25f, y, t*.18f, 10); formsCircle(formas, x+t*.15f, y+t*.2f, t*.22f);
    }

    private static void formsCircle(ShapeRenderer formas, float x, float y, float r) { formas.circle(x, y, r, 10); }

    public static void seloDeBrilho(ShapeRenderer formas, float x, float y, float t, float alfa) {
        formas.setColor(Paleta.OURO);
        formas.triangle(x, y+t*.4f, x+t*.4f, y, x, y-t*.4f);
        formas.triangle(x, y+t*.4f, x, y-t*.4f, x-t*.4f, y);
    }

    public static void flecha(ShapeRenderer formas, float x, float y, Direcao direcao,
            float comprimento, Color cor) {
        float a = anguloDe(direcao)*MathUtils.degreesToRadians;
        float dx=MathUtils.cos(a), dy=MathUtils.sin(a);
        Desenho.linha(formas, x-dx*comprimento/2f, y-dy*comprimento/2f,
                x+dx*comprimento/2f, y+dy*comprimento/2f, 2f, cor);
    }
}
