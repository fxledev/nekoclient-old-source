package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;
import net.minecraft.class_310;

public class FpsHud extends HudComponent {
   private final ThemeManager themes;
   private final Deque<Integer> history = new ArrayDeque<>();

   public FpsHud(ThemeManager themes, BooleanSupplier visible) {
      super("fps", 0.02F, 0.9F, visible);
      this.themes = themes;
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return vg.textWidth("120", 15.0F) + 78.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 30.0F;
   }

   private void shadowText(NVGRenderer vg, String s, float x, float y, float size, int col) {
      vg.text(s, x + 1.0F, y + 1.0F, size, Colors.withAlpha(-16777216, 0.6F));
      vg.text(s, x, y, size, col);
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      int fps = 0;

      try {
         fps = class_310.method_1551().method_47599();
      } catch (Exception var22) {
      }

      this.history.addLast(fps);

      while (this.history.size() > 72) {
         this.history.pollFirst();
      }

      int dot = fps >= 50 ? theme.accent() : (fps >= 25 ? -340971 : -495247);
      vg.circle(x + 4.0F, y + 8.0F, 3.0F, Colors.withAlpha(-16777216, 0.6F));
      vg.circle(x + 3.0F, y + 7.0F, 3.0F, dot);
      this.shadowText(vg, "FPS", x + 10.0F, y + 7.0F, 10.0F, theme.textMuted());
      this.shadowText(vg, Integer.toString(fps), x + 1.0F, y + 22.0F, 15.0F, theme.textPrimary());
      float gx = x + vg.textWidth("120", 15.0F) + 12.0F;
      float gw = 64.0F;
      float gy = y + 6.0F;
      float gh = h - 12.0F;
      float max = 60.0F;

      for (int s : this.history) {
         max = Math.max(max, (float)s);
      }

      vg.line(gx, gy + gh, gx + gw, gy + gh, 1.0F, Colors.withAlpha(theme.textDisabled(), 0.4F));
      Float px = null;
      Float py = null;
      int i = 0;
      int n = this.history.size();

      for (int s : this.history) {
         float fx = gx + gw * (n <= 1 ? 1.0F : (float)i / (n - 1));
         float fy = gy + gh - gh * Math.clamp(s / max, 0.0F, 1.0F);
         if (px != null) {
            vg.line(px, py, fx, fy, 1.4F, Colors.withAlpha(theme.accent(), 0.9F));
         }

         px = fx;
         py = fy;
         i++;
      }

      if (px != null) {
         vg.circle(px, py, 2.2F, theme.accentBright());
      }
   }
}
