package wumpusworld;

import java.util.ArrayList;
import java.util.Random;

/**
 * Agente baseado em regras simples.
 * Ele combina memória, percepções e uma estimativa numérica de risco.
 */
public class AgenteInteligente {

    public static final int CUSTO_MOVIMENTO = -1;
    public static final int BONUS_OURO = 100;
    public static final int BONUS_WUMPUS = 50;
    public static final int PENALIDADE_MORTE = -100;
    public static final int BONUS_VITORIA = 200;
    public static final int CUSTO_FLECHA = -10;

    private static final int[][] DIRECOES = {
        {-1, 0}, // cima
        {1, 0},  // baixo
        {0, -1}, // esquerda
        {0, 1}   // direita
    };

    private static final String[] NOMES = {
        "CIMA", "BAIXO", "ESQUERDA", "DIREITA"
    };

    private static final char[] COMANDOS = {'W', 'S', 'A', 'D'};

    private int linha;
    private int coluna;
    private int quantidadeDeMovimentos;
    private int pontuacao;
    private boolean vivo = true;
    private boolean possuiOuro;
    private boolean possuiFlecha = true;

    // visitas guarda memória; risco guarda suspeitas criadas pelas percepções.
    private final int[][] visitas;
    private final int[][] risco;
    private final boolean[][] percepcaoRegistrada;
    // Guarda o caminho conhecido entre a casa inicial e a posição atual.
    // Voltas repetidas são retiradas para tornar o retorno mais curto.
    private final ArrayList<int[]> caminhoPercorrido = new ArrayList<>();
    private final Random sorteador = new Random();

    public AgenteInteligente() {
        visitas = new int[Mundo.TAMANHO][Mundo.TAMANHO];
        risco = new int[Mundo.TAMANHO][Mundo.TAMANHO];
        percepcaoRegistrada = new boolean[Mundo.TAMANHO][Mundo.TAMANHO];
        visitas[0][0] = 1;

        // A primeira posição do caminho é a casa inicial.
        caminhoPercorrido.add(new int[]{0, 0});
    }

    /**
     * Brisa ou fedor aumentam a suspeita das casas vizinhas desconhecidas.
     * Casas visitadas já são conhecidas e não recebem risco.
     */
    public void observar(Mundo mundo, boolean sentiuBrisa,
            boolean sentiuFedor) {
        if (!sentiuBrisa && !sentiuFedor) {
            return;
        }

        // A mesma percepção é registrada uma única vez nesta coordenada.
        if (percepcaoRegistrada[linha][coluna]) {
            return;
        }
        percepcaoRegistrada[linha][coluna] = true;

        int aumento = 0;
        if (sentiuBrisa) {
            aumento++;
        }
        if (sentiuFedor) {
            aumento++;
        }

        for (int direcao = 0; direcao < DIRECOES.length; direcao++) {
            int linhaVizinha = linha + DIRECOES[direcao][0];
            int colunaVizinha = coluna + DIRECOES[direcao][1];

            if (mundo.estaDentroDoMapa(linhaVizinha, colunaVizinha)
                    && visitas[linhaVizinha][colunaVizinha] == 0) {
                risco[linhaVizinha][colunaVizinha]
                        = risco[linhaVizinha][colunaVizinha] + aumento;
            }
        }
    }

