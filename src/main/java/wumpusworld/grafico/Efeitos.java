package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Efeitos de curta duração que reforçam o que acabou de acontecer.
 *
 * <p>São três recursos complementares: um clarão de tela inteira, um tremor da
 * câmera e ondas circulares com textos flutuantes. Todos se apagam sozinhos
 * com o tempo, então basta disparar o efeito no momento do evento.</p>
 */
public final class Efeitos {

    /** Onda circular que se expande e desaparece. */
    private static final class Onda {
        float x;
        float y;
        float raioFinal;
        float vida;
        float duracao;
        Color cor;
    }

    /** Pontuação que sobe e se dissolve. */
    private static final class TextoFlutuante {
        String conteudo;
        float x;
        float y;
        float vida;
        float duracao;
        Color cor;
    }

    private final Array<Onda> ondas = new Array<>();
    private final Array<TextoFlutuante> textos = new Array<>();

    private float intensidadeDoTremor;
    private float faseDoTremor;
    private float deslocamentoX;
    private float deslocamentoY;

    private final Color corDoClarao = new Color(Color.WHITE);
    private float intensidadeDoClarao;

    public void atualizar(float delta) {
        // Tremor: amortecido e com direção sorteada a cada quadro.
        if (intensidadeDoTremor > 0f) {
            faseDoTremor += delta * 42f;
            deslocamentoX = MathUtils.sin(faseDoTremor) * intensidadeDoTremor;
            deslocamentoY = MathUtils.cos(faseDoTremor * 1.37f) * intensidadeDoTremor;
            intensidadeDoTremor = Math.max(0f, intensidadeDoTremor - delta * 26f);
        } else {
            deslocamentoX = 0f;
            deslocamentoY = 0f;
        }

        if (intensidadeDoClarao > 0f) {
            intensidadeDoClarao = Math.max(0f, intensidadeDoClarao - delta * 1.6f);
        }

        for (int indice = ondas.size - 1; indice >= 0; indice--) {
            Onda onda = ondas.get(indice);
            onda.vida += delta;
            if (onda.vida >= onda.duracao) {
                ondas.removeIndex(indice);
            }
        }

        for (int indice = textos.size - 1; indice >= 0; indice--) {
            TextoFlutuante texto = textos.get(indice);
            texto.vida += delta;
            if (texto.vida >= texto.duracao) {
                textos.removeIndex(indice);
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Disparo dos efeitos
    // -----------------------------------------------------------------------

    public void tremer(float intensidade) {
        intensidadeDoTremor = Math.max(intensidadeDoTremor, intensidade);
    }

    public void clarao(Color cor, float intensidade) {
        corDoClarao.set(cor);
        intensidadeDoClarao = Math.max(intensidadeDoClarao, intensidade);
    }

    public void onda(float x, float y, float raioFinal, Color cor, float duracao) {
        Onda nova = new Onda();
        nova.x = x;
        nova.y = y;
        nova.raioFinal = raioFinal;
        nova.duracao = duracao;
        nova.cor = new Color(cor);
        ondas.add(nova);
    }

    public void textoFlutuante(String conteudo, float x, float y, Color cor) {
        TextoFlutuante novo = new TextoFlutuante();
        novo.conteudo = conteudo;
        novo.x = x;
        novo.y = y;
        novo.duracao = 1.5f;
        novo.cor = new Color(cor);
        textos.add(novo);
    }

    public void limpar() {
        ondas.clear();
        textos.clear();
        intensidadeDoTremor = 0f;
        intensidadeDoClarao = 0f;
        deslocamentoX = 0f;
        deslocamentoY = 0f;
    }

    // -----------------------------------------------------------------------
    //  Desenho
    // -----------------------------------------------------------------------

    public void desenharOndas(ShapeRenderer formas) {
        for (Onda onda : ondas) {
            float fator = onda.vida / onda.duracao;
            float raio = onda.raioFinal * Desenho.suavizar(fator);
            float alfa = (1f - fator) * 0.75f;
            float espessura = Math.max(1.5f, onda.raioFinal * 0.06f * (1f - fator));
            Desenho.anel(formas, onda.x, onda.y,
                    Math.max(0f, raio - espessura), raio,
                    Paleta.comAlfa(onda.cor, alfa), 48);
        }
    }

    public void desenharTextos(SpriteBatch lote, BitmapFont fonte) {
        for (TextoFlutuante texto : textos) {
            float fator = texto.vida / texto.duracao;
            float alfa = 1f - Desenho.suavizar(fator);
            Desenho.textoCentralizado(lote, fonte, texto.conteudo,
                    texto.x, texto.y + fator * 46f,
                    Paleta.comAlfa(texto.cor, alfa));
        }
    }

    /** Véu colorido sobre toda a tela, usado em mortes e conquistas. */
    public void desenharClarao(ShapeRenderer formas, float largura, float altura) {
        if (intensidadeDoClarao <= 0f) {
            return;
        }
        formas.setColor(Paleta.comAlfa(corDoClarao, intensidadeDoClarao));
        formas.rect(0f, 0f, largura, altura);
    }

    public float getDeslocamentoX() {
        return deslocamentoX;
    }

    public float getDeslocamentoY() {
        return deslocamentoY;
    }
}
