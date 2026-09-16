package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import wumpusworld.nucleo.Direcao;

/**
 * Desenho vetorial dos elementos do mundo.
 *
 * <p>Nenhum ícone vem de arquivo de imagem: todos são compostos por círculos,
 * triângulos, anéis e polígonos calculados a partir do centro e do tamanho da
 * casa. Isso mantém o projeto sem dependência de recursos externos, deixa as
 * figuras nítidas em qualquer escala e permite animá-las (pulsação, oscilação
 * e brilho) apenas variando o tempo.</p>
 *
 * <p>Todos os métodos esperam o {@link ShapeRenderer} já aberto no modo
 * {@code Filled}, com mistura de transparência habilitada.</p>
 */
public final class Icones {

    private Icones() {
    }

    /** Ângulo, em graus, correspondente a cada direção da matriz. */
    private static float anguloDe(Direcao direcao) {
        return switch (direcao) {
            case DIREITA -> 0f;
            case CIMA -> 90f;
            case ESQUERDA -> 180f;
            case BAIXO -> 270f;
        };
    }

    // -----------------------------------------------------------------------
    //  Agente
    // -----------------------------------------------------------------------

    /**
     * Explorador visto de cima: um capacete com lanterna apontando para a
     * direção do último movimento.
     *
     * @param lado     lado da casa, usado como referência de tamanho
     * @param olhar    direção para onde o agente está voltado
     * @param vivo     define se o ícone é o explorador ou a caveira
     * @param comOuro  acrescenta o anel dourado do tesouro recolhido
     */
    public static void agente(ShapeRenderer formas, float centroX, float centroY,
            float lado, Direcao olhar, boolean vivo, boolean comOuro, float tempo) {

        if (!vivo) {
            caveira(formas, centroX, centroY, lado);
            return;
        }

        float raio = lado * 0.22f;
        float angulo = anguloDe(olhar) * MathUtils.degreesToRadians;
        float cosseno = MathUtils.cos(angulo);
        float seno = MathUtils.sin(angulo);

        // Aura respirando ao redor do agente.
        float pulso = Desenho.pulsar(tempo, 3.2f);
        for (int camada = 3; camada >= 1; camada--) {
            // O maior raio fica em 1,81 × raio, ou seja, dentro da própria casa.
            float raioAura = raio * (1.15f + camada * 0.22f + pulso * 0.10f);
            formas.setColor(Paleta.comAlfa(Paleta.AGENTE, 0.07f * camada));
            formas.circle(centroX, centroY, raioAura, 40);
        }

        // Feixe da lanterna, apontando para onde o agente decidiu ir.
        float alcance = raio * 2.05f;
        float meiaBase = raio * 1.05f;
        float pontaX = centroX + cosseno * alcance;
        float pontaY = centroY + seno * alcance;
        float perpendicularX = -seno * meiaBase;
        float perpendicularY = cosseno * meiaBase;
        formas.setColor(Paleta.comAlfa(Paleta.AGENTE_BRILHO, 0.12f + pulso * 0.05f));
        formas.triangle(centroX, centroY,
                pontaX + perpendicularX, pontaY + perpendicularY,
                pontaX - perpendicularX, pontaY - perpendicularY);

        // Corpo: aro externo, capacete e lanterna.
        formas.setColor(Paleta.AGENTE_ESCURO);
        formas.circle(centroX, centroY, raio * 1.16f, 36);
        formas.setColor(Paleta.AGENTE);
        formas.circle(centroX, centroY, raio * 0.94f, 36);

        formas.setColor(Paleta.comAlfa(Paleta.AGENTE_ESCURO, 0.55f));
        formas.circle(centroX - cosseno * raio * 0.22f,
                centroY - seno * raio * 0.22f, raio * 0.58f, 28);

        formas.setColor(Paleta.AGENTE_BRILHO);
        formas.circle(centroX + cosseno * raio * 0.40f,
                centroY + seno * raio * 0.40f, raio * 0.34f, 24);

        // Ponta triangular indicando o sentido do movimento.
        float distanciaDaPonta = raio * 1.52f;
        float meiaPonta = raio * 0.46f;
        float baseX = centroX + cosseno * raio * 1.02f;
        float baseY = centroY + seno * raio * 1.02f;
        formas.setColor(Paleta.AGENTE_BRILHO);
        formas.triangle(
                centroX + cosseno * distanciaDaPonta,
                centroY + seno * distanciaDaPonta,
                baseX - seno * meiaPonta, baseY + cosseno * meiaPonta,
                baseX + seno * meiaPonta, baseY - cosseno * meiaPonta);

        if (comOuro) {
            Desenho.anel(formas, centroX, centroY,
                    raio * 1.30f, raio * 1.52f,
                    Paleta.comAlfa(Paleta.OURO, 0.75f + pulso * 0.25f), 40);
        }
    }

