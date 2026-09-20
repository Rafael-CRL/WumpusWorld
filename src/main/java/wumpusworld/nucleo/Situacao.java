package wumpusworld.nucleo;

public enum Situacao {

    EM_ANDAMENTO("EM ANDAMENTO", "O agente ainda está explorando a caverna."),
    VITORIA("VITÓRIA", "O agente voltou à casa inicial com o ouro."),
    MORTE("FIM DE JOGO", "O agente não sobreviveu à caverna."),
    LIMITE_ATINGIDO("LIMITE ATINGIDO", "O limite de exploração foi alcançado sem o ouro.");

    private final String titulo;
    private final String descricao;

    Situacao(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean encerrada() {
        return this != EM_ANDAMENTO;
    }
}
