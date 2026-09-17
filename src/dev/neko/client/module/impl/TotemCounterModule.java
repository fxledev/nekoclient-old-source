package dev.neko.client.module.impl;

import dev.neko.client.NekoClient;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.SliderSetting;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_310;

public class TotemCounterModule extends Module {
   public final SliderSetting threshold = this.addSetting(new SliderSetting("Alert Below", "Warn when total totems are at or below this", 3.0, 0.0, 36.0, 1.0));
   public final BooleanSetting chatAlert = this.addSetting(new BooleanSetting("Chat Alert", "Send a chat warning when low", true));
   private int lastCount = -1;

   public TotemCounterModule() {
      super("TotemCounter", "Counts totems and warns when low", Category.MISC);
   }

   @Override
   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         int count = countTotems(mc);
         int limit = this.threshold.get().intValue();
         if (this.lastCount != -1 && count < this.lastCount && count <= limit && this.chatAlert.get()) {
            mc.field_1724.method_7353(class_2561.method_43470("§d[Neko] §cLow totems: " + count), false);

            try {
               if (NekoClient.notifications() != null) {
                  NekoClient.notifications().push("Totems: " + count, false);
               }
            } catch (Exception var5) {
            }
         }

         this.lastCount = count;
      }
   }

   @Override
   protected void onDisable() {
      this.lastCount = -1;
   }

   public static int countTotems(class_310 mc) {
      if (mc.field_1724 == null) {
         return 0;
      } else {
         int n = 0;

         try {
            for (int i = 0; i < mc.field_1724.method_31548().method_5439(); i++) {
               if (mc.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8288)) {
                  n += mc.field_1724.method_31548().method_5438(i).method_7947();
               }
            }

            if (mc.field_1724.method_6079().method_31574(class_1802.field_8288)) {
            }
         } catch (Exception var3) {
         }

         return n;
      }
   }

   public int current() {
      return Math.max(0, this.lastCount);
   }
}