    /**
     * Calcula uma nota para cada vizinho. Casas novas recebem bônus; risco e
     * repetição recebem penalidades. A maior nota é escolhida.
     */
    public String moverExplorando(Mundo mundo) {
        int melhorNota = Integer.MIN_VALUE;
        int[] melhoresDirecoes = new int[4];
        int quantidadeDeMelhores = 0;

        for (int direcao = 0; direcao < DIRECOES.length; direcao++) {
            int novaLinha = linha + DIRECOES[direcao][0];
            int novaColuna = coluna + DIRECOES[direcao][1];

            if (!mundo.estaDentroDoMapa(novaLinha, novaColuna)) {
                continue;
            }

            int bonusCasaNova = visitas[novaLinha][novaColuna] == 0 ? 100 : 0;
            int nota = bonusCasaNova
                    - (risco[novaLinha][novaColuna] * 120)
                    - (visitas[novaLinha][novaColuna] * 5);

            if (nota > melhorNota) {
                melhorNota = nota;
                quantidadeDeMelhores = 0;
                melhoresDirecoes[quantidadeDeMelhores++] = direcao;
            } else if (nota == melhorNota) {
                melhoresDirecoes[quantidadeDeMelhores++] = direcao;
            }
        }

        int direcaoEscolhida = melhoresDirecoes[
                sorteador.nextInt(quantidadeDeMelhores)];
        linha = linha + DIRECOES[direcaoEscolhida][0];
        coluna = coluna + DIRECOES[direcaoEscolhida][1];
        visitas[linha][coluna]++;

        registrarPosicaoNoCaminho();
        quantidadeDeMovimentos++;
        alterarPontuacao(CUSTO_MOVIMENTO);

        return NOMES[direcaoEscolhida]
                + " | risco=" + risco[linha][coluna]
                + " | visitas=" + visitas[linha][coluna]
                + " | nota=" + melhorNota;
    }

    /**
     * Mantém somente um caminho direto entre o início e a posição atual.
     * Se o agente voltar a uma casa que já faz parte do caminho, as posições
     * posteriores são retiradas. Assim, movimentos em círculo não serão
     * repetidos durante o retorno com o ouro.
     */
    private void registrarPosicaoNoCaminho() {
        int indiceEncontrado = -1;

        for (int indice = 0; indice < caminhoPercorrido.size(); indice++) {
            int[] posicao = caminhoPercorrido.get(indice);
            if (posicao[0] == linha && posicao[1] == coluna) {
                indiceEncontrado = indice;
                break;
            }
        }

        if (indiceEncontrado == -1) {
            caminhoPercorrido.add(new int[]{linha, coluna});
        } else {
            while (caminhoPercorrido.size() > indiceEncontrado + 1) {
                caminhoPercorrido.remove(caminhoPercorrido.size() - 1);
            }
        }
    }

    /**
     * Volta pelo caminho conhecido durante a exploração.
     * A posição atual é retirada da lista e a posição anterior vira o destino.
     * Como esse caminho já foi percorrido com vida, ele é um caminho conhecido.
     */
    public String retornarPeloCaminho() {
        if (caminhoPercorrido.size() <= 1) {
            return "RETORNO CONCLUÍDO: o agente está na casa inicial";
        }

        // Remove a posição atual.
        caminhoPercorrido.remove(caminhoPercorrido.size() - 1);

        // A última posição restante é a casa visitada imediatamente antes.
        int[] posicaoAnterior
                = caminhoPercorrido.get(caminhoPercorrido.size() - 1);
        linha = posicaoAnterior[0];
        coluna = posicaoAnterior[1];

        visitas[linha][coluna]++;
        quantidadeDeMovimentos++;
        alterarPontuacao(CUSTO_MOVIMENTO);

        return "MODO RETORNO | seguindo o histórico até ["
                + linha + "][" + coluna + "]";
    }

    /**
     * Quando sente fedor, aponta para uma casa vizinha desconhecida com maior
     * risco. Empates ainda são resolvidos por sorteio.
     */
    public char escolherDirecaoDaFlecha(Mundo mundo) {
        int maiorRisco = Integer.MIN_VALUE;
        int[] candidatas = new int[4];
        int quantidade = 0;

        for (int direcao = 0; direcao < DIRECOES.length; direcao++) {
            int novaLinha = linha + DIRECOES[direcao][0];
            int novaColuna = coluna + DIRECOES[direcao][1];

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
            for (int direcao = 0; direcao < DIRECOES.length; direcao++) {
                int novaLinha = linha + DIRECOES[direcao][0];
                int novaColuna = coluna + DIRECOES[direcao][1];
                if (mundo.estaDentroDoMapa(novaLinha, novaColuna)) {
                    candidatas[quantidade++] = direcao;
                }
            }
        }

        return COMANDOS[candidatas[sorteador.nextInt(quantidade)]];
    }

    public boolean tentarMatarWumpus() {
        return sorteador.nextBoolean();
    }

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
}
