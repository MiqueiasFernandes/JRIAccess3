/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.live;

import java.io.File;
import jriacces3.log.Errors;
import jriacces3.log.ILog;
import jriacces3.log.LogType;
import jriaccess3.Status;
import jriaccess3.live.writers.StatusWriter;
import org.rosuda.JRI.Rengine;

/**
 *
 * @author mfernandes
 */
public class RJava {

    public boolean inUse = false, ocioso = false;
    private final TextConsole console;
    private final ILog log;
    private final int timeout_iteration = 100;
    private Rengine re = null;
    private String pid = null;
    private Status status = null;
    private final Thread thread;

    public RJava(ILog log) {
        this.log = log;
        thread = new Thread(() -> {
            while (re != null && re.isAlive()) {
                logDebug("aguardando para enviar status...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    logError("thread para ocioso impossivel aguardar: " + ex);
                }

                if (ocioso) {
                    logDebug("ocioso: enviando status...");
                    getConsole().sendStatus();
                }
            }
        });

        this.console = new TextConsole(log) {
            @Override
            public void ocioso() {
                ocioso = true;
            }

            @Override
            public void normal() {
                ocioso = false;
            }

            @Override
            public boolean isAlive() {
                return re != null && re.isAlive();
            }

            @Override
            public void endJRI() {
                if (re != null) {
                    re.end();
                }
            }

            @Override
            public void sendStatus() {
                if (status == null) {
                    log.printLog(LogType.LOG_ERROR, "impossivel enviar status, atributo nulo.");
                    return;
                }
                escrever(new StatusWriter(status.getStatusJson(re.isAlive())));
            }

            @Override
            public void inUse() {
                inUse = true;
            }
        };
    }

    public void init(String[] args) {
        // just making sure we have the right version of everything
        if (!Rengine.versionCheck()) {
            logError("** Version mismatch - Java files don't match library version.");
            System.exit(Errors.R_VERSIOIN_ERROR.ordinal());
        }
        logInfo("Creating Rengine (with arguments)");
        // 1) we pass the arguments from the command line
        // 2) we won't use the main loop at first, we'll start it later
        //    (that's the "false" as second argument)
        // 3) the callbacks are implemented by the TextConsole class above
        re = new Rengine(args, false, console);
        logInfo("Rengine created, waiting for R");
        // the engine creates R is a new thread, so we should wait until it's ready
        if (!re.waitForR()) {
            logError("Cannot load R");
            return;
        }

        pid = String.valueOf(re.eval("pid = Sys.getpid()").asInt());

        status = new Status(pid, log, new File(".").getAbsolutePath()) {
            @Override
            public void onClose() {
                re.end();
            }

            @Override
            public String addStatus() {
                return ",\"jriState\":\"" + re.getState().name() + "\""
                        + ",\"isAlive\":" + re.isAlive();
            }
        };

        thread.start();

        log.printLog(LogType.LOG_INFO, "processo de obtenção de status iniciado;");
        if (true) {
            logInfo("Now the console is yours ... have fun");
            re.startMainLoop();
            log.printLog(LogType.LOG_DEBUG, "inicializando timeout user interation.tempo limite: "
                    + timeout_iteration + " {RJava.java/51}");
            int i = 0;
            while (i++ < timeout_iteration && re.isAlive() && !inUse) {
                try {
                    console.sendStatus();
                    Thread.sleep(1000);
                    logWarning("waiting user iteration. (" + i + ")");
                } catch (InterruptedException ex) {
                    logError("thread para timeout: " + ex);
                    System.exit(Errors.THREAD_TIMEOUT.ordinal());
                }
            }
            if (!inUse) {
                log.printLog(LogType.LOG_DEBUG, "encerrando rengine, não esta em uso. {RJava.java/64}");
                re.end();
                logError("timeout para interação do usuário.");
                System.exit(Errors.TIMEOUT_USER_ITERATION.ordinal());
            }
        } else {
            logError("erro desconhecido.");
            log.printLog(LogType.LOG_DEBUG, "encerrando rengine. {RJava.java/71}");
            re.end();
            System.exit(Errors.FAIL_GENERAL.ordinal());
        }
    }

    public void logInfo(String texto) {
        log(LogType.LOG_INFO, texto);
    }

    public void logDebug(String texto) {
        log(LogType.LOG_DEBUG, texto);
    }

    public void logWarning(String texto) {
        log(LogType.LOG_WARNING, texto);
    }

    public void logError(String texto) {
        log(LogType.LOG_ERROR, texto);
    }

    public void log(LogType tipo, String texto) {
        log.printLog(LogType.LOG_DEBUG, "imprimindo log. {RJava.java/90}");
        log.printLog(tipo, "[ RJAVA ] " + texto);
    }

    public Rengine getRengine() {
        return re;
    }

    public String getPid() {
        return pid;
    }

    public Status getStatus() {
        return status;
    }

    public ILog getLog() {
        return log;
    }

    public TextConsole getConsole() {
        return console;
    }

}
