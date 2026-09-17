package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.settings.StringSetting;
import dev.neko.client.util.Amounts;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class FakeStatsModule extends Module {
   private static final int GRAY = 11184810;
   private static final int GREEN = 5635925;
   private static final String[] MONEY_KEYS = new String[]{"money", "balance", "coins", "cash"};
   private static final String[] SHARD_KEYS = new String[]{"shard"};
   private static final String[] KILL_KEYS = new String[]{"kill"};
   private static final String[] DEATH_KEYS = new String[]{"death"};
   private static final String[] PLAYTIME_KEYS = new String[]{"playtime", "play time", "time played", "played"};
   public final StringSetting money = this.addSetting(
      new StringSetting("Money", "Your fake balance (1m, 67k, 250000). Wired to FakePay. Blank = off.", "1m", 32, "e.g. 1m, 250k")
   );
   public final StringSetting shards = this.addSetting(new StringSetting("Shards", "Fake shard count. Blank = off.", "", 32, "e.g. 9,999"));
   public final SliderSetting shardsLine = this.addSetting(
      new SliderSetting("Shards Line", "Which sidebar line is shards. 0 = auto by name.", 2.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   public final StringSetting kills = this.addSetting(new StringSetting("Kills", "Fake kill count. Blank = off.", "", 32, "e.g. 1,000"));
   public final SliderSetting killsLine = this.addSetting(
      new SliderSetting("Kills Line", "Which sidebar line is kills. 0 = auto by name.", 3.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   public final StringSetting deaths = this.addSetting(new StringSetting("Deaths", "Fake death count. Blank = off.", "", 32, "e.g. 0"));
   public final SliderSetting deathsLine = this.addSetting(
      new SliderSetting("Deaths Line", "Which sidebar line is deaths. 0 = auto by name.", 4.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   public final StringSetting playtime = this.addSetting(new StringSetting("Playtime", "Fake playtime, any text. Blank = off.", "", 32, "e.g. 365d 12h"));
   public final SliderSetting playtimeLine = this.addSetting(
      new SliderSetting("Playtime Line", "Which sidebar line is playtime. 0 = auto by name.", 5.0, 0.0, 15.0, 1.0).withLabel(FakeStatsModule::lineLabel)
   );
   private String lastMoneyText;
   private double liveBalance;
   private int widthIndex;
   private int drawIndex;

   public FakeStatsModule() {
      super("FakeStats", "Fake balance + editable leaderboard, wired to FakePay", Category.MISC);
      this.shardsLine.visibleWhen(() -> !this.shards.get().isBlank());
      this.killsLine.visibleWhen(() -> !this.kills.get().isBlank());
      this.deathsLine.visibleWhen(() -> !this.deaths.get().isBlank());
      this.playtimeLine.visibleWhen(() -> !this.playtime.get().isBlank());
   }

   @Override
   protected void onEnable() {
      this.lastMoneyText = null;
      this.syncFromSetting();
   }

   private static String lineLabel(double v) {
      return (int)v == 0 ? "Auto" : "Line " + (int)v;
   }

   private void syncFromSetting() {
      String text = this.money.get();
      if (!Objects.equals(text, this.lastMoneyText)) {
         this.lastMoneyText = text;
         double parsed = Amounts.parse(text);
         this.liveBalance = Double.isNaN(parsed) ? 0.0 : Math.max(0.0, parsed);
      }
   }

   public double getLiveBalance() {
      this.syncFromSetting();
      return this.liveBalance;
   }

   public void deduct(double amount) {
      this.syncFromSetting();
      this.liveBalance = Math.max(0.0, this.liveBalance - Math.max(0.0, amount));
   }

   public void beginSidebar() {
      this.widthIndex = 0;
      this.drawIndex = 0;
   }

   public class_2561 rewriteForWidth(class_2561 comp) {
      return this.rewriteLine(comp, true);
   }

   public class_2561 rewriteForDraw(class_2561 comp) {
      return this.rewriteLine(comp, false);
   }

   private class_2561 rewriteLine(class_2561 comp, boolean widthPass) {
      if (this.isEnabled()) {
         String text = stripCodes(comp.getString());
         if (text.isBlank()) {
            return comp;
         } else {
            int var10000;
            if (widthPass) {
               int var10002 = this.widthIndex;
               var10000 = var10002;
               this.widthIndex = var10002 + 1;
            } else {
               int var5 = this.drawIndex;
               var10000 = var5;
               this.drawIndex = var5 + 1;
            }

            return this.applyOverride(var10000, text.toLowerCase(Locale.ROOT), comp);
         }
      } else {
         return comp;
      }
   }

   private class_2561 applyOverride(int line, String lower, class_2561 comp) {
      if (!this.money.get().isBlank() && line == 0) {
         return Amounts.replaceNumberStyled(comp, Amounts.shortForm(this.getLiveBalance()));
      } else if (!this.shards.get().isBlank() && matches(this.shardsLine, line, lower, SHARD_KEYS)) {
         return Amounts.replaceNumberStyled(comp, this.shards.get().trim());
      } else if (!this.kills.get().isBlank() && matches(this.killsLine, line, lower, KILL_KEYS)) {
         return Amounts.replaceNumberStyled(comp, this.kills.get().trim());
      } else if (!this.deaths.get().isBlank() && matches(this.deathsLine, line, lower, DEATH_KEYS)) {
         return Amounts.replaceNumberStyled(comp, this.deaths.get().trim());
      } else {
         return !this.playtime.get().isBlank() && matches(this.playtimeLine, line, lower, PLAYTIME_KEYS)
            ? Amounts.replaceValueStyled(comp, this.playtime.get().trim())
            : comp;
      }
   }

   private static boolean matches(SliderSetting lineSetting, int line, String lower, String[] keys) {
      int configured = lineSetting.getInt();
      return configured > 0 ? line + 1 == configured : containsAny(lower, keys);
   }

   private static boolean containsAny(String haystack, String[] keys) {
      for (String k : keys) {
         if (haystack.contains(k)) {
            return true;
         }
      }

      return false;
   }

   public boolean tryInterceptBalance(String command) {
      if (this.isEnabled()) {
         String[] parts = command.trim().split("\\s+");
         if (parts.length == 0) {
            return false;
         } else {
            String name = parts[0];
            if (name.startsWith("/")) {
               name = name.substring(1);
            }

            if (!name.equalsIgnoreCase("bal") && !name.equalsIgnoreCase("balance")) {
               return false;
            } else {
               class_310 mc = class_310.method_1551();
               if (mc.field_1724 == null) {
                  return false;
               } else if (parts.length >= 2 && !parts[1].equalsIgnoreCase(mc.field_1724.method_7334().name())) {
                  return false;
               } else {
                  String amount = Amounts.shortForm(this.getLiveBalance());
                  class_2561 line = class_2561.method_43473()
                     .method_10852(class_2561.method_43470("You have ").method_54663(11184810))
                     .method_10852(class_2561.method_43470(amount).method_54663(5635925));
                  mc.field_1724.method_7353(line, false);
                  mc.field_1724.method_7353(line, true);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static String stripCodes(String s) {
      return s == null ? "" : s.replaceAll("(?i)§[0-9A-FK-OR]", "");
   }
}
