/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solus;

/**
 *
 * @author Lucas
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LOG {
    private Locale locale = new Locale("pt","BR");
    private GregorianCalendar calendar = new GregorianCalendar();
    private SimpleDateFormat formatador = new SimpleDateFormat("DD-MM-YYYY-HH-mm-ss",locale);
    private String logfile="logs/log-"+formatador.format(calendar.getTime())+".txt";
    private FileWriter arq;
    private PrintWriter gravarArq;
    public LOG(){
        
        System.out.println(formatador.format(calendar.getTime()));
        try {
            arq = new FileWriter(logfile);
            gravarArq = new PrintWriter(arq);
            //gravarArq.printf("LOG_TEST");
            //arq.close();
         } catch (IOException ex) {
            Logger.getLogger(LOG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    protected void writeLOG(String tipo, String msg){
        try{
            gravarArq.write("["+tipo+"]"+msg+"\r\n");
            arq.flush();
    } catch (IOException ex) {
            Logger.getLogger(LOG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