    /** Marca da derrota: uma caveira simples no lugar do explorador. */
    public static void caveira(ShapeRenderer formas, float centroX, float centroY,
            float lado) {
        float raio = lado * 0.21f;

        formas.setColor(Paleta.comAlfa(Paleta.PERIGO, 0.16f));
        formas.circle(centroX, centroY, raio * 2.0f, 34);

        formas.setColor(Paleta.TEXTO);
        formas.circle(centroX, centroY + raio * 0.18f, raio, 32);
        formas.rect(centroX - raio * 0.52f, centroY - raio * 1.05f,
                raio * 1.04f, raio * 0.75f);

        formas.setColor(Paleta.POCO);
        formas.circle(centroX - raio * 0.40f, centroY + raio * 0.26f, raio * 0.30f, 20);
        formas.circle(centroX + raio * 0.40f, centroY + raio * 0.26f, raio * 0.30f, 20);
        formas.rect(centroX - raio * 0.12f, centroY - raio * 0.40f,
                raio * 0.24f, raio * 0.30f);

        formas.setColor(Paleta.PERIGO);
        formas.rect(centroX - raio * 0.44f, centroY - raio * 1.02f, raio * 0.12f, raio * 0.68f);
        formas.rect(centroX - raio * 0.06f, centroY - raio * 1.02f, raio * 0.12f, raio * 0.68f);
        formas.rect(centroX + raio * 0.32f, centroY - raio * 1.02f, raio * 0.12f, raio * 0.68f);
    }

    // -----------------------------------------------------------------------
    //  Poço
    // -----------------------------------------------------------------------

    /** Buraco escuro com profundidade sugerida por anéis concêntricos. */
    public static void poco(ShapeRenderer formas, float centroX, float centroY,
            float lado, float tempo) {
        float raio = lado * 0.32f;

        // Halo frio ao redor da boca do poço.
        formas.setColor(Paleta.comAlfa(Paleta.POCO_BORDA, 0.55f));
        formas.circle(centroX, centroY, raio * 1.14f, 44);

        Desenho.anel(formas, centroX, centroY, raio * 0.95f, raio * 1.08f,
                Paleta.comAlfa(Paleta.BORDA_FORTE, 0.9f), 44);

        int camadas = 8;
        for (int camada = 0; camada < camadas; camada++) {
            float fator = camada / (float) (camadas - 1);
            float raioDaCamada = raio * (1f - fator * 0.82f);
            formas.setColor(Paleta.misturar(Paleta.POCO_BORDA, Paleta.POCO, fator));
            formas.circle(centroX, centroY - fator * raio * 0.10f,
                    raioDaCamada, 40);
        }

        // Pequenas pedras soltas na borda, para quebrar a simetria.
        float giro = tempo * 0.35f;
        formas.setColor(Paleta.comAlfa(Paleta.BORDA_FORTE, 0.6f));
        for (int pedra = 0; pedra < 5; pedra++) {
            float angulo = giro + pedra * MathUtils.PI2 / 5f;
            formas.circle(
                    centroX + MathUtils.cos(angulo) * raio * 1.18f,
                    centroY + MathUtils.sin(angulo) * raio * 1.18f,
                    raio * 0.06f, 10);
        }
    }

    // -----------------------------------------------------------------------
    //  Wumpus
    // -----------------------------------------------------------------------

