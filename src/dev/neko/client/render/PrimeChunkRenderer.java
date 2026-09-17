package dev.neko.client.render;

import dev.neko.client.module.impl.PrimeChunkFinderModule;
import dev.neko.client.util.Colors;
import net.minecraft.class_1923;
import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4597.class_4598;

public final class PrimeChunkRenderer {
   private static final double FIXED_CHUNK_Y = 64.0;
   private static final int REPEATER_FILL = Colors.argb(60, 50, 205, 50);
   private static final int REPEATER_OUTLINE = Colors.argb(180, 50, 205, 50);
   private static final int NORMAL_FILL = Colors.argb(120, 50, 205, 50);
   private static final int NORMAL_OUTLINE = Colors.argb(255, 50, 205, 50);
   private static final double SIZE = 52.0;

   private PrimeChunkRenderer() {
   }

   public static void render(class_4598 bufferSource, class_4587 poseStack, class_243 camera, PrimeChunkFinderModule module) {
      for (class_1923 pos : module.markedChunks.keySet()) {
         double cx = pos.field_9181 * 16.0 + 8.0;
         double cz = pos.field_9180 * 16.0 + 8.0;
         double x0 = cx - 26.0;
         double z0 = cz - 26.0;
         double x1 = cx + 26.0;
         double z1 = cz + 26.0;
         String type = module.markedChunks.get(pos);
         if ("repeater".equals(type)) {
            drawQuad(bufferSource, poseStack, camera, x0, z0, x1, z1, 64.0, REPEATER_FILL, REPEATER_OUTLINE);
         } else {
            drawQuad(bufferSource, poseStack, camera, x0, z0, x1, z1, 64.0, NORMAL_FILL, NORMAL_OUTLINE);
         }
      }

      FlatOverlay.flush(bufferSource);
   }

   private static void drawQuad(
      class_4598 bufferSource, class_4587 poseStack, class_243 camera, double x0, double z0, double x1, double z1, double y, int fillColor, int outlineColor
   ) {
      FlatOverlay.fillQuad(bufferSource, poseStack, camera, x0, z0, x1, z1, y, fillColor);
      FlatOverlay.edge(bufferSource, poseStack, camera, x0, z0, x1, z0, y, outlineColor, 2.0F);
      FlatOverlay.edge(bufferSource, poseStack, camera, x1, z0, x1, z1, y, outlineColor, 2.0F);
      FlatOverlay.edge(bufferSource, poseStack, camera, x1, z1, x0, z1, y, outlineColor, 2.0F);
      FlatOverlay.edge(bufferSource, poseStack, camera, x0, z1, x0, z0, y, outlineColor, 2.0F);
   }
}
