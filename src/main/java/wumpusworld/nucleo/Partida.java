package wumpusworld.nucleo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Partida {

    public static final int LIMITE_DE_EXPLORACAO = 180;

    private final Mundo mundo;
    private final AgenteInteligente agente;

    private final List<String> registro = new ArrayList<>();

    private Situacao situacao = Situacao.EM_ANDAMENTO;
    private String causaDaMorte;
    private int turno;

    private Posicao origemDoPasso = Posicao.INICIAL;
    private Posicao destinoDoPasso = Posicao.INICIAL;
    private boolean ouroColetadoNoPasso;
    private boolean agenteAndouNoPasso;

    public Partida() {
        this(new Mundo(), new AgenteInteligente());
    }

    public Partida(Mundo mundo, AgenteInteligente agente) {
        this.mundo = mundo;
        this.agente = agente;
        registrar("Partida iniciada.");
    }

    public static Partida comFaseSorteada(Random sorteador) {
        return new Partida(Mundo.sortear(sorteador), new AgenteInteligente());
    }

    public boolean executarPasso() {
        if (situacao.encerrada()) {
            return false;
        }

        turno++;
        ouroColetadoNoPasso = false;
        origemDoPasso = agente.getPosicao();

        if (agente.possuiOuro()) {
            executarRetorno();
        } else {
            executarExploracao();
        }

        destinoDoPasso = agente.getPosicao();
        agenteAndouNoPasso = !origemDoPasso.equals(destinoDoPasso);
        mundo.marcarVisitada(destinoDoPasso);

        resolverCasaAtual();
        avaliarFimDaPartida();
        return true;
    }

    private void executarRetorno() {
        if (agente.retornarPeloCaminho()) {
            registrar("Voltando pelo caminho seguro.");
        } else {
            registrar("Chegou na base inicial.");
        }
    }

    private void executarExploracao() {
        Percepcoes percepcoes = mundo.percepcoesEm(agente.getPosicao());

        if (percepcoes.brisa() || percepcoes.fedor()) {
            registrar("Sentiu " + percepcoes.descricao() + ".");
        }

        agente.observar(mundo, percepcoes);

        if (percepcoes.fedor() && agente.possuiFlecha()) {
            dispararFlecha();
        }

        Direcao direcao = agente.moverExplorando(mundo);
        registrar("Andou para " + direcao.getRotulo() + ".");
    }

    private void dispararFlecha() {
        Direcao direcao = agente.escolherDirecaoDaFlecha(mundo);
        Posicao origem = agente.getPosicao();
        Posicao destino = mundo.calcularAlcanceDaFlecha(origem, direcao);

        agente.usarFlecha();
        agente.alterarPontuacao(AgenteInteligente.CUSTO_FLECHA);

        boolean acertou = mundo.atirarFlecha(
                origem.linha(), origem.coluna(), direcao);

        registrar("Atirou flecha para " + direcao.getRotulo() + ".");

        if (acertou) {
            agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
            registrar("GRITO! Acertou o Wumpus!");
        } else {
            registrar("Errou a flecha.");
        }
    }

    private void resolverCasaAtual() {
        Posicao posicao = agente.getPosicao();

        switch (mundo.getElemento(posicao)) {
            case Mundo.POCO -> {
                causaDaMorte = "O agente morreu porque caiu no poço.";
                registrar(causaDaMorte);
                aplicarMorte();
            }
            case Mundo.WUMPUS -> {
                causaDaMorte = "O agente morreu porque foi devorado pelo Wumpus.";
                registrar(causaDaMorte);
                aplicarMorte();
            }
            case Mundo.OURO -> {
                agente.pegarOuro();
                mundo.removerElemento(posicao);
                agente.alterarPontuacao(AgenteInteligente.BONUS_OURO);
                ouroColetadoNoPasso = true;
                registrar("Pegou o OURO! Voltando...");
            }
            case Mundo.FLECHA -> {
                agente.pegarFlecha();
                mundo.removerElemento(posicao);
                registrar("Pegou uma flecha no chão.");
            }
            default -> {
            }
        }
    }

    private void aplicarMorte() {
        agente.morrer();
        agente.alterarPontuacao(AgenteInteligente.PENALIDADE_MORTE);
    }

    private void avaliarFimDaPartida() {
        if (!agente.estaVivo()) {
            situacao = Situacao.MORTE;
            registrar("GAME OVER. Pontos: " + agente.getPontuacao());
            return;
        }

        if (agente.possuiOuro() && agente.getPosicao().equals(Posicao.INICIAL)) {
            agente.alterarPontuacao(AgenteInteligente.BONUS_VITORIA);
            situacao = Situacao.VITORIA;
            registrar("VENCEU! Pontos: " + agente.getPontuacao());
            return;
        }

        if (!agente.possuiOuro()
                && agente.getQuantidadeDeMovimentos() >= LIMITE_DE_EXPLORACAO) {
            situacao = Situacao.LIMITE_ATINGIDO;
            registrar("Fim do tempo! Pontos: " + agente.getPontuacao());
        }
    }

    private void registrar(String mensagem) {
        String evento = agente.getPosicao() + " " + mensagem;
        registro.add(evento);
    }

    public Mundo getMundo() {
        return mundo;
    }

    public AgenteInteligente getAgente() {
        return agente;
    }

    public String getDescricaoResultado() {
        return situacao == Situacao.MORTE && causaDaMorte != null
                ? causaDaMorte : situacao.getDescricao();
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public int getTurno() {
        return turno;
    }

    public Percepcoes getPercepcoesAtuais() {
        return mundo.percepcoesEm(agente.getPosicao());
    }

    public List<String> getRegistro() {
        return Collections.unmodifiableList(registro);
    }

    public Posicao getOrigemDoPasso() {
        return origemDoPasso;
    }

    public Posicao getDestinoDoPasso() {
        return destinoDoPasso;
    }

    public boolean agenteAndouNoUltimoPasso() {
        return agenteAndouNoPasso;
    }

    public boolean mapaDeveSerRevelado() {
        return situacao.encerrada();
    }
}
