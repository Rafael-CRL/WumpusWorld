package wumpusworld.grafico;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import wumpusworld.nucleo.AgenteInteligente;
import wumpusworld.nucleo.Mundo;
import wumpusworld.nucleo.Partida;
import wumpusworld.nucleo.Percepcoes;
import wumpusworld.nucleo.Situacao;

/**
 * Painel com as informações da partida exigidas pelo enunciado:
 * posição atual, percepções, movimentos, pontuação e posse de ouro e flecha.
 *
 * <p>O layout é calculado uma única vez em {@link #definirArea(Area)} e
 * guardado em campos, para que a passagem de formas e a passagem de textos
 * usem exatamente as mesmas coordenadas.</p>
 */
public final class PainelStatus {

    private static final float MARGEM = 18f;

    private final Ativos ativos;

    private Area area = new Area(0f, 0f, 1f, 1f);
    private Area blocoPontuacao = area;
    private Area blocoObjetivo = area;
    private final Area[] botoes = new Area[4];
    private final Area[] azulejos = new Area[4];
    private final Area[] chipsDePercepcao = new Area[3];
    private final Area[] chipsDeInventario = new Area[2];
    private float baseDoTitulo;
    private float baseDoRotuloPercepcoes;
    private Acao acaoEmHover = Acao.NENHUMA;

    public enum Acao {
        JOGAR,
        PAUSAR,
        REINICIAR,
        ENCERRAR,
        NENHUMA
    }

    public PainelStatus(Ativos ativos) {
        this.ativos = ativos;
    }

    public void definirArea(Area novaArea) {
        this.area = novaArea;

        float x = novaArea.x() + MARGEM;
        float largura = novaArea.largura() - MARGEM * 2f;
        float cursor = novaArea.topo() - MARGEM;

        baseDoTitulo = cursor - 4f;
        cursor -= 44f;

        float larguraDosBotoes = 82f + 88f + 112f + 96f + 3f * 8f;
        float botaoX = novaArea.centroX() - larguraDosBotoes / 2f;
        float botaoY = cursor;
        botoes[0] = new Area(botaoX, botaoY, 82f, 32f);
        botaoX += 90f;
        botoes[1] = new Area(botaoX, botaoY, 88f, 32f);
        botaoX += 96f;
        botoes[2] = new Area(botaoX, botaoY, 112f, 32f);
        botaoX += 120f;
        botoes[3] = new Area(botaoX, botaoY, 96f, 32f);

        cursor -= 8f;
        float alturaDoDestaque = 62f;
        float larguraDaPontuacao = largura * 0.46f;
        blocoPontuacao = new Area(x, cursor - alturaDoDestaque,
                larguraDaPontuacao, alturaDoDestaque);
        blocoObjetivo = new Area(x + larguraDaPontuacao + 12f,
                cursor - alturaDoDestaque,
                largura - larguraDaPontuacao - 12f, alturaDoDestaque);
        cursor -= alturaDoDestaque + 12f;

        float alturaDoAzulejo = 60f;
        float larguraDoAzulejo = (largura - 3f * 9f) / 4f;
        for (int indice = 0; indice < azulejos.length; indice++) {
            azulejos[indice] = new Area(
                    x + indice * (larguraDoAzulejo + 9f),
                    cursor - alturaDoAzulejo,
                    larguraDoAzulejo, alturaDoAzulejo);
        }
        cursor -= alturaDoAzulejo + 16f;

        baseDoRotuloPercepcoes = cursor - 2f;
        cursor -= 14f;

        float alturaDoChip = 36f;
        float larguraDoChip = (largura - 2f * 9f) / 3f;
        for (int indice = 0; indice < chipsDePercepcao.length; indice++) {
            chipsDePercepcao[indice] = new Area(
                    x + indice * (larguraDoChip + 9f),
                    cursor - alturaDoChip, larguraDoChip, alturaDoChip);
        }
        cursor -= alturaDoChip + 10f;

        float larguraDoInventario = (largura - 9f) / 2f;
        for (int indice = 0; indice < chipsDeInventario.length; indice++) {
            chipsDeInventario[indice] = new Area(
                    x + indice * (larguraDoInventario + 9f),
                    cursor - alturaDoChip, larguraDoInventario, alturaDoChip);
        }
    }

    // -----------------------------------------------------------------------
    //  Formas
    // -----------------------------------------------------------------------

