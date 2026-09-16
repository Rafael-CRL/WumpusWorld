package wumpusworld;

import wumpusworld.console.ModoConsole;
import wumpusworld.grafico.LancadorDesktop;

/**
 * Ponto de entrada do trabalho.
 *
 * <p>Sem argumentos, abre a janela gráfica construída com libGDX. Com
 * {@code --console}, executa a mesma simulação em modo texto, exatamente como
 * nas aulas 1 a 7 — as duas versões compartilham as classes de regra do
 * pacote {@code wumpusworld.nucleo}.</p>
 *
 * <pre>
 *   ./gradlew run          janela gráfica
 *   ./gradlew console      modo texto
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (pediuModoConsole(args)) {
            ModoConsole.executar();
        } else {
            LancadorDesktop.iniciar();
        }
    }

    private static boolean pediuModoConsole(String[] args) {
        for (String argumento : args) {
            if ("--console".equalsIgnoreCase(argumento)
                    || "-c".equalsIgnoreCase(argumento)
                    || "console".equalsIgnoreCase(argumento)) {
                return true;
            }
        }
        return false;
    }
}
