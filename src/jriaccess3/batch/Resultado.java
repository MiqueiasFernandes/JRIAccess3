/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.batch;

import java.io.IOException;
import java.util.Scanner;
import jriacces3.log.ILog;
import jriacces3.log.LogType;

/**
 *
 * @author mfernandes
 */
public abstract class Resultado {

    String pid;
    ILog log;
    String[] headers, values;
    Process processo;
    ProcessBuilder processBuilder;
    Thread read;

    public Resultado(String pid, ILog log) {
        this.pid = pid;
        this.log = log;
        processBuilder = new ProcessBuilder(new String[]{"top", "-p", pid, "-b"});
        printLog(LogType.LOG_INFO, "inicializando processo de recuperação de status: " + pid);
        try {
            printLog(LogType.LOG_DEBUG, "a startar processo...");
            processo = processBuilder.start();
            printLog(LogType.LOG_DEBUG, "processo startado...");

            Scanner sc = new Scanner(processo.getInputStream());
            printLog(LogType.LOG_DEBUG, "a sccenar processo...");

            read = new Thread(() -> {
                while (sc.hasNextLine() && processo.isAlive()) {
                    String next = sc.nextLine();
                    printLog(LogType.LOG_DEBUG, "linha lida de top: " + next);
                    if (next.contains("PID")
                            && next.contains("VIRT")
                            && next.contains("%CPU")
                            && next.contains("%MEM")) {

                        headers = ((next.trim()
                                .replaceAll(",", ".")
                                .replaceAll("\\s+", ",")
                                .split(",")));
                        printLog(LogType.LOG_DEBUG, "header: " + next + " -> "
                                + headers);
                        String value = sc.nextLine();
                        values = ((value.trim()
                                .replaceAll(",", ".")
                                .replaceAll("\\s+", ",")
                                .split(",")));
                        printLog(LogType.LOG_DEBUG, "values: " + value + " -> "
                                + values);
                        printLog(LogType.LOG_DEBUG, "PID: " + pid + " CPU " + getValue("%CPU"));
                    }
                }
            });
            read.start();
        } catch (IOException ex) {
            printLog(LogType.LOG_ERROR, "impossivel obter status do programa: " + ex);
            close();
        }
    }

    public String getValue(String nome) {

        if (headers == null || headers.length < 1 || values == null || values.length < 1) {
            printLog(LogType.LOG_WARNING, "não há headers ou values disponiveis para status.");
            return null;
        }

        int i = 0;
        for (String header : headers) {
            if (header.equals(nome)) {
                if (values.length > i) {
                    return values[i];
                }
            }
            i++;
        }

        printLog(LogType.LOG_ERROR, "impossivel obter status de: " + nome);
        return null;
    }

    void printLog(LogType tipo, String text) {
        log.printLog(tipo, "Resultado PID: " + pid + " :" + text);
    }

    public abstract void close();

    public Process getProcesso() {
        return processo;
    }

    public String getCPU() {
        return getValue("%CPU");

    }

    public void terminar() {
        processo.destroy();
    }

}
