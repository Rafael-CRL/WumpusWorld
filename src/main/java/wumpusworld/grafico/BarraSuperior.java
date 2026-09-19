package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Situacao;

/** Cabeçalho com a identificação do trabalho e o estado geral da simulação. */
public final class BarraSuperior {

    private static final float MARGEM = 20f;
    private static final float ALTURA_DA_PILULA = 32f;

    private final Ativos ativos;

    private Area area = new Area(0f, 0f, 1f, 1f);
    private Area pilulaDaSituacao = area;

    public BarraSuperior(Ativos ativos) {
        this.ativos = ativos;
    }

    public void definirArea(Area novaArea) {
        this.area = novaArea;
        float y = novaArea.centroY() - ALTURA_DA_PILULA / 2f;
        float direita = novaArea.direita() - MARGEM;

        pilulaDaSituacao = new Area(direita - 230f, y, 230f, ALTURA_DA_PILULA);
    }

    public void desenharFormas(ShapeRenderer formas, Partida partida,
            EstadoDaAnimacao animacao) {

        Desenho.sombra(formas, area, 16f, 0.45f);
        Desenho.painel(formas, area, 16f, Paleta.PAINEL, Paleta.BORDA, 1.5f);

        // Emblema: uma gema sobre um disco, à esquerda do título.
        float emblemaX = area.x() + MARGEM + 22f;
        float emblemaY = area.centroY();
        formas.setColor(Paleta.comAlfa(Paleta.OURO, 0.12f));
        formas.circle(emblemaX, emblemaY, 22f, 32);
        Desenho.anel(formas, emblemaX, emblemaY, 20f, 21.5f,
                Paleta.comAlfa(Paleta.OURO, 0.45f), 40);
        Icones.ouro(formas, emblemaX, emblemaY, 58f, animacao.tempo());

        Color corDaSituacao = corDaSituacao(partida);
        pilula(formas, pilulaDaSituacao, corDaSituacao);

        formas.setColor(corDaSituacao);
        formas.circle(pilulaDaSituacao.x() + 17f, pilulaDaSituacao.centroY(),
                4.5f, 16);
    }

    private void pilula(ShapeRenderer formas, Area alvo, Color cor) {
        Desenho.painel(formas, alvo, alvo.altura() / 2f,
                Paleta.comAlfa(cor, 0.12f), Paleta.comAlfa(cor, 0.45f), 1.2f);
    }

    public void desenharTextos(SpriteBatch lote, Partida partida,
            EstadoDaAnimacao animacao) {

        float textoX = area.x() + MARGEM + 54f;
        Desenho.texto(lote, ativos.fonteTitulo, "MUNDO DE WUMPUS",
                textoX, area.centroY() + 7f, Paleta.TEXTO);
        Desenho.texto(lote, ativos.fonteSubtitulo,
                "UFPA · Campus Cametá · Computação Gráfica  —  agente autônomo "
                + "com interface em libGDX",
                textoX, area.centroY() - 19f, Paleta.TEXTO_FRACO);

        float meiaLetra = ativos.fonteMiuda.getCapHeight() / 2f;

        Desenho.textoCentralizado(lote, ativos.fonteMiuda,
                tituloDaSituacao(partida),
                pilulaDaSituacao.centroX() + 8f,
                pilulaDaSituacao.centroY() + meiaLetra, corDaSituacao(partida));
    }

    private static Color corDaSituacao(Partida partida) {
        return switch (partida.getSituacao()) {
            case EM_ANDAMENTO -> partida.getAgente().possuiOuro()
                    ? Paleta.OURO : Paleta.AGENTE;
            case VITORIA -> Paleta.SUCESSO;
            case MORTE -> Paleta.PERIGO;
            case LIMITE_ATINGIDO -> Paleta.ALERTA;
        };
    }

    private static String tituloDaSituacao(Partida partida) {
        if (partida.getSituacao() != Situacao.EM_ANDAMENTO) {
            return partida.getSituacao().getTitulo();
        }
        return partida.getAgente().possuiOuro() ? "VOLTANDO COM O OURO"
                : "EXPLORANDO";
    }
}

