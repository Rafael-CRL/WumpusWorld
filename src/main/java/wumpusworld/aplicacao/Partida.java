package wumpusworld.aplicacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Objects;
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

    /** Turno autônomo, mantido para a demonstração no terminal. */
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

    /** Executa somente a direção escolhida; não dispara nem retorna automaticamente. */
    public boolean mover(Direcao direcao) {
        Objects.requireNonNull(direcao);
        if (estado.terminou()) return false;
        if (!agente.mover(mundo, direcao)) {
            ultimaDecisao = "Parede: não é possível mover para " + direcao.getNome() + ".";
            registrarEventos(List.of(ultimaDecisao));
            return false;
        }
        mundo.marcarVisitada(agente.getLinha(), agente.getColuna());
        ultimaDecisao = "Você moveu para " + direcao.getNome() + " ["
                + agente.getLinha() + ", " + agente.getColuna() + "] (-1).";
        List<String> eventos = new ArrayList<>();
        eventos.add(ultimaDecisao);
        resolverCasa(eventos);
        atualizarEstado(eventos);
        registrarEventos(eventos);
        return true;
    }

    /** Disparo é uma ação independente: usa a flecha, mas não move o jogador. */
    public boolean atirar(Direcao direcao) {
        Objects.requireNonNull(direcao);
        if (estado.terminou()) return false;
        if (!agente.possuiFlecha()) {
            ultimaDecisao = "Você não tem mais flechas.";
            registrarEventos(List.of(ultimaDecisao));
            return false;
        }
        List<String> eventos = new ArrayList<>();
        disparar(direcao, eventos);
        ultimaDecisao = eventos.get(eventos.size() - 1);
        registrarEventos(eventos);
        return true;
    }

    private void registrarEventos(List<String> eventos) {
        String prefixo = "%03d  ".formatted(agente.getQuantidadeDeMovimentos());
        eventos.forEach(evento -> historico.add(prefixo + evento));
    }

    private void disparar(List<String> eventos) {
        disparar(Direcao.peloComando(agente.escolherDirecaoDaFlecha(mundo)), eventos);
    }

    private void disparar(Direcao direcao, List<String> eventos) {
        agente.usarFlecha();
        agente.alterarPontuacao(AgenteInteligente.CUSTO_FLECHA);
        eventos.add("Flecha disparada para " + direcao.getNome() + " (-10).");
        if (mundo.atirarFlecha(agente.getLinha(), agente.getColuna(), direcao.getComando())) {
            agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
            eventos.add("Grito! O Wumpus foi abatido (+50).");
        } else {
            eventos.add("A flecha não acertou o Wumpus.");
        }
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
    public String getUltimaDecisao() { return ultimaDecisao; }
    public List<String> getHistorico() { return List.copyOf(historico); }
}
