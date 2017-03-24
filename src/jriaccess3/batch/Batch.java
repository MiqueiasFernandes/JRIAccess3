/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3.batch;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import jriacces3.log.Errors;
import jriacces3.log.ILog;
import jriacces3.log.LogType;
import jriaccess3.JRIAccess3;
import jriaccess3.Status;

/**
 *
 * @author mfernandes
 */
public class Batch {

    private static final String R_BATCH_CMD1 = "R";
    private static final String R_BATCH_CMD2 = "CMD";
    private static final String R_BATCH_CMD3 = "BATCH";
    private final String diretorio;
    private final String fileName;
    private final String token;
    private final ILog log;
    private Process process;
    private ProcessBuilder processBuilder;
    private File output, error;
    private String pid = null;
    private Status status = null;
    private static final int PID_TIMEOUT = 10;

    public Batch(ILog log, String fileName, String diretorio, String token) {
        this.log = log;
        this.fileName = fileName;
        this.diretorio = diretorio;
        this.token = token;
        logInfo("nome do arquivo de script: " + fileName);
        logInfo("diretorio para o script: " + diretorio);
        try {
            output = File.createTempFile("status", null);
            error = File.createTempFile("status", null);
        } catch (IOException ex) {
            logError("impossivel criar arquivos temporarios de output e error: " + ex);
        }
    }

    public boolean startProcess() {
        try {
            processBuilder = new ProcessBuilder(R_BATCH_CMD1, R_BATCH_CMD2, R_BATCH_CMD3, fileName)
                    .directory(new File(diretorio))
                    .redirectOutput(output)
                    .redirectError(error);
            process = processBuilder.start();

            logInfo("processo iniciado com o comando: "
                    + Arrays.toString(new String[]{R_BATCH_CMD1, R_BATCH_CMD2, R_BATCH_CMD3, fileName}));

            new Thread(() -> {

                int cont = 0;
                while (process.isAlive() && cont++ < PID_TIMEOUT
                        && (pid == null || pid.isEmpty() || !pid.matches("\\d{2,9}"))) {
                    logWarning("aguardando por PID: " + pid + " time: " + cont + " de " + PID_TIMEOUT);
                    try {
                        Thread.sleep(1000);
                        pid = new Scanner(new File(diretorio + token + ".pid")).nextLine();
                    } catch (Exception ex) {
                        logWarning("impossivel obter PID => PID: " + pid + " ex: " + ex);
                    }
                }

                if (cont >= PID_TIMEOUT) {
                    logError("timeout para obter PID em "
                            + diretorio + "process.pid => PID: " + pid + ", abortando...");
                    stopProcess(true);
                    stopProcess(true);
                    System.exit(Errors.PID_TIMEOUT.ordinal());
                }

                if (process.isAlive()) {
                    status = new Status(pid, log, diretorio) {
                        @Override
                        public void onClose() {
                            stopProcess(true);
                            logError("impossivel iniciar processo de status, BATCH:68");
                            System.exit(Errors.STATUS_FAIL.ordinal());
                        }

                        @Override
                        public String addStatus() {
                            return ",\"outputs\":\"" + output.getAbsolutePath() + "\""
                                    + ",\"errors\":\"" + error.getAbsolutePath() + "\""
                                    + ",\"isAlive\":" + process.isAlive()
                                    + (!process.isAlive()
                                    ? ",\"exitValue\":\"" + process.exitValue() + "\"" : "");
                        }
                    };
                } else {
                    logError("falhou ao tentar obter PID em "
                            + diretorio + "process.pid => PID: " + pid + " em " + cont + "sg, abortando...");
//                    try {
//                        stopProcess(true);
//                        stopProcess(true);
//                    } catch (Exception ex) {
//                        logError("falhou ao tentar abortar processo. " + ex);
//                    }
//                    System.exit(Errors.PID_INVALID.ordinal());
                }
            }).start();

        } catch (IOException ex) {
            logError("imporssivel iniciar o processo: " + ex);
        }
        return true;
    }

