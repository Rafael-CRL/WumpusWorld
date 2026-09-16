package wumpusworld.nucleo;

/**
 * Conjunto de sinais que o agente capta na casa em que está.
 *
 * <p>É o contrato oficial entre o mundo e quem o observa: nem o agente nem a
 * interface enxergam o conteúdo das casas vizinhas, apenas estes três sinais.</p>
 *
 * @param brisa  há pelo menos um poço numa casa vizinha
 * @param fedor  há o Wumpus numa casa vizinha
 * @param brilho o ouro está exatamente nesta casa
 */
public record Percepcoes(boolean brisa, boolean fedor, boolean brilho) {

    /** Indica se pelo menos um sinal foi percebido. */
    public boolean existeAlgum() {
        return brisa || fedor || brilho;
    }

    /** Quantos sinais de perigo (brisa e fedor) estão ativos. */
    public int quantidadeDeSinaisDePerigo() {
        return (brisa ? 1 : 0) + (fedor ? 1 : 0);
    }

    /** Texto curto usado no console e no painel de informações. */
    public String descricao() {
        if (!existeAlgum()) {
            return "NENHUMA";
        }
        StringBuilder texto = new StringBuilder();
        if (brisa) {
            texto.append("BRISA");
        }
        if (fedor) {
            if (texto.length() > 0) {
                texto.append("  ");
            }
            texto.append("FEDOR");
        }
        if (brilho) {
            if (texto.length() > 0) {
                texto.append("  ");
            }
            texto.append("BRILHO");
        }
        return texto.toString();
    }
}