    /** Criatura roxa com chifres, olhos vermelhos e dentes. */
    public static void wumpus(ShapeRenderer formas, float centroX, float centroY,
            float lado, float tempo) {
        float raio = lado * 0.25f;
        float balanco = MathUtils.sin(tempo * 2.1f) * lado * 0.014f;
        float y = centroY + balanco;

        formas.setColor(Paleta.comAlfa(Paleta.WUMPUS, 0.14f));
        formas.circle(centroX, y, raio * 1.85f, 38);

        // Chifres.
        formas.setColor(Paleta.WUMPUS_ESCURO);
        formas.triangle(
                centroX - raio * 0.86f, y + raio * 0.42f,
                centroX - raio * 0.30f, y + raio * 0.66f,
                centroX - raio * 0.62f, y + raio * 1.34f);
        formas.triangle(
                centroX + raio * 0.86f, y + raio * 0.42f,
                centroX + raio * 0.30f, y + raio * 0.66f,
                centroX + raio * 0.62f, y + raio * 1.34f);

        // Corpo.
        formas.setColor(Paleta.WUMPUS_ESCURO);
        formas.circle(centroX, y, raio * 1.08f, 40);
        formas.setColor(Paleta.WUMPUS);
        formas.circle(centroX, y, raio * 0.94f, 40);

        // Boca com dentes.
        formas.setColor(Paleta.POCO);
        formas.arc(centroX, y - raio * 0.10f, raio * 0.62f, 196f, 148f, 24);

        formas.setColor(Paleta.TEXTO);
        for (int dente = 0; dente < 4; dente++) {
            float dx = centroX - raio * 0.44f + dente * raio * 0.29f;
            formas.triangle(
                    dx, y - raio * 0.14f,
                    dx + raio * 0.19f, y - raio * 0.14f,
                    dx + raio * 0.095f, y - raio * 0.42f);
        }

        // Olhos.
        formas.setColor(Paleta.TEXTO);
        formas.circle(centroX - raio * 0.38f, y + raio * 0.30f, raio * 0.26f, 20);
        formas.circle(centroX + raio * 0.38f, y + raio * 0.30f, raio * 0.26f, 20);
        formas.setColor(Paleta.PERIGO);
        formas.circle(centroX - raio * 0.34f, y + raio * 0.28f, raio * 0.13f, 16);
        formas.circle(centroX + raio * 0.42f, y + raio * 0.28f, raio * 0.13f, 16);
    }

    // -----------------------------------------------------------------------
    //  Ouro
    // -----------------------------------------------------------------------

    /** Gema dourada com facetas e raios de brilho pulsantes. */
    public static void ouro(ShapeRenderer formas, float centroX, float centroY,
            float lado, float tempo) {
        float escala = lado * 0.30f;
        float pulso = Desenho.pulsar(tempo, 2.6f);

        // Halo.
        formas.setColor(Paleta.comAlfa(Paleta.OURO, 0.10f + pulso * 0.10f));
        formas.circle(centroX, centroY, escala * (1.9f + pulso * 0.22f), 40);

        // Raios de brilho em cruz.
        float comprimento = escala * (1.22f + pulso * 0.30f);
        formas.setColor(Paleta.comAlfa(Paleta.OURO_CLARO, 0.35f + pulso * 0.30f));
        for (int raio = 0; raio < 4; raio++) {
            float angulo = raio * MathUtils.PI / 2f + tempo * 0.25f;
            float pontaX = centroX + MathUtils.cos(angulo) * comprimento;
            float pontaY = centroY + MathUtils.sin(angulo) * comprimento;
            float ladoX = -MathUtils.sin(angulo) * escala * 0.10f;
            float ladoY = MathUtils.cos(angulo) * escala * 0.10f;
            formas.triangle(pontaX, pontaY,
                    centroX + ladoX, centroY + ladoY,
                    centroX - ladoX, centroY - ladoY);
        }

        // Corpo da gema: mesa no topo, ombros laterais e ponta embaixo.
        float[] contorno = {
            centroX - escala * 0.52f, centroY + escala * 0.34f,
            centroX + escala * 0.52f, centroY + escala * 0.34f,
            centroX + escala * 0.84f, centroY + escala * 0.02f,
            centroX, centroY - escala * 0.92f,
            centroX - escala * 0.84f, centroY + escala * 0.02f
        };
        Desenho.leque(formas, centroX, centroY, contorno, Paleta.OURO);

        // Facetas claras e escuras dão volume à pedra.
        formas.setColor(Paleta.OURO_CLARO);
        formas.triangle(
                centroX - escala * 0.52f, centroY + escala * 0.34f,
                centroX + escala * 0.52f, centroY + escala * 0.34f,
                centroX, centroY + escala * 0.02f);
        formas.setColor(Paleta.OURO_ESCURO);
        formas.triangle(
                centroX + escala * 0.52f, centroY + escala * 0.34f,
                centroX + escala * 0.84f, centroY + escala * 0.02f,
                centroX, centroY - escala * 0.92f);
        formas.setColor(Paleta.comAlfa(Paleta.OURO_CLARO, 0.55f));
        formas.triangle(
                centroX - escala * 0.52f, centroY + escala * 0.34f,
                centroX - escala * 0.84f, centroY + escala * 0.02f,
                centroX, centroY - escala * 0.92f);
    }

    // -----------------------------------------------------------------------
    //  Casa inicial
    // -----------------------------------------------------------------------

