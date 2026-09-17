package dev.neko.client.render;

import dev.neko.client.module.impl.HitParticlesModule;
import dev.neko.client.util.Colors;
import java.util.Deque;
import net.minecraft.class_12249;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4608;
import net.minecraft.class_4587.class_4665;
import net.minecraft.class_4597.class_4598;
import org.joml.Vector3f;

public final class HitParticleRenderer {
   private static final class_2960 TEXTURE_67 = class_2960.method_60655("nekoclient", "textures/misc/67.png");
   private static final int HEART_SEGMENTS = 20;
   private static final float[] HEART_X = new float[21];
   private static final float[] HEART_Y = new float[21];
   private static final float SHOCK_TIME = 0.5F;

   private HitParticleRenderer() {
   }

   public static void render(class_4598 bufferSource, class_4587 poseStack, class_243 cam, HitParticlesModule module) {
      Deque<HitParticlesModule.HitParticle> particles = module.particles();
      Deque<HitParticlesModule.Shock> shocks = module.shocks();
      if (!particles.isEmpty() || !shocks.isEmpty()) {
         long now = System.nanoTime();

         while (!particles.isEmpty() && particles.peekFirst().ageSeconds(now) > particles.peekFirst().lifetime) {
            particles.removeFirst();
         }

         while (!shocks.isEmpty() && shocks.peekFirst().ageSeconds(now) > 0.5F) {
            shocks.removeFirst();
         }

         if (!particles.isEmpty() || !shocks.isEmpty()) {
            class_4184 camera = class_310.method_1551().field_1773.method_19418();
            Vector3f fwd = new Vector3f(camera.method_19335());
            Vector3f right = new Vector3f();
            fwd.cross(new Vector3f(0.0F, 1.0F, 0.0F), right);
            if (right.lengthSquared() < 1.0E-6F) {
               right.set(1.0F, 0.0F, 0.0F);
            }

            right.normalize();
            Vector3f up = new Vector3f();
            right.cross(fwd, up);
            up.normalize();
            float glow = module.glowStrength();
            class_4665 pose = poseStack.method_23760();
            class_4588 lines = bufferSource.method_73477(FlatOverlay.LINES);

            for (HitParticlesModule.Shock shock : shocks) {
               renderShock(lines, pose, cam, right, up, shock, now);
            }

            boolean any67 = false;

            for (HitParticlesModule.HitParticle p : particles) {
               float age = p.ageSeconds(now);
               if (!(age < 0.0F) && !(age > p.lifetime)) {
                  if (p.styleId == 2) {
                     renderLightning(lines, pose, cam, right, up, p, age);
                  } else if (p.styleId == 3) {
                     any67 = true;
                  }
               }
            }

            class_4588 fill = bufferSource.method_73477(FlatOverlay.FILL);

            for (HitParticlesModule.HitParticle px : particles) {
               float age = px.ageSeconds(now);
               if (!(age < 0.0F) && !(age > px.lifetime)) {
                  if (px.styleId == 1) {
                     renderHeart(fill, pose, cam, right, up, px, age, glow);
                  } else if (px.styleId == 0) {
                     renderSpark(fill, pose, cam, right, up, px, age, glow);
                  }
               }
            }

            FlatOverlay.flush(bufferSource);
            if (any67) {
               class_4588 glyphs = bufferSource.method_73477(class_12249.method_76002(TEXTURE_67));

               for (HitParticlesModule.HitParticle pxx : particles) {
                  if (pxx.styleId == 3) {
                     float age = pxx.ageSeconds(now);
                     if (!(age < 0.0F) && !(age > pxx.lifetime)) {
                        render67(glyphs, pose, cam, right, up, fwd, pxx, age);
                     }
                  }
               }
            }
         }
      }
   }

