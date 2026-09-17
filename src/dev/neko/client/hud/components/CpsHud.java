package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import dev.neko.client.util.CpsTracker;
import java.util.function.BooleanSupplier;

public class CpsHud extends HudComponent {
   private final ThemeManager themes;

   public CpsHud(ThemeManager themes, BooleanSupplier visible) {
      super("cps", 0.58F, 0.9F, visible);
      this.themes = themes;
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return vg.textWidth("L 00  R 00", 13.0F) + 4.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 30.0F;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      int l = 0;
      int r = 0;

      try {
         l = CpsTracker.get(0);
         r = CpsTracker.get(1);
      } catch (Exception var10) {
      }

      float peak = Math.max(12.0F, (float)Math.max(l, r));
      this.column(vg, theme, x + 2.0F, y + 2.0F, 52.0F, "L", l, peak);
      this.column(vg, theme, x + 60.0F, y + 2.0F, 52.0F, "R", r, peak);
   }

   private void column(NVGRenderer vg, Theme theme, float bx, float by, float bw, String tag, int v, float peak) {
      vg.text(tag, bx + 1.0F, by + 6.0F, 10.0F, Colors.withAlpha(-16777216, 0.6F));
      vg.text(tag, bx, by + 5.0F, 10.0F, theme.textMuted());
      String s = Integer.toString(v);
      vg.text(s, bx + bw - vg.textWidth(s, 13.0F) + 1.0F, by + 6.0F, 13.0F, Colors.withAlpha(-16777216, 0.6F));
      vg.text(s, bx + bw - vg.textWidth(s, 13.0F), by + 5.0F, 13.0F, theme.textPrimary());
      float frac = Math.clamp(v / peak, 0.0F, 1.0F);
      vg.rect(bx, by + 16.0F, bw, 5.0F, 2.5F, Colors.withAlpha(-16777216, 0.5F));
      if (frac > 0.01F) {
         vg.rect(bx, by + 16.0F, Math.max(5.0F, bw * frac), 5.0F, 2.5F, theme.accent());
      }
   }
}
