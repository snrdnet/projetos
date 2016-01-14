/*    */ package WQWServer;
/*    */ 
/*    */ import java.io.PrintWriter;
/*    */ 
/*    */ public class Party
/*    */ {
/* 12 */   public String[] partySlot = new String[10];
/*    */   public String partyOwner;
/*    */   public int partyID;
/* 15 */   public int users = 0;
/*    */ 
/*    */   public Party(int numb)
/*    */   {
/* 19 */     this.partyID = numb;
/* 20 */     for (int i = 0; i < 10; i++) {
/* 21 */       this.partySlot[i] = "";
/*    */     }
/* 23 */     this.partyOwner = "";
/*    */   }
/*    */ 
/*    */   protected int getPlayerSlot(String charname)
/*    */   {
/* 28 */     for (int i = 0; i < 10; i++) {
/* 29 */       if (this.partySlot[i].equals(charname)) {
/* 30 */         return i;
/*    */       }
/*    */     }
/* 33 */     return -1;
/*    */   }
/*    */ 
/*    */   protected void addPlayer(String charname, PrintWriter socket)
/*    */   {
/* 38 */     for (int i = 0; i < 10; i++)
/* 39 */       if ((this.partySlot[i].equals("")) || (this.partySlot[i] == null)) {
/* 40 */         if (i == 0) {
/* 41 */           this.partyOwner = charname;
/*    */         }
/* 43 */         this.partySlot[i] = charname;
/* 44 */         this.users += 1;
/* 45 */         break;
/*    */       }
/*    */   }
/*    */ 
/*    */   protected String getPlayers()
/*    */   {
/* 52 */     String result = "[";
/* 53 */     for (int i = 0; i < 10; i++) {
/* 54 */       if ((!this.partySlot[i].equals("")) && (this.partySlot[i] != null)) {
/* 55 */         if (i != 0) {
/* 56 */           result = result + ",";
/*    */         }
/* 58 */         result = result + "\"" + this.partySlot[i] + "\"";
/*    */       }
/*    */     }
/* 61 */     result = result + "]";
/* 62 */     return result;
/*    */   }
/*    */ 
/*    */   protected void removePlayer(String charname)
/*    */   {
/* 67 */     int ID = getPlayerSlot(charname);
/* 68 */     this.partySlot[ID] = "";
/* 69 */     if (this.partyOwner.equals("charname")) {
/* 70 */       for (int i = 0; i < 10; i++) {
/* 71 */         if ((!this.partySlot[i].equals("")) && (this.partySlot[i] != null)) {
/* 72 */           this.partyOwner = this.partySlot[i];
/* 73 */           break;
/*    */         }
/*    */       }
/*    */     }
/* 77 */     this.users -= 1;
/*    */   }
/*    */ 
/*    */   protected void finalize()
/*    */   {
/* 83 */     for (int e = 0; e < 10; e++)
/* 84 */       removePlayer(this.partySlot[e]);
/*    */   }
/*    */ }