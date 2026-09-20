package wumpusworld.nucleo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Árbitro da simulação: executa a partida um passo de cada vez.
 *
 * <p>Esta classe concentra o laço que antes vivia dentro do {@code main} do
 * modo texto. A separação é o que torna a versão gráfica possível sem duplicar
 * regra nenhuma: o console chama {@link #executarPasso()} dentro de um laço
 * com pausa, e a janela chama exatamente o mesmo método a cada intervalo do
 * relógio de animação. As duas apresentações consomem o mesmo estado.</p>
 *
 * <p><strong>Direção da dependência:</strong> {@code nucleo} não conhece nem o
 * console nem a libGDX. Quem desenha apenas lê; nada do que é desenhado
 * interfere nas decisões do agente.</p>
 */
public final class Partida {

    /**
     * O limite vale apenas enquanto o agente ainda está procurando o ouro.
     * Depois de encontrá-lo, o caminho de retorno sempre pode ser concluído.
     */
    public static final int LIMITE_DE_EXPLORACAO = 180;

    private final Mundo mundo;
    private final AgenteInteligente agente;

    private final List<String> registro = new ArrayList<>();

    private Situacao situacao = Situacao.EM_ANDAMENTO;
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

    /** Cria uma partida com fase sorteada, mantendo as mesmas regras. */
    public static Partida comFaseSorteada(Random sorteador) {
        return new Partida(Mundo.sortear(sorteador), new AgenteInteligente());
    }

    // -----------------------------------------------------------------------
    //  O passo da simulação
    // -----------------------------------------------------------------------

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
        
        // Chance of shooting in the wrong direction
        if (Math.random() < 0.3) {
            Direcao[] todas = Direcao.values();
            direcao = todas[(int)(Math.random() * todas.length)];
        }

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
                registrar("Caiu no poço!");
                aplicarMorte();
            }
            case Mundo.WUMPUS -> {
                registrar("Devorado pelo Wumpus!");
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
                // Casa vazia: nada acontece.
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

    // -----------------------------------------------------------------------
    //  Consultas de estado (usadas pelo console e pela janela gráfica)
    // -----------------------------------------------------------------------

    public Mundo getMundo() {
        return mundo;
    }

    public AgenteInteligente getAgente() {
        return agente;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public int getTurno() {
        return turno;
    }

    /** Sinais sentidos agora, na casa onde o agente está. */
    public Percepcoes getPercepcoesAtuais() {
        return mundo.percepcoesEm(agente.getPosicao());
    }

    /** Histórico completo, do primeiro ao último acontecimento. */
    public List<String> getRegistro() {
        return Collections.unmodifiableList(registro);
    }

    /** Casa de onde o agente saiu no passo mais recente (para a animação). */
    public Posicao getOrigemDoPasso() {
        return origemDoPasso;
    }

    /** Casa onde o agente chegou no passo mais recente (para a animação). */
    public Posicao getDestinoDoPasso() {
        return destinoDoPasso;
    }

    public boolean agenteAndouNoUltimoPasso() {
        return agenteAndouNoPasso;
    }

    /**
     * Ao fim da partida o mapa inteiro pode ser mostrado, como pede o enunciado.
     * Antes disso, a interface só desenha o que o agente já visitou.
     */
    public boolean mapaDeveSerRevelado() {
        return situacao.encerrada();
    }
}
