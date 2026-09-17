package dev.neko.agent;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.GamblePanel;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.impl.GambleRiggerModule;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_480;

public final class NekoScreens {
   private static GamblePanel panel;
   private static class_437 openFor;

   private NekoScreens() {
   }

   public static void init() {
   }

   public static void tick() {
      try {
         class_310 mc = class_310.method_1551();
         ModuleManager mm = NekoClient.modules();
         class_437 cur = mc.field_1755;
         boolean want = mm != null && mm.gambleRigger != null && mm.gambleRigger.isEnabled() && cur instanceof class_480;
         if (want) {
            if (panel == null || openFor != cur) {
               GambleRiggerModule mod = mm.gambleRigger;
               panel = new GamblePanel((class_480)cur, mod);
               openFor = cur;
            }
         } else {
            panel = null;
            openFor = null;
         }
      } catch (Throwable var5) {
         panel = null;
         openFor = null;
      }
   }

   public static void renderCurrent(class_332 graphics, int mx, int my) {
      GamblePanel p = panel;
      if (p != null) {
         try {
            p.render(graphics, mx, my);
         } catch (Throwable var5) {
         }
      }
   }

   public static boolean click(double mx, double my, int button) {
      GamblePanel p = panel;
      if (p == null) {
         return false;
      } else {
         try {
            return p.handleClick(mx, my, button);
         } catch (Throwable var7) {
            return false;
         }
      }
   }
}
