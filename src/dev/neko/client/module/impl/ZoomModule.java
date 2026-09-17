package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.SliderSetting;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends Module {
   public final SliderSetting zoomFov = this.addSetting(new SliderSetting("Zoom FOV", "FOV while zooming", 30.0, 10.0, 90.0, 1.0));
   private int prevFov = -1;
   private boolean zooming;

   public ZoomModule() {
      super("Zoom", "Hold to zoom (default C)", Category.RENDER);
      this.getKeybind().set(67);
   }

   @Override
   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1687 != null && mc.method_22683() != null) {
         int key = this.getKeybind().get();
         boolean held = key != -1 && GLFW.glfwGetKey(mc.method_22683().method_4490(), key) == 1;
         if (this.isEnabled() && held) {
            try {
               int cur = (Integer)mc.field_1690.method_41808().method_41753();
               if (!this.zooming) {
                  this.prevFov = cur;
                  this.zooming = true;
               }

               int target = this.zoomFov.get().intValue();
               if (cur != target) {
                  mc.field_1690.method_41808().method_41748(target);
               }
            } catch (Exception var6) {
            }
         } else {
            this.restore(mc);
         }
      } else {
         this.restore(mc);
      }
   }

   @Override
   protected void onDisable() {
      this.restore(class_310.method_1551());
   }

   private void restore(class_310 mc) {
      if (this.zooming && this.prevFov >= 0) {
         try {
            if (mc != null && mc.field_1690 != null) {
               mc.field_1690.method_41808().method_41748(this.prevFov);
            }
         } catch (Exception var3) {
         }

         this.zooming = false;
         this.prevFov = -1;
      }
   }
}