   private static void renderSpark(
      class_4588 buf, class_4665 pose, class_243 cam, Vector3f right, Vector3f up, HitParticlesModule.HitParticle p, float age, float glow
   ) {
      float t = age / p.lifetime;
      float alpha = fadeAlpha(t);
      if (!(alpha <= 0.01F)) {
         float bx = (float)(p.x(age) - cam.field_1352);
         float by = (float)(p.y(age) - cam.field_1351);
         float bz = (float)(p.z(age) - cam.field_1350);
         float vy = p.vy - p.gravity * age;
         float sr = p.vx * right.x + vy * right.y + p.vz * right.z;
         float su = p.vx * up.x + vy * up.y + p.vz * up.z;
         float slen = class_3532.method_15355(sr * sr + su * su);
         float dirR;
         float dirU;
         if (slen > 1.0E-4F) {
            dirR = sr / slen;
            dirU = su / slen;
         } else {
            dirR = 0.0F;
            dirU = 1.0F;
         }

         float shrink = 0.35F + 0.65F * (1.0F - t);
         float streak = 0.28F * p.size * shrink;
         float width = 0.055F * p.size * shrink;
         Vector3f lAx = axis(right, up, dirR * streak, dirU * streak);
         Vector3f sAx = axis(right, up, -dirU * width, dirR * width);
         int core = Colors.withAlpha(Colors.lighten(p.rgb, 0.55F), alpha);
         quad(buf, pose, bx, by, bz, lAx, sAx, core);
         if (glow > 0.01F) {
            float halo = 0.06F * p.size * shrink * (1.0F + 0.6F * glow);
            Vector3f hA = axis(right, up, halo, halo);
            Vector3f hB = axis(right, up, -halo, halo);
            int haloColor = Colors.withAlpha(p.rgb, alpha * 0.22F * glow);
            quad(buf, pose, bx, by, bz, hA, hB, haloColor);
         }
      }
   }

   private static void renderHeart(
      class_4588 buf, class_4665 pose, class_243 cam, Vector3f right, Vector3f up, HitParticlesModule.HitParticle p, float age, float glow
   ) {
      float t = age / p.lifetime;
      float alpha = fadeAlpha(t);
      if (!(alpha <= 0.01F)) {
         float pop = t < 0.2F ? easeOutBack(t / 0.2F) : 1.0F;
         float scale = 0.16F * p.size * pop;
         float bx = (float)(p.x(age) - cam.field_1352);
         float by = (float)(p.y(age) - cam.field_1351);
         float bz = (float)(p.z(age) - cam.field_1350);
         float wobble = 0.18F * class_3532.method_15374(age * 6.0F + p.rot);
         int core = Colors.withAlpha(Colors.lighten(p.rgb, 0.25F), alpha);
         fanHeart(buf, pose, right, up, bx, by, bz, scale, wobble, core);
         if (glow > 0.01F) {
            int haloColor = Colors.withAlpha(p.rgb, alpha * 0.3F * glow);
            fanHeart(buf, pose, right, up, bx, by, bz, scale * (1.35F + 0.35F * glow), wobble, haloColor);
         }
      }
   }

   private static void fanHeart(class_4588 buf, class_4665 pose, Vector3f right, Vector3f up, float bx, float by, float bz, float scale, float shearX, int argb) {
      for (int i = 0; i < 20; i++) {
         float ax0 = (HEART_X[i] + shearX * HEART_Y[i]) * scale;
         float ay0 = HEART_Y[i] * scale;
         float ax1 = (HEART_X[i + 1] + shearX * HEART_Y[i + 1]) * scale;
         float ay1 = HEART_Y[i + 1] * scale;
         v(buf, pose, bx, by, bz, argb);
         v(buf, pose, bx + right.x * ax0 + up.x * ay0, by + right.y * ax0 + up.y * ay0, bz + right.z * ax0 + up.z * ay0, argb);
         v(buf, pose, bx + right.x * ax1 + up.x * ay1, by + right.y * ax1 + up.y * ay1, bz + right.z * ax1 + up.z * ay1, argb);
         v(buf, pose, bx, by, bz, argb);
      }
   }

