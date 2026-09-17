package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.ModeSetting;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_2588;
import net.minecraft.class_310;
import net.minecraft.class_5250;
import net.minecraft.class_7417;
import net.minecraft.class_8828;

public class FakeRolesModule extends Module {
   public static final String ROLE_NONE = "None";
   public static final String ROLE_MOD = "Mod";
   public static final String ROLE_MEDIA = "MEDIA";
   private static final int GRAY = 8355711;
   private static final int GREEN = 5635925;
   private static final int PINK = 16733695;
   private static final int RED = 16733269;
   private static final int WHITE = 16777215;
   public final ModeSetting role = this.addSetting(new ModeSetting("Role", "Which fake rank tag to wear in front of your name", "None", "None", "Mod", "MEDIA"));

   public FakeRolesModule() {
      super("FakeRoles", "Fake [Mod] / [MEDIA] rank tag on your own name", Category.MISC);
   }

   public boolean isActive() {
      class_310 mc = class_310.method_1551();
      return this.isEnabled() && !this.role.is("None") && mc.field_1724 != null;
   }

   private String selfName() {
      class_310 mc = class_310.method_1551();
      return mc.field_1724 == null ? null : mc.field_1724.method_7334().name();
   }

   private boolean isSelf(String exactName) {
      String me = this.selfName();
      return me != null && !me.isEmpty() && me.equals(exactName);
   }

   private int roleColor() {
      String value = this.role.get();
      if (!value.equals("Mod") && !value.equals("SR.MOD")) {
         return value.equals("MEDIA") ? 16733695 : 16777215;
      } else {
         return 5635925;
      }
   }

   private class_2583 bracketStyle() {
      return class_2583.field_24360.method_36139(8355711).method_10982(false);
   }

   private class_2583 tagStyle() {
      return class_2583.field_24360.method_36139(this.roleColor()).method_10982(true);
   }

   private class_2583 nameStyle() {
      String value = this.role.get();
      if (!value.equals("Mod") && !value.equals("SR.MOD")) {
         return value.equals("MEDIA") ? class_2583.field_24360.method_36139(16777215).method_10982(false) : class_2583.field_24360;
      } else {
         return class_2583.field_24360.method_36139(5635925).method_10982(true);
      }
   }

   public class_2561 tagComponent() {
      return class_2561.method_43473()
         .method_10852(class_2561.method_43470("[").method_10862(this.bracketStyle()))
         .method_10852(class_2561.method_43470(this.role.get()).method_10862(this.tagStyle()))
         .method_10852(class_2561.method_43470("] ").method_10862(this.bracketStyle()));
   }

   public class_2561 buildPrefixedDisplayName(String name) {
      return this.isActive() && name != null
         ? class_2561.method_43473().method_10852(this.tagComponent()).method_10852(class_2561.method_43470(name).method_10862(this.nameStyle()))
         : null;
   }

   public class_2561 decorateNametag(class_2561 tag) {
      return this.isActive() ? this.modifyText(tag) : tag;
   }

   public class_2561 decorateTab(class_2561 display, String realName) {
      if (this.isActive()) {
         class_2561 prefixed = this.isSelf(realName) ? this.buildPrefixedDisplayName(realName) : null;
         return prefixed != null ? prefixed : display;
      } else {
         return display;
      }
   }

   public class_2561 decorateChat(class_2561 input) {
      return this.isActive() ? this.modifyText(input) : input;
   }

   private class_2561 modifyText(class_2561 input) {
      if (input == null) {
         return null;
      } else {
         String me = this.selfName();
         return me != null && !me.isBlank() && input.getString().contains(me) ? this.splice(input, me, new boolean[]{false}) : input;
      }
   }

   private class_2561 splice(class_2561 c, String name, boolean[] done) {
      class_7417 contents = c.method_10851();
      class_5250 result;
      if (!done[0] && contents instanceof class_8828 ptc) {
         String text = ptc.comp_737();
         int idx = text.indexOf(name);
         if (idx >= 0) {
            done[0] = true;
            result = class_2561.method_43473();
            if (idx > 0) {
               result.method_10852(class_2561.method_43470(text.substring(0, idx)).method_10862(c.method_10866()));
            }

            result.method_10852(this.buildPrefixedDisplayName(name));
            int end = idx + name.length();
            if (end < text.length()) {
               result.method_10852(class_2561.method_43470(text.substring(end)).method_10862(c.method_10866()));
            }
         } else {
            result = class_5250.method_43477(contents).method_10862(c.method_10866());
         }
      } else if (!done[0] && contents instanceof class_2588 tc) {
         Object[] args = tc.method_11023();
         Object[] out = new Object[args.length];

         for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof class_2561 ac) {
               out[i] = this.splice(ac, name, done);
            } else if (!done[0] && arg instanceof String s && s.contains(name)) {
               done[0] = true;
               int idx = s.indexOf(name);
               class_5250 split = class_2561.method_43473();
               if (idx > 0) {
                  split.method_10852(class_2561.method_43470(s.substring(0, idx)));
               }

               split.method_10852(this.buildPrefixedDisplayName(name));
               int end = idx + name.length();
               if (end < s.length()) {
                  split.method_10852(class_2561.method_43470(s.substring(end)));
               }

               out[i] = split;
            } else {
               out[i] = arg;
            }
         }

         result = class_5250.method_43477(new class_2588(tc.method_11022(), tc.method_48323(), out)).method_10862(c.method_10866());
      } else {
         result = class_5250.method_43477(contents).method_10862(c.method_10866());
      }

      for (class_2561 sibling : c.method_10855()) {
         result.method_10852(this.splice(sibling, name, done));
      }

      return result;
   }
}
