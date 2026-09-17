package dev.neko.client.render;

import net.minecraft.class_243;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_4597.class_4598;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class EspBoxRenderer {
   private static final float TRACER_START = 0.35F;
   private static final float TRACER_NEAR = 0.1F;

   private EspBoxRenderer() {
   }

   public static void outline(
      class_4598 bufferSource, class_4587 poseStack, class_243 cam, double x0, double y0, double z0, double x1, double y1, double z1, int color, float width
   ) {
      class_4588 buf = bufferSource.method_73477(FlatOverlay.LINES);
      class_4665 pose = poseStack.method_23760();
      float ax = (float)(x0 - cam.field_1352);
      float ay = (float)(y0 - cam.field_1351);
      float az = (float)(z0 - cam.field_1350);
      float bx = (float)(x1 - cam.field_1352);
      float by = (float)(y1 - cam.field_1351);
      float bz = (float)(z1 - cam.field_1350);
      line(buf, pose, ax, ay, az, bx, ay, az, color, width);
      line(buf, pose, bx, ay, az, bx, ay, bz, color, width);
      line(buf, pose, bx, ay, bz, ax, ay, bz, color, width);
      line(buf, pose, ax, ay, bz, ax, ay, az, color, width);
      line(buf, pose, ax, by, az, bx, by, az, color, width);
      line(buf, pose, bx, by, az, bx, by, bz, color, width);
      line(buf, pose, bx, by, bz, ax, by, bz, color, width);
      line(buf, pose, ax, by, bz, ax, by, az, color, width);
      line(buf, pose, ax, ay, az, ax, by, az, color, width);
      line(buf, pose, bx, ay, az, bx, by, az, color, width);
      line(buf, pose, bx, ay, bz, bx, by, bz, color, width);
      line(buf, pose, ax, ay, bz, ax, by, bz, color, width);
   }

   public static void fill(
      class_4598 bufferSource, class_4587 poseStack, class_243 cam, double x0, double y0, double z0, double x1, double y1, double z1, int color
   ) {
      class_4588 buf = bufferSource.method_73477(FlatOverlay.FILL);
      class_4665 pose = poseStack.method_23760();
      float ax = (float)(x0 - cam.field_1352);
      float ay = (float)(y0 - cam.field_1351);
      float az = (float)(z0 - cam.field_1350);
      float bx = (float)(x1 - cam.field_1352);
      float by = (float)(y1 - cam.field_1351);
      float bz = (float)(z1 - cam.field_1350);
      quad(buf, pose, color, ax, ay, az, bx, ay, az, bx, ay, bz, ax, ay, bz);
      quad(buf, pose, color, ax, by, az, ax, by, bz, bx, by, bz, bx, by, az);
      quad(buf, pose, color, ax, ay, az, ax, by, az, bx, by, az, bx, ay, az);
      quad(buf, pose, color, ax, ay, bz, bx, ay, bz, bx, by, bz, ax, by, bz);
      quad(buf, pose, color, ax, ay, az, ax, ay, bz, ax, by, bz, ax, by, az);
      quad(buf, pose, color, bx, ay, az, bx, by, az, bx, by, bz, bx, ay, bz);
   }

   public static void tracer(
      class_4598 bufferSource, class_4587 poseStack, class_243 cam, Vector3fc forward, double tx, double ty, double tz, int color, float width
   ) {
      float fx = forward.x();
      float fy = forward.y();
      float fz = forward.z();
      float flen = (float)Math.sqrt(fx * fx + fy * fy + fz * fz);
      if (flen > 1.0E-6F) {
         fx /= flen;
         fy /= flen;
         fz /= flen;
      }

      float sx = fx * 0.35F;
      float sy = fy * 0.35F;
      float sz = fz * 0.35F;
      float ex = (float)(tx - cam.field_1352);
      float ey = (float)(ty - cam.field_1351);
      float ez = (float)(tz - cam.field_1350);
      float endDot = ex * fx + ey * fy + ez * fz;
      if (endDot < 0.1F) {
         float t = -0.25F / (endDot - 0.35F);
         ex = sx + (ex - sx) * t;
         ey = sy + (ey - sy) * t;
         ez = sz + (ez - sz) * t;
      }

      class_4588 buf = bufferSource.method_73477(FlatOverlay.LINES);
      class_4665 pose = poseStack.method_23760();
      line(buf, pose, sx, sy, sz, ex, ey, ez, color, width);
   }

   public static void ring(
      class_4598 bufferSource, class_4587 poseStack, class_243 cam, double cx, double cy, double cz, double radius, int segments, int color, float width
   ) {
      class_4588 buf = bufferSource.method_73477(FlatOverlay.LINES);
      class_4665 pose = poseStack.method_23760();
      float fy = (float)(cy - cam.field_1351);
      double prevX = cx + radius;
      double prevZ = cz;

      for (int i = 1; i <= segments; i++) {
         double a = (Math.PI * 2) * i / segments;
         double x = cx + Math.cos(a) * radius;
         double z = cz + Math.sin(a) * radius;
         line(
            buf,
            pose,
            (float)(prevX - cam.field_1352),
            fy,
            (float)(prevZ - cam.field_1350),
            (float)(x - cam.field_1352),
            fy,
            (float)(z - cam.field_1350),
            color,
            width
         );
         prevX = x;
         prevZ = z;
      }
   }

   public static void flush(class_4598 bufferSource) {
      FlatOverlay.flush(bufferSource);
   }

   private static void line(class_4588 buf, class_4665 pose, float x1, float y1, float z1, float x2, float y2, float z2, int color, float width) {
      Vector3f n = new Vector3f(x2 - x1, y2 - y1, z2 - z1);
      if (n.lengthSquared() > 1.0E-9F) {
         n.normalize();
      } else {
         n.set(0.0F, 1.0F, 0.0F);
      }

      buf.method_56824(pose, x1, y1, z1).method_39415(color).method_61959(pose, n).method_75298(width);
      buf.method_56824(pose, x2, y2, z2).method_39415(color).method_61959(pose, n).method_75298(width);
   }

   private static void quad(
      class_4588 buf,
      class_4665 pose,
      int color,
      float ax,
      float ay,
      float az,
      float bx,
      float by,
      float bz,
      float cx,
      float cy,
      float cz,
      float dx,
      float dy,
      float dz
   ) {
      buf.method_56824(pose, ax, ay, az).method_39415(color);
      buf.method_56824(pose, bx, by, bz).method_39415(color);
      buf.method_56824(pose, cx, cy, cz).method_39415(color);
      buf.method_56824(pose, dx, dy, dz).method_39415(color);
   }
}
