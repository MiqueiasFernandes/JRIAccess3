/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.live;

import java.util.Base64;
import java.util.Scanner;
import jriacces3.log.Errors;
import jriacces3.log.ILog;
import jriacces3.log.LogType;
import jriaccess3.live.writers.BusyWriter;
import jriaccess3.live.writers.ConsoleWriter;
import jriaccess3.live.writers.EchoWriter;
import jriaccess3.live.writers.FlushWriter;
import jriaccess3.live.writers.IWriter;
import jriaccess3.live.writers.MessageWriter;
import jriaccess3.live.writers.PromptWriter;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;

///vide http://rforge.net/org/doc/org/rosuda/JRI/RMainLoopCallbacks.html for details
/**
 *
 * @author mfernandes
 */
public abstract class TextConsole implements RMainLoopCallbacks {

    ILog log;

    public TextConsole(ILog log) {
        this.log = log;
    }

    @Override
    public void rWriteConsole(Rengine re, String text, int oType) {
        log.printLog(LogType.LOG_DEBUG, "escrevendo "
                + (oType == 0 ? "regular" : "error/warning")
                + ". {TectConsole.java/30}");
        escrever(new ConsoleWriter(oType, text));
    }

    @Override
    public void rBusy(Rengine re, int which) {
        log.printLog(LogType.LOG_DEBUG, "console esta "
                + (which == 0 ? "trabalhando" : "pronto")
                + ". {TextConsole.java/36}");
        escrever(new BusyWriter(which));
        if (which == 1) {
            ocioso();
        } else {
            normal();
        }
    }

    @Override
    public String rReadConsole(Rengine re, String prompt, int addToHistory) {
        log.printLog(LogType.LOG_DEBUG, "lendo. {TextConsole.java/42}");
        return readLine(prompt) + System.lineSeparator();
    }

    @Override
    public void rShowMessage(Rengine re, String message) {
        log.printLog(LogType.LOG_DEBUG, "informando mensagem. {TextConsole.java/48}");
        escrever(new MessageWriter(message));
    }

    @Override
    public String rChooseFile(Rengine re, int newFile) {
        log.printLog(LogType.LOG_DEBUG, "escolhendo arquivo. {TextConsole.java/54}");
        return readLine((newFile == 0) ? "Select a file" : "Select a new file");
    }

    @Override
    public void rFlushConsole(Rengine rngn) {
        log.printLog(LogType.LOG_DEBUG, "limpando console. {TextConsole.java/60}");
        escrever(new FlushWriter());
    }

    @Override
    public void rSaveHistory(Rengine rngn, String string) {
        log.printLog(LogType.LOG_DEBUG, "salvando history. {TextConsole.java/66}");
        escrever(new MessageWriter("Salvar arquivo history: " + string));
    }

    @Override
    public void rLoadHistory(Rengine rngn, String string) {
        log.printLog(LogType.LOG_DEBUG, "carregando history. {TextConsole.java/72}");
        escrever(new MessageWriter("Abrir arquivo history: " + string));
    }

    public String escrever(IWriter writer) {
        return escrever(writer, log);
    }

    public static String escrever(IWriter writer, ILog log) {
        String texto = "[" + writer.getTipo() + "]" + writer.getText();
        return sendTexto(texto, log);
    }

    public static String sendTexto(String texto, ILog log) {
        System.out.println(Base64.getEncoder().encodeToString(texto.getBytes()));
        log.printLog(LogType.LOG_INFO, "escreveu: " + texto);
        return texto;
    }

    public void echo(String echo) {
        escrever(new EchoWriter(echo + System.lineSeparator()));
        log.printLog(LogType.LOG_DEBUG, "echo enviado. {ReadingState.java/26}");
    }

    public String readLine(String prompt) {
        escrever(new PromptWriter(prompt));
        String line = null;
        while (((line = readLine()) == null) && isAlive()) {
            log.printLog(LogType.LOG_WARNING, "linha vazia lida: [" + line + "]");
        }
        echo(prompt + line);
        return line;
    }

    public String readLine() {
        Scanner scanner = new Scanner(System.in);
        String line = null;
        while (scanner.hasNextLine() && isAlive()) {
            inUse();
            line = scanner.nextLine();
            log.printLog(LogType.LOG_INFO, "leu: " + line);
            if ("stop".equals(line)) {
                log.printLog(LogType.LOG_INFO, "encerrando JRI por comando stop.");
                endJRI();
                log.printLog(LogType.LOG_INFO, "encerrando programa por comando stop.");
                System.exit(Errors.SIGNAL_STOP.ordinal());
            }
            if ("status".equals(line)) {
                sendStatus();
                continue;
            }

            try {
                return new String(Base64.getDecoder().decode(line));
            } catch (Exception ex) {
                log.printLog(LogType.LOG_ERROR, "impossivel decodificar: " + line + " ex: " + ex);
            }
        }
        return line;
    }

    public abstract void ocioso();

    public abstract void normal();

    public abstract boolean isAlive();

    public abstract void endJRI();

    public abstract void sendStatus();

    public abstract void inUse();

}
