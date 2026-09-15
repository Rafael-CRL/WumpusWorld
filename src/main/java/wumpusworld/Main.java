package wumpusworld;

import wumpusworld.aplicacao.Partida;
import wumpusworld.dominio.AgenteInteligente;
import wumpusworld.dominio.Mundo;

/** Alternativa de terminal que utiliza as mesmas regras da interface gráfica. */
public final class Main {
    private Main() { }

    public static void main(String[] args) throws InterruptedException {
        Partida partida = new Partida();
        System.out.println("MUNDO DE WUMPUS — encontre o ouro e retorne à base.");
        mostrar(partida);
        int registrosExibidos = 0;
        while (!partida.getEstado().terminou()) {
            Thread.sleep(300);
            partida.avancar();
            var historico = partida.getHistorico();
            while (registrosExibidos < historico.size()) {
                System.out.println(historico.get(registrosExibidos++));
            }
            mostrar(partida);
        }
        System.out.println(partida.getEstado().getTitulo());
        System.out.println("PONTUAÇÃO FINAL: " + partida.getAgente().getPontuacao());
    }

    private static void mostrar(Partida partida) {
        Mundo mundo = partida.getMundo();
        AgenteInteligente agente = partida.getAgente();
        for (int linha = 0; linha < Mundo.TAMANHO; linha++) {
            for (int coluna = 0; coluna < Mundo.TAMANHO; coluna++) {
                char simbolo;
                if (linha == agente.getLinha() && coluna == agente.getColuna()) {
                    simbolo = agente.estaVivo() ? 'A' : 'X';
                } else if (partida.getEstado().terminou()) {
                    simbolo = mundo.getElemento(linha, coluna);
                } else {
                    simbolo = mundo.foiVisitada(linha, coluna) ? '+' : '?';
                }
                System.out.print("[" + simbolo + "] ");
            }
            System.out.println();
        }
        System.out.printf("Movimentos: %d | Pontuação: %d | Ouro: %s | Flecha: %s%n",
                agente.getQuantidadeDeMovimentos(), agente.getPontuacao(),
                agente.possuiOuro(), agente.possuiFlecha());
        if (agente.estaVivo()) {
            System.out.printf("Brisa: %s | Fedor: %s%n",
                    mundo.temBrisa(agente.getLinha(), agente.getColuna()),
                    mundo.temFedor(agente.getLinha(), agente.getColuna()));
        }
    }
}
