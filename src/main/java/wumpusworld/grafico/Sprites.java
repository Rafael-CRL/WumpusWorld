package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Desenho procedural dos elementos do jogo com primitivas geométricas.
 * Todos os métodos esperam um {@link ShapeRenderer} aberto em modo preenchido, com
 * transparência habilitada, e usam o tempo (em segundos) apenas para animações ambiente.
 */
final class Sprites {
    private static final Color NEVOA_TOPO = Color.valueOf("1D2C3F");
    private static final Color NEVOA_BASE = Color.valueOf("182739");
    private static final Color VISITADA_TOPO = Color.valueOf("2B4E56");
    private static final Color VISITADA_BASE = Color.valueOf("21414A");
    private static final Color BRILHO = new Color(1, 1, 1, 0.07f);
    private static final Color BORDA_DO_POCO = Color.valueOf("3B5670");
    private static final Color FUNDO_DO_POCO = Color.valueOf("0A1119");
    private static final Color CENTRO_ESCURO = Color.valueOf("0F1B29");
    private static final Color CENTRO_CLARO = Color.valueOf("1E3247");
    private static final Color HALO_DO_OURO = Color.valueOf("FFAA33");
    private static final Color OURO_ESCURO = Color.valueOf("BE8C38");
    private static final Color SOMBRA = new Color(0, 0, 0, 0.35f);
    private static final Color TEMPORARIA = new Color();

    private Sprites() { }

    /** Casa com degradê vertical e um filete de luz no topo. */
    static void casa(ShapeRenderer f, float x, float y, float tamanho, boolean visitada) {
        Color topo = visitada ? VISITADA_TOPO : NEVOA_TOPO;
        Color base = visitada ? VISITADA_BASE : NEVOA_BASE;
        f.rect(x, y, tamanho, tamanho, base, base, topo, topo);
        f.setColor(BRILHO);
        f.rect(x, y + tamanho - 2, tamanho, 2);
    }

    /** Marca de uma casa ainda desconhecida. */
    static void nevoa(ShapeRenderer f, float x, float y) {
        f.setColor(Tema.BORDA);
        f.circle(x, y, 3, 12);
    }

    /** Base em [0,0]. Com o ouro em mãos, pulsa para indicar o destino do agente. */
    static void base(ShapeRenderer f, float x, float y, float passo, float tempo, boolean chamando) {
        float r = passo * 0.22f;
        if (chamando) {
            float fase = (tempo * 0.9f) % 1f;
            f.setColor(Tema.VERDE.r, Tema.VERDE.g, Tema.VERDE.b, 0.22f * (1 - fase));
            f.circle(x, y, passo * (0.22f + 0.22f * fase), 32);
        }
        f.setColor(Tema.BORDA);
        f.triangle(x - r, y - r * 0.6f, x + r, y - r * 0.6f, x, y + r);
        f.setColor(Tema.VERDE);
        f.rect(x - 3, y - r * 0.6f, 6, r);
    }

    /** Poço com borda, fundo escuro e um centro que pulsa suavemente. */
    static void poco(ShapeRenderer f, float x, float y, float passo, float tempo) {
        float r = passo * 0.18f;
        float pulso = 0.5f + 0.5f * MathUtils.sin(tempo * 2f);
        f.setColor(BORDA_DO_POCO);
        f.ellipse(x - r * 1.5f, y - r * 0.85f, r * 3f, r * 1.7f, 32);
        f.setColor(FUNDO_DO_POCO);
        f.ellipse(x - r * 1.3f, y - r * 0.7f, r * 2.6f, r * 1.4f, 32);
        f.setColor(TEMPORARIA.set(CENTRO_ESCURO).lerp(CENTRO_CLARO, pulso));
        f.ellipse(x - r * 0.8f, y - r * 0.4f, r * 1.6f, r * 0.8f, 32);
    }

