package dev.neko.client.render;

import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public final class WorldProjection {
   private static final Matrix4f mvp = new Matrix4f();
   private static final Vector4f scratch = new Vector4f();
   private static class_243 camPos = class_243.field_1353;
   private static int fbWidth;
   private static int fbHeight;
   private static float partialTick;
   private static boolean valid;

   private WorldProjection() {
   }

   public static void capture(Matrix4f projection, float partialTick) {
      class_310 mc = class_310.method_1551();
      class_4184 camera = mc.field_1773.method_19418();
      if (camera == null) {
         valid = false;
      } else {
         Quaternionf view = camera.method_23767().conjugate(new Quaternionf());
         mvp.set(projection).rotate(view);
         camPos = camera.method_71156();
         class_276 target = mc.method_1522();
         fbWidth = target.field_1482;
         fbHeight = target.field_1481;
         WorldProjection.partialTick = partialTick;
         valid = true;
      }
   }

   public static void invalidate() {
      valid = false;
   }

   public static boolean isValid() {
      return valid;
   }

   public static float partialTick() {
      return partialTick;
   }

   public static float[] project(double wx, double wy, double wz) {
      float[] px = projectRaw(wx, wy, wz);
      if (px == null) {
         return null;
      } else {
         float scale = OverlayRenderer.uiScale();
         return new float[]{px[0] / scale, px[1] / scale};
      }
   }

   public static float[] projectRaw(double wx, double wy, double wz) {
      if (!valid) {
         return null;
      } else {
         scratch.set((float)(wx - camPos.field_1352), (float)(wy - camPos.field_1351), (float)(wz - camPos.field_1350), 1.0F);
         mvp.transform(scratch);
         if (scratch.w <= 1.0E-4F) {
            return null;
         } else {
            float ndcX = scratch.x / scratch.w;
            float ndcY = scratch.y / scratch.w;
            float px = (ndcX * 0.5F + 0.5F) * fbWidth;
            float py = (1.0F - (ndcY * 0.5F + 0.5F)) * fbHeight;
            return new float[]{px, py};
         }
      }
   }
}
