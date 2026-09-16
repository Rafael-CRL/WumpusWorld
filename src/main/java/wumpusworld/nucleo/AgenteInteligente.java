package wumpusworld.nucleo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Agente baseado em regras simples.
 *
 * <p>Ele combina memória, percepções e uma estimativa numérica de risco.
 * A lógica é exatamente a construída ao longo das aulas 1 a 7; o que mudou
 * aqui foi apenas a organização: as direções viraram um {@link Direcao enum},
 * as decisões viraram objetos ({@link Decisao}) e a memória interna passou a
 * ser consultável em modo somente leitura, para que a interface gráfica possa
 * desenhar o que o agente <em>acredita</em> sem interferir no que ele decide.</p>
 */
public class AgenteInteligente {

    public static final int CUSTO_MOVIMENTO = -1;
    public static final int BONUS_OURO = 100;
    public static final int BONUS_WUMPUS = 50;
    public static final int PENALIDADE_MORTE = -100;
    public static final int BONUS_VITORIA = 200;
    public static final int CUSTO_FLECHA = -10;

    /** Peso do bônus dado a uma casa ainda não explorada. */
    private static final int BONUS_CASA_NOVA = 100;
    /** Peso da penalidade por suspeita de perigo. */
    private static final int PESO_DO_RISCO = 120;
    /** Peso da penalidade por repetir uma casa já conhecida. */
    private static final int PESO_DA_REPETICAO = 5;

    private int linha;
    private int coluna;
    private int quantidadeDeMovimentos;
    private int pontuacao;
    private boolean vivo = true;
    private boolean possuiOuro;
    private boolean possuiFlecha = true;

    /** Última direção efetivamente tomada; usada para orientar o desenho. */
    private Direcao ultimaDirecao = Direcao.DIREITA;

    // visitas guarda memória; risco guarda suspeitas criadas pelas percepções.
    private final int[][] visitas;
    private final int[][] risco;
    private final boolean[][] percepcaoRegistrada;

    // Guarda o caminho conhecido entre a casa inicial e a posição atual.
    // Voltas repetidas são retiradas para tornar o retorno mais curto.
    private final ArrayList<Posicao> caminhoPercorrido = new ArrayList<>();
    private final Random sorteador;

    public AgenteInteligente() {
        this(new Random());
    }

    /** Construtor com semente fixa: útil para testes reproduzíveis. */
    public AgenteInteligente(long semente) {
        this(new Random(semente));
    }

    private AgenteInteligente(Random sorteador) {
        this.sorteador = sorteador;
        visitas = new int[Mundo.TAMANHO][Mundo.TAMANHO];
        risco = new int[Mundo.TAMANHO][Mundo.TAMANHO];
        percepcaoRegistrada = new boolean[Mundo.TAMANHO][Mundo.TAMANHO];
        visitas[0][0] = 1;

        // A primeira posição do caminho é a casa inicial.
        caminhoPercorrido.add(Posicao.INICIAL);
    }

    // -----------------------------------------------------------------------
    //  Percepção
    // -----------------------------------------------------------------------

