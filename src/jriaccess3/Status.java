/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriaccess3;

import java.io.File;
import java.io.FileInputStream;
import jriacces3.log.ILog;
import jriacces3.log.LogType;
import jriaccess3.batch.Resultado;

/**
 *
 * @author mfernandes
 */
public abstract class Status {

    private final Resultado resultadoR, resultadoJava;
    private final ILog log;
    private final String pid;
    private final String diretorio;

    public Status(String pid, ILog log, String diretorio) {
        this.log = log;
        this.pid = pid;
        this.diretorio = diretorio;

        resultadoR = new Resultado(pid, log) {
            @Override
            public void close() {
                onClose();
            }
        };

        if (pid == null ? JRIAccess3.getPID() != null : !pid.equals(JRIAccess3.getPID())) {
            resultadoJava = new Resultado(JRIAccess3.getPID(), log) {
                @Override
                public void close() {
                    onClose();
                }
            };
        } else {
            resultadoJava = null;
        }

    }

//    public void iniciaProcesso(String pid, ArrayList<String> headers, ArrayList<String> values) {
//
//        ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"top", "-p", pid, "-b"});
//        log.printLog(LogType.LOG_INFO, "inicializando processo de recuperação de status: " + pid);
//        try {
//            log.printLog(LogType.LOG_DEBUG, "a startar processo...");
//            Process processo = processBuilder.start();
//            log.printLog(LogType.LOG_DEBUG, "processo startado...");
//
//            Scanner sc = new Scanner(processo.getInputStream());
//            log.printLog(LogType.LOG_DEBUG, "a sccenar processo...");
//
//            Thread read = new Thread(() -> {
//                while (sc.hasNextLine() && processo.isAlive()) {
//                    String next = sc.nextLine();
//                    log.printLog(LogType.LOG_DEBUG, "linha lida de top: " + next);
//                    if (next.contains("PID")
//                            && next.contains("VIRT")
//                            && next.contains("%CPU")
//                            && next.contains("%MEM")) {
//                        headers.clear();
//                        values.clear();
//                        headers.addAll(Arrays.asList(next.trim()
//                                .replaceAll(",", ".")
//                                .replaceAll("\\s+", ",")
//                                .split(",")));
//                        log.printLog(LogType.LOG_DEBUG, "header: " + next + " -> "
//                                + headers);
//                        String value = sc.nextLine();
//                        values.addAll(Arrays.asList(value.trim()
//                                .replaceAll(",", ".")
//                                .replaceAll("\\s+", ",")
//                                .split(",")));
//                        log.printLog(LogType.LOG_DEBUG, "values: " + value + " -> "
//                                + values);
//                        log.printLog(LogType.LOG_DEBUG, "PID: " + pid + " CPU " + getValue("%CPU", headers, values));
//                    }
//                }
//            });
//            read.start();
//        } catch (IOException ex) {
//            log.printLog(LogType.LOG_ERROR, "impossivel obter status do programa: " + ex);
//            close();
//        }
//    }
//    public String getValue(String nome, ArrayList<String> headers, ArrayList<String> values) {
//
//        if (headers == null || headers.size() < 1 || values == null || values.size() < 1) {
//            log.printLog(LogType.LOG_WARNING, "não há headers ou values disponiveis para status.");
//            return null;
//        }
//
//        int i = 0;
//        for (String header : headers) {
//            if (header.equals(nome)) {
//                if (values.size() > i) {
//                    return values.get(i);
//                }
//            }
//            i++;
//        }
//
//        log.printLog(LogType.LOG_ERROR, "impossivel obter status de: " + nome);
//        return null;
//    }
    public String getMemory() {
//        String res = getValue("RES"), shr = getValue("SHR");
//        if (res == null || res.isEmpty() || shr == null || shr.isEmpty()) {
//            return null;
//        }
//        try {
//            return String.valueOf(((Integer.parseInt(res) + Integer.parseInt(shr)) / 1024));
//        } catch (NumberFormatException ex) {
//            log.printLog(LogType.LOG_ERROR, "Status/100: impossivel converter: res "
//                    + res + " shr " + shr + " ex: " + ex);
//            return null;
//        }

//        if (pid == null ? JRIAccess3.getPID() != null : !pid.equals(JRIAccess3.getPID())) {
//
//            String memR = getValorOfCMD("cat /sys/fs/cgroup/memory/itgm"
//                    + pid + "/memory.memsw.usage_in_bytes", "b");
//            String memJava = getValorOfCMD("cat /sys/fs/cgroup/memory/itgm"
//                    + JRIAccess3.getPID() + "/memory.memsw.usage_in_bytes", "b");
//
//            return ((memR == null) ? (memJava)
//                    : ((memJava == null) ? (memR)
//                            : ((Float.parseFloat(memR) > Float.parseFloat(memJava)) ? memR : memJava)));
//        }
        return getValorOfCMD("cat /sys/fs/cgroup/memory/itgm"
                + pid + "/memory.memsw.usage_in_bytes", "b");

    }