    public boolean stopProcess(boolean exit) {
        logInfo("encerrando processo.");
        if (process.isAlive()) {
            process.destroy();
        }

        if (pid != null) {
            try {
                Runtime.getRuntime().exec("kill -9 " + pid);
                logWarning("processo forcado a terminar.");
            } catch (IOException ex) {
                logWarning("impossivel terminar processo pid "
                        + pid + " processo ja terminado? " + process.isAlive()
                        + " EX: " + ex
                );
            }
        }

        if (!exit) {
            return true;
        }

        if (status != null) {
            status.terminar();
        }

        if (JRIAccess3.getPID() != null) {
            try {

                File f = new File("/proc/" + JRIAccess3.getPID() + "/task/");
                String[] list = f.list();
                for (int i = list.length - 1; i >= 0; i--) {
                    Runtime.getRuntime().exec("kill -9 " + list[i]);
                }

                Runtime.getRuntime().exec("kill -9 " + JRIAccess3.getPID());
                logWarning("processo forçado a terminar.");
                return true;
            } catch (IOException ex) {
                logWarning("impossivel terminar processo pid "
                        + JRIAccess3.getPID() + " processo ja terminado? " + process.isAlive()
                        + " EX: " + ex
                );
            }
        }

        logWarning("impossivel abortar processo, terminado");
        return false;
    }

    public String getStatus() {
//        if (!process.isAlive()) {
//            logInfo("obtido status: processo morto.");
//            return "{\"isAlive\":false,\"exitValue\":" + process.exitValue() + "}";
//        }
        if (pid == null) {
            return "{\"error\":\"pid não encontrado.\"}";
        }
        if (status == null) {
            return "{\"error\":\"status não iniciado.\"}";
        }
        return status.getStatusJson(process.isAlive());
    }

//    public String getResult(int opcao) {
//        if (!process.isAlive()) {
//            logInfo("lendo arquivo de saida em: " + diretorio + fileName + "out");
//            File result = new File(diretorio + fileName + "out");
//            return getLines(result, opcao, 0);
//        }
//        return "PROCESSANDO";
//    }
//
//    public String getOutput(int opcao) {
//        return (getLines(output, opcao, 0));
//    }
//
//    public String getErrorOutput(int opcao) {
//        return (getLines(error, opcao, 0));
//    }
    //// case option < 0 = count lines botton to up starting on byte option
    //// case option > 0 = count lines up to botton
    //// case option = 0 = all lines
//    String getLines(File file, int option, int limit) {
//        try {
//            StringBuilder lines = new StringBuilder();
//            lines.append("[FILE START]").append(System.lineSeparator());
//            int linescount = 0;
//            if (option >= 0) {
//                Scanner scanner = new Scanner(file);
//                int count = 0;
//                while ((option == 0 || (option > count++)) && scanner.hasNextLine() && ((linescount < limit) || limit == 0)) {
//                    lines.append("[").append(linescount++).append("]")
//                            .append(scanner.nextLine()).append(System.lineSeparator());
//                }
//            } else {
//                RandomAccessFile randFile = new RandomAccessFile(file, "r");
//                randFile.seek(randFile.length() + option);
//                while (randFile.getFilePointer() < randFile.length() && ((linescount < limit) || limit == 0)) {
//                    lines.append("[").append(linescount++).append("]")
//                            .append(randFile.readLine()).append(System.lineSeparator());
//                }
//                randFile.close();
//            }
//            lines.append("[FILE END]").append(System.lineSeparator());
//            return lines.toString();
////            File f = File.createTempFile("output", "itgm");
////            FileWriter writer = new FileWriter(f);
////            writer.write(lines.toString());
////            writer.close();
////            return f.getAbsolutePath();
//        } catch (Exception ex) {
//            logError("impossivel ler arquivo: " + file.getAbsolutePath() + " causa: " + ex);
//        }
//        return null;
//    }
//
//    public String getPid() {
//        return pid;
//    }
    public final void logInfo(String texto) {
        log(LogType.LOG_INFO, texto);
    }

    public final void logWarning(String texto) {
        log(LogType.LOG_WARNING, texto);
    }

    public final void logError(String texto) {
        log(LogType.LOG_ERROR, texto);
    }

    public final void logDebug(String texto) {
        log(LogType.LOG_DEBUG, texto);
    }

    public final void log(LogType tipo, String texto) {
        log.printLog(tipo, "[ BATCH ] " + texto);
    }

//    public String getBytes() {
//        try {
//            return String.valueOf(new RandomAccessFile(new File(diretorio + fileName + "out"), "r").length());
//        } catch (Exception ex) {
//            logError("impossivel obter o tamanho do arquivo " + diretorio + fileName + "out EX: " + ex);
//            return "0";
//        }
//    }
//    public String getResult(int opcao, int lines) {
//        if (!process.isAlive()) {
//            logInfo("lendo arquivo de saida em: " + diretorio + fileName + "out");
//            File result = new File(diretorio + fileName + "out");
//            return getLines(result, opcao, lines);
//        }
//        return "PROCESSANDO";
//    }
}
