package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.module.Modules;
import dev.neko.client.render.nanovg.NVGImages;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_742;

public class RadarHud extends HudComponent {
   private static final float SIZE = 128.0F;
   private final Modules.HudModule module;
   private final ThemeManager themes;

   public RadarHud(Modules.HudModule module, ThemeManager themes, BooleanSupplier visible) {
      super("radar", 0.12F, 0.34F, visible);
      this.module = module;
      this.themes = themes;
      this.setScale(1.25F);
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return 128.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 128.0F;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_1657 self = class_310.method_1551().field_1724;
      if (self != null) {
         float radius = w / 2.0F;
         float cx = x + radius;
         float cy = y + radius;
         vg.circle(cx, cy, radius, Colors.withAlpha(theme.background(), 0.94F));
         vg.circleOutline(cx, cy, radius, 1.4F, -1);
         int cross = Colors.withAlpha(theme.accent(), 0.18F);
         vg.line(cx - radius, cy, cx + radius, cy, 1.0F, cross);
         vg.line(cx, cy - radius, cx, cy + radius, 1.0F, cross);
         float yaw = self.method_36454();
         float facing = (float)Math.toRadians(yaw + 90.0F);
         String[] names = new String[]{"N", "E", "S", "W"};
         float[][] dirs = new float[][]{{0.0F, -1.0F}, {1.0F, 0.0F}, {0.0F, 1.0F}, {-1.0F, 0.0F}};

         for (int i = 0; i < 4; i++) {
            float sx = screenX(dirs[i][0], dirs[i][1], facing);
            float sy = screenY(dirs[i][0], dirs[i][1], facing);
            float mx = cx + sx * (radius - 9.0F);
            float my = cy + sy * (radius - 9.0F);
            vg.text(names[i], mx - vg.textWidth(names[i], 11.0F) / 2.0F, my, 11.0F, -1);
         }

         vg.circleGlow(cx, cy, 3.0F, 4.0F, Colors.withAlpha(theme.accent(), 0.6F));
         vg.circle(cx, cy, 3.0F, theme.accentBright());
         float headSize = Math.max(10.0F, radius * 0.17F);
         float range = 48.0F;

         for (class_742 other : class_310.method_1551().field_1687.method_18456()) {
            if (other != self) {
               float dx = (float)(other.method_23317() - self.method_23317());
               float dz = (float)(other.method_23321() - self.method_23321());
               float dist = (float)Math.sqrt(dx * dx + dz * dz);
               if (!(dist > range)) {
                  float t = dist / range;
                  float px = cx + screenX(dx, dz, facing) / Math.max(dist, 0.001F) * t * (radius - headSize / 2.0F - 3.0F);
                  float py = cy + screenY(dx, dz, facing) / Math.max(dist, 0.001F) * t * (radius - headSize / 2.0F - 3.0F);
                  int skin = NVGImages.wrapGlTexture(other.method_52814().comp_1626().comp_3627(), 64, 64);
                  if (skin > 0) {
                     float hx = px - headSize / 2.0F;
                     float hy = py - headSize / 2.0F;
                     NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 8.0F, 8.0F, 16.0F, 16.0F, hx, hy, headSize, headSize, 1.0F);
                     NVGImages.drawSubImage(vg, skin, 64.0F, 64.0F, 40.0F, 8.0F, 48.0F, 16.0F, hx, hy, headSize, headSize, 1.0F);
                     vg.rectOutline(hx - 1.0F, hy - 1.0F, headSize + 2.0F, headSize + 2.0F, 3.0F, 1.0F, Colors.withAlpha(-1, 0.35F));
                  } else {
                     vg.circleGlow(px, py, 2.5F, 3.0F, Colors.withAlpha(-1, 0.4F));
                     vg.circle(px, py, 2.5F, -1);
                  }
               }
            }
         }
      }
   }

   private static float screenX(float dx, float dz, float facing) {
      return (float)(-dx * Math.sin(facing) + dz * Math.cos(facing));
   }

   private static float screenY(float dx, float dz, float facing) {
      return (float)(-(dx * Math.cos(facing) + dz * Math.sin(facing)));
   }
}
