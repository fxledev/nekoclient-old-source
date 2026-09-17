package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGImages;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_2960;

public class WatermarkHud extends HudComponent {
   private static final float HEIGHT = 30.0F;
   private static final float PAD = 13.0F;
   private static final float LOGO_SIZE = 24.0F;
   private static final float TEXT_SIZE = 14.0F;
   private static final class_2960 MEOW_ID = class_2960.method_60655("nekoclient", "meow-white.png");
   private static final float MEOW_ASPECT = 1.3364055F;
   private static final float MEOW_H = 20.0F;
   private final ThemeManager themes;

   public WatermarkHud(ThemeManager themes, BooleanSupplier visible) {
      super("watermark", 0.03F, 0.04F, visible);
      this.themes = themes;
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return 47.72811F + vg.textWidth("NekoClient", 16.0F) + 13.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 30.0F;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      float cy = y + h / 2.0F;
      float breathe = (float)(0.5 + 0.5 * Math.sin(System.nanoTime() / 8.0E8));
      float drift = (float)(0.5 + 0.5 * Math.sin(System.nanoTime() / 4.8E8));
      int gradTop = Colors.lerp(theme.accentBright(), theme.accent(), drift);
      int gradBottom = Colors.lerp(theme.accent(), theme.accentBright(), drift);
      vg.glow(x, y, w, h, h / 2.0F, 8.0F, Colors.withAlpha(theme.accent(), 0.1F + 0.1F * breathe));
      vg.rect(x, y, w, h, 5.0F, Colors.withAlpha(theme.background(), 0.94F));
      vg.rectOutline(x, y, w, h, 5.0F, 1.5F, -1);
      float tx = x + 13.0F;
      int meow = -1;

      try {
         meow = NVGImages.fromResource(MEOW_ID);
      } catch (Exception var15) {
      }

      if (meow > 0) {
         vg.imageTinted(meow, tx, cy - 10.0F, 26.728111F, 20.0F, Colors.withAlpha(theme.accent(), 0.45F + 0.3F * breathe));
         tx += 34.72811F;
      }

      vg.textGlow("NekoClient", tx, cy, 16.0F, Colors.withAlpha(theme.accent(), 0.45F + 0.3F * breathe));
      vg.textGradient("NekoClient", tx, cy, 16.0F, gradTop, gradBottom);
   }
}
