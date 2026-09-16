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

    private final List<EventoJogo> registro = new ArrayList<>();
    private final List<EventoJogo> eventosDoPasso = new ArrayList<>();

    private Situacao situacao = Situacao.EM_ANDAMENTO;
    private int turno;

    private Posicao origemDoPasso = Posicao.INICIAL;
    private Posicao destinoDoPasso = Posicao.INICIAL;
    private DisparoDeFlecha ultimoDisparo;
    private boolean ouroColetadoNoPasso;
    private boolean agenteAndouNoPasso;

    public Partida() {
        this(new Mundo(), new AgenteInteligente());
    }

    public Partida(Mundo mundo, AgenteInteligente agente) {
        this.mundo = mundo;
        this.agente = agente;
        registrar(TipoEvento.SISTEMA,
                "Partida iniciada na casa " + Posicao.INICIAL
                + ". O agente procura o ouro e precisa voltar vivo.");
    }

    /** Cria uma partida com fase sorteada, mantendo as mesmas regras. */
    public static Partida comFaseSorteada(Random sorteador) {
        return new Partida(Mundo.sortear(sorteador), new AgenteInteligente());
    }

    // -----------------------------------------------------------------------
    //  O passo da simulação
    // -----------------------------------------------------------------------

    /**
     * Executa um único passo do agente, na mesma ordem das aulas:
     * perceber, deduzir, eventualmente atirar, mover e resolver a casa.
     *
     * @return {@code true} quando o passo foi executado; {@code false} quando a
     *         partida já havia terminado.
     */
    public boolean executarPasso() {
        if (situacao.encerrada()) {
            return false;
        }

        turno++;
        eventosDoPasso.clear();
        ultimoDisparo = null;
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

    /** O objetivo mudou: não é mais explorar, mas voltar para casa. */
    private void executarRetorno() {
        if (agente.retornarPeloCaminho()) {
            registrar(TipoEvento.DECISAO,
                    "Modo retorno: refazendo o caminho seguro até "
                    + agente.getPosicao() + ".");
        } else {
            registrar(TipoEvento.DECISAO,
                    "Retorno concluído: o agente já está na casa inicial.");
        }
    }

    private void executarExploracao() {
        Percepcoes percepcoes = mundo.percepcoesEm(agente.getPosicao());

        if (percepcoes.brisa() || percepcoes.fedor()) {
            registrar(TipoEvento.PERCEPCAO,
                    "Em " + agente.getPosicao() + " o agente sentiu "
                    + percepcoes.descricao() + ".");
        }

        agente.observar(mundo, percepcoes);

        // O fedor, e não um sorteio de momento, provoca o disparo.
        if (percepcoes.fedor() && agente.possuiFlecha()) {
            dispararFlecha();
        }

        Decisao decisao = agente.moverExplorando(mundo);
        registrar(TipoEvento.DECISAO,
                "Andou para " + decisao.direcao().getRotulo()
                + " (risco " + decisao.risco()
                + ", visitas " + decisao.visitas()
                + ", nota " + decisao.nota() + ").");
    }

    private void dispararFlecha() {
        Direcao direcao = agente.escolherDirecaoDaFlecha(mundo);
        Posicao origem = agente.getPosicao();
        Posicao destino = mundo.calcularAlcanceDaFlecha(origem, direcao);

        agente.usarFlecha();
        agente.alterarPontuacao(AgenteInteligente.CUSTO_FLECHA);

        boolean acertou = mundo.atirarFlecha(
                origem.linha(), origem.coluna(), direcao);
        ultimoDisparo = new DisparoDeFlecha(origem, destino, direcao, acertou);

        registrar(TipoEvento.FLECHA,
                "Fedor detectado: flecha disparada para "
                + direcao.getRotulo() + " (" + AgenteInteligente.CUSTO_FLECHA
                + " pontos).");

        if (acertou) {
            agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
            registrar(TipoEvento.FLECHA,
                    "GRITO! A flecha acertou o Wumpus (+"
                    + AgenteInteligente.BONUS_WUMPUS + " pontos).");
        } else {
            registrar(TipoEvento.FLECHA,
                    "A hipótese estava errada: a flecha não acertou nada.");
        }
    }

    /** Aplica as consequências do conteúdo da casa em que o agente parou. */
    private void resolverCasaAtual() {
        Posicao posicao = agente.getPosicao();

        switch (mundo.getElemento(posicao)) {
            case Mundo.POCO -> {
                registrar(TipoEvento.PERIGO,
                        "A estimativa falhou: o agente caiu no poço em "
                        + posicao + ".");
                aplicarMorte();
            }
            case Mundo.WUMPUS -> {
                registrar(TipoEvento.PERIGO,
                        "O agente encontrou o Wumpus em " + posicao
                        + " sem flecha. Corpo a corpo!");
                if (agente.tentarMatarWumpus()) {
                    mundo.removerElemento(posicao);
                    agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
                    registrar(TipoEvento.PERIGO,
                            "O agente venceu a luta e matou o Wumpus (+"
                            + AgenteInteligente.BONUS_WUMPUS + " pontos).");
                } else {
                    registrar(TipoEvento.PERIGO, "O Wumpus venceu a luta.");
                    aplicarMorte();
                }
            }
            case Mundo.OURO -> {
                agente.pegarOuro();
                mundo.removerElemento(posicao);
                agente.alterarPontuacao(AgenteInteligente.BONUS_OURO);
                ouroColetadoNoPasso = true;
                registrar(TipoEvento.TESOURO,
                        "BRILHO! O ouro foi recolhido em " + posicao + " (+"
                        + AgenteInteligente.BONUS_OURO
                        + " pontos). Agora é voltar ao início.");
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
            registrar(TipoEvento.DERROTA,
                    "Fim de jogo. Pontuação final: " + agente.getPontuacao() + ".");
            return;
        }

        if (agente.possuiOuro() && agente.getPosicao().equals(Posicao.INICIAL)) {
            agente.alterarPontuacao(AgenteInteligente.BONUS_VITORIA);
            situacao = Situacao.VITORIA;
            registrar(TipoEvento.VITORIA,
                    "O agente voltou com o ouro (+"
                    + AgenteInteligente.BONUS_VITORIA
                    + " pontos). Pontuação final: "
                    + agente.getPontuacao() + ".");
            return;
        }

        if (!agente.possuiOuro()
                && agente.getQuantidadeDeMovimentos() >= LIMITE_DE_EXPLORACAO) {
            situacao = Situacao.LIMITE_ATINGIDO;
            registrar(TipoEvento.DERROTA,
                    "O limite de " + LIMITE_DE_EXPLORACAO
                    + " movimentos foi atingido. Pontuação final: "
                    + agente.getPontuacao() + ".");
        }
    }

    private void registrar(TipoEvento tipo, String mensagem) {
        EventoJogo evento = new EventoJogo(turno, tipo, mensagem);
        registro.add(evento);
        eventosDoPasso.add(evento);
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
    public List<EventoJogo> getRegistro() {
        return Collections.unmodifiableList(registro);
    }

    /** Somente os acontecimentos gerados no passo mais recente. */
    public List<EventoJogo> getEventosDoUltimoPasso() {
        return Collections.unmodifiableList(eventosDoPasso);
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

    public boolean ouroFoiColetadoNoUltimoPasso() {
        return ouroColetadoNoPasso;
    }

    /** Disparo ocorrido no passo mais recente, ou {@code null} se não houve. */
    public DisparoDeFlecha getUltimoDisparo() {
        return ultimoDisparo;
    }

    /**
     * Ao fim da partida o mapa inteiro pode ser mostrado, como pede o enunciado.
     * Antes disso, a interface só desenha o que o agente já visitou.
     */
    public boolean mapaDeveSerRevelado() {
        return situacao.encerrada();
    }
}
