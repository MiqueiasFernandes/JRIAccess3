/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jriacces3.log;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TXTLog implements ILog {

    private final FileWriter fileWriter;
    private final SimpleDateFormat formata = new SimpleDateFormat("dd' de 'MMMMM' de 'yyyy' - 'HH':'mm':'ss", new Locale("pt", "BR"));
    private final LogType nivelDeLog;

    public TXTLog(String fileName, LogType type) throws IOException {
        this.nivelDeLog = type != null ? type : LogType.LOG_DEBUG;
        if (fileName == null || fileName.isEmpty()) {
            fileName = "jriaccess.log";
        }
        fileWriter = new FileWriter(fileName, true);
    }

//    NIVEIS DE LOG    APARECEM
//      DEBUG --------DEBUG,INFO,WARNING,ERRORR
//      INFO ---------INFO,WARNING,ERROR
//      WARNING ------WARNING,ERROR
//      ERROR---------ERROR
    @Override
    public void printLog(LogType tipo, String texto) {
        try {
            String modo = null;

            switch (tipo) {
                case LOG_DEBUG: {
                    if (nivelDeLog == LogType.LOG_ERROR || nivelDeLog == LogType.LOG_WARNING || nivelDeLog == LogType.LOG_INFO) {
                        return;
                    }
                    modo = "DEBUG";
                    break;
                }
                case LOG_INFO: { //// ñ vai aparecer caso: error, warning
                    if (nivelDeLog == LogType.LOG_ERROR || nivelDeLog == LogType.LOG_WARNING) {
                        return;
                    }
                    modo = "INFO";
                    break;
                }
                case LOG_WARNING: { ///não vai aparecer caso 
                    if (nivelDeLog == LogType.LOG_ERROR) {
                        return;
                    }
                    modo = "WARNING";
                    break;
                }
                case LOG_ERROR: { /// sempre aparece
                    modo = "ERROR";
                    break;
                }
            }
            String txt = modo + " [" + formata.format(new java.util.Date()) + "] "
                    + texto + System.lineSeparator();
            fileWriter.append(txt);
            fileWriter.flush();
           /// System.out.println(txt);
        } catch (IOException ex) {
            System.err.println("erro ao gravar log: " + ex);
            System.exit(Errors.LOG_WRITE.ordinal());
        }
    }

}