    /**
     * Brisa ou fedor aumentam a suspeita das casas vizinhas desconhecidas.
     * Casas visitadas já são conhecidas e não recebem risco.
     */
    public void observar(Mundo mundo, Percepcoes percepcoes) {
        if (!percepcoes.brisa() && !percepcoes.fedor()) {
            return;
        }

        // A mesma percepção é registrada uma única vez nesta coordenada.
        if (percepcaoRegistrada[linha][coluna]) {
            return;
        }
        percepcaoRegistrada[linha][coluna] = true;

        int aumento = percepcoes.quantidadeDeSinaisDePerigo();

        for (Direcao direcao : Direcao.TODAS) {
            int linhaVizinha = linha + direcao.getDeltaLinha();
            int colunaVizinha = coluna + direcao.getDeltaColuna();

            if (mundo.estaDentroDoMapa(linhaVizinha, colunaVizinha)
                    && visitas[linhaVizinha][colunaVizinha] == 0) {
                risco[linhaVizinha][colunaVizinha]
                        = risco[linhaVizinha][colunaVizinha] + aumento;
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Decisão de movimento
    // -----------------------------------------------------------------------

    /**
     * Calcula uma nota para cada vizinho. Casas novas recebem bônus; risco e
     * repetição recebem penalidades. A maior nota é escolhida.
     */
    public Decisao moverExplorando(Mundo mundo) {
        int melhorNota = Integer.MIN_VALUE;
        Direcao[] melhoresDirecoes = new Direcao[Direcao.TODAS.length];
        int quantidadeDeMelhores = 0;

        for (Direcao direcao : Direcao.TODAS) {
            int novaLinha = linha + direcao.getDeltaLinha();
            int novaColuna = coluna + direcao.getDeltaColuna();

            if (!mundo.estaDentroDoMapa(novaLinha, novaColuna)) {
                continue;
            }

            int bonusCasaNova = visitas[novaLinha][novaColuna] == 0
                    ? BONUS_CASA_NOVA : 0;
            int nota = bonusCasaNova
                    - (risco[novaLinha][novaColuna] * PESO_DO_RISCO)
                    - (visitas[novaLinha][novaColuna] * PESO_DA_REPETICAO);

            if (nota > melhorNota) {
                melhorNota = nota;
                quantidadeDeMelhores = 0;
                melhoresDirecoes[quantidadeDeMelhores++] = direcao;
            } else if (nota == melhorNota) {
                melhoresDirecoes[quantidadeDeMelhores++] = direcao;
            }
        }

        Direcao escolhida = melhoresDirecoes[
                sorteador.nextInt(quantidadeDeMelhores)];

        linha = linha + escolhida.getDeltaLinha();
        coluna = coluna + escolhida.getDeltaColuna();
        visitas[linha][coluna]++;
        ultimaDirecao = escolhida;

        registrarPosicaoNoCaminho();
        quantidadeDeMovimentos++;
        alterarPontuacao(CUSTO_MOVIMENTO);

        return new Decisao(escolhida, melhorNota,
                risco[linha][coluna], visitas[linha][coluna]);
    }

    /**
     * Mantém somente um caminho direto entre o início e a posição atual.
     * Se o agente voltar a uma casa que já faz parte do caminho, as posições
     * posteriores são retiradas. Assim, movimentos em círculo não serão
     * repetidos durante o retorno com o ouro.
     */
    private void registrarPosicaoNoCaminho() {
        Posicao atual = new Posicao(linha, coluna);
        int indiceEncontrado = caminhoPercorrido.indexOf(atual);

        if (indiceEncontrado == -1) {
            caminhoPercorrido.add(atual);
        } else {
            while (caminhoPercorrido.size() > indiceEncontrado + 1) {
                caminhoPercorrido.remove(caminhoPercorrido.size() - 1);
            }
        }
    }

    /**
     * Volta pelo caminho conhecido durante a exploração.
     * A posição atual é retirada da lista e a posição anterior vira o destino.
     * Como esse caminho já foi percorrido com vida, ele é um caminho seguro.
     *
     * @return {@code true} quando o agente realmente andou uma casa.
     */
    public boolean retornarPeloCaminho() {
        if (caminhoPercorrido.size() <= 1) {
            return false;
        }

        // Remove a posição atual.
        caminhoPercorrido.remove(caminhoPercorrido.size() - 1);

        // A última posição restante é a casa visitada imediatamente antes.
        Posicao posicaoAnterior
                = caminhoPercorrido.get(caminhoPercorrido.size() - 1);

        ultimaDirecao = direcaoEntre(new Posicao(linha, coluna), posicaoAnterior);
        linha = posicaoAnterior.linha();
        coluna = posicaoAnterior.coluna();

        visitas[linha][coluna]++;
        quantidadeDeMovimentos++;
        alterarPontuacao(CUSTO_MOVIMENTO);
        return true;
    }

    private static Direcao direcaoEntre(Posicao origem, Posicao destino) {
        for (Direcao direcao : Direcao.TODAS) {
            if (origem.vizinha(direcao).equals(destino)) {
                return direcao;
            }
        }
        return Direcao.DIREITA;
    }

    // -----------------------------------------------------------------------
    //  Flecha
    // -----------------------------------------------------------------------

    /**
     * Quando sente fedor, aponta para uma casa vizinha desconhecida com maior
     * risco. Empates ainda são resolvidos por sorteio.
     */
    public Direcao escolherDirecaoDaFlecha(Mundo mundo) {
        int maiorRisco = Integer.MIN_VALUE;
        Direcao[] candidatas = new Direcao[Direcao.TODAS.length];
        int quantidade = 0;

        for (Direcao direcao : Direcao.TODAS) {
            int novaLinha = linha + direcao.getDeltaLinha();
            int novaColuna = coluna + direcao.getDeltaColuna();

            if (!mundo.estaDentroDoMapa(novaLinha, novaColuna)
                    || visitas[novaLinha][novaColuna] > 0) {
                continue;
            }

            int riscoDaCasa = risco[novaLinha][novaColuna];
            if (riscoDaCasa > maiorRisco) {
                maiorRisco = riscoDaCasa;
                quantidade = 0;
                candidatas[quantidade++] = direcao;
            } else if (riscoDaCasa == maiorRisco) {
                candidatas[quantidade++] = direcao;
            }
        }

        // Segurança para o caso de todos os vizinhos já serem conhecidos.
        if (quantidade == 0) {
            for (Direcao direcao : Direcao.TODAS) {
                int novaLinha = linha + direcao.getDeltaLinha();
                int novaColuna = coluna + direcao.getDeltaColuna();
                if (mundo.estaDentroDoMapa(novaLinha, novaColuna)) {
                    candidatas[quantidade++] = direcao;
                }
            }
        }

        return candidatas[sorteador.nextInt(quantidade)];
    }

    /** Corpo a corpo com o Wumpus: metade de chance para cada lado. */
    public boolean tentarMatarWumpus() {
        return sorteador.nextBoolean();
    }

    // -----------------------------------------------------------------------
    //  Estado
    // -----------------------------------------------------------------------

    public void alterarPontuacao(int pontos) {
        pontuacao = pontuacao + pontos;
    }

    public void usarFlecha() {
        possuiFlecha = false;
    }

    public void morrer() {
        vivo = false;
    }

    public void pegarOuro() {
        possuiOuro = true;
    }

    public int getLinha() {
        return linha;
    }

    public int getColuna() {
        return coluna;
    }

    public Posicao getPosicao() {
        return new Posicao(linha, coluna);
    }

    public Direcao getUltimaDirecao() {
        return ultimaDirecao;
    }

    public int getQuantidadeDeMovimentos() {
        return quantidadeDeMovimentos;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public boolean estaVivo() {
        return vivo;
    }

    public boolean possuiOuro() {
        return possuiOuro;
    }

    public boolean possuiFlecha() {
        return possuiFlecha;
    }

    // -----------------------------------------------------------------------
    //  Memória do agente exposta em modo somente leitura
    //
    //  A interface usa estes dados para desenhar o que o agente acredita.
    //  Nenhum deles revela o conteúdo real das casas: são apenas suspeitas
    //  construídas a partir das percepções já sentidas.
    // -----------------------------------------------------------------------

    /** Suspeita acumulada sobre uma casa (0 significa nenhuma suspeita). */
    public int getRisco(int linha, int coluna) {
        return risco[linha][coluna];
    }

    /** Quantas vezes o agente já pisou na casa. */
    public int getVisitas(int linha, int coluna) {
        return visitas[linha][coluna];
    }

    /** Maior suspeita registrada até agora, usada para normalizar o mapa de calor. */
    public int getMaiorRisco() {
        int maior = 0;
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                maior = Math.max(maior, risco[linha][coluna]);
            }
        }
        return maior;
    }

    /** Caminho seguro atualmente memorizado entre a casa inicial e o agente. */
    public List<Posicao> getCaminhoConhecido() {
        return Collections.unmodifiableList(caminhoPercorrido);
    }
}
