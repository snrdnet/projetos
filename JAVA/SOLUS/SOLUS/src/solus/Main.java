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
public class Main {

    /**
     * @param args the command line arguments
     */
    public static LOG log;
    public static SQLDatabase sql;
    public static MainGUI gui;
    public static SOLUS solus;
    public static String version="0.0.0.1";
    public static void main(String[] args) {
        // TODO code application logic here
       log = new LOG();
       log.writeLOG("INIT", "Modulo LOG iniciado com sucesso.");
       log.writeLOG("INIT", "Iniciando Modulo SQL");
       sql = new SQLDatabase("SOLUS");
       sql.start();
       log.writeLOG("INIT", "Iniciando SOLUS");
       solus = new SOLUS();
       log.writeLOG("INIT", "Iniciando Interface Grafica");
       gui = new MainGUI();
       gui.setTitle("SOLUS -  by Lucas Marques");
       gui.setLocationRelativeTo(null);
       gui.setVisible(true);
       //gui.hidLogin();
       
       
    }
    public static void debug(String msg){
        log.writeLOG("SYS", msg);
    }
    public static void exit(){
        System.exit(0);
    }
}