    /** Portal verde que marca a casa de partida e de retorno. */
    public static void saida(ShapeRenderer formas, float centroX, float centroY,
            float lado, boolean destacada, float tempo) {
        float largura = lado * 0.30f;
        float altura = lado * 0.30f;
        float pulso = destacada ? Desenho.pulsar(tempo, 3.4f) : 0.25f;

        formas.setColor(Paleta.comAlfa(Paleta.INICIO, 0.10f + pulso * 0.14f));
        formas.circle(centroX, centroY, lado * 0.30f, 36);

        Color corDoPortal = Paleta.comAlfa(Paleta.INICIO, 0.55f + pulso * 0.35f);
        formas.setColor(corDoPortal);
        formas.rect(centroX - largura / 2f, centroY - altura * 0.72f,
                largura, altura);
        formas.circle(centroX, centroY + altura * 0.28f, largura / 2f, 26);

        formas.setColor(Paleta.PAINEL_INTERNO);
        float larguraInterna = largura * 0.58f;
        formas.rect(centroX - larguraInterna / 2f, centroY - altura * 0.72f,
                larguraInterna, altura * 0.92f);
        formas.circle(centroX, centroY + altura * 0.20f, larguraInterna / 2f, 22);
    }

    // -----------------------------------------------------------------------
    //  Sinais de percepção (selos pequenos dentro da casa já visitada)
    // -----------------------------------------------------------------------

    /** Três traços curvos, sugerindo a corrente de ar de um poço próximo. */
    public static void seloDeBrisa(ShapeRenderer formas, float centroX,
            float centroY, float tamanho, float alfa) {
        Color cor = Paleta.comAlfa(Paleta.BRISA, alfa);
        for (int traco = 0; traco < 3; traco++) {
            float y = centroY + (traco - 1) * tamanho * 0.34f;
            float comprimento = tamanho * (traco == 1 ? 1.0f : 0.72f);
            Desenho.linha(formas,
                    centroX - comprimento / 2f, y,
                    centroX + comprimento / 2f, y,
                    tamanho * 0.16f, cor);
        }
    }

    /** Três bolhas subindo, sugerindo o cheiro do Wumpus. */
    public static void seloDeFedor(ShapeRenderer formas, float centroX,
            float centroY, float tamanho, float alfa) {
        formas.setColor(Paleta.comAlfa(Paleta.FEDOR, alfa));
        formas.circle(centroX - tamanho * 0.34f, centroY - tamanho * 0.22f,
                tamanho * 0.22f, 14);
        formas.circle(centroX + tamanho * 0.02f, centroY + tamanho * 0.06f,
                tamanho * 0.30f, 16);
        formas.circle(centroX + tamanho * 0.40f, centroY + tamanho * 0.36f,
                tamanho * 0.18f, 12);
    }

    /** Estrela de quatro pontas usada como selo do brilho do ouro. */
    public static void seloDeBrilho(ShapeRenderer formas, float centroX,
            float centroY, float tamanho, float alfa) {
        Color cor = Paleta.comAlfa(Paleta.BRILHO, alfa);
        Desenho.losango(formas, centroX, centroY,
                tamanho * 0.26f, tamanho * 0.85f, cor);
        Desenho.losango(formas, centroX, centroY,
                tamanho * 0.85f, tamanho * 0.26f, cor);
    }

    // -----------------------------------------------------------------------
    //  Flecha
    // -----------------------------------------------------------------------

    /** Flecha em voo, com haste, ponta e penas. */
    public static void flecha(ShapeRenderer formas, float centroX, float centroY,
            Direcao direcao, float comprimento, Color cor) {
        float angulo = anguloDe(direcao) * MathUtils.degreesToRadians;
        float cosseno = MathUtils.cos(angulo);
        float seno = MathUtils.sin(angulo);
        float metade = comprimento / 2f;

        float traseiraX = centroX - cosseno * metade;
        float traseiraY = centroY - seno * metade;
        float frenteX = centroX + cosseno * metade;
        float frenteY = centroY + seno * metade;

        float espessura = comprimento * 0.10f;
        Desenho.linha(formas, traseiraX, traseiraY,
                frenteX - cosseno * comprimento * 0.22f,
                frenteY - seno * comprimento * 0.22f, espessura, cor);

        // Ponta.
        float baseX = frenteX - cosseno * comprimento * 0.26f;
        float baseY = frenteY - seno * comprimento * 0.26f;
        float meiaBase = comprimento * 0.15f;
        formas.setColor(cor);
        formas.triangle(frenteX, frenteY,
                baseX - seno * meiaBase, baseY + cosseno * meiaBase,
                baseX + seno * meiaBase, baseY - cosseno * meiaBase);

        // Penas.
        float penaX = traseiraX + cosseno * comprimento * 0.10f;
        float penaY = traseiraY + seno * comprimento * 0.10f;
        float meiaPena = comprimento * 0.13f;
        formas.triangle(traseiraX, traseiraY,
                penaX - seno * meiaPena, penaY + cosseno * meiaPena,
                penaX + seno * meiaPena, penaY - cosseno * meiaPena);
    }
}
