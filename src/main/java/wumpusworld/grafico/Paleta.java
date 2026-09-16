package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;

import wumpusworld.nucleo.TipoEvento;

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
    public static final Color FUNDO_BAIXO = cor("070A11");
    public static final Color FUNDO_ALTO = cor("121A2B");
    public static final Color PAINEL = cor("141B29");
    public static final Color PAINEL_INTERNO = cor("0F1520");
    public static final Color PAINEL_DESTAQUE = cor("1B2434");
    public static final Color BORDA = cor("253048");
    public static final Color BORDA_FORTE = cor("3A4B70");
    public static final Color SOMBRA = new Color(0f, 0f, 0f, 0.45f);

    // Texto -------------------------------------------------------------------
    public static final Color TEXTO = cor("E9EDF6");
    public static final Color TEXTO_SUAVE = cor("98A2B9");
    public static final Color TEXTO_FRACO = cor("5F6C87");

    // Elementos do mundo ------------------------------------------------------
    public static final Color AGENTE = cor("5CC8F5");
    public static final Color AGENTE_ESCURO = cor("1B5F82");
    public static final Color AGENTE_BRILHO = cor("A9E6FF");
    public static final Color OURO = cor("F3C55A");
    public static final Color OURO_CLARO = cor("FFE7A8");
    public static final Color OURO_ESCURO = cor("9A7226");
    public static final Color WUMPUS = cor("A96BDE");
    public static final Color WUMPUS_ESCURO = cor("5B2E86");
    public static final Color POCO = cor("05070C");
    public static final Color POCO_BORDA = cor("2E2438");
    public static final Color INICIO = cor("5FD9A0");

    // Percepções --------------------------------------------------------------
    public static final Color BRISA = cor("6FD3F7");
    public static final Color FEDOR = cor("9BE07A");
    public static final Color BRILHO = cor("F3C55A");

    // Estados -----------------------------------------------------------------
    public static final Color PERIGO = cor("FF6B6B");
    public static final Color SUCESSO = cor("5FD9A0");
    public static final Color ALERTA = cor("FFB454");
    public static final Color NEUTRO = cor("7C8AA5");

    // Casas do tabuleiro ------------------------------------------------------
    public static final Color CASA_DESCONHECIDA = cor("0E1421");
    public static final Color CASA_VISITADA = cor("1A2536");
    public static final Color CASA_ATUAL = cor("223A52");
    public static final Color CASA_INICIAL = cor("16302A");
    public static final Color GRADE = cor("222D42");

    /** Cor da linha do registro, de acordo com a natureza do acontecimento. */
    public static Color doEvento(TipoEvento tipo) {
        return switch (tipo) {
            case SISTEMA -> TEXTO_FRACO;
            case PERCEPCAO -> BRISA;
            case DECISAO -> TEXTO_SUAVE;
            case FLECHA -> ALERTA;
            case TESOURO -> OURO;
            case PERIGO -> PERIGO;
            case VITORIA -> SUCESSO;
            case DERROTA -> PERIGO;
        };
    }

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
