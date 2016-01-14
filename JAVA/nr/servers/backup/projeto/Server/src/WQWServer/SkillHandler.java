/*     */ package WQWServer;
/*     */
/*     */ import java.sql.ResultSet;
/*     */ import java.util.ArrayList;
/*     */
/*     */ public class SkillHandler
/*     */ {
/*  21 */   protected ArrayList<Integer> id = new ArrayList();
/*  22 */   protected ArrayList<Integer> range = new ArrayList();
/*  23 */   protected ArrayList<Integer> mpcost = new ArrayList();
/*  24 */   protected ArrayList<Integer> cooldown = new ArrayList();
/*  25 */   protected ArrayList<Integer> tgtmax = new ArrayList();
/*  26 */   protected ArrayList<Integer> tgtmin = new ArrayList();
/*  27 */   protected ArrayList<Integer> auras = new ArrayList();
/*     */
/*  29 */   protected ArrayList<Double> damage = new ArrayList();
/*     */
/*  31 */   protected ArrayList<Boolean> iscrit = new ArrayList();
/*     */
/*  33 */   protected ArrayList<String> name = new ArrayList();
/*  34 */   protected ArrayList<String> anim = new ArrayList();
/*  35 */   protected ArrayList<String> icon = new ArrayList();
/*  36 */   protected ArrayList<String> tgt = new ArrayList();
/*  37 */   protected ArrayList<String> typ = new ArrayList();
/*  38 */   protected ArrayList<String> str1 = new ArrayList();
/*  39 */   protected ArrayList<String> ref = new ArrayList();
/*  40 */   protected ArrayList<String> fx = new ArrayList();
/*  41 */   protected ArrayList<String> dsrc = new ArrayList();
/*  42 */   protected ArrayList<String> desc = new ArrayList();
/*     */
/*     */   public void Init()
/*     */   {
/*     */     try
/*     */     {
/*  48 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_skills");
/*  49 */       while (rs.next()) {
/*  50 */         int skid = rs.getInt("id");
/*  51 */         if (!this.id.contains(Integer.valueOf(skid))) {
/*  52 */           this.id.add(Integer.valueOf(skid));
/*  53 */           this.name.add(rs.getString("name"));
/*  54 */           this.anim.add(rs.getString("anim"));
/*  55 */           this.mpcost.add(Integer.valueOf(rs.getInt("mana")));
/*  56 */           this.desc.add(rs.getString("desc"));
/*  57 */           this.icon.add(rs.getString("icon"));
/*  58 */           this.range.add(Integer.valueOf(rs.getInt("range")));
/*  59 */           this.dsrc.add(rs.getString("dsrc"));
/*  60 */           this.ref.add(rs.getString("ref"));
/*  61 */           this.tgt.add(rs.getString("tgt"));
/*  62 */           this.typ.add(rs.getString("typ"));
/*  63 */           this.str1.add(rs.getString("str1"));
/*  64 */           this.tgtmax.add(Integer.valueOf(rs.getInt("tgtMax")));
/*  65 */           this.tgtmin.add(Integer.valueOf(rs.getInt("tgtMin")));
/*  66 */           this.cooldown.add(Integer.valueOf(rs.getInt("cd")));
/*  67 */           this.auras.add(Integer.valueOf(rs.getInt("aura")));
/*  68 */           this.fx.add(rs.getString("fx"));
/*  69 */           this.iscrit.add(Boolean.valueOf(Boolean.parseBoolean(rs.getString("iscrit"))));
/*  70 */           this.damage.add(Double.valueOf(Double.parseDouble(rs.getString("damage"))));
/*     */         }
/*     */       }
/*     */     } catch (Exception e) {
/*  74 */       Main.debug("Skill Handler", e.getMessage());
/*     */     }
/*     */   }
/*     */
/*     */   public void reload()
/*     */   {
/*     */     try {
/*  81 */       this.id.clear();
/*  82 */       this.name.clear();
/*  83 */       this.anim.clear();
/*  84 */       this.mpcost.clear();
/*  85 */       this.desc.clear();
/*  86 */       this.icon.clear();
/*  87 */       this.range.clear();
/*  88 */       this.dsrc.clear();
/*  89 */       this.ref.clear();
/*  90 */       this.tgt.clear();
/*  91 */       this.typ.clear();
/*  92 */       this.str1.clear();
/*  93 */       this.tgtmax.clear();
/*  94 */       this.tgtmin.clear();
/*  95 */       this.cooldown.clear();
/*  96 */       this.auras.clear();
/*  97 */       this.fx.clear();
/*  98 */       this.iscrit.clear();
/*  99 */       this.damage.clear();
/* 100 */       Init();
/* 101 */       Main.debug("Skill Handler - ", "Reinitialized");
/*     */     } catch (Exception e) {
/* 103 */       Main.debug("Skill Handler - ", e.getMessage());
/*     */     }
/*     */   }
/*     */
/*     */   protected int getIndex(int skillid) {
/* 108 */     int index = this.id.lastIndexOf(Integer.valueOf(skillid));
/* 109 */     return index;
/*     */   }
/*     */
/*     */   protected int getAura(int skillid) {
/* 113 */     int i = getIndex(skillid);
/* 114 */     return ((Integer)this.auras.get(i)).intValue();
/*     */   }
/*     */
/*     */   protected String getIcon(int skillid) {
/* 118 */     int i = getIndex(skillid);
/* 119 */     return (String)this.icon.get(i);
/*     */   }
/*     */
/*     */   protected String getTgt(int skillid) {
/* 123 */     int i = getIndex(skillid);
/* 124 */     return (String)this.tgt.get(i);
/*     */   }
/*     */
/*     */   protected String getType(int skillid) {
/* 128 */     int i = getIndex(skillid);
/* 129 */     return (String)this.typ.get(i);
/*     */   }
/*     */
/*     */   protected String getStr1(int skillid) {
/* 133 */     int i = getIndex(skillid);
/* 134 */     return (String)this.str1.get(i);
/*     */   }
/*     */
/*     */   protected String getDsrc(int skillid) {
/* 138 */     int i = getIndex(skillid);
/* 139 */     return (String)this.dsrc.get(i);
/*     */   }
/*     */
/*     */   protected String getFx(int skillid) {
/* 143 */     int i = getIndex(skillid);
/* 144 */     return (String)this.fx.get(i);
/*     */   }
/*     */
/*     */   protected String getRef(int skillid) {
/* 148 */     int i = getIndex(skillid);
/* 149 */     return (String)this.ref.get(i);
/*     */   }
/*     */
/*     */   protected String getAnim(int skillid) {
/* 153 */     int i = getIndex(skillid);
/* 154 */     return (String)this.anim.get(i);
/*     */   }
/*     */
/*     */   protected String getDesc(int skillid) {
/* 158 */     int i = getIndex(skillid);
/* 159 */     return (String)this.desc.get(i);
/*     */   }
/*     */
/*     */   protected String getName(int skillid) {
/* 163 */     int i = getIndex(skillid);
/* 164 */     return (String)this.name.get(i);
/*     */   }
/*     */
/*     */   protected double getDamage(int skillid) {
/* 168 */     int i = getIndex(skillid);
/* 169 */     return ((Double)this.damage.get(i)).doubleValue();
/*     */   }
/*     */
/*     */   protected boolean isCrit(int skillid) {
/* 173 */     int i = getIndex(skillid);
/* 174 */     return ((Boolean)this.iscrit.get(i)).booleanValue();
/*     */   }
/*     */
/*     */   protected int getTgtMax(int skillid) {
/* 178 */     int i = getIndex(skillid);
/* 179 */     return ((Integer)this.tgtmax.get(i)).intValue();
/*     */   }
/*     */
/*     */   protected int getRange(int skillid) {
/* 183 */     int i = getIndex(skillid);
/* 184 */     return ((Integer)this.range.get(i)).intValue();
/*     */   }
/*     */
/*     */   protected int getTgtMin(int skillid) {
/* 188 */     int i = getIndex(skillid);
/* 189 */     return ((Integer)this.tgtmin.get(i)).intValue();
/*     */   }
/*     */
/*     */   protected int getCooldown(int skillid) {
/* 193 */     int i = getIndex(skillid);
/* 194 */     return ((Integer)this.cooldown.get(i)).intValue();
/*     */   }
/*     */
/*     */   protected int getMpCost(int skillid) {
/* 198 */     int i = getIndex(skillid);
/* 199 */     return ((Integer)this.mpcost.get(i)).intValue();
/*     */   }
/*     */ }