    /** Ouro: losango facetado que flutua, com halo e brilho. */
    static void ouro(ShapeRenderer f, float x, float y, float passo, float tempo) {
        float r = passo * 0.18f;
        float pulso = 0.5f + 0.5f * MathUtils.sin(tempo * 3f);
        y += MathUtils.sin(tempo * 2.2f) * r * 0.15f;
        f.setColor(HALO_DO_OURO.r, HALO_DO_OURO.g, HALO_DO_OURO.b, 0.035f + 0.03f * pulso);
        for (int anel = 0; anel < 4; anel++) {
            f.circle(x, y, r * (2.5f - 0.5f * anel), 28);
        }
        f.setColor(Tema.OURO);
        f.triangle(x - r, y, x, y + r * 1.3f, x, y - r * 1.3f);
        f.setColor(OURO_ESCURO);
        f.triangle(x, y + r * 1.3f, x + r, y, x, y - r * 1.3f);
        float brilho = r * 0.5f * pulso;
        float bx = x + r * 1.4f;
        float by = y + r * 1.2f;
        f.setColor(1, 1, 1, 0.5f + 0.5f * pulso);
        f.rectLine(bx - brilho, by, bx + brilho, by, 2);
        f.rectLine(bx, by - brilho, bx, by + brilho, 2);
    }

    /** Wumpus com orelhas, presas e olhos que piscam. */
    static void wumpus(ShapeRenderer f, float x, float y, float passo, float tempo) {
        float r = passo * 0.19f;
        y += MathUtils.sin(tempo * 2.5f) * r * 0.08f;
        f.setColor(Tema.PERIGO);
        f.circle(x, y, r, 24);
        f.triangle(x - r, y, x - r * 1.2f, y + r * 1.5f, x - r * 0.2f, y + r * 0.7f);
        f.triangle(x + r, y, x + r * 1.2f, y + r * 1.5f, x + r * 0.2f, y + r * 0.7f);
        f.setColor(Tema.FUNDO);
        f.rect(x - r * 0.4f, y - r * 0.4f, r * 0.8f, r * 0.14f);
        boolean piscando = tempo % 3.2f > 3.05f;
        for (int lado = -1; lado <= 1; lado += 2) {
            float ex = x + lado * r * 0.4f;
            if (piscando) {
                f.rect(ex - 3, y + 1, 6, 2);
            } else {
                f.circle(ex, y + 2, 3, 12);
            }
        }
        f.setColor(Tema.TEXTO);
        f.triangle(x - r * 0.35f, y - r * 0.3f, x - r * 0.1f, y - r * 0.3f, x - r * 0.22f, y - r * 0.7f);
        f.triangle(x + r * 0.1f, y - r * 0.3f, x + r * 0.35f, y - r * 0.3f, x + r * 0.22f, y - r * 0.7f);
    }

    /**
     * Explorador de capacete.
     *
     * @param salto altura do passo, em unidades de tela; zero quando parado
     * @param olhar -1, 0 ou 1: para onde o visor aponta na horizontal
     */
    static void explorador(ShapeRenderer f, float x, float y, float passo, boolean vivo,
                           boolean ouro, float salto, float olhar) {
        float r = passo * 0.17f;
        float altura = MathUtils.clamp(salto / (r * 0.5f), 0, 1);
        f.setColor(SOMBRA.r, SOMBRA.g, SOMBRA.b, 0.35f - 0.15f * altura);
        f.ellipse(x - r * (0.9f - 0.25f * altura), y - r * 1.15f, r * (1.8f - 0.5f * altura), r * 0.5f, 24);
        y += salto;
        f.setColor(Tema.FUNDO);
        f.circle(x, y, r * 1.35f, 32);
        f.setColor(vivo ? Tema.VERDE : Tema.PERIGO);
        if (!vivo) {
            f.rectLine(x - r * 0.6f, y - r * 0.6f, x + r * 0.6f, y + r * 0.6f, 5);
            f.rectLine(x - r * 0.6f, y + r * 0.6f, x + r * 0.6f, y - r * 0.6f, 5);
            return;
        }
        f.circle(x, y + r * 0.4f, r * 0.7f, 24);
        f.rect(x - r * 0.7f, y - r * 0.9f, r * 1.4f, r * 0.9f);
        f.setColor(Tema.FUNDO);
        f.rect(x - r * 0.4f + olhar * r * 0.12f, y + r * 0.25f, r * 0.8f, r * 0.25f);
        if (ouro) {
            f.setColor(Tema.OURO.r, Tema.OURO.g, Tema.OURO.b, 0.3f);
            f.circle(x, y + r, 9, 16);
        }
        f.setColor(ouro ? Tema.OURO : Tema.TEXTO);
        f.circle(x, y + r, ouro ? 4 : 3, 12);
    }
}
