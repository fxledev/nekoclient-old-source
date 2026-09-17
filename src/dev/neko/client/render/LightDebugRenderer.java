package dev.neko.client.render;

import dev.neko.client.module.impl.LightDebugModule;
import dev.neko.client.util.Colors;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;

public final class LightDebugRenderer {
   private static final int MAX_CELLS = 50000;

   private LightDebugRenderer() {
   }

   public static void render(class_4598 bufferSource, class_4587 poseStack, class_243 camera, LightDebugModule module) {
      int rendered = 0;
      boolean stop = false;

      label26:
      for (List<LightDebugModule.Cell> cells : module.cells().values()) {
         Iterator var8 = cells.iterator();

         while (true) {
            if (var8.hasNext()) {
               LightDebugModule.Cell cell = (LightDebugModule.Cell)var8.next();
               float brightness = (cell.light() - 1) / 14.0F;
               int shade = Math.round(Math.clamp(brightness, 0.0F, 1.0F) * 255.0F);
               int color = Colors.withAlpha(shade << 16 | shade << 8 | shade, 110);
               double x = cell.pos().method_10263();
               double y = cell.pos().method_10264();
               double z = cell.pos().method_10260();
               EspBoxRenderer.fill(bufferSource, poseStack, camera, x, y, z, x + 1.0, y + 1.0, z + 1.0, color);
               if (++rendered < 50000) {
                  continue;
               }

               stop = true;
            }

            if (stop) {
               break label26;
            }
            break;
         }
      }

      EspBoxRenderer.flush(bufferSource);
   }
}
