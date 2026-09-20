package wumpusworld.nucleo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class AgenteInteligente {

    public static final int CUSTO_MOVIMENTO = -1;
    public static final int BONUS_OURO = 100;
    public static final int BONUS_WUMPUS = 50;
    public static final int PENALIDADE_MORTE = -100;
    public static final int BONUS_VITORIA = 200;
    public static final int CUSTO_FLECHA = -10;

    private static final int BONUS_CASA_NOVA = 200;
    private static final int PESO_DO_RISCO = 150;
    private static final int PESO_DA_REPETICAO = 10;

    private int linha;
    private int coluna;
    private int quantidadeDeMovimentos;
    private int pontuacao;
    private boolean vivo = true;
    private boolean possuiOuro;
    private boolean possuiFlecha = false;

    private Direcao ultimaDirecao = Direcao.DIREITA;

    private final int[][] visitas;
    private final int[][] risco;
    private final boolean[][] percepcaoRegistrada;

    private final ArrayList<Posicao> caminhoPercorrido = new ArrayList<>();
    private final Random sorteador;

    public AgenteInteligente() {
        this(new Random());
    }

    public AgenteInteligente(long semente) {
        this(new Random(semente));
    }

    private AgenteInteligente(Random sorteador) {
        this.sorteador = sorteador;
        visitas = new int[Mundo.TAMANHO][Mundo.TAMANHO];
        risco = new int[Mundo.TAMANHO][Mundo.TAMANHO];
        percepcaoRegistrada = new boolean[Mundo.TAMANHO][Mundo.TAMANHO];
        visitas[0][0] = 1;

        caminhoPercorrido.add(Posicao.INICIAL);
    }

    public void observar(Mundo mundo, Percepcoes percepcoes) {
        if (!percepcoes.brisa() && !percepcoes.fedor()) {
            return;
        }

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

    public Direcao moverExplorando(Mundo mundo) {
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

        return escolhida;
    }

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

    public boolean retornarPeloCaminho() {
        if (caminhoPercorrido.size() <= 1) {
            return false;
        }

        caminhoPercorrido.remove(caminhoPercorrido.size() - 1);

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

    public boolean tentarMatarWumpus() {
        return false;
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

    public void pegarFlecha() {
        possuiFlecha = true;
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
    public int getRisco(int linha, int coluna) {
        return risco[linha][coluna];
    }

    public int getVisitas(int linha, int coluna) {
        return visitas[linha][coluna];
    }

    public int getMaiorRisco() {
        int maior = 0;
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                maior = Math.max(maior, risco[linha][coluna]);
            }
        }
        return maior;
    }

    public List<Posicao> getCaminhoConhecido() {
        return Collections.unmodifiableList(caminhoPercorrido);
    }
}