   private static void renderLightning(class_4588 buf, class_4665 pose, class_243 cam, Vector3f right, Vector3f up, HitParticlesModule.HitParticle p, float age) {
      float t = age / p.lifetime;
      float alpha = fadeAlpha(t);
      if (!(alpha <= 0.01F)) {
         float flicker = 0.45F + 0.55F * class_3532.method_15379(class_3532.method_15374(age * 42.0F + p.rot));
         alpha *= flicker;
         float sx = (float)(p.ox - cam.field_1352);
         float sy = (float)(p.oy - cam.field_1351);
         float sz = (float)(p.oz - cam.field_1350);
         float ex = (float)(p.x(age) - cam.field_1352);
         float ey = (float)(p.y(age) - cam.field_1351);
         float ez = (float)(p.z(age) - cam.field_1350);
         float dx = ex - sx;
         float dy = ey - sy;
         float dz = ez - sz;
         float dr = dx * right.x + dy * right.y + dz * right.z;
         float du = dx * up.x + dy * up.y + dz * up.z;
         float dlen = class_3532.method_15355(dr * dr + du * du);
         float pr;
         float pu;
         if (dlen > 1.0E-4F) {
            pr = -du / dlen;
            pu = dr / dlen;
         } else {
            pr = 1.0F;
            pu = 0.0F;
         }

         float amp = 0.16F * p.size;
         int core = Colors.withAlpha(Colors.lighten(p.rgb, 0.6F), alpha);
         float lineW = 2.4F * p.size;
         int kinks = 4;
         float px = sx;
         float py = sy;
         float pz = sz;

         for (int i = 1; i <= kinks; i++) {
            float f = (float)i / kinks;
            float taper = class_3532.method_15374(f * (float) Math.PI);
            float j = i == kinks ? 0.0F : (hash(p, i) * 2.0F - 1.0F) * amp * taper;
            float nx = sx + dx * f + (right.x * pr + up.x * pu) * j;
            float ny = sy + dy * f + (right.y * pr + up.y * pu) * j;
            float nz = sz + dz * f + (right.z * pr + up.z * pu) * j;
            line(buf, pose, px, py, pz, nx, ny, nz, core, lineW);
            px = nx;
            py = ny;
            pz = nz;
         }
      }
   }

   private static void render67(
      class_4588 buf, class_4665 pose, class_243 cam, Vector3f right, Vector3f up, Vector3f fwd, HitParticlesModule.HitParticle p, float age
   ) {
      float t = age / p.lifetime;
      float alpha = fadeAlpha(t);
      if (!(alpha <= 0.01F)) {
         float pop = t < 0.22F ? easeOutBack(t / 0.22F) : 1.0F;
         float half = 0.16F * p.size * pop;
         float angle = p.rot + p.rotSpeed * age * 0.5F;
         float ca = class_3532.method_15362(angle);
         float sa = class_3532.method_15374(angle);
         Vector3f rAx = axis(right, up, ca * half, sa * half);
         Vector3f uAx = axis(right, up, -sa * half, ca * half);
         float bx = (float)(p.x(age) - cam.field_1352);
         float by = (float)(p.y(age) - cam.field_1351);
         float bz = (float)(p.z(age) - cam.field_1350);
         int argb = Colors.withAlpha(p.rgb, alpha);
         float nx = -fwd.x;
         float ny = -fwd.y;
         float nz = -fwd.z;
         texVertex(buf, pose, bx - rAx.x - uAx.x, by - rAx.y - uAx.y, bz - rAx.z - uAx.z, 0.0F, 0.0F, argb, nx, ny, nz);
         texVertex(buf, pose, bx - rAx.x + uAx.x, by - rAx.y + uAx.y, bz - rAx.z + uAx.z, 0.0F, 1.0F, argb, nx, ny, nz);
         texVertex(buf, pose, bx + rAx.x + uAx.x, by + rAx.y + uAx.y, bz + rAx.z + uAx.z, 1.0F, 1.0F, argb, nx, ny, nz);
         texVertex(buf, pose, bx + rAx.x - uAx.x, by + rAx.y - uAx.y, bz + rAx.z - uAx.z, 1.0F, 0.0F, argb, nx, ny, nz);
      }
   }

   private static void renderShock(class_4588 buf, class_4665 pose, class_243 cam, Vector3f right, Vector3f up, HitParticlesModule.Shock shock, long now) {
      float age = shock.ageSeconds(now);
      if (!(age < 0.0F) && !(age >= 0.5F)) {
         float t = age / 0.5F;
         float radius = 0.15F + easeOutCubic(t) * 1.15F;
         float alpha = 0.8F * (1.0F - easeInQuad(t));
         int color = Colors.withAlpha(Colors.lighten(shock.rgb, 0.2F), alpha);
         float bx = (float)(shock.x - cam.field_1352);
         float by = (float)(shock.y - cam.field_1351);
         float bz = (float)(shock.z - cam.field_1350);
         int segs = 28;
         float prevX = 0.0F;
         float prevY = 0.0F;
         float prevZ = 0.0F;

         for (int i = 0; i <= segs; i++) {
            float a = (float)i / segs * (float) (Math.PI * 2);
            float ox = class_3532.method_15362(a) * radius;
            float oy = class_3532.method_15374(a) * radius;
            float x = bx + right.x * ox + up.x * oy;
            float y = by + right.y * ox + up.y * oy;
            float z = bz + right.z * ox + up.z * oy;
            if (i > 0) {
               line(buf, pose, prevX, prevY, prevZ, x, y, z, color, 2.2F);
            }

            prevX = x;
            prevY = y;
            prevZ = z;
         }
      }
   }

