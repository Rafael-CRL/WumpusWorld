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
        if (visivel) {
            progresso = Math.min(1f, progresso + delta * 2.4f);
        } else {
            progresso = 0f;
        }
    }

    public boolean estaVisivel() {
        return progresso > 0f;
    }

    public void desenharFormas(ShapeRenderer formas, Partida partida,
            float tempo) {
        if (progresso <= 0f) {
            return;
        }

        float suave = Desenho.suavizar(progresso);
        Color cor = corDoResultado(partida.getSituacao());

        // Escurece o restante da tela sem apagá-lo por completo.
        formas.setColor(0f, 0f, 0f, 0.30f * suave);
        formas.rect(0f, 0f, larguraDaTela, alturaDaTela);

        // O cartão cresce um pouco ao entrar.
        float escala = 0.94f + 0.06f * suave;
        Area alvo = escalar(cartao, escala);

        Desenho.sombra(formas, alvo, 20f, 0.8f * suave);
        Desenho.painel(formas, alvo, 20f,
                Paleta.comAlfa(Paleta.PAINEL, suave),
                Paleta.comAlfa(cor, 0.65f * suave), 2f);

        // Faixa colorida no topo do cartão.
        Desenho.caixa(formas, alvo.x() + 20f, alvo.topo() - 6f,
                alvo.largura() - 40f, 4f, 2f, Paleta.comAlfa(cor, suave));

        // Emblema do desfecho.
        float emblemaX = alvo.x() + 78f;
        float emblemaY = alvo.topo() - 80f;
        formas.setColor(Paleta.comAlfa(cor, 0.12f * suave));
        formas.circle(emblemaX, emblemaY, 42f, 40);
        Desenho.anel(formas, emblemaX, emblemaY, 40f, 42f,
                Paleta.comAlfa(cor, 0.5f * suave), 48);

        switch (partida.getSituacao()) {
            case VITORIA -> Icones.ouro(formas, emblemaX, emblemaY, 118f, tempo);
            case MORTE -> Icones.caveira(formas, emblemaX, emblemaY, 150f);
            default -> relogio(formas, emblemaX, emblemaY, 30f,
                    Paleta.comAlfa(cor, suave), tempo);
        }

        // Área dos números.
        Desenho.caixa(formas, alvo.x() + 26f, alvo.y() + 56f,
                alvo.largura() - 52f, 68f, 12f,
                Paleta.comAlfa(Paleta.PAINEL_INTERNO, suave));
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

        float suave = Desenho.suavizar(progresso);
        Situacao situacao = partida.getSituacao();
        AgenteInteligente agente = partida.getAgente();
        Color cor = Paleta.comAlfa(corDoResultado(situacao), suave);
        Area alvo = escalar(cartao, 0.94f + 0.06f * suave);

        float textoX = alvo.x() + 140f;
        Desenho.texto(lote, ativos.fonteBanner, situacao.getTitulo(),
                textoX, alvo.topo() - 46f, cor);
        Desenho.texto(lote, ativos.fonteTexto, situacao.getDescricao(),
                textoX, alvo.topo() - 104f,
                Paleta.comAlfa(Paleta.TEXTO_SUAVE, suave));

        // Quatro números que resumem a partida.
        float larguraDaColuna = (alvo.largura() - 52f) / 4f;
        float baseDosRotulos = alvo.y() + 108f;
        float baseDosValores = alvo.y() + 92f;

        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 0.5f,
                baseDosRotulos, baseDosValores, "PONTUAÇÃO FINAL",
                String.valueOf(agente.getPontuacao()),
                Paleta.comAlfa(agente.getPontuacao() >= 0
                        ? Paleta.SUCESSO : Paleta.PERIGO, suave), suave);
        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 1.5f,
                baseDosRotulos, baseDosValores, "MOVIMENTOS",
                String.valueOf(agente.getQuantidadeDeMovimentos()),
                Paleta.comAlfa(Paleta.TEXTO, suave), suave);
        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 2.5f,
                baseDosRotulos, baseDosValores, "CASAS VISITADAS",
                partida.getMundo().quantidadeDeCasasVisitadas() + "/"
                + (Mundo.TAMANHO * Mundo.TAMANHO),
                Paleta.comAlfa(Paleta.TEXTO, suave), suave);
        escreverResumo(lote, alvo.x() + 26f + larguraDaColuna * 3.5f,
                baseDosRotulos, baseDosValores, "OURO",
                agente.possuiOuro() ? "SIM" : "NÃO",
                Paleta.comAlfa(agente.possuiOuro()
                        ? Paleta.OURO : Paleta.TEXTO_FRACO, suave), suave);
    }

    private void escreverResumo(SpriteBatch lote, float centroX,
            float baseDoRotulo, float baseDoValor, String rotulo, String valor,
            Color corDoValor, float suave) {
        Desenho.textoCentralizado(lote, ativos.fonteMiuda, rotulo,
                centroX, baseDoRotulo, Paleta.comAlfa(Paleta.TEXTO_FRACO, suave));
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
