package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import wumpusworld.nucleo.Direcao;

/**
 * Rodapé com a legenda dos símbolos e a lista de atalhos do teclado.
 *
 * <p>A legenda usa exatamente os mesmos desenhos do tabuleiro, em escala
 * menor: assim o que aparece aqui é garantidamente o que aparece no mapa.</p>
 */
public final class PainelRodape {

    private static final float MARGEM = 18f;

    /** Cada símbolo da legenda e o texto que o explica. */
    private enum Item {
        AGENTE("Agente"),
        POCO("Poço"),
        WUMPUS("Wumpus"),
        OURO("Ouro"),
        INICIO("Início / saída"),
        BRISA("Brisa"),
        FEDOR("Fedor"),
        RISCO("Risco estimado"),
        ROTA("Rota de retorno");

        private final String rotulo;

        Item(String rotulo) {
            this.rotulo = rotulo;
        }
    }

    private static final String ATALHOS =
            "ESPAÇO pausa  ·  ENTER avança um passo  ·  + / −  velocidade  ·  "
            + "R sorteia um novo mapa  ·  "
            + "H mostra o risco  ·  ESC encerra";

    private final Ativos ativos;

    private Area area = new Area(0f, 0f, 1f, 1f);
    private final float[] centrosDosIcones = new float[Item.values().length];
    private final float[] inicioDosRotulos = new float[Item.values().length];
    private float linhaDosSimbolos;
    private float linhaDosAtalhos;
    private float baseDoTitulo;

    public PainelRodape(Ativos ativos) {
        this.ativos = ativos;
    }

    public void definirArea(Area novaArea) {
        this.area = novaArea;

        baseDoTitulo = novaArea.topo() - MARGEM + 2f;
        linhaDosSimbolos = novaArea.topo() - MARGEM - 28f;
        linhaDosAtalhos = novaArea.y() + MARGEM + 11f;

        Item[] itens = Item.values();
        float larguraUtil = novaArea.largura() - MARGEM * 2f;
        float passo = larguraUtil / itens.length;

        for (int indice = 0; indice < itens.length; indice++) {
            float inicio = novaArea.x() + MARGEM + indice * passo;
            centrosDosIcones[indice] = inicio + 14f;
            inicioDosRotulos[indice] = inicio + 32f;
        }
    }

    public void desenharFormas(ShapeRenderer formas, float tempo) {
        Desenho.painel(formas, area, 4f, Paleta.PAINEL, Paleta.BORDA, 1f);

        // Faixa discreta atrás da linha de atalhos.
        Desenho.caixa(formas, area.x() + MARGEM - 6f, area.y() + MARGEM - 8f,
                area.largura() - MARGEM * 2f + 12f, 26f, 8f,
                Paleta.PAINEL_INTERNO);

        Item[] itens = Item.values();
        for (int indice = 0; indice < itens.length; indice++) {
            float cx = centrosDosIcones[indice];
            float cy = linhaDosSimbolos;
            float escala = 38f;

            switch (itens[indice]) {
                case AGENTE -> Icones.agente(formas, cx, cy, escala,
                        Direcao.DIREITA, true, false, tempo);
                case POCO -> Icones.poco(formas, cx, cy, escala, tempo);
                case WUMPUS -> Icones.wumpus(formas, cx, cy, escala, tempo);
                case OURO -> Icones.ouro(formas, cx, cy, escala, tempo);
                case INICIO -> Icones.saida(formas, cx, cy, escala, false, tempo);
                case BRISA -> Icones.seloDeBrisa(formas, cx, cy, 6.5f, 0.95f);
                case FEDOR -> Icones.seloDeFedor(formas, cx, cy, 8f, 0.95f);
                case RISCO -> {
                    Desenho.caixa(formas, cx - 11f, cy - 11f, 22f, 22f, 5f,
                            Paleta.comAlfa(Paleta.PERIGO, 0.32f));
                    Desenho.caixa(formas, cx - 11f, cy - 11f, 22f, 2f, 1f,
                            Paleta.comAlfa(Paleta.PERIGO, 0.8f));
                }
                case ROTA -> Desenho.linhaTracejada(formas,
                        cx - 12f, cy, cx + 12f, cy, 2.6f, 6f, 5f,
                        Paleta.comAlfa(Paleta.INICIO, 0.85f));
            }
        }
    }

    public void desenharTextos(SpriteBatch lote) {
        Desenho.texto(lote, ativos.fonteSecao, "LEGENDA",
                area.x() + MARGEM + 2f, baseDoTitulo, Paleta.TEXTO_SUAVE);
        Desenho.textoDireita(lote, ativos.fonteMiuda,
                "O MAPA COMPLETO SÓ É REVELADO AO FIM DA PARTIDA",
                area.direita() - MARGEM - 2f, baseDoTitulo - 2f,
                Paleta.TEXTO_FRACO);

        Item[] itens = Item.values();
        for (int indice = 0; indice < itens.length; indice++) {
            Desenho.texto(lote, ativos.fontePequena, itens[indice].rotulo,
                    inicioDosRotulos[indice],
                    linhaDosSimbolos + ativos.fontePequena.getCapHeight() / 2f,
                    corDoItem(itens[indice]));
        }

        Desenho.textoCentralizado(lote, ativos.fonteMiuda, ATALHOS,
                area.centroX(), linhaDosAtalhos, Paleta.TEXTO_FRACO);
    }

    private static Color corDoItem(Item item) {
        return switch (item) {
            case AGENTE -> Paleta.AGENTE;
            case POCO -> Paleta.TEXTO_SUAVE;
            case WUMPUS -> Paleta.WUMPUS;
            case OURO -> Paleta.OURO;
            case INICIO, ROTA -> Paleta.INICIO;
            case BRISA -> Paleta.BRISA;
            case FEDOR -> Paleta.FEDOR;
            case RISCO -> Paleta.PERIGO;
        };
    }
}
