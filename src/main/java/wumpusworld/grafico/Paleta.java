package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;


/**
 * Identidade visual da aplicação.
 *
 * <p>Todas as cores ficam reunidas aqui para que a tela tenha um tema coerente
 * e para que ajustes de contraste sejam feitos num lugar só. O tema é escuro,
 * remetendo ao interior da caverna, com acentos frios para o agente e quentes
 * para o ouro.</p>
 */
public final class Paleta {

    private Paleta() {
    }

    // Fundo e superfícies -----------------------------------------------------
    public static final Color FUNDO_BAIXO = cor("252525");
    public static final Color FUNDO_ALTO = FUNDO_BAIXO;
    public static final Color PAINEL = cor("303030");
    public static final Color PAINEL_INTERNO = cor("292929");
    public static final Color PAINEL_DESTAQUE = cor("383838");
    public static final Color BORDA = cor("505050");
    public static final Color BORDA_FORTE = BORDA;

    // Texto -------------------------------------------------------------------
    public static final Color TEXTO = cor("E8E8E8");
    public static final Color TEXTO_SUAVE = cor("B8B8B8");
    public static final Color TEXTO_FRACO = cor("909090");

    // Elementos do mundo ------------------------------------------------------
    public static final Color AGENTE = cor("6D9FC1");
    public static final Color AGENTE_ESCURO = AGENTE;
    public static final Color AGENTE_BRILHO = AGENTE;
    public static final Color OURO = cor("B99A4B");
    public static final Color OURO_CLARO = OURO;
    public static final Color OURO_ESCURO = OURO;
    public static final Color WUMPUS = cor("8B7896");
    public static final Color WUMPUS_ESCURO = WUMPUS;
    public static final Color POCO = cor("151515");
    public static final Color POCO_BORDA = cor("606060");
    public static final Color INICIO = AGENTE;

    // Percepções --------------------------------------------------------------
    public static final Color BRISA = AGENTE;
    public static final Color FEDOR = AGENTE;
    public static final Color BRILHO = OURO;

    // Estados -----------------------------------------------------------------
    public static final Color PERIGO = cor("B96A6A");
    public static final Color SUCESSO = AGENTE;
    public static final Color ALERTA = OURO;
    public static final Color NEUTRO = TEXTO_SUAVE;

    // Casas do tabuleiro ------------------------------------------------------
    public static final Color CASA_DESCONHECIDA = cor("0E1421");
    public static final Color CASA_VISITADA = cor("1A2536");
    public static final Color CASA_ATUAL = cor("223A52");
    public static final Color CASA_INICIAL = cor("16302A");
    public static final Color GRADE = cor("222D42");

    /** Converte "RRGGBB" em uma cor opaca. */
    private static Color cor(String hexadecimal) {
        return Color.valueOf(hexadecimal + "FF");
    }

    /** Devolve uma cópia da cor com outra transparência. */
    public static Color comAlfa(Color base, float alfa) {
        return new Color(base.r, base.g, base.b, alfa);
    }

    /** Mistura duas cores; {@code fator} 0 devolve a primeira, 1 a segunda. */
    public static Color misturar(Color inicial, Color fim, float fator) {
        float f = Math.max(0f, Math.min(1f, fator));
        return new Color(
                inicial.r + (fim.r - inicial.r) * f,
                inicial.g + (fim.g - inicial.g) * f,
                inicial.b + (fim.b - inicial.b) * f,
                inicial.a + (fim.a - inicial.a) * f);
    }
}
