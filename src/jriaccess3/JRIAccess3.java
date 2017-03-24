/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import jriacces3.log.Errors;
import jriacces3.log.ILog;
import jriacces3.log.LogType;
import jriacces3.log.TXTLog;
import jriaccess3.batch.Batch;
import jriaccess3.live.RJava;

/**
 *
 * @author mfernandes
 */
public class JRIAccess3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //test proposits
        if (args == null || args.length < 1) {
             args = new String[]{"LIVE", "", "INFO", "--vanilla"};
           // args = new String[]{"BATCH", "log.txt", "DEBUG", "script.R", "/home/mfernandes/jritest/", "token"};
        }

        if (args.length < 1 || args[0] == null || args[0].isEmpty()) {
            System.out.println("USAGE: [LIVE/BATCH] [ LOG FILE NAME ] [ LOG NIVEL ] [ OPTIONS ]");
            System.exit(Errors.ARGS_INVALIDO.ordinal());
        }

        String path = System.getenv("R_HOME");

        if (path == null || path.isEmpty() || path.length() < 2) {

            String s = "R_HOME=/usr/lib64/R\n"
                    + "export R_HOME\n"
                    + "R_SHARE_DIR=/usr/share/R\n"
                    + "export R_SHARE_DIR\n"
                    + "R_INCLUDE_DIR=/usr/include/R\n"
                    + "export R_INCLUDE_DIR\n"
                    + "R_DOC_DIR=/usr/share/doc/R\n"
                    + "export R_DOC_DIR\n"
                    + "LD_LIBRARY_PATH=/home/mfernandes/R/x86_64-redhat-linux-gnu-library/3.3/rJava/jri\n"
                    + "export LD_LIBRARY_PATH";
            System.err.println("Não é possivel iniciar devido às variaveis de ambiente do JRI.\n,"
                    + "Para solucionar, altere e adicione em ~/.bashrc:\n" + s);
            System.exit(Errors.SYSTEM_ENVIRONMENT.ordinal());
        }

        ILog log = null;

        try {
            log = new TXTLog(args[1], LogType.getLogType(args[2]));
            log.printLog(LogType.LOG_DEBUG, "Arquivo Log inicializado com sucesso. {JRIaccess2.java/62}");
            log.printLog(LogType.LOG_INFO, "[ ARGS ]" + Arrays.toString(args));
        } catch (IOException ex) {
            System.err.println("erro ao inicializar arquivo de log: " + ex);
            System.exit(Errors.LOG_INICIALIZE.ordinal());
        }

        String modo = args[0];

        if (null == modo) {
            System.out.println("USAGE: [LIVE/BATCH] [ LOG FILE NAME ] [ LOG NIVEL ] [ OPTIONS ]");
            System.exit(Errors.ARGS_INVALIDO.ordinal());
        } else {
            switch (modo) {
                case "LIVE":
                    log.printLog(LogType.LOG_DEBUG, "Inicializando protocolo. {JRIaccess2.java/77}");
                    if (args.length > 3) {
                        new RJava(log).init(Arrays.copyOfRange(args, 3, args.length));
                    } else {
                        System.out.println("USAGE: [ LIVE ] [ LOG FILE NAME ] [ LOG NIVEL ] [ JRI ARGS ] ... ");
                        System.exit(Errors.ARGS_INSUFICIENTE.ordinal());
                    }
                    log.printLog(LogType.LOG_DEBUG, "Protocolo inicializado com sucesso. {JRIaccess2.java/79}");
                    break;

                case "BATCH":
                    log.printLog(LogType.LOG_DEBUG, "inicializando em modo BATCH. {JRIaccess2.java/60}");
                    if (args.length == 6) {
                        Batch batch = new Batch(log, args[3], args[4], args[5]);
                        log.printLog(LogType.LOG_DEBUG, "starting process. {JRIaccess2.java/88}");
                        batch.startProcess();
                        Scanner scanner = new Scanner(System.in);
                        while (scanner.hasNextLine()) {
                            String cmd = scanner.nextLine();
                            log.printLog(LogType.LOG_INFO, "requisitado: [" + cmd + "]");
                            switch (cmd) {
                                case "status":
                                    String retorno = batch.getStatus();
                                    log.printLog(LogType.LOG_INFO, "status requisitado, retorno:"
                                            + retorno + " {JRIaccess2.java/88}");
                                    System.out.println(retorno);
                                    break;
                                case "stop": ////termina o processo (mas ñ o batch) p ler saida
                                    log.printLog(LogType.LOG_INFO, "stop requisitado: "
                                            + batch.stopProcess(false) + " {JRIaccess2.java/88}");
                                    break;
                                case "exit":
                                    log.printLog(LogType.LOG_INFO,
                                            "o processo sera encerrado: " + batch.stopProcess(true));
                                    System.exit(Errors.SIGNAL_EXIT.ordinal());
                                default:

//                                    try {
//                                        if (cmd.startsWith("output")) {
//                                            String opt = cmd.substring(6);
//                                            String numero = ((opt != null)
//                                                    && (!opt.isEmpty()) && (opt.length() > 0)) ? opt : "0";
//                                            int num = Integer.parseInt(numero);
//                                            log.printLog(LogType.LOG_DEBUG, "outputs requisitado com opçao: " + num + " com: " + cmd);
//                                            retorno = batch.getOutput(num);
//                                            log.printLog(LogType.LOG_INFO, "output requisitado.");
//                                            break;
//                                        } else if (cmd.startsWith("error")) {
//                                            String opt = cmd.substring(5);
//                                            String numero = ((opt != null)
//                                                    && (!opt.isEmpty()) && (opt.length() > 0)) ? opt : "0";
//                                            int num = Integer.parseInt(numero);
//                                            log.printLog(LogType.LOG_DEBUG, "erros requisitado com opçao: " + num + " com: " + cmd);
//                                            retorno = batch.getErrorOutput(num);
//                                            log.printLog(LogType.LOG_INFO, "erros requisitado.");
//                                            break;
//                                        } else {
//                                            if (cmd.matches("\\d+,\\d+")) {
//                                                String[] spl = cmd.split(",");
//                                                retorno = batch.getResult(
//                                                        Integer.parseInt(spl[0]),
//                                                        Integer.parseInt(spl[1]));
//                                                break;
//                                            }
//                                            if (cmd.matches("-\\d+,\\d+")) {
//                                                String[] spl = cmd.split(",");
//                                                retorno = batch.getResult(
//                                                        Integer.parseInt(spl[0]),
//                                                        Integer.parseInt(spl[1]));
//                                                break;
//                                            }
//                                            retorno = batch.getResult(Integer.parseInt(cmd));
//                                            log.printLog(LogType.LOG_INFO, "result requisitado: " + cmd);
//                                            break;
//                                        }
//                                    } catch (Exception ex) {
//                                        log.printLog(LogType.LOG_ERROR, "impossivel entender comando: " + cmd + " ex: " + ex);
//                                    }
//
                                    log.printLog(LogType.LOG_WARNING, "requição não definida. {JRIaccess2.java/88}");
                                    System.out.println("OPTIONS: status, stop, exit");
                                    break;
//                            }
//                            processo++;
//                            if (retorno == null) {
//                                log.printLog(LogType.LOG_WARNING, "nulo ao processar: " + cmd);
//                                continue;
//                            }
//                            for (String line : retorno.split(System.lineSeparator())) {
//                                System.out.println("[" + processo + "]" + line);
//                            }

                            }
                        }
                    } else {
                        System.out.println("USAGE: [ BATCH ] [ LOG FILE NAME ] [ LOG NIVEL ] [ FILE .R ] [ DIRETORIO ] [ TOKEN ]");
                        log.printLog(LogType.LOG_ERROR, "argumentos insuficientes: "
                                + "USAGE: [ BATCH ] [ LOG FILE NAME ] [ LOG NIVEL ] [ FILE .R ] [ DIRETORIO ] [ TOKEN ] : " + Arrays.toString(args));
                        System.exit(Errors.ARGS_INSUFICIENTE.ordinal());
                    }
                    break;
                default:
                    log.printLog(LogType.LOG_DEBUG, "Arquivo Log inicializado com sucesso. {JRIaccess2.java/85}");
                    System.out.println("USAGE: [LIVE/BATCH] [ LOG FILE NAME ] [ LOG NIVEL ] [ OPTIONS ]");
                    System.exit(Errors.ARGS_INVALIDO.ordinal());
            }

        }
    }

    public static String getPID() {
        String processName
                = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return processName.split("@")[0];
    }
}
