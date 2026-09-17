package dev.neko.client.staff;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5251;

public final class StaffDetector {
   public static final String MODE_STAR_RANK = "Marker + Rank";
   public static final String MODE_STAR = "Marker Only";
   public static final String MODE_RANK = "Rank Only";
   public static final String MODE_NAMES = "Names Only";
   public static final String DEFAULT_SYMBOLS = "★☆✦✧✪✩✫✬✭✮✯⭐✰❂⚝✴✵✶✷✸✹⍟";
   public static final List<String> DEFAULT_RANK_KEYWORDS = List.of(
      "coowner",
      "owner",
      "manager",
      "administrator",
      "admin",
      "developer",
      "dev",
      "srmod",
      "seniormod",
      "moderator",
      "mod",
      "srhelper",
      "seniorhelper",
      "helper",
      "trialmod",
      "trial",
      "builder",
      "support",
      "staff"
   );
   public static final String DEFAULT_RANK_KEYWORDS_STRING = String.join(", ", DEFAULT_RANK_KEYWORDS);
   private static final Map<String, String> RANK_LABELS = Map.ofEntries(
      Map.entry("coowner", "Co-Owner"),
      Map.entry("owner", "Owner"),
      Map.entry("manager", "Manager"),
      Map.entry("administrator", "Admin"),
      Map.entry("admin", "Admin"),
      Map.entry("developer", "Dev"),
      Map.entry("dev", "Dev"),
      Map.entry("srmod", "Sr.Mod"),
      Map.entry("seniormod", "Sr.Mod"),
      Map.entry("moderator", "Mod"),
      Map.entry("mod", "Mod"),
      Map.entry("srhelper", "Sr.Helper"),
      Map.entry("seniorhelper", "Sr.Helper"),
      Map.entry("helper", "Helper"),
      Map.entry("trialmod", "Trial"),
      Map.entry("trial", "Trial"),
      Map.entry("builder", "Builder"),
      Map.entry("support", "Support"),
      Map.entry("staff", "Staff")
   );

   private StaffDetector() {
   }

   public static StaffEntry classify(
      String name,
      class_2561 display,
      class_2561 teamPrefix,
      class_2561 teamSuffix,
      String teamName,
      boolean vanished,
      int latency,
      StaffDetector.DetectConfig cfg
   ) {
      if (name != null && !name.isEmpty()) {
         boolean listed = cfg.names().contains(name.toLowerCase(Locale.ROOT));
         Integer starColor = scanMarker(display, cfg);
         if (starColor == null) {
            starColor = scanMarker(teamPrefix, cfg);
         }

         if (starColor == null) {
            starColor = scanMarker(teamSuffix, cfg);
         }

         boolean hasStar = starColor != null;
         String var10000 = plain(display);
         String combined = var10000 + " " + plain(teamPrefix) + " " + plain(teamSuffix) + " " + (teamName == null ? "" : teamName);
         StaffDetector.Rank rank = deriveRank(stripName(combined, name), cfg.rankKeywords());
         boolean hasRank = rank != null;
         String var15 = cfg.mode();
         byte var16 = -1;
         switch (var15.hashCode()) {
            case -203046726:
               if (var15.equals("Marker Only") || var15.equals("Star Only")) {
                  var16 = 0;
               }
               break;
            case 1302991648:
               if (var15.equals("Rank Only")) {
                  var16 = 1;
               }
               break;
            case 2083116484:
               if (var15.equals("Names Only")) {
                  var16 = 2;
               }
         }
         if (!listed && !(switch (var16) {
            case 0 -> hasStar;
            case 1 -> hasRank;
            case 2 -> false;
            default -> hasStar || hasRank;
         })) {
            return null;
         } else {
            int color = hasStar ? starColor : 0;
            String label = hasRank ? rank.label() : "";
            int priority = hasRank ? rank.priority() : (hasStar ? 1 : 0);
            return new StaffEntry(name, label, color, vanished, latency, priority);
         }
      } else {
         return null;
      }
   }

   static Integer scanMarker(class_2561 c, StaffDetector.DetectConfig cfg) {
      return c == null ? null : (Integer)c.method_27658((style, str) -> {
         int i = 0;

         while (i < str.length()) {
            int cp = str.codePointAt(i);
            i += Character.charCount(cp);
            if (isMarker(cp, cfg)) {
               class_5251 tc = style.method_10973();
               return Optional.of(tc != null ? 0xFF000000 | tc.method_27716() : 0);
            }
         }

         return Optional.empty();
      }, class_2583.field_24360).orElse(null);
   }

   private static boolean isMarker(int cp, StaffDetector.DetectConfig cfg) {
      if (cfg.symbols().indexOf(cp) >= 0) {
         return true;
      } else {
         return !cfg.fontIcons() ? false : cp >= 57344 && cp <= 63743 || cp >= 983040 && cp <= 1048573 || cp >= 1048576 && cp <= 1114109;
      }
   }

   private static StaffDetector.Rank deriveRank(String tagText, List<String> keywords) {
      String cleaned = lettersOnly(tagText.toLowerCase(Locale.ROOT));
      if (cleaned.isEmpty()) {
         return null;
      } else {
         for (int i = 0; i < keywords.size(); i++) {
            String kw = keywords.get(i);
            if (!kw.isEmpty() && cleaned.contains(kw)) {
               return new StaffDetector.Rank(labelFor(kw), keywords.size() - i);
            }
         }

         return null;
      }
   }

   private static String labelFor(String keyword) {
      String known = RANK_LABELS.get(keyword);
      if (known != null) {
         return known;
      } else if (keyword.isEmpty()) {
         return "Staff";
      } else {
         char var10000 = Character.toUpperCase(keyword.charAt(0));
         return var10000 + keyword.substring(1);
      }
   }

   private static String plain(class_2561 c) {
      return c == null ? "" : c.getString();
   }

   private static String lettersOnly(String s) {
      StringBuilder sb = new StringBuilder(s.length());

      for (int i = 0; i < s.length(); i++) {
         char ch = s.charAt(i);
         if (ch >= 'a' && ch <= 'z') {
            sb.append(ch);
         }
      }

      return sb.toString();
   }

   private static String stripName(String text, String name) {
      return name != null && !name.isEmpty() ? text.replaceAll("(?i)" + Pattern.quote(name), " ") : text;
   }

   public record DetectConfig(String mode, Set<String> names, List<String> rankKeywords, String symbols, boolean fontIcons, boolean showVanished) {
   }

   private record Rank(String label, int priority) {
   }
}
