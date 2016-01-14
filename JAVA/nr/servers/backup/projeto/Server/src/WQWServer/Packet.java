/*     */ package WQWServer;
/*     */
/*     */ public class Packet
/*     */ {
/*  17 */   private String packet = "";
/*  18 */   private String header = "";
/*     */
/*     */   public int getLen()
/*     */   {
/*  22 */     return this.packet.length();
/*     */   }
/*     */
/*     */   public void setPacket(String pack)
/*     */   {
/*  27 */     this.packet = pack;
/*     */   }
/*     */
/*     */   public void removeHeader()
/*     */   {
/*  32 */     if (this.packet.startsWith("<")) {
/*  33 */       int endArrow = this.packet.indexOf(">") + 1;
/*  34 */       int endSlash = this.packet.indexOf("/>") + 2;
/*  35 */       if (endSlash < endArrow)
/*  36 */         this.packet = this.packet.substring(endSlash);
/*     */       else
/*  38 */         this.packet = this.packet.substring(endArrow);
/*     */     }
/*  40 */     else if (this.packet.startsWith("%")) {
/*  41 */       String[] packet_handled = this.packet.split("%");
/*  42 */       this.packet = this.packet.substring(packet_handled[1].length() + packet_handled[2].length() + packet_handled[3].length() + 3);
/*     */     }
/*     */   }
/*     */
/*     */   public void addXML(String field, String value, int part)
/*     */   {
/*  48 */     switch (part) {
/*     */     case 0:
/*  50 */       this.packet = (this.packet + "<" + field + ">" + value + "</" + field + ">");
/*  51 */       break;
/*     */     case 1:
/*  53 */       this.packet = (this.packet + "<" + field + ">");
/*  54 */       break;
/*     */     case 2:
/*  56 */       this.packet = (this.packet + "</" + field + ">");
/*     */     }
/*     */   }
/*     */
/*     */   public String getXML(String field)
/*     */   {
/*  63 */     int startPos = this.packet.indexOf("<" + field + ">") + field.length() + 2;
/*  64 */     int endPos = this.packet.indexOf("</" + field + ">", startPos);
/*  65 */     return this.packet.substring(startPos, endPos);
/*     */   }
/*     */
/*     */   public void addXMLSingle(int part, String[] data)
/*     */   {
/*  70 */     int i = 0;
/*  71 */     if (part == 2)
/*  72 */       this.packet += "</";
/*     */     else {
/*  74 */       this.packet += "<";
/*     */     }
/*  76 */     for (String str : data) {
/*  77 */       if (i % 2 == 0) {
/*  78 */         if (i != 0) {
/*  79 */           this.packet += " ";
/*     */         }
/*  81 */         this.packet += str;
/*     */       } else {
/*  83 */         this.packet = (this.packet + "='" + str + "'");
/*     */       }
/*  85 */       i++;
/*     */     }
/*  87 */     if (part == 0)
/*  88 */       this.packet += " />";
/*     */     else
/*  90 */       this.packet += ">";
/*     */   }
/*     */
/*     */   public String getXMLSingle(String field)
/*     */   {
/*  96 */     int startPos = this.packet.indexOf(field + "='") + field.length() + 2;
/*  97 */     int endPos = this.packet.indexOf("'", startPos);
/*  98 */     return this.packet.substring(startPos, endPos);
/*     */   }
/*     */
/*     */   public void addCDATA(String string)
/*     */   {
/* 103 */     this.packet = (this.packet + "<![CDATA[" + string + "]]>");
/*     */   }
/*     */
/*     */   public String getCDATA(String string)
/*     */   {
/* 110 */     int startPos = string.indexOf("CDATA[") + 6;
/* 111 */     int endPos = string.indexOf("]", startPos);
/*     */
/* 113 */     String toReturn = string.substring(startPos, endPos);
/*     */
/* 115 */     if (string.contains("N5O8S4M4J7N4B3B6K7")) {
/* 116 */       return toReturn.split("~")[1];
/*     */     }
/*     */
/* 119 */     return toReturn;
/*     */   }
/*     */
/*     */   public void addString(String string)
/*     */   {
/* 124 */     this.packet += string;
/*     */   }
/*     */
/*     */   public String getString(int start, int end, boolean nulled)
/*     */   {
/* 129 */     String thestring = this.packet.substring(start, end);
/* 130 */     this.packet = this.packet.substring(end);
/* 131 */     if (nulled) {
/* 132 */       return thestring;
/*     */     }
/* 134 */     return removenullbyte(thestring);
/*     */   }
/*     */
/*     */   protected String removenullbyte(String thestring)
/*     */   {
/* 140 */     byte[] stringbyte = thestring.getBytes();
/* 141 */     int a = 0;
/* 142 */     while (stringbyte[a] != 0) {
/* 143 */       a++;
/*     */     }
/* 145 */     return thestring.substring(0, a);
/*     */   }
/*     */
/*     */   public String getasByte(int var, int num)
/*     */   {
/* 150 */     if (num == 1) {
/* 151 */       int b1 = var & 0xFF;
/* 152 */       byte[] varbyte = { (byte)b1 };
/* 153 */       return new String(varbyte);
/* 154 */     }if (num == 2) {
/* 155 */       int b1 = var & 0xFF;
/* 156 */       int b2 = var >> 8 & 0xFF;
/* 157 */       byte[] varbyte = { (byte)b1, (byte)b2 };
/* 158 */       return new String(varbyte);
/* 159 */     }if (num == 4) {
/* 160 */       int b1 = var & 0xFF;
/* 161 */       int b2 = var >> 8 & 0xFF;
/* 162 */       int b3 = var >> 16 & 0xFF;
/* 163 */       int b4 = var >> 24 & 0xFF;
/* 164 */       byte[] varbyte = { (byte)b1, (byte)b2, (byte)b3, (byte)b4 };
/* 165 */       return new String(varbyte);
/*     */     }
/* 167 */     return null;
/*     */   }
/*     */
/*     */   public void addInt(int var)
/*     */   {
/* 172 */     this.packet = (this.packet + "" + var);
/*     */   }
/*     */
/*     */   public int getInt(int bytec)
/*     */   {
/* 177 */     String thestring = this.packet.substring(0, bytec);
/* 178 */     String hex_data_s = "";
/* 179 */     for (int i = bytec - 1; i >= 0; i--)
/*     */     {
/* 181 */       int data = thestring.getBytes()[i];
/* 182 */       if (data < 0) {
/* 183 */         data += 256;
/*     */       }
/* 185 */       String hex_data = Integer.toHexString(data);
/* 186 */       if (hex_data.length() == 1)
/*     */       {
/* 188 */         hex_data_s = hex_data_s + "0" + hex_data;
/*     */       }
/* 190 */       else hex_data_s = hex_data_s + hex_data;
/*     */
/*     */     }
/*     */
/* 194 */     this.packet = this.packet.substring(bytec);
/*     */
/* 196 */     return Integer.parseInt(hex_data_s, 16);
/*     */   }
/*     */
/*     */   public void addByte(byte b1)
/*     */   {
/*     */     try
/*     */     {
/* 203 */       byte[] packbyte = { b1 };
/* 204 */       this.packet += new String(packbyte, "ISO8859-1");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */   }
/*     */
/*     */   public void addByte2(byte b1, byte b2) {
/* 212 */     byte[] packbyte = { b1, b2 };
/* 213 */     this.packet += new String(packbyte);
/*     */   }
/*     */
/*     */   public void addByte4(byte b1, byte b2, byte b3, byte b4)
/*     */   {
/* 218 */     byte[] packbyte = { b1, b2, b3, b4 };
/* 219 */     this.packet += new String(packbyte);
/*     */   }
/*     */
/*     */   public void addByteArray(byte[] bytearr)
/*     */   {
/*     */     try
/*     */     {
/* 226 */       this.packet += new String(bytearr, "ISO8859-1");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */   }
/*     */
/*     */   public String getPacket() {
/* 234 */     return this.packet;
/*     */   }
/*     */
/*     */   public void clean() {
/* 238 */     this.header = "";
/* 239 */     this.packet = "";
/*     */   }
/*     */ }