package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import net.minecraft.class_310;

public class SprintModule extends Module {
   public final BooleanSetting keepSprint = this.addSetting(new BooleanSetting("KeepSprint", "Keep sprinting even when hitting or using", true));
   public final BooleanSetting onlyForward = this.addSetting(new BooleanSetting("OnlyForward", "Only sprint when moving forward", true));

   public SprintModule() {
      super("Sprint", "Auto-sprint while moving", Category.MISC);
   }

   @Override
   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null) {
         if (!mc.field_1724.method_5715() && !mc.field_1724.method_6115()) {
            if (mc.field_1724.method_7344().method_7586() > 6 || mc.field_1724.method_31549().field_7478) {
               boolean moving;
               if (this.onlyForward.get()) {
                  moving = mc.field_1690.field_1894.method_1434();
               } else {
                  moving = mc.field_1690.field_1894.method_1434()
                     || mc.field_1690.field_1881.method_1434()
                     || mc.field_1690.field_1913.method_1434()
                     || mc.field_1690.field_1849.method_1434();
               }

               if (moving) {
                  if (!mc.field_1724.field_5976 || this.keepSprint.get()) {
                     mc.field_1724.method_5728(true);
                  }
               }
            }
         }
      }
   }
}
