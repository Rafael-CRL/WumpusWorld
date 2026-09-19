package wumpusworld.grafico;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import wumpusworld.nucleo.AgenteInteligente;
import wumpusworld.nucleo.DisparoDeFlecha;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Percepcoes;
import wumpusworld.nucleo.Posicao;

/**
 * O mapa em grade: o centro visual da aplicação.
 *
 * <h2>Da matriz para a tela</h2>
 * A matriz do mundo é indexada por <em>linha</em> (de cima para baixo) e
 * <em>coluna</em> (da esquerda para a direita), enquanto a libGDX usa um eixo
 * Y que cresce para cima. A conversão é feita num ponto só:
 *
 * <pre>
 *   x(coluna) = origemX + coluna * (lado + ESPACO)
 *   y(linha)  = origemY + (TAMANHO - 1 - linha) * (lado + ESPACO)
 * </pre>
 *
 * A inversão {@code TAMANHO - 1 - linha} é o que faz a linha 0 aparecer no
 * alto da janela, exatamente como no mapa impresso do modo console.
 *
 * <h2>O que pode ser desenhado</h2>
 * Enquanto a partida corre, só aparecem as casas já visitadas e aquilo que o
 * agente <em>deduziu</em> (as suspeitas de risco). O conteúdo real das casas
 * ocultas só é exibido quando a partida termina, como pede o enunciado.
 */
public final class PainelTabuleiro {

    private static final float MARGEM_INTERNA = 18f;
    private static final float ESPACO = 7f;
    /** Faixa reservada aos números de linha e de coluna. */
    private static final float REGUA = 26f;
    /** Faixa reservada ao título do painel, acima da grade. */
    private static final float FAIXA_DO_TITULO = 26f;

    private final Ativos ativos;

    private Area area = new Area(0f, 0f, 1f, 1f);
    private float origemX;
    private float origemY;
    private float lado = 1f;
    private float passo = 1f;
    private float grade = 1f;

    public PainelTabuleiro(Ativos ativos) {
        this.ativos = ativos;
    }

    /** Recalcula a geometria das casas a partir da área reservada ao painel. */
    public void definirArea(Area novaArea) {
        this.area = novaArea;

        Area interna = novaArea.encolher(MARGEM_INTERNA);
        // O corpo é o que sobra depois de reservar a faixa do título.
        float disponivelX = interna.largura() - REGUA;
        float disponivelY = interna.altura() - REGUA - FAIXA_DO_TITULO;

        int n = Mundo.TAMANHO;
        lado = Math.min(
                (disponivelX - ESPACO * (n - 1)) / n,
                (disponivelY - ESPACO * (n - 1)) / n);
        passo = lado + ESPACO;
        grade = lado * n + ESPACO * (n - 1);

        origemX = interna.x() + REGUA + (disponivelX - grade) / 2f;
        origemY = interna.y() + (disponivelY - grade) / 2f;
    }

    /** Borda esquerda da coluna informada, em coordenadas de tela. */
    public float x(int coluna) {
        return origemX + coluna * passo;
    }

    /** Borda inferior da linha informada, em coordenadas de tela. */
    public float y(int linha) {
        return origemY + (Mundo.TAMANHO - 1 - linha) * passo;
    }

    public float centroX(int coluna) {
        return x(coluna) + lado / 2f;
    }

    public float centroY(int linha) {
        return y(linha) + lado / 2f;
    }

    public float getLado() {
        return lado;
    }

    /** Centro da casa, interpolando entre duas posições (usado na animação). */
    public float centroXInterpolado(Posicao origem, Posicao destino, float fator) {
        return centroX(origem.coluna())
                + (centroX(destino.coluna()) - centroX(origem.coluna())) * fator;
    }

    public float centroYInterpolado(Posicao origem, Posicao destino, float fator) {
        return centroY(origem.linha())
                + (centroY(destino.linha()) - centroY(origem.linha())) * fator;
    }