    public void desenharFormas(ShapeRenderer formas, Partida partida,
            EstadoDoJogo estado) {

        Desenho.painel(formas, area, 4f, Paleta.PAINEL, Paleta.BORDA, 1f);
        desenharBotoes(formas, estado);

        AgenteInteligente agente = partida.getAgente();
        Percepcoes percepcoes = partida.getPercepcoesAtuais();
        boolean vivo = agente.estaVivo();

        // Destaque da pontuação: verde quando positiva, vermelha quando negativa.
        Color corDaPontuacao = agente.getPontuacao() >= 0
                ? Paleta.SUCESSO : Paleta.PERIGO;
        Desenho.caixa(formas, blocoPontuacao, 12f, Paleta.PAINEL_INTERNO);

        // Objetivo do momento.
        Color corDoObjetivo = corDaSituacao(partida);
        Desenho.painel(formas, blocoObjetivo, 4f,
                Paleta.PAINEL_INTERNO, corDoObjetivo, 1f);

        for (Area azulejo : azulejos) {
            Desenho.caixa(formas, azulejo, 10f, Paleta.PAINEL_INTERNO);
        }

        // Chips das percepções: acesos somente quando o sinal existe.
        desenharChipDePercepcao(formas, chipsDePercepcao[0],
                percepcoes.brisa() && vivo, Paleta.BRISA);
        desenharChipDePercepcao(formas, chipsDePercepcao[1],
                percepcoes.fedor() && vivo, Paleta.FEDOR);
        desenharChipDePercepcao(formas, chipsDePercepcao[2],
                percepcoes.brilho() && vivo, Paleta.BRILHO);

        // Selos dentro dos chips.
        float seloX = chipsDePercepcao[0].x() + 20f;
        float seloY = chipsDePercepcao[0].centroY();
        Icones.seloDeBrisa(formas, seloX, seloY, 6.5f,
                percepcoes.brisa() && vivo ? 1f : 0.30f);
        Icones.seloDeFedor(formas, chipsDePercepcao[1].x() + 20f, seloY, 8f,
                percepcoes.fedor() && vivo ? 1f : 0.30f);
        Icones.seloDeBrilho(formas, chipsDePercepcao[2].x() + 20f, seloY, 7.5f,
                percepcoes.brilho() && vivo ? 1f : 0.30f);

        // Inventário.
        desenharChipDeInventario(formas, chipsDeInventario[0],
                agente.possuiOuro(), Paleta.OURO);
        desenharChipDeInventario(formas, chipsDeInventario[1],
                agente.possuiFlecha(), Paleta.ALERTA);
    }

    private void desenharBotoes(ShapeRenderer formas, EstadoDoJogo estado) {
        desenharBotao(formas, botoes[0], Paleta.AGENTE,
                estado == EstadoDoJogo.PARADO, Acao.JOGAR);
        desenharBotao(formas, botoes[1], Paleta.ALERTA,
                estado == EstadoDoJogo.JOGANDO || estado == EstadoDoJogo.PAUSADO,
                Acao.PAUSAR);
        desenharBotao(formas, botoes[2], Paleta.NEUTRO, true, Acao.REINICIAR);
        desenharBotao(formas, botoes[3], Paleta.PERIGO, true, Acao.ENCERRAR);
    }

    private void desenharBotao(ShapeRenderer formas, Area botao, Color cor,
            boolean habilitado, Acao acao) {
        Color fundo = habilitado && acao == acaoEmHover ? Paleta.BORDA
                : habilitado ? Paleta.PAINEL_DESTAQUE : Paleta.PAINEL_INTERNO;
        Desenho.painel(formas, botao, 4f, fundo,
                habilitado ? cor : Paleta.BORDA, 1f);
    }

    private void desenharChipDePercepcao(ShapeRenderer formas, Area chip,
            boolean ativo, Color cor) {
        if (ativo) {
            Desenho.painel(formas, chip, 4f, Paleta.PAINEL_DESTAQUE, cor, 1f);
        } else {
            Desenho.caixa(formas, chip, 4f, Paleta.PAINEL_INTERNO);
        }
    }

