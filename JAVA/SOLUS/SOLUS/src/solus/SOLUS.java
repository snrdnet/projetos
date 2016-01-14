/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package solus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lucas
 */
public class SOLUS {
    private int permission=0;
    private int UID=0;
    private boolean logged=false;
    private String Name="";
    public SOLUS(){
        debug("SOLUS iniciado com sucesso.");
    }
    
    protected int GetPermission(){
       return permission;
    }
    protected int GetUID(){
        return UID;
    }
    protected String GetName(){
        return Name;
    }
    protected boolean GetLogged(){
        return logged;
    }
    protected void login(String login, String password){
            ResultSet rs = Main.sql.doquery("SELECT * FROM users WHERE LOGIN='"+login+"'");
        try {
            if(rs.next()){
               UID = rs.getInt("ID");
               Name = rs.getString("NAME");
               permission = rs.getInt("PERMISSION");
               debug("Tentativa de Login:"+Name+" UID:"+UID+" permission:"+permission);
              if(rs.getString("PASSWORD") == null ? password == null : rs.getString("PASSWORD").equals(password)){
                  debug("Login Efetuado Com sucesso!");
                  logged=true;         
                  Main.gui.login.SetLabelError("");
                  Main.gui.login.setVisible(false);
                  Main.gui.ShowLogoff();
                  logon();
              } else{
                  debug("Senha inválida:"+password);
                  Main.gui.login.SetLabelError("Senha Inválida");
              }
            }else{
             debug("Tentativa de login: Login Inválido:"+login+", senha:"+password);
        Main.gui.login.SetLabelError("Login Inválido");   
            }
        } catch (SQLException ex) {
            Logger.getLogger(SOLUS.class.getName()).log(Level.SEVERE, null, ex);
            debug("Erro Ao logar: " + ex.getMessage());
            Main.gui.login.SetLabelError("Erro #001");
        }
          
    }
    private void logon(){
        ResultSet rs = Main.sql.doquery("SELECT * FROM permissions WHERE ID='"+permission+"'");
        try {
            if(rs.next()){
               String menus = rs.getString("Menus");
               if(menus==""  | menus== null){}
               else{
                String[] split = menus.split(",");
                //debug("split permissions length:"+Integer.toString(split.length));
                for(int i=0;i<split.length;i++){
                    Main.gui.loadMenu(split[i]);
                }
               }
              
            }
        } catch (SQLException ex) {
            Logger.getLogger(SOLUS.class.getName()).log(Level.SEVERE, null, ex);
            debug("Erro Ao logar: " + ex.getMessage());
            Main.gui.login.SetLabelError("Erro #001");
        }
    }
    protected void logoff(){
        logged= false;
        Main.gui.CloseAllWindow();
       Main.gui.HideAllMenu();
      Main.gui.LoadLogin();
    }
    private void debug(String msg){
        Main.log.writeLOG("SOLUS", msg);
    }
}