    // -----------------------------------------------------------------------
    //  Camada de formas
    // -----------------------------------------------------------------------

    public void desenharFormas(ShapeRenderer formas, Partida partida,
            EstadoDaAnimacao animacao) {

        Desenho.painel(formas, area, 4f, Paleta.PAINEL, Paleta.BORDA, 1f);

        // Leito do tabuleiro, um pouco mais escuro que o painel.
        Desenho.caixa(formas,
                origemX - 9f, origemY - 9f, grade + 18f, grade + 18f,
                12f, Paleta.PAINEL_INTERNO);

        boolean revelar = partida.mapaDeveSerRevelado();
        Mundo mundo = partida.getMundo();
        AgenteInteligente agente = partida.getAgente();

        desenharCasas(formas, mundo, agente, partida, animacao, revelar);
        desenharCaminhoMemorizado(formas, agente, animacao);
        desenharElementos(formas, mundo, agente, animacao, revelar);
        desenharFlecha(formas, partida, animacao);
        desenharAgente(formas, partida, animacao);
    }

    private void desenharCasas(ShapeRenderer formas, Mundo mundo,
            AgenteInteligente agente, Partida partida,
            EstadoDaAnimacao animacao, boolean revelar) {

        int maiorRisco = Math.max(1, agente.getMaiorRisco());
        Posicao posicaoDoAgente = agente.getPosicao();

        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                float casaX = x(coluna);
                float casaY = y(linha);

                boolean visitada = mundo.foiVisitada(linha, coluna);
                boolean atual = posicaoDoAgente.linha() == linha
                        && posicaoDoAgente.coluna() == coluna;

                Color fundo;
                if (atual) {
                    fundo = Paleta.CASA_ATUAL;
                } else if (visitada) {
                    fundo = Paleta.CASA_VISITADA;
                } else if (revelar) {
                    // Revelada no fim, porém nunca pisada pelo agente.
                    fundo = Paleta.misturar(Paleta.CASA_DESCONHECIDA,
                            Paleta.CASA_VISITADA, 0.45f);
                } else {
                    fundo = Paleta.CASA_DESCONHECIDA;
                }
                if (linha == 0 && coluna == 0 && !atual) {
                    fundo = Paleta.misturar(fundo, Paleta.CASA_INICIAL, 0.55f);
                }

                Desenho.caixa(formas, casaX, casaY, lado, lado, 9f, fundo);

                // Contorno sutil, mais forte nas casas já conhecidas.
                desenharContornoDaCasa(formas, casaX, casaY, Paleta.GRADE);

                // Mapa de calor das suspeitas que o agente construiu.
                if (animacao.mostrarMapaDeRisco() && !visitada && !revelar) {
                    int risco = agente.getRisco(linha, coluna);
                    if (risco > 0) {
                        // Um véu leve, só o bastante para ler a hierarquia
                        // entre as casas mais e menos suspeitas.
                        Desenho.caixa(formas, casaX + 2f, casaY + 2f,
                                lado - 4f, lado - 4f, 4f, Paleta.PERIGO);
                        Desenho.contorno(formas, casaX + 2f, casaY + 2f,
                                lado - 4f, lado - 4f, 4f, 1f, Paleta.PERIGO);
                    }
                }

                // Destaque pulsante ao redor da casa ocupada pelo agente.
                if (atual && !partida.getSituacao().encerrada()) {
                    desenharMoldura(formas, casaX, casaY,
                            Paleta.AGENTE, 1f);
                }

                // Selos das percepções sentidas em casas já conhecidas.
                if (visitada && !atual) {
                    desenharSelos(formas, mundo, linha, coluna, casaX, casaY);
                }
            }
        }
    }

    private void desenharContornoDaCasa(ShapeRenderer formas, float casaX,
            float casaY, Color cor) {
        formas.setColor(cor);
        float espessura = 1.2f;
        formas.rect(casaX + 6f, casaY, lado - 12f, espessura);
        formas.rect(casaX + 6f, casaY + lado - espessura, lado - 12f, espessura);
        formas.rect(casaX, casaY + 6f, espessura, lado - 12f);
        formas.rect(casaX + lado - espessura, casaY + 6f, espessura, lado - 12f);
    }

    private void desenharMoldura(ShapeRenderer formas, float casaX, float casaY,
            Color cor, float espessura) {
        formas.setColor(cor);
        formas.rect(casaX - espessura, casaY - espessura,
                lado + espessura * 2f, espessura);
        formas.rect(casaX - espessura, casaY + lado,
                lado + espessura * 2f, espessura);
        formas.rect(casaX - espessura, casaY, espessura, lado);
        formas.rect(casaX + lado, casaY, espessura, lado);
    }

    /** Pequenos selos de brisa e fedor nas casas que o agente já conhece. */
    private void desenharSelos(ShapeRenderer formas, Mundo mundo,
            int linha, int coluna, float casaX, float casaY) {
        Percepcoes percepcoes = mundo.percepcoesEm(linha, coluna);
        float tamanho = lado * 0.085f;
        float margem = lado * 0.16f;

        if (percepcoes.brisa()) {
            Icones.seloDeBrisa(formas, casaX + margem,
                    casaY + lado - margem, tamanho, 0.75f);
        }
        if (percepcoes.fedor()) {
            Icones.seloDeFedor(formas, casaX + lado - margem,
                    casaY + lado - margem, tamanho, 0.75f);
        }
    }

    /** Rota segura que o agente memorizou para voltar ao ponto de partida. */
    private void desenharCaminhoMemorizado(ShapeRenderer formas,
            AgenteInteligente agente, EstadoDaAnimacao animacao) {
        List<Posicao> caminho = agente.getCaminhoConhecido();
        if (caminho.size() < 2) {
            return;
        }

        Color cor = agente.possuiOuro() ? Paleta.OURO : Paleta.INICIO;

        for (int indice = 0; indice < caminho.size() - 1; indice++) {
            Posicao de = caminho.get(indice);
            Posicao para = caminho.get(indice + 1);
            Desenho.linhaTracejada(formas,
                    centroX(de.coluna()), centroY(de.linha()),
                    centroX(para.coluna()), centroY(para.linha()),
                    2.6f, 7f, 6f, cor);
        }

        for (Posicao posicao : caminho) {
            formas.setColor(cor);
            formas.circle(centroX(posicao.coluna()), centroY(posicao.linha()),
                    2.6f, 12);
        }
    }

    private void desenharElementos(ShapeRenderer formas, Mundo mundo,
            AgenteInteligente agente, EstadoDaAnimacao animacao,
            boolean revelar) {

        Icones.saida(formas, centroX(0), centroY(0), lado,
                agente.possuiOuro(), animacao.tempo());

        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                boolean conhecida = revelar || mundo.foiVisitada(linha, coluna);
                if (!conhecida) {
                    continue;
                }

                float cx = centroX(coluna);
                float cy = centroY(linha);

                switch (mundo.getElemento(linha, coluna)) {
                    case Mundo.POCO -> Icones.poco(formas, cx, cy, lado,
                            animacao.tempo());
                    case Mundo.WUMPUS -> Icones.wumpus(formas, cx, cy, lado,
                            animacao.tempo());
                    case Mundo.OURO -> Icones.ouro(formas, cx, cy, lado,
                            animacao.tempo());
                    default -> {
                        // Casa vazia: nada a desenhar.
                    }
                }
            }
        }
    }

    private void desenharFlecha(ShapeRenderer formas, Partida partida,
            EstadoDaAnimacao animacao) {
        DisparoDeFlecha disparo = partida.getUltimoDisparo();
        if (disparo == null || animacao.progressoDoDisparo() >= 1f) {
            return;
        }

        float fator = Desenho.suavizar(animacao.progressoDoDisparo());
        float cx = centroXInterpolado(disparo.origem(), disparo.destino(), fator);
        float cy = centroYInterpolado(disparo.origem(), disparo.destino(), fator);

        Color cor = disparo.acertou() ? Paleta.ALERTA : Paleta.TEXTO_SUAVE;

        Icones.flecha(formas, cx, cy, disparo.direcao(), lado * 0.42f, cor);
    }

    private void desenharAgente(ShapeRenderer formas, Partida partida,
            EstadoDaAnimacao animacao) {
        AgenteInteligente agente = partida.getAgente();
        float fator = Desenho.suavizar(animacao.progressoDoPasso());

        float cx = centroXInterpolado(partida.getOrigemDoPasso(),
                partida.getDestinoDoPasso(), fator);
        float cy = centroYInterpolado(partida.getOrigemDoPasso(),
                partida.getDestinoDoPasso(), fator);

        // Um pequeno salto vertical dá peso ao movimento.
        float salto = partida.agenteAndouNoUltimoPasso()
                ? (float) Math.sin(fator * Math.PI) * lado * 0.06f : 0f;

        Icones.agente(formas, cx, cy + salto, lado,
                agente.getUltimaDirecao(), agente.estaVivo(),
                agente.possuiOuro(), animacao.tempo());
    }

    // -----------------------------------------------------------------------
    //  Camada de texto
    // -----------------------------------------------------------------------

    public void desenharTextos(SpriteBatch lote, Partida partida,
            EstadoDaAnimacao animacao) {

        Mundo mundo = partida.getMundo();
        AgenteInteligente agente = partida.getAgente();
        boolean revelar = partida.mapaDeveSerRevelado();

        Desenho.texto(lote, ativos.fonteSecao, "MAPA DA CAVERNA",
                area.x() + MARGEM_INTERNA + 2f,
                area.topo() - MARGEM_INTERNA, Paleta.TEXTO_SUAVE);
        Desenho.textoDireita(lote, ativos.fonteMiuda,
                "LINHAS ↓   ·   COLUNAS →",
                area.direita() - MARGEM_INTERNA - 2f,
                area.topo() - MARGEM_INTERNA - 2f, Paleta.TEXTO_FRACO);

        // Réguas: números das colunas no alto e das linhas à esquerda.
        float meiaLetra = ativos.fonteMiuda.getCapHeight() / 2f;
        for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
            Desenho.textoCentralizado(lote, ativos.fonteMiuda,
                    String.valueOf(coluna), centroX(coluna),
                    origemY + grade + 16f, Paleta.TEXTO_FRACO);
        }
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            Desenho.textoCentralizado(lote, ativos.fonteMiuda,
                    String.valueOf(linha), origemX - 14f,
                    centroY(linha) + meiaLetra, Paleta.TEXTO_FRACO);
        }


        // Coordenada discreta em cada casa e número das suspeitas.
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                boolean visitada = mundo.foiVisitada(linha, coluna);
                Desenho.texto(lote, ativos.fonteMiuda,
                        linha + "," + coluna,
                        x(coluna) + 8f, y(linha) + 17f,
                        Paleta.TEXTO_FRACO);

                if (animacao.mostrarMapaDeRisco() && !visitada && !revelar) {
                    int risco = agente.getRisco(linha, coluna);
                    if (risco > 0) {
                        // No canto oposto ao da coordenada, para não colidirem.
                        Desenho.textoDireita(lote, ativos.fonteMiuda,
                                "RISCO " + risco,
                                x(coluna) + lado - 8f, y(linha) + lado - 12f,
                                Paleta.PERIGO);
                    }
                }
            }
        }

        Desenho.textoCentralizado(lote, ativos.fonteMiuda, "INÍCIO",
                centroX(0), y(0) + lado - 8f,
                Paleta.INICIO);
    }
}
