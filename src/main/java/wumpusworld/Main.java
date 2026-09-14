package wumpusworld;

/** Classe principal da demonstração do agente orientado por regras. */
public class Main {

    private static final int TEMPO_ENTRE_MOVIMENTOS = 300;
    // O limite vale apenas enquanto o agente ainda está procurando o ouro.
    // Depois de encontrá-lo, o caminho de retorno sempre pode ser concluído.
    private static final int LIMITE_DE_EXPLORACAO = 180;

    public static void main(String[] args) throws InterruptedException {
        Mundo mundo = new Mundo();
        AgenteInteligente agente = new AgenteInteligente();

        mostrarExplicacao();
        mostrarEstado(mundo, agente, false);

        while (agente.estaVivo() && !venceu(agente)
                && (agente.possuiOuro()
                || agente.getQuantidadeDeMovimentos()
                < LIMITE_DE_EXPLORACAO)) {
            Thread.sleep(TEMPO_ENTRE_MOVIMENTOS);
            System.out.println();
            System.out.println("------------------------------------------");

            String decisao;

            if (agente.possuiOuro()) {
                // O objetivo mudou: não é mais explorar, mas voltar para casa.
                decisao = agente.retornarPeloCaminho();
            } else {
                boolean brisa = mundo.temBrisa(
                        agente.getLinha(), agente.getColuna());
                boolean fedor = mundo.temFedor(
                        agente.getLinha(), agente.getColuna());

                agente.observar(mundo, brisa, fedor);

                if (fedor && agente.possuiFlecha()) {
                    realizarDisparoInteligente(mundo, agente);
                }

                decisao = agente.moverExplorando(mundo);
            }

            mundo.marcarVisitada(agente.getLinha(), agente.getColuna());
            System.out.println("DECISÃO DA REGRA: " + decisao);

            resolverCasaAtual(mundo, agente);
            mostrarEstado(mundo, agente, false);
        }

        if (venceu(agente)) {
            agente.alterarPontuacao(AgenteInteligente.BONUS_VITORIA);
            System.out.println("PONTUAÇÃO: +" + AgenteInteligente.BONUS_VITORIA
                    + " por retornar com o ouro.");
        }

        System.out.println();
        System.out.println("MAPA COMPLETO REVELADO NO FINAL:");
        mostrarEstado(mundo, agente, true);

        if (venceu(agente)) {
            System.out.println("VITÓRIA: o agente voltou com o ouro!");
        } else if (!agente.estaVivo()) {
            System.out.println("FIM DE JOGO: o agente morreu.");
        } else {
            System.out.println("O limite de movimentos foi atingido.");
        }

        System.out.println("PONTUAÇÃO FINAL: " + agente.getPontuacao());
    }

    /** O fedor, e não um sorteio de momento, provoca o disparo. */
    private static void realizarDisparoInteligente(Mundo mundo,
            AgenteInteligente agente) {
        char direcao = agente.escolherDirecaoDaFlecha(mundo);
        agente.usarFlecha();
        agente.alterarPontuacao(AgenteInteligente.CUSTO_FLECHA);
        System.out.println("REGRA: sentiu FEDOR, então disparou para "
                + nomeDaDirecao(direcao) + ".");

        if (mundo.atirarFlecha(agente.getLinha(), agente.getColuna(), direcao)) {
            System.out.println("GRITO: a flecha matou o Wumpus!");
            agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
        } else {
            System.out.println("A hipótese estava errada: a flecha não acertou.");
        }
    }

    private static String nomeDaDirecao(char direcao) {
        switch (direcao) {
            case 'W':
                return "CIMA";
            case 'S':
                return "BAIXO";
            case 'A':
                return "ESQUERDA";
            default:
                break;
        }
        return "DIREITA";
    }

    private static void resolverCasaAtual(Mundo mundo,
            AgenteInteligente agente) {
        int linha = agente.getLinha();
        int coluna = agente.getColuna();
        char elemento = mundo.getElemento(linha, coluna);

        switch (elemento) {
            case Mundo.POCO:
                System.out.println("A estimativa falhou: o agente caiu no poço!");
                aplicarMorte(agente);
                break;
            case Mundo.WUMPUS:
                System.out.println("O agente encontrou o Wumpus sem flecha!");
                if (agente.tentarMatarWumpus()) {
                    System.out.println("SORTEIO: o agente matou o Wumpus!");
                    mundo.removerElemento(linha, coluna);
                    agente.alterarPontuacao(AgenteInteligente.BONUS_WUMPUS);
                } else {
                    System.out.println("SORTEIO: o Wumpus matou o agente!");
                    aplicarMorte(agente);
                }   break;
            case Mundo.OURO:
                System.out.println("PERCEPÇÃO: BRILHO! O agente pegou o ouro.");
                agente.pegarOuro();
                mundo.removerElemento(linha, coluna);
                agente.alterarPontuacao(AgenteInteligente.BONUS_OURO);
                break;
            default:
                break;
        }
    }

    private static void aplicarMorte(AgenteInteligente agente) {
        agente.morrer();
        agente.alterarPontuacao(AgenteInteligente.PENALIDADE_MORTE);
    }

    private static void mostrarEstado(Mundo mundo, AgenteInteligente agente,
            boolean revelarTudo) {
        mundo.mostrar(agente, revelarTudo);

        if (agente.estaVivo()) {
            mundo.mostrarPercepcoes(agente);
        }

        System.out.println("Movimentos: " + agente.getQuantidadeDeMovimentos());
        System.out.println("Pontuação: " + agente.getPontuacao());
        System.out.println("Ouro: " + (agente.possuiOuro() ? "SIM" : "NÃO")
                + " | Flecha: " + (agente.possuiFlecha() ? "SIM" : "NÃO"));
    }

    private static boolean venceu(AgenteInteligente agente) {
        return agente.possuiOuro()
                && agente.getLinha() == 0
                && agente.getColuna() == 0;
    }

    private static void mostrarExplicacao() {
        System.out.println("==========================================");
        System.out.println("        MUNDO DE WUMPUS - SÉTIMO DIA");
        System.out.println("==========================================");
        System.out.println("O agente usa memória, brisa e fedor para decidir.");
        System.out.println("Casas novas recebem bônus; risco e repetição,");
        System.out.println("penalidades. A melhor nota orienta o movimento.");
        System.out.println("O fedor ativa a regra de disparo da flecha.");
        System.out.println("Depois do ouro, o agente refaz o caminho de volta.");
        System.out.println("O agente melhorou, mas ainda pode cometer erros.");
    }
}
