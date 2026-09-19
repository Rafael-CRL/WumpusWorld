package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import wumpusworld.nucleo.AgenteInteligente;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Situacao;

/**
 * Cartão de encerramento sobreposto à tela.
 *
 * <p>Mostra com clareza o desfecho — morte, vitória com o ouro ou limite de
 * exploração — junto do resumo numérico da partida. Enquanto ele aparece, o
 * mapa já está revelado no tabuleiro, atendendo ao pedido do enunciado de
 * exibir o mapa completo ao final.</p>
 */
public final class CamadaResultado {

    private final Ativos ativos;

    private float larguraDaTela;
    private float alturaDaTela;
    private Area cartao = new Area(0f, 0f, 660f, 300f);
    private float progresso;

    public CamadaResultado(Ativos ativos) {
        this.ativos = ativos;
    }

    /**
     * @param alvo    onde o cartão deve aparecer; fica sobre a coluna lateral
     *                para que o mapa revelado continue inteiramente visível
     * @param largura largura da tela virtual, usada pelo véu de escurecimento
     * @param altura  altura da tela virtual
     */
    public void definirArea(Area alvo, float largura, float altura) {
        this.cartao = alvo;
        this.larguraDaTela = largura;
        this.alturaDaTela = altura;
    }

    /** Reinicia a animação de entrada do cartão. */
    public void reiniciar() {
        progresso = 0f;
    }

    public void atualizar(float delta, boolean visivel) {
        progresso = visivel ? 1f : 0f;
    }

    public boolean estaVisivel() {
        return progresso > 0f;
    }

    public void desenharFormas(ShapeRenderer formas, Partida partida,
            float tempo) {
        if (progresso <= 0f) {
            return;
        }

        Color cor = corDoResultado(partida.getSituacao());
        Area alvo = cartao;

        Desenho.painel(formas, alvo, 4f, Paleta.PAINEL, cor, 1f);

        // Emblema do desfecho.
        float emblemaX = alvo.x() + 78f;
        float emblemaY = alvo.topo() - 80f;
        switch (partida.getSituacao()) {
            case VITORIA -> Icones.ouro(formas, emblemaX, emblemaY, 118f, tempo);
            case MORTE -> Icones.caveira(formas, emblemaX, emblemaY, 150f);
            default -> relogio(formas, emblemaX, emblemaY, 30f,
                    cor, tempo);
        }

        // Área dos números.
        Desenho.caixa(formas, alvo.x() + 26f, alvo.y() + 56f,
                alvo.largura() - 52f, 68f, 4f, Paleta.PAINEL_INTERNO);
    }

    /** Relógio simples usado quando a partida acaba por limite de movimentos. */
    private void relogio(ShapeRenderer formas, float centroX, float centroY,
            float raio, Color cor, float tempo) {
        Desenho.anel(formas, centroX, centroY, raio * 0.82f, raio, cor, 44);
        float angulo = tempo * 1.4f;
        Desenho.linha(formas, centroX, centroY,
                centroX + MathUtils.cos(angulo) * raio * 0.55f,
                centroY + MathUtils.sin(angulo) * raio * 0.55f, 4f, cor);
        Desenho.linha(formas, centroX, centroY,
                centroX + MathUtils.cos(angulo * 0.35f) * raio * 0.34f,
                centroY + MathUtils.sin(angulo * 0.35f) * raio * 0.34f, 4f, cor);
    }

    public void desenharTextos(SpriteBatch lote, Partida partida) {
        if (progresso <= 0f) {
            return;
        }

        Situacao situacao = partida.getSituacao();
        AgenteInteligente agente = partida.getAgente();
        Color cor = corDoResultado(situacao);
        Area alvo = cartao;

        float textoX = alvo.x() + 140f;
        Desenho.texto(lote, ativos.fonteBanner, situacao.getTitulo(),
                textoX, alvo.topo() - 46f, cor);
        Desenho.texto(lote, ativos.fonteTexto, situacao.getDescricao(),
                textoX, alvo.topo() - 104f,
                Paleta.TEXTO_SUAVE);

        // Quatro números que resumem a partida.
        float larguraDaColuna = (alvo.largura() - 52f) / 4f;
        float baseDosRotulos = alvo.y() + 108f;
        float baseDosValores = alvo.y() + 92f;

        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 0.5f,
                baseDosRotulos, baseDosValores, "PONTUAÇÃO FINAL",
                String.valueOf(agente.getPontuacao()),
                agente.getPontuacao() >= 0 ? Paleta.SUCESSO : Paleta.PERIGO, 1f);
        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 1.5f,
                baseDosRotulos, baseDosValores, "MOVIMENTOS",
                String.valueOf(agente.getQuantidadeDeMovimentos()),
                Paleta.TEXTO, 1f);
        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 2.5f,
                baseDosRotulos, baseDosValores, "CASAS VISITADAS",
                partida.getMundo().quantidadeDeCasasVisitadas() + "/"
                + (Mundo.TAMANHO * Mundo.TAMANHO),
                Paleta.TEXTO, 1f);
        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 3.5f,
                baseDosRotulos, baseDosValores, "OURO",
                agente.possuiOuro() ? "SIM" : "NÃO",
                agente.possuiOuro() ? Paleta.OURO : Paleta.TEXTO_FRACO, 1f);

        Desenho.textoCentralizado(lote, ativos.fonteMiuda,
                "R sorteia um novo mapa   ·   ESC encerra",
                alvo.centroX(), alvo.y() + 36f,
                Paleta.TEXTO_FRACO);
    }

    private void escreverResumo(SpriteBatch lote, float centroX,
            float baseDoRotulo, float baseDoValor, String rotulo, String valor,
            Color corDoValor, float suave) {
        Desenho.textoCentralizado(lote, ativos.fonteMiuda, rotulo,
                centroX, baseDoRotulo, Paleta.TEXTO_FRACO);
        Desenho.textoCentralizado(lote, ativos.fonteValor, valor,
                centroX, baseDoValor, corDoValor);
    }

    private static Area escalar(Area base, float escala) {
        float largura = base.largura() * escala;
        float altura = base.altura() * escala;
        return new Area(base.centroX() - largura / 2f,
                base.centroY() - altura / 2f, largura, altura);
    }

    private static Color corDoResultado(Situacao situacao) {
        return switch (situacao) {
            case VITORIA -> Paleta.SUCESSO;
            case MORTE -> Paleta.PERIGO;
            case LIMITE_ATINGIDO -> Paleta.ALERTA;
            default -> Paleta.NEUTRO;
        };
    }
}
