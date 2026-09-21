package wumpusworld.aplicacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import wumpusworld.dominio.AgenteInteligente;
import wumpusworld.dominio.Mundo;
import wumpusworld.dominio.Direcao;

/** Coordena um turno completo, sem dependência de interface ou relógio. */
public final class Partida {
    public static final int LIMITE_DE_EXPLORACAO = 180;

    private final Mundo mundo;
    private final AgenteInteligente agente;
    private final int limite;
    private final List<String> historico = new ArrayList<>();
    private EstadoPartida estado = EstadoPartida.EXPLORANDO;
    private Disparo disparo;
    private String ultimaDecisao = "Aguardando o primeiro movimento.";

    public Partida() {
        this(new Random());
    }

    public Partida(Random sorteador) {
        this(new Mundo(), new AgenteInteligente(sorteador), LIMITE_DE_EXPLORACAO);
    }

    Partida(Mundo mundo, AgenteInteligente agente, int limite) {
        if (limite < 1) {
            throw new IllegalArgumentException("O limite deve ser positivo.");
        }
        this.mundo = mundo;
        this.agente = agente;
        this.limite = limite;
        historico.add("Missão: encontrar o ouro e voltar à base [0, 0].");
    }

    /** Executa uma decisão do agente autônomo: observar, disparar se necessário e mover. */
    public void avancar() {
        if (estado.terminou()) {
            return;
        }
        List<String> eventos = new ArrayList<>();
        if (agente.possuiOuro()) {
            ultimaDecisao = agente.retornarPeloCaminho();
        } else {
            boolean brisa = mundo.temBrisa(agente.getLinha(), agente.getColuna());
            boolean fedor = mundo.temFedor(agente.getLinha(), agente.getColuna());
            agente.observar(mundo, brisa, fedor);
            if (fedor && agente.possuiFlecha()) {
                disparar(eventos);
            }
            ultimaDecisao = agente.moverExplorando(mundo);
        }
        mundo.marcarVisitada(agente.getLinha(), agente.getColuna());
        eventos.add(ultimaDecisao);
        resolverCasa(eventos);
        atualizarEstado(eventos);
        registrarEventos(eventos);
    }

    private void registrarEventos(List<String> eventos) {
        String prefixo = "%03d  ".formatted(agente.getQuantidadeDeMovimentos());
        eventos.forEach(evento -> historico.add(prefixo + evento));
    }

    private void disparar(List<String> eventos) {
        Direcao direcao = Direcao.peloComando(agente.escolherDirecaoDaFlecha(mundo));
        int linha = agente.getLinha();
        int coluna = agente.getColuna();
        int alcance = alcanceDaFlecha(linha, coluna, direcao);
        agente.usarFlecha();
        agente.alterarPontuacao(AgenteInteligente.CUSTO_FLECHA);
        eventos.add("Flecha disparada para " + direcao.getNome() + " (-10).");
        boolean acertou = mundo.atirarFlecha(linha, coluna, direcao.getComando());
        disparo = new Disparo(linha, coluna, direcao, alcance, acertou);
        if (acertou) {
            agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
            eventos.add("Grito! O Wumpus foi abatido (+50).");
        } else {
            eventos.add("A flecha não acertou o Wumpus.");
        }
    }

    /** Casas que a flecha percorre antes de atingir o Wumpus ou sair do mapa. */
    private int alcanceDaFlecha(int linha, int coluna, Direcao direcao) {
        int passos = 0;
        int l = linha + direcao.getVariacaoLinha();
        int c = coluna + direcao.getVariacaoColuna();
        while (l >= 0 && l < Mundo.TAMANHO && c >= 0 && c < Mundo.TAMANHO) {
            passos++;
            if (mundo.getElemento(l, c) == Mundo.WUMPUS) break;
            l += direcao.getVariacaoLinha();
            c += direcao.getVariacaoColuna();
        }
        return passos;
    }

    private void resolverCasa(List<String> eventos) {
        int linha = agente.getLinha();
        int coluna = agente.getColuna();
        switch (mundo.getElemento(linha, coluna)) {
            case Mundo.POCO -> {
                matarAgente();
                eventos.add("O agente caiu em um poço (-100).");
            }
            case Mundo.WUMPUS -> {
                if (agente.tentarMatarWumpus()) {
                    mundo.removerElemento(linha, coluna);
                    agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
                    eventos.add("Encontro direto: o agente venceu o sorteio (+50).");
                } else {
                    matarAgente();
                    eventos.add("Encontro direto: o Wumpus venceu o sorteio (-100).");
                }
            }
            case Mundo.OURO -> {
                agente.pegarOuro();
                mundo.removerElemento(linha, coluna);
                agente.alterarPontuacao(AgenteInteligente.BONUS_OURO);
                eventos.add("Brilho! Ouro coletado (+100). Retornando à base.");
            }
            default -> { }
        }
    }

    private void matarAgente() {
        agente.morrer();
        agente.alterarPontuacao(AgenteInteligente.PENALIDADE_MORTE);
    }

    private void atualizarEstado(List<String> eventos) {
        if (!agente.estaVivo()) {
            estado = EstadoPartida.MORTE;
        } else if (agente.possuiOuro() && agente.getLinha() == 0 && agente.getColuna() == 0) {
            estado = EstadoPartida.VITORIA;
            agente.alterarPontuacao(AgenteInteligente.BONUS_VITORIA);
            eventos.add("Ouro entregue na base! Bônus de vitória: +200.");
        } else if (agente.possuiOuro()) {
            estado = EstadoPartida.RETORNANDO;
        } else if (agente.getQuantidadeDeMovimentos() >= limite) {
            estado = EstadoPartida.LIMITE_ATINGIDO;
            eventos.add("Limite de exploração atingido.");
        }
    }

    public Mundo getMundo() { return mundo; }
    public AgenteInteligente getAgente() { return agente; }
    public EstadoPartida getEstado() { return estado; }
    /** Última flecha disparada, ou {@code null} enquanto nenhuma foi usada. */
    public Disparo getDisparo() { return disparo; }
    public String getUltimaDecisao() { return ultimaDecisao; }
    public List<String> getHistorico() { return List.copyOf(historico); }
}
