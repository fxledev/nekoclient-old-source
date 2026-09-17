package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_310;
import net.minecraft.class_640;

public class PingHud extends HudComponent {
   private final ThemeManager themes;

   public PingHud(ThemeManager themes, BooleanSupplier visible) {
      super("ping", 0.15F, 0.9F, visible);
      this.themes = themes;
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return 30.0F + vg.textWidth("999ms", 14.0F);
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 24.0F;
   }

   private static int pingMs() {
      try {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null && mc.method_1562() != null) {
            class_640 e = mc.method_1562().method_2871(mc.field_1724.method_5667());
            return e == null ? -1 : e.method_2959();
         } else {
            return -1;
         }
      } catch (Exception var2) {
         return -1;
      }
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      int ms = pingMs();
      int col;
      int lit;
      if (ms < 0) {
         col = theme.textDisabled();
         lit = 0;
      } else if (ms < 60) {
         col = -11870592;
         lit = 4;
      } else if (ms < 130) {
         col = -340971;
         lit = 3;
      } else if (ms < 250) {
         col = -290244;
         lit = 2;
      } else {
         col = -495247;
         lit = 1;
      }

      vg.neuRaised(x, y, w, h, 11.0F, theme.card());
      float baseY = y + h - 4.0F;

      for (int i = 0; i < 4; i++) {
         float bh = 5.0F + i * 4.5F;
         float bx = x + 1.0F + i * 7.0F;
         if (i < lit) {
            vg.rect(bx, baseY - bh, 4.5F, bh, 1.5F, col);
         } else {
            vg.rect(bx, baseY - bh, 4.5F, bh, 1.5F, Colors.withAlpha(theme.textDisabled(), 0.35F));
         }
      }

      String s = ms < 0 ? "--" : ms + "ms";
      vg.text(s, x + 31.0F, y + h / 2.0F + 1.0F, 14.0F, Colors.withAlpha(-16777216, 0.6F));
      vg.text(s, x + 30.0F, y + h / 2.0F, 14.0F, ms < 0 ? theme.textDisabled() : col);
   }
}
