package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Cabeçalho com o título e os controles da simulação. */
public final class BarraSuperior {

    private static final float MARGEM = 20f;
    private static final float ALTURA_DO_BOTAO = 36f;

    private final Ativos ativos;

    private Area area = new Area(0f, 0f, 1f, 1f);
    private Area botaoJogar = area;
    private Area botaoPausar = area;
    private Area botaoReiniciar = area;
    private Acao acaoEmHover = Acao.NENHUMA;

    public enum Acao {
        JOGAR,
        PAUSAR,
        REINICIAR,
        NENHUMA
    }

    public BarraSuperior(Ativos ativos) {
        this.ativos = ativos;
    }

    public void definirArea(Area novaArea) {
        this.area = novaArea;
        float y = novaArea.centroY() - ALTURA_DO_BOTAO / 2f;
        float direita = novaArea.direita() - MARGEM;

        botaoReiniciar = new Area(direita - 112f, y, 112f, ALTURA_DO_BOTAO);
        direita -= 112f + 10f;
        botaoPausar = new Area(direita - 88f, y, 88f, ALTURA_DO_BOTAO);
        direita -= 88f + 10f;
        botaoJogar = new Area(direita - 82f, y, 82f, ALTURA_DO_BOTAO);
    }

    public void desenharFormas(ShapeRenderer formas, EstadoDoJogo estado) {

        Desenho.painel(formas, area, 4f, Paleta.PAINEL, Paleta.BORDA, 1f);

        botao(formas, botaoJogar, Paleta.AGENTE, estado == EstadoDoJogo.PARADO,
                acaoEmHover == Acao.JOGAR);
        botao(formas, botaoPausar, Paleta.ALERTA,
                estado == EstadoDoJogo.JOGANDO || estado == EstadoDoJogo.PAUSADO,
                acaoEmHover == Acao.PAUSAR);
        botao(formas, botaoReiniciar, Paleta.NEUTRO, true,
                acaoEmHover == Acao.REINICIAR);
    }

    private void botao(ShapeRenderer formas, Area alvo, Color cor,
            boolean habilitado, boolean hover) {
        Color fundo = habilitado && hover ? Paleta.BORDA
                : habilitado ? Paleta.PAINEL_DESTAQUE : Paleta.PAINEL_INTERNO;
        Color borda = habilitado ? cor : Paleta.BORDA;
        Desenho.painel(formas, alvo, 4f, fundo, borda, 1f);
    }

    public void desenharTextos(SpriteBatch lote, EstadoDoJogo estado) {

        float textoX = area.x() + MARGEM;
        Desenho.texto(lote, ativos.fonteTitulo, "MUNDO DE WUMPUS",
                textoX, area.centroY() + 7f, Paleta.TEXTO);

        float meiaLetra = ativos.fonteMiuda.getCapHeight() / 2f;
        Desenho.textoCentralizado(lote, ativos.fonteMiuda, "JOGAR",
                botaoJogar.centroX(), botaoJogar.centroY() + meiaLetra,
                corDoTexto(estado == EstadoDoJogo.PARADO, Paleta.AGENTE_BRILHO));
        boolean podePausar = estado == EstadoDoJogo.JOGANDO
                || estado == EstadoDoJogo.PAUSADO;
        Desenho.textoCentralizado(lote, ativos.fonteMiuda,
                estado == EstadoDoJogo.PAUSADO ? "CONTINUAR" : "PAUSAR",
                botaoPausar.centroX(), botaoPausar.centroY() + meiaLetra,
                corDoTexto(podePausar, Paleta.ALERTA));
        Desenho.textoCentralizado(lote, ativos.fonteMiuda, "REINICIAR",
                botaoReiniciar.centroX(), botaoReiniciar.centroY() + meiaLetra,
                Paleta.TEXTO_SUAVE);
    }

    private static Color corDoTexto(boolean habilitado, Color cor) {
        return habilitado ? cor : Paleta.TEXTO_FRACO;
    }

    public Acao acaoNoPonto(float x, float y, EstadoDoJogo estado) {
        if (contem(botaoJogar, x, y) && estado == EstadoDoJogo.PARADO) {
            return Acao.JOGAR;
        }
        if (contem(botaoPausar, x, y)
                && (estado == EstadoDoJogo.JOGANDO
                || estado == EstadoDoJogo.PAUSADO)) {
            return Acao.PAUSAR;
        }
        if (contem(botaoReiniciar, x, y)) {
            return Acao.REINICIAR;
        }
        return Acao.NENHUMA;
    }

    public void atualizarHover(float x, float y, EstadoDoJogo estado) {
        acaoEmHover = acaoNoPonto(x, y, estado);
    }

    private static boolean contem(Area area, float x, float y) {
        return x >= area.x() && x <= area.direita()
                && y >= area.y() && y <= area.topo();
    }
}
