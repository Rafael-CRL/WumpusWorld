package wumpusworld.nucleo;

/**
 * Um acontecimento registrado durante a partida.
 *
 * @param turno    número do passo em que o evento ocorreu
 * @param tipo     categoria usada para colorir e prefixar a mensagem
 * @param mensagem texto já pronto para leitura humana
 */
public record EventoJogo(int turno, TipoEvento tipo, String mensagem) {

    /** Linha formatada para o modo console. */
    public String linhaDeTexto() {
        return String.format("[%03d] %-11s %s", turno, tipo.getRotulo(), mensagem);
    }
}