    public String getCPU() {
        String cpuR = resultadoR.getCPU();
        String cpuJava = null;
        if (pid == null ? JRIAccess3.getPID() != null : !pid.equals(JRIAccess3.getPID())) {
            cpuJava = resultadoJava.getCPU();
        }

        String tam = ((cpuJava == null) ? cpuR
                : String.valueOf(Float.parseFloat(cpuR) + Float.parseFloat(cpuJava)));
        if (tam != null && !tam.isEmpty() && tam.contains(".") && ((tam.indexOf(".") + 2) < tam.length())) {
            tam = tam.substring(0, tam.indexOf(".") + 2);
        }
        return tam;
    }

    public String getSize() {
        String tam = getValorOfCMD("du " + diretorio + " -sh", "");
        if (tam != null && !tam.isEmpty() && tam.contains(".") && ((tam.indexOf(".") + 2) < tam.length())) {
            tam = tam.substring(0, tam.indexOf(".") + 2);
        }
        return tam;
    }

    public String getValorOfCMD(String cmd, String base) {
        String tam = null;
        try {
            File f = File.createTempFile("size", "itgm");
            log.printLog(LogType.LOG_DEBUG, "arquivo temporario: " + f.getAbsolutePath());
            log.printLog(LogType.LOG_DEBUG, "a executar cmd: " + cmd);
            ProcessBuilder pb = new ProcessBuilder(cmd.split(" "))
                    .redirectOutput(f);
            pb.start().waitFor();
            byte[] bytes = new byte[1000];
            log.printLog(LogType.LOG_DEBUG, "a ler arquivo temporario");
            int size = new FileInputStream(f).read(bytes);
            tam = new String(bytes, 0, size);
            tam = tam.substring(0, tam.replaceAll("\\s.*", "@").indexOf("@"));
            tam = tam.trim();
            tam = tam.replace(",", ".");
            tam = tam.toLowerCase();
            if (base.equals("b")) {
                tam = String.valueOf((Float.parseFloat(tam) / 1024) / 1024);
                if (tam != null && !tam.isEmpty() && tam.contains(".") && (tam.indexOf(".") + 2) < tam.length()) {
                    tam = tam.substring(0, tam.indexOf(".") + 2);
                }
            }

            if (tam.endsWith("k")) {
                tam = String.valueOf(Float.parseFloat(tam.replace("k", "")) / 1024);
            } else if (tam.endsWith("g") || base.equals("g")) {
                tam = String.valueOf(Float.parseFloat(tam.replace("g", "")) * 1024);
            } else if (tam.endsWith("m")) {
                tam = tam.replace("m", "");
            }

            log.printLog(LogType.LOG_DEBUG, "tamanho "
                    + "recuperado, de: " + tam + " resultou: " + tam);
            return tam;
        } catch (Exception ex) {
            log.printLog(LogType.LOG_ERROR, "impossivel obter tamnho, de: " + tam + " EX: " + ex);
        }
        return null;
    }

    public String getStatusJson(boolean isAlive) {
        if (!isAlive) {
            resultadoR.terminar();
            if (resultadoJava != null) {
                resultadoJava.terminar();
            }
        }
        return "{"
                + "\"pid\":\"" + pid + "\","
                + "\"pidJava\":\"" + JRIAccess3.getPID() + "\","
                + "\"cpu\":\"" + (isAlive ? getCPU() : "0") + "\","
                + "\"memoria\":\"" + getMemory() + "\","
                + "\"disco\":\"" + getSize() + "\""
                + addStatus()
                + "}";

//            log.printLog(LogType.LOG_INFO, "obtido status: processo morto.");
//            return "{\"isAlive\":false}";
//        }
    }

    public void terminar() {
        try {
            resultadoR.getProcesso().destroy();
            if (resultadoJava != null) {
                resultadoJava.getProcesso().destroy();
            }
        } catch (Exception ex) {
            log.printLog(LogType.LOG_ERROR, "impossivel interromper processo: " + ex + " STATUS/196");
        }
        log.printLog(LogType.LOG_DEBUG, "encerrando programa - Status/200");
    }

    public abstract void onClose();

    public abstract String addStatus();

}
