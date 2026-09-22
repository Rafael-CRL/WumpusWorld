package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;

/**
 * Todas as cores da janela, num lugar só.
 *
 * <p>O tabuleiro e a legenda leem daqui as mesmas constantes, e por isso
 * nunca podem discordar sobre o que cada forma significa.</p>
 */
public final class Paleta {

    public static final Color FUNDO = Color.valueOf("1C1F26");
    public static final Color PAINEL = Color.valueOf("262A33");

    public static final Color CASA_DESCONHECIDA = Color.valueOf("323845");
    public static final Color CASA_CONHECIDA = Color.valueOf("BFC6D0");

    public static final Color POCO = Color.valueOf("15171C");
    public static final Color WUMPUS = Color.valueOf("C94A4A");
    public static final Color OURO = Color.valueOf("D4961F");
    public static final Color FLECHA = Color.valueOf("3F8F5C");
    public static final Color AGENTE = Color.valueOf("3D7BD9");

    public static final Color BOTAO = Color.valueOf("2E8B4F");
    public static final Color BOTAO_CONTORNO = Color.valueOf("6FD18E");

    public static final Color TEXTO = Color.valueOf("E4E7EC");
    public static final Color TEXTO_SUAVE = Color.valueOf("9AA3B0");

    private Paleta() {
    }
}