    private void desenharChipDeInventario(ShapeRenderer formas, Area chip,
            boolean possui, Color cor) {
        if (possui) {
            Desenho.painel(formas, chip, 4f, Paleta.PAINEL_DESTAQUE, cor, 1f);
        } else {
            Desenho.caixa(formas, chip, 4f, Paleta.PAINEL_INTERNO);
        }

        // Pequeno indicador redondo à esquerda do rótulo.
        formas.setColor(possui ? cor : Paleta.TEXTO_FRACO);
        formas.circle(chip.x() + 20f, chip.centroY(), 5.5f, 18);
    }

    // -----------------------------------------------------------------------
    //  Textos
    // -----------------------------------------------------------------------

    public void desenharTextos(SpriteBatch lote, Partida partida,
            EstadoDoJogo estado) {
        AgenteInteligente agente = partida.getAgente();
        Percepcoes percepcoes = partida.getPercepcoesAtuais();
        Mundo mundo = partida.getMundo();
        boolean vivo = agente.estaVivo();

        Desenho.texto(lote, ativos.fonteSecao, "INFORMAÇÕES DA PARTIDA",
                area.x() + MARGEM + 2f, baseDoTitulo, Paleta.TEXTO_SUAVE);
        desenharTextosDosBotoes(lote, estado);

        // Pontuação.
        Desenho.texto(lote, ativos.fonteMiuda, "PONTUAÇÃO",
                blocoPontuacao.x() + 16f, blocoPontuacao.topo() - 9f,
                Paleta.TEXTO_FRACO);
        Desenho.texto(lote, ativos.fonteTitulo,
                String.valueOf(agente.getPontuacao()),
                blocoPontuacao.x() + 16f, blocoPontuacao.topo() - 26f,
                agente.getPontuacao() >= 0 ? Paleta.SUCESSO : Paleta.PERIGO);

        // Objetivo.
        Color corDoObjetivo = corDaSituacao(partida);
        Desenho.texto(lote, ativos.fonteMiuda, "SITUAÇÃO",
                blocoObjetivo.x() + 16f, blocoObjetivo.topo() - 9f,
                Paleta.TEXTO_FRACO);
        Desenho.texto(lote, ativos.fonteTextoForte, tituloDaSituacao(partida),
                blocoObjetivo.x() + 16f, blocoObjetivo.topo() - 26f,
                corDoObjetivo);
        Desenho.texto(lote, ativos.fontePequena, detalheDaSituacao(partida),
                blocoObjetivo.x() + 16f, blocoObjetivo.topo() - 44f,
                Paleta.TEXTO_FRACO);

        // Azulejos numéricos.
        escreverAzulejo(lote, azulejos[0], "POSIÇÃO",
                agente.getPosicao().toString(), Paleta.AGENTE);
        escreverAzulejo(lote, azulejos[1], "MOVIMENTOS",
                String.valueOf(agente.getQuantidadeDeMovimentos()), Paleta.TEXTO);
        escreverAzulejo(lote, azulejos[2], "TURNO",
                String.valueOf(partida.getTurno()), Paleta.TEXTO);
        escreverAzulejo(lote, azulejos[3], "CASAS VISTAS",
                mundo.quantidadeDeCasasVisitadas() + "/"
                + (Mundo.TAMANHO * Mundo.TAMANHO), Paleta.TEXTO);

        // Percepções.
        Desenho.texto(lote, ativos.fonteMiuda, "PERCEPÇÕES NA CASA ATUAL",
                area.x() + MARGEM + 2f, baseDoRotuloPercepcoes, Paleta.TEXTO_FRACO);

        escreverChip(lote, chipsDePercepcao[0], "BRISA",
                percepcoes.brisa() && vivo, Paleta.BRISA);
        escreverChip(lote, chipsDePercepcao[1], "FEDOR",
                percepcoes.fedor() && vivo, Paleta.FEDOR);
        escreverChip(lote, chipsDePercepcao[2], "BRILHO",
                percepcoes.brilho() && vivo, Paleta.BRILHO);

        // Inventário.
        escreverChip(lote, chipsDeInventario[0],
                agente.possuiOuro() ? "OURO: RECOLHIDO" : "OURO: NÃO ENCONTRADO",
                agente.possuiOuro(), Paleta.OURO);
        escreverChip(lote, chipsDeInventario[1],
                agente.possuiFlecha() ? "FLECHA: DISPONÍVEL" : "FLECHA: USADA",
                agente.possuiFlecha(), Paleta.ALERTA);
    }

