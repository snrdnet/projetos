/*    */ package WQWServer;
/*    */ 
/*    */ public class Functions
/*    */ {
/*    */   protected String removenullbyte(String thestring)
/*    */   {
/* 12 */     byte[] stringbyte = thestring.getBytes();
/* 13 */     int a = 0;
/* 14 */     while (stringbyte[a] != 0) {
/* 15 */       a++;
/*    */     }
/* 17 */     return thestring.substring(0, a);
/*    */   }
/*    */ 
/*    */   protected int bytetoint(String thestring, int bytec)
/*    */   {
/* 23 */     String hex_data_s = "";
/* 24 */     for (int i = bytec - 1; i >= 0; i--)
/*    */     {
/* 26 */       int data = thestring.getBytes()[i];
/* 27 */       if (data < 0) {
/* 28 */         data += 256;
/*    */       }
/* 30 */       String hex_data = Integer.toHexString(data);
/* 31 */       if (hex_data.length() == 1)
/*    */       {
/* 33 */         hex_data_s = hex_data_s + "0" + hex_data;
/*    */       }
/* 35 */       else hex_data_s = hex_data_s + hex_data;
/*    */ 
/*    */     }
/*    */ 
/* 39 */     return Integer.parseInt(hex_data_s, 16);
/*    */   }
/*    */ 
/*    */   protected String replaceCharAt(String s, int pos, char c)
/*    */   {
/* 44 */     return s.substring(0, pos) + c + s.substring(pos + 1);
/*    */   }
/*    */ }