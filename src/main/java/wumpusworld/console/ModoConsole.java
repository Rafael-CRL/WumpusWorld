package wumpusworld.console;

import java.util.List;

import wumpusworld.nucleo.AgenteInteligente;
import wumpusworld.nucleo.EventoJogo;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Percepcoes;
import wumpusworld.nucleo.Posicao;

/**
 * Versão em texto da simulação, equivalente à das aulas 1 a 7.
 *
 * <p>Mantida no projeto por dois motivos: serve de comparação com a versão
 * gráfica e comprova que as regras ficaram mesmo fora da interface — as duas
 * apresentações usam a mesma classe {@link Partida}, sem nenhuma duplicação.</p>
 */
public final class ModoConsole {

    private static final int TEMPO_ENTRE_MOVIMENTOS = 300;

    private ModoConsole() {
    }

    public static void executar() {
        Partida partida = new Partida();

        mostrarExplicacao();
        mostrarEstado(partida, false);

        int eventosJaImpressos = partida.getRegistro().size();

        while (!partida.getSituacao().encerrada()) {
            dormir();
            System.out.println();
            System.out.println("------------------------------------------");

            partida.executarPasso();

            List<EventoJogo> registro = partida.getRegistro();
            for (int indice = eventosJaImpressos; indice < registro.size(); indice++) {
                System.out.println(registro.get(indice).linhaDeTexto());
            }
            eventosJaImpressos = registro.size();

            mostrarEstado(partida, false);
        }

        System.out.println();
        System.out.println("MAPA COMPLETO REVELADO NO FINAL:");
        mostrarEstado(partida, true);

        System.out.println(partida.getSituacao().getTitulo() + ": "
                + partida.getSituacao().getDescricao());
        System.out.println("PONTUAÇÃO FINAL: "
                + partida.getAgente().getPontuacao());
    }

    private static void dormir() {
        try {
            Thread.sleep(TEMPO_ENTRE_MOVIMENTOS);
        } catch (InterruptedException interrupcao) {
            Thread.currentThread().interrupt();
        }
    }

    private static void mostrarEstado(Partida partida, boolean revelarTudo) {
        desenharMapa(partida, revelarTudo);

        AgenteInteligente agente = partida.getAgente();
        if (agente.estaVivo()) {
            Percepcoes percepcoes = partida.getPercepcoesAtuais();
            System.out.println("Percepções: " + percepcoes.descricao());
        }

        System.out.println("Posição: " + agente.getPosicao()
                + " | Movimentos: " + agente.getQuantidadeDeMovimentos()
                + " | Pontuação: " + agente.getPontuacao());
        System.out.println("Ouro: " + (agente.possuiOuro() ? "SIM" : "NÃO")
                + " | Flecha: " + (agente.possuiFlecha() ? "SIM" : "NÃO"));
    }

    /**
     * Durante o jogo, ? representa uma posição desconhecida e + uma posição
     * visitada. No final, revelarTudo permite discutir o mapa real com a turma.
     */
    private static void desenharMapa(Partida partida, boolean revelarTudo) {
        Mundo mundo = partida.getMundo();
        Posicao agente = partida.getAgente().getPosicao();
        boolean vivo = partida.getAgente().estaVivo();

        System.out.print("      ");
        for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
            System.out.print(coluna + "   ");
        }
        System.out.println("  COLUNAS");

        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            System.out.print("  " + linha + "  ");

            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                if (linha == agente.linha() && coluna == agente.coluna()) {
                    System.out.print(vivo ? "[A] " : "[X] ");
                } else if (revelarTudo) {
                    System.out.print("[" + mundo.getElemento(linha, coluna) + "] ");
                } else if (mundo.foiVisitada(linha, coluna)) {
                    System.out.print("[+] ");
                } else {
                    System.out.print("[?] ");
                }
            }

            System.out.println();
        }
        System.out.println("LINHAS");
    }

    private static void mostrarExplicacao() {
        System.out.println("==========================================");
        System.out.println("        MUNDO DE WUMPUS - MODO CONSOLE");
        System.out.println("==========================================");
        System.out.println("O agente usa memória, brisa e fedor para decidir.");
        System.out.println("Casas novas recebem bônus; risco e repetição,");
        System.out.println("penalidades. A melhor nota orienta o movimento.");
        System.out.println("O fedor ativa a regra de disparo da flecha.");
        System.out.println("Depois do ouro, o agente refaz o caminho de volta.");
        System.out.println("O agente melhorou, mas ainda pode cometer erros.");
    }
}
