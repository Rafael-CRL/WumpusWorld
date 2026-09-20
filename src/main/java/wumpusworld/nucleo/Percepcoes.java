package wumpusworld.nucleo;

public record Percepcoes(boolean brisa, boolean fedor, boolean brilho) {
    
    public boolean existeAlgum() {
        return brisa || fedor || brilho;
    }

    public int quantidadeDeSinaisDePerigo() {
        return (brisa ? 1 : 0) + (fedor ? 1 : 0);
    }

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
