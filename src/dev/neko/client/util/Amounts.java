package dev.neko.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_5250;

public final class Amounts {
   private static final String[] SUFFIX = new String[]{"qt", "q", "t", "b", "m", "k"};
   private static final String[] DISPLAY = new String[]{"QT", "Q", "T", "B", "M", "k"};
   private static final int[] POWER = new int[]{18, 15, 12, 9, 6, 3};
   private static final Pattern NUMBER = Pattern.compile("\\d[\\d.,]*(?:\\s?(?:[qQ][tT]|[kKmMbBtTqQ]))?");

   private Amounts() {
   }

   public static double parse(String raw) {
      if (raw == null) {
         return Double.NaN;
      } else {
         String s = raw.trim().toLowerCase(Locale.ROOT).replaceAll("[,\\s_]", "");

         while (!s.isEmpty() && (s.charAt(0) == '$' || s.charAt(0) == 8364 || s.charAt(0) == 163)) {
            s = s.substring(1);
         }

         if (s.isEmpty()) {
            return Double.NaN;
         } else {
            for (int i = 0; i < SUFFIX.length; i++) {
               if (s.endsWith(SUFFIX[i]) && s.length() > SUFFIX[i].length()) {
                  String num = s.substring(0, s.length() - SUFFIX[i].length());

                  try {
                     return Double.parseDouble(num) * Math.pow(10.0, POWER[i]);
                  } catch (NumberFormatException var5) {
                     return Double.NaN;
                  }
               }
            }

            try {
               return Double.parseDouble(s);
            } catch (NumberFormatException var61) {
               return Double.NaN;
            }
         }
      }
   }

   public static String shortForm(double n) {
      double abs = Math.abs(n);

      for (int i = 0; i < POWER.length; i++) {
         double pow = Math.pow(10.0, POWER[i]);
         if (abs >= pow) {
            double v = Math.round(n / pow * 10.0) / 10.0;
            String var10000 = trimZero(v);
            return var10000 + DISPLAY[i];
         }
      }

      return Long.toString(Math.round(n));
   }

   public static String comma(double n) {
      return String.format(Locale.US, "%,d", (long)Math.floor(n));
   }

   public static String plain(double n) {
      return Long.toString((long)Math.floor(n));
   }

   public static String format(double n, String mode) {
      return switch (mode) {
         case "Short" -> shortForm(n);
         case "Plain" -> plain(n);
         default -> comma(n);
      };
   }

   private static String trimZero(double v) {
      return v == Math.floor(v) && !Double.isInfinite(v) ? Long.toString((long)v) : String.valueOf(v);
   }

   public static int[] lastNumberSpan(String text) {
      Matcher m = NUMBER.matcher(text);
      int start = -1;

      int end;
      for (end = -1; m.find(); end = m.end()) {
         start = m.start();
      }

      return start < 0 ? null : new int[]{start, end};
   }

   public static int[] valueSpan(String text) {
      for (int i = 0; i < text.length(); i++) {
         if (Character.isDigit(text.charAt(i))) {
            return new int[]{i, text.length()};
         }
      }

      return null;
   }

   public static class_2561 replaceNumberStyled(class_2561 original, String replacement) {
      return spliceStyled(original, replacement, false);
   }

   public static class_2561 replaceValueStyled(class_2561 original, String replacement) {
      return spliceStyled(original, replacement, true);
   }

   private static class_2561 spliceStyled(class_2561 original, String replacement, boolean toEnd) {
      List<class_2583> styles = new ArrayList<>();
      List<String> parts = new ArrayList<>();
      original.method_27658((style, str) -> {
         styles.add(style);
         parts.add(str);
         return Optional.empty();
      }, class_2583.field_24360);
      StringBuilder sb = new StringBuilder();

      for (String p : parts) {
         sb.append(p);
      }

      int[] span = toEnd ? valueSpan(sb.toString()) : lastNumberSpan(sb.toString());
      if (span == null) {
         return original;
      } else {
         int start = span[0];
         int end = span[1];
         class_5250 out = class_2561.method_43473();
         int pos = 0;
         boolean inserted = false;

         for (int i = 0; i < parts.size(); i++) {
            String s = parts.get(i);
            class_2583 st = styles.get(i);
            int re = pos + s.length();
            int beforeEnd = Math.min(re, start);
            if (beforeEnd > pos) {
               out.method_10852(class_2561.method_43470(s.substring(0, beforeEnd - pos)).method_10862(st));
            }

            if (!inserted && start >= pos && start < re) {
               out.method_10852(class_2561.method_43470(replacement).method_10862(st));
               inserted = true;
            }

            int afterStart = Math.max(pos, end);
            if (re > afterStart) {
               out.method_10852(class_2561.method_43470(s.substring(afterStart - pos)).method_10862(st));
            }

            pos = re;
         }

         if (!inserted) {
            out.method_10852(class_2561.method_43470(replacement));
         }

         return out;
      }
   }
}
