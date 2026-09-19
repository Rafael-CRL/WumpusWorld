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
    public static final Color FUNDO_BAIXO = cor("1A1D24");
    public static final Color FUNDO_ALTO = cor("1A1D24");
    public static final Color PAINEL = cor("222630");
    public static final Color PAINEL_INTERNO = cor("1E212A");
    public static final Color PAINEL_DESTAQUE = cor("2A2F3D");
    public static final Color BORDA = cor("343A4A");
    public static final Color BORDA_FORTE = cor("454D62");
    public static final Color SOMBRA = new Color(0f, 0f, 0f, 0f);

    // Texto -------------------------------------------------------------------
    public static final Color TEXTO = cor("E2E4E9");
    public static final Color TEXTO_SUAVE = cor("9DA4B4");
    public static final Color TEXTO_FRACO = cor("687082");

    // Elementos do mundo ------------------------------------------------------
    public static final Color AGENTE = cor("4C8BF5");
    public static final Color AGENTE_ESCURO = cor("2B4C8C");
    public static final Color AGENTE_BRILHO = cor("7AA7F8");
    public static final Color OURO = cor("D9A74A");
    public static final Color OURO_CLARO = cor("E5C37E");
    public static final Color OURO_ESCURO = cor("8C6826");
    public static final Color WUMPUS = cor("8A5CB8");
    public static final Color WUMPUS_ESCURO = cor("4D3369");
    public static final Color POCO = cor("12141A");
    public static final Color POCO_BORDA = cor("2C303D");
    public static final Color INICIO = cor("3EA97D");

    // Percepções --------------------------------------------------------------
    public static final Color BRISA = cor("5FA8D3");
    public static final Color FEDOR = cor("7EBC65");
    public static final Color BRILHO = cor("D9A74A");

    // Estados -----------------------------------------------------------------
    public static final Color PERIGO = cor("D9534F");
    public static final Color SUCESSO = cor("3EA97D");
    public static final Color ALERTA = cor("D99B4A");
    public static final Color NEUTRO = cor("687082");

    // Casas do tabuleiro ------------------------------------------------------
    public static final Color CASA_DESCONHECIDA = cor("16181F");
    public static final Color CASA_VISITADA = cor("222630");
    public static final Color CASA_ATUAL = cor("2B3344");
    public static final Color CASA_INICIAL = cor("1F2E28");
    public static final Color GRADE = cor("2E3342");

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
