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
/*    */ 
/*    */ import java.io.FileInputStream;
/*    */ import java.sql.Connection;
/*    */ import java.sql.DriverManager;
/*    */ import java.sql.PreparedStatement;
/*    */ import java.sql.ResultSet;
/*    */ import java.sql.Statement;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class SQLDatabase
/*    */ {
/*    */   private String owner;
/* 16 */   private Properties sqldata = new Properties();
/*    */   private Connection con;
/*    */   protected Statement st;
/*    */   private String ip;
/*    */   private String port;
/*    */   private String user;
/*    */   private String pass;
/*    */   private String database;
/*    */ 
/*    */   public SQLDatabase(String createdby)
/*    */   {
/* 23 */     this.owner = createdby;
/*    */   }
/*    */ 
/*    */   protected void debug(String msg)
/*    */   {
/* 28 */     Main.log.writeLOG("SQL", msg);
/*    */   }
/*    */ 
/*    */   private void loadconfigs()
/*    */   {
/*    */     try
/*    */     {
/* 38 */       FileInputStream fin = new FileInputStream("configs/mysql.conf");
/* 39 */       this.sqldata.load(fin);
/* 40 */       fin.close();
/*    */     }
/*    */     catch (Exception ex)
/*    */     {
/* 44 */       ex.printStackTrace();
               debug("Excepção em SQL Database: " + ex.getMessage());
/*    */     }
/* 46 */     this.ip = this.sqldata.getProperty("MySQL_ip");
/* 47 */     this.port = this.sqldata.getProperty("MySQL_port");
/* 48 */     this.user = this.sqldata.getProperty("MySQL_id");
/* 49 */     this.pass = this.sqldata.getProperty("MySQL_pw");
/* 50 */     this.database = this.sqldata.getProperty("MySQL_db");
/*    */   }
/*    */ 
/*    */   public void start()
/*    */   {
/* 55 */     loadconfigs();
/*    */     try
/*    */     {
/* 61 */       Class.forName("com.mysql.jdbc.Driver");
               debug("Conectando ao Servidor:"+this.ip+" porta:"+this.port+" Base:"+this.database+" User:"+this.user);
/* 62 */       this.con = DriverManager.getConnection("jdbc:mysql://" + this.ip + ":" + this.port + "/" + this.database, this.user, this.pass);
/* 63 */       this.st = this.con.createStatement();
/* 64 */       if (!this.con.equals(null))
/* 65 */         debug("Modulo SQL Online.");
/*    */     }
/*    */     catch (Exception ex)
/*    */     {
                ex.printStackTrace();
/* 69 */       debug("Excepção em SQL Database: " + ex.getMessage());
/*    */     }
/*    */   }
/*    */ 
/*    */   public ResultSet doquery(String query)
/*    */   {
/* 75 */     ResultSet rs = null;
/*    */     try
/*    */     {
/* 79 */       PreparedStatement pt = this.con.prepareStatement(query);
/* 80 */       rs = pt.executeQuery(query);
/*    */     }
/*    */     catch (Exception ex)
/*    */     {
/* 84 */       debug("Excepção em do query: " + ex.getMessage());
/*    */     }
/* 86 */     return rs;
/*    */   }
/*    */ 
/*    */   public void doupdate(String query)
/*    */   {
/*    */     try
/*    */     {
/* 94 */       PreparedStatement pt = this.con.prepareStatement(query);
/* 95 */       pt.executeUpdate(query);
/*    */     }
/*    */     catch (Exception ex)
/*    */     {
/* 99 */       debug("Excepção em do update: " + ex.getMessage());
/*    */     }
/*    */   }
/*    */ }
