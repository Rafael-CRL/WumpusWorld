package wumpusworld.grafico;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

import wumpusworld.nucleo.EventoJogo;

/**
 * Registro rolante dos acontecimentos, no espírito da saída do modo console.
 *
 * <p>As entradas mais recentes ficam embaixo. O painel monta a lista de baixo
 * para cima, medindo cada mensagem já quebrada em linhas, e para assim que a
 * altura disponível se esgota — o que evita qualquer recorte e dispensa barra
 * de rolagem.</p>
 */
public final class PainelRegistro {

    private static final float MARGEM = 18f;
    private static final float RECUO_DO_TEXTO = 24f;
    private static final float ESPACO_ENTRE_ENTRADAS = 9f;

    /** Uma entrada já posicionada e pronta para desenhar. */
    private record Linha(EventoJogo evento, float y, float altura, float alfa) {
    }

    private final Ativos ativos;
    private final GlyphLayout medida = new GlyphLayout();
    private final List<Linha> visiveis = new ArrayList<>();

    private Area area = new Area(0f, 0f, 1f, 1f);
    private float baseDoTitulo;
    private Area areaDoConteudo = area;
    private int totalRegistradoNaUltimaMedida = -1;

    public PainelRegistro(Ativos ativos) {
        this.ativos = ativos;
    }

    public void definirArea(Area novaArea) {
        this.area = novaArea;
        this.baseDoTitulo = novaArea.topo() - MARGEM - 4f;
        this.areaDoConteudo = new Area(
                novaArea.x() + MARGEM,
                novaArea.y() + MARGEM,
                novaArea.largura() - MARGEM * 2f,
                novaArea.altura() - MARGEM * 2f - 30f);
        this.totalRegistradoNaUltimaMedida = -1;
    }

    /**
     * Recalcula quais entradas cabem no painel.
     * Só refaz a medição quando o registro cresce, o que mantém o custo baixo.
     */
    public void atualizar(List<EventoJogo> registro) {
        if (registro.size() == totalRegistradoNaUltimaMedida) {
            return;
        }
        totalRegistradoNaUltimaMedida = registro.size();
        visiveis.clear();

        float larguraDoTexto = areaDoConteudo.largura() - RECUO_DO_TEXTO;
        float cursor = areaDoConteudo.y();
        float limite = areaDoConteudo.topo();

        for (int indice = registro.size() - 1; indice >= 0; indice--) {
            EventoJogo evento = registro.get(indice);
            medida.setText(ativos.fontePequena, textoDe(evento), Color.WHITE,
                    larguraDoTexto, Align.left, true);
            float altura = medida.height;

            if (cursor + altura > limite) {
                break;
            }

            // As entradas mais antigas desbotam, criando sensação de histórico.
            int distancia = registro.size() - 1 - indice;
            float alfa = Math.max(0.40f, 1f - distancia * 0.055f);

            visiveis.add(new Linha(evento, cursor + altura, altura, alfa));
            cursor += altura + ESPACO_ENTRE_ENTRADAS;
        }
    }

    public void desenharFormas(ShapeRenderer formas) {
        Desenho.sombra(formas, area, 16f, 0.45f);
        Desenho.painel(formas, area, 16f, Paleta.PAINEL, Paleta.BORDA, 1.5f);
        Desenho.caixa(formas, areaDoConteudo.x() - 6f, areaDoConteudo.y() - 8f,
                areaDoConteudo.largura() + 12f, areaDoConteudo.altura() + 16f,
                10f, Paleta.PAINEL_INTERNO);

        // Marcador discreto no início de cada entrada.
        for (Linha linha : visiveis) {
            Color cor = Paleta.comAlfa(Paleta.BORDA_FORTE, linha.alfa());
            Desenho.caixa(formas, areaDoConteudo.x() + 2f,
                    linha.y() - linha.altura() + 3f,
                    3f, linha.altura() - 4f, 1.5f, cor);
        }
    }

    public void desenharTextos(SpriteBatch lote, int totalDeEventos) {
        Desenho.texto(lote, ativos.fonteSecao, "REGISTRO DA PARTIDA",
                area.x() + MARGEM + 2f, baseDoTitulo, Paleta.TEXTO_SUAVE);
        Desenho.textoDireita(lote, ativos.fonteMiuda,
                totalDeEventos + " EVENTOS",
                area.direita() - MARGEM - 2f, baseDoTitulo - 2f,
                Paleta.TEXTO_FRACO);

        float x = areaDoConteudo.x() + RECUO_DO_TEXTO;
        float larguraDoTexto = areaDoConteudo.largura() - RECUO_DO_TEXTO;

        for (Linha linha : visiveis) {
            Color cor = Paleta.comAlfa(Paleta.TEXTO_SUAVE, linha.alfa());
            ativos.fontePequena.setColor(cor);
            ativos.fontePequena.draw(lote, textoDe(linha.evento()),
                    x, linha.y(), larguraDoTexto, Align.left, true);
        }
    }

    private static String textoDe(EventoJogo evento) {
        return evento.turno() + " · " + evento.tipo().getRotulo()
                + " — " + evento.mensagem();
    }
}