    private void desenharTextosDosBotoes(SpriteBatch lote, EstadoDoJogo estado) {
        float meiaLetra = ativos.fonteMiuda.getCapHeight() / 2f;
        escreverBotao(lote, botoes[0], "Jogar", estado == EstadoDoJogo.PARADO,
                Paleta.AGENTE_BRILHO, meiaLetra);
        boolean podePausar = estado == EstadoDoJogo.JOGANDO
                || estado == EstadoDoJogo.PAUSADO;
        escreverBotao(lote, botoes[1], estado == EstadoDoJogo.PAUSADO
                ? "Continuar" : "Pausar", podePausar, Paleta.ALERTA, meiaLetra);
        escreverBotao(lote, botoes[2], "Reiniciar", true,
                Paleta.TEXTO_SUAVE, meiaLetra);
        escreverBotao(lote, botoes[3], "Encerrar", true,
                Paleta.PERIGO, meiaLetra);
    }

    private void escreverBotao(SpriteBatch lote, Area botao, String texto,
            boolean habilitado, Color cor, float meiaLetra) {
        Desenho.textoCentralizado(lote, ativos.fonteMiuda, texto,
                botao.centroX(), botao.centroY() + meiaLetra,
                habilitado ? cor : Paleta.TEXTO_FRACO);
    }

    public Acao acaoNoPonto(float x, float y, EstadoDoJogo estado) {
        if (contem(botoes[0], x, y) && estado == EstadoDoJogo.PARADO) {
            return Acao.JOGAR;
        }
        if (contem(botoes[1], x, y) && (estado == EstadoDoJogo.JOGANDO
                || estado == EstadoDoJogo.PAUSADO)) {
            return Acao.PAUSAR;
        }
        if (contem(botoes[2], x, y)) {
            return Acao.REINICIAR;
        }
        if (contem(botoes[3], x, y)) {
            return Acao.ENCERRAR;
        }
        return Acao.NENHUMA;
    }

    public void atualizarHover(float x, float y, EstadoDoJogo estado) {
        acaoEmHover = acaoNoPonto(x, y, estado);
    }

    private static boolean contem(Area area, float x, float y) {
        return x >= area.x() && x <= area.direita()
                && y >= area.y() && y <= area.topo();
    }

    private void escreverAzulejo(SpriteBatch lote, Area azulejo, String rotulo,
            String valor, Color corDoValor) {
        Desenho.textoCentralizado(lote, ativos.fonteMiuda, rotulo,
                azulejo.centroX(), azulejo.topo() - 9f, Paleta.TEXTO_FRACO);
        Desenho.textoCentralizado(lote, ativos.fonteValor, valor,
                azulejo.centroX(), azulejo.topo() - 26f, corDoValor);
    }

    private void escreverChip(SpriteBatch lote, Area chip, String rotulo,
            boolean ativo, Color cor) {
        Desenho.texto(lote, ativos.fonteTextoForte, rotulo,
                chip.x() + 34f,
                chip.centroY() + ativos.fonteTextoForte.getCapHeight() / 2f,
                ativo ? cor : Paleta.TEXTO_FRACO);
    }

    // -----------------------------------------------------------------------

    private static Color corDaSituacao(Partida partida) {
        Situacao situacao = partida.getSituacao();
        if (situacao == Situacao.EM_ANDAMENTO) {
            return partida.getAgente().possuiOuro() ? Paleta.OURO : Paleta.AGENTE;
        }
        return switch (situacao) {
            case VITORIA -> Paleta.SUCESSO;
            case MORTE -> Paleta.PERIGO;
            case LIMITE_ATINGIDO -> Paleta.ALERTA;
            default -> Paleta.NEUTRO;
        };
    }

    private static String tituloDaSituacao(Partida partida) {
        if (partida.getSituacao() != Situacao.EM_ANDAMENTO) {
            return partida.getSituacao().getTitulo();
        }
        return partida.getAgente().possuiOuro()
                ? "RETORNANDO COM O OURO" : "EXPLORANDO A CAVERNA";
    }

    private static String detalheDaSituacao(Partida partida) {
        if (partida.getSituacao() != Situacao.EM_ANDAMENTO) {
            return partida.getSituacao().getDescricao();
        }
        return partida.getAgente().possuiOuro()
                ? "Refazendo o caminho seguro até [0][0]."
                : "Procurando o ouro e evitando os perigos.";
    }
}