   private static Vector3f axis(Vector3f right, Vector3f up, float a, float b) {
      return new Vector3f(right.x * a + up.x * b, right.y * a + up.y * b, right.z * a + up.z * b);
   }

   private static void quad(class_4588 buf, class_4665 pose, float bx, float by, float bz, Vector3f ax1, Vector3f ax2, int argb) {
      v(buf, pose, bx - ax1.x - ax2.x, by - ax1.y - ax2.y, bz - ax1.z - ax2.z, argb);
      v(buf, pose, bx + ax1.x - ax2.x, by + ax1.y - ax2.y, bz + ax1.z - ax2.z, argb);
      v(buf, pose, bx + ax1.x + ax2.x, by + ax1.y + ax2.y, bz + ax1.z + ax2.z, argb);
      v(buf, pose, bx - ax1.x + ax2.x, by - ax1.y + ax2.y, bz - ax1.z + ax2.z, argb);
   }

   private static void v(class_4588 buf, class_4665 pose, float x, float y, float z, int argb) {
      buf.method_56824(pose, x, y, z).method_39415(argb);
   }

   private static void line(class_4588 buf, class_4665 pose, float x1, float y1, float z1, float x2, float y2, float z2, int argb, float width) {
      Vector3f n = new Vector3f(x2 - x1, y2 - y1, z2 - z1);
      if (n.lengthSquared() > 1.0E-9F) {
         n.normalize();
      } else {
         n.set(0.0F, 1.0F, 0.0F);
      }

      buf.method_56824(pose, x1, y1, z1).method_39415(argb).method_61959(pose, n).method_75298(width);
      buf.method_56824(pose, x2, y2, z2).method_39415(argb).method_61959(pose, n).method_75298(width);
   }

   private static void texVertex(class_4588 buf, class_4665 pose, float x, float y, float z, float u, float vv, int argb, float nx, float ny, float nz) {
      buf.method_56824(pose, x, y, z)
         .method_39415(argb)
         .method_22913(u, vv)
         .method_22922(class_4608.field_21444)
         .method_60803(15728880)
         .method_60831(pose, nx, ny, nz);
   }

   private static float hash(HitParticlesModule.HitParticle p, int salt) {
      float s = class_3532.method_15374((float)(p.ox * 12.9898 + p.oz * 78.233 + p.rot * 3.17 + salt * 43.123)) * 43758.547F;
      return s - class_3532.method_15375(s);
   }

   private static float fadeAlpha(float t) {
      t = clamp01(t);
      return t < 0.12F ? t / 0.12F : 1.0F - easeInQuad((t - 0.12F) / 0.88F);
   }

   private static float clamp01(float t) {
      return t < 0.0F ? 0.0F : (t > 1.0F ? 1.0F : t);
   }

   private static float easeInQuad(float t) {
      t = clamp01(t);
      return t * t;
   }

   private static float easeOutCubic(float t) {
      t = clamp01(t);
      float inv = 1.0F - t;
      return 1.0F - inv * inv * inv;
   }

   private static float easeOutBack(float t) {
      t = clamp01(t);
      float overshoot = 2.4F;
      float c3 = overshoot + 1.0F;
      float u = t - 1.0F;
      return 1.0F + c3 * u * u * u + overshoot * u * u;
   }

   static {
      for (int i = 0; i <= 20; i++) {
         double t = i / 20.0 * Math.PI * 2.0;
         double hx = 16.0 * Math.pow(Math.sin(t), 3.0);
         double hy = 13.0 * Math.cos(t) - 5.0 * Math.cos(2.0 * t) - 2.0 * Math.cos(3.0 * t) - Math.cos(4.0 * t);
         HEART_X[i] = (float)(hx / 17.0);
         HEART_Y[i] = (float)(hy / 17.0);
      }
   }
}
