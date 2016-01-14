/*     */ package WQWServer;
/*     */
/*     */ import java.sql.ResultSet;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Timer;
/*     */ import java.util.TimerTask;
/*     */
/*     */ public class AuraHandler
/*     */ {
/*  16 */   protected ArrayList<Integer> id = new ArrayList();
/*  17 */   protected ArrayList<String> name = new ArrayList();
/*  18 */   protected ArrayList<String> cat = new ArrayList();
/*  19 */   protected ArrayList<String> type = new ArrayList();
/*     */
/*  21 */   protected ArrayList<Integer> seconds = new ArrayList();
/*  22 */   protected ArrayList<Double> dmg = new ArrayList();
/*  23 */   protected ArrayList<Double> reduce = new ArrayList();
/*     */
/*  25 */   protected ArrayList<Boolean> iscrit = new ArrayList();
/*     */
/*  27 */   public int[][] userAuras = new int[50][10];
/*     */
/*     */   public void Init()
/*     */   {
/*     */     try {
/*  32 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_skills_auras");
/*  33 */       while (rs.next()) {
/*  34 */         int skid = rs.getInt("id");
/*  35 */         if (!this.id.contains(Integer.valueOf(skid))) {
/*  36 */           this.id.add(Integer.valueOf(skid));
/*  37 */           this.name.add(rs.getString("name"));
/*  38 */           this.cat.add(rs.getString("cat"));
/*  39 */           this.type.add(rs.getString("type"));
/*  40 */           this.seconds.add(Integer.valueOf(rs.getInt("seconds")));
/*  41 */           this.reduce.add(Double.valueOf(Double.parseDouble(rs.getString("reduction"))));
/*  42 */           this.dmg.add(Double.valueOf(Double.parseDouble(rs.getString("damage"))));
/*  43 */           this.iscrit.add(Boolean.valueOf(Boolean.parseBoolean(rs.getString("iscrit"))));
/*     */         }
/*     */       }
/*     */     } catch (Exception e) {
/*  47 */       Main.debug("Aura Handler - ", e.getMessage());
/*     */     }
/*     */   }
/*     */
/*     */   public void reload()
/*     */   {
/*  53 */     this.id.clear();
/*  54 */     this.name.clear();
/*  55 */     this.cat.clear();
/*  56 */     this.type.clear();
/*  57 */     this.seconds.clear();
/*  58 */     this.reduce.clear();
/*  59 */     this.dmg.clear();
/*  60 */     this.iscrit.clear();
/*  61 */     Init();
/*  62 */     Main.debug("Aura Handler - ", "Reinitialized");
/*     */   }
/*     */
/*     */   protected int getIndex(int skillid)
/*     */   {
/*  71 */     if (skillid == 0) {
/*  72 */       return 0;
/*     */     }
/*  74 */     int index = this.id.lastIndexOf(Integer.valueOf(skillid));
/*  75 */     return index;
/*     */   }
/*     */
/*     */   protected boolean isCrit(int auraid) {
/*  79 */     int i = getIndex(auraid);
/*  80 */     return ((Boolean)this.iscrit.get(i)).booleanValue();
/*     */   }
/*     */
/*     */   protected double getDamage(int auraid) {
/*  84 */     int i = getIndex(auraid);
/*  85 */     return ((Double)this.dmg.get(i)).doubleValue();
/*     */   }
/*     */
/*     */   protected int getSeconds(int auraid) {
/*  89 */     int i = getIndex(auraid);
/*  90 */     return ((Integer)this.seconds.get(i)).intValue();
/*     */   }
/*     */
/*     */   protected double getReduction(int auraid) {
/*  94 */     int i = getIndex(auraid);
/*  95 */     return ((Double)this.reduce.get(i)).doubleValue();
/*     */   }
/*     */
/*     */   protected String getName(int auraid) {
/*  99 */     int i = getIndex(auraid);
/* 100 */     return (String)this.name.get(i);
/*     */   }
/*     */
/*     */   protected String getType(int auraid) {
/* 104 */     int i = getIndex(auraid);
/* 105 */     return (String)this.type.get(i);
/*     */   }
/*     */
/*     */   protected String getCat(int auraid) {
/* 109 */     int i = getIndex(auraid);
/* 110 */     return (String)this.cat.get(i);
/*     */   }
/*     */
/*     */   public boolean isAuraActive(int uID, int auraID)
/*     */   {
/* 121 */     for (int i = 1; i < 10; i++) {
/* 122 */       if (this.userAuras[uID][i] == auraID) {
/* 123 */         return true;
/*     */       }
/*     */     }
/* 126 */     return false;
/*     */   }
/*     */
/*     */   public boolean addAura(ServerConnection user, int auraID, int seconds, int[] ids, String[] type, String tgt, int max)
/*     */   {
/* 131 */     for (int i = 1; i < 10; i++) {
/* 132 */       if (this.userAuras[user.accountid][i] == auraID)
/* 133 */         return false;
/* 134 */       if (this.userAuras[user.accountid][i] == 0) {
/* 135 */         this.userAuras[user.accountid][i] = auraID;
/* 136 */         removeAura(user, auraID, seconds, ids, type, tgt, max);
/* 137 */         return true;
/*     */       }
/*     */     }
/* 140 */     return false;
/*     */   }
/*     */
/*     */   public void removeAura(final ServerConnection user,final int auraID, int seconds,final int[] ids,final String[] type,final String tgt,final int max)
/*     */   {
/* 145 */     Timer timer = new Timer();
/* 146 */     timer.schedule(new TimerTask()
/*     */     {
/*     */       public void run()
/*     */       {
/* 150 */         for (int i = 1; i < 10; i++)
/* 151 */           if (AuraHandler.this.userAuras[user.accountid][i] == auraID) {
/* 152 */             AuraHandler.this.userAuras[user.accountid][i] = 0;
/*     */           }
/*     */       }
/*     */     }
/*     */     , seconds * 1000);
/*     */   }
/*     */
/*     */   public void clearAuras(int uID)
/*     */   {
/* 161 */     for (int i = 1; i < 10; i++)
/* 162 */       this.userAuras[uID][i] = 0;
/*     */   }
/*     */ }