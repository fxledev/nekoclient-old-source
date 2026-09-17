package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class InfoHud extends HudComponent {
   private static final float HEIGHT = 22.0F;
   private static final float FONT_SIZE = 13.0F;
   private static final float PAD_X = 9.0F;
   private final ThemeManager themes;
   private final String label;
   private final Supplier<String> value;

   public InfoHud(String id, ThemeManager themes, String label, Supplier<String> value, float defaultFx, float defaultFy, BooleanSupplier visible) {
      super(id, defaultFx, defaultFy, visible);
      this.themes = themes;
      this.label = label;
      this.value = value;
      this.setScale(1.18F);
   }

   private String currentValue() {
      try {
         return this.value.get();
      } catch (Exception var2) {
         return "?";
      }
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return 9.0F + vg.textWidth(this.label, 13.0F) + 5.0F + vg.textWidth(this.currentValue(), 13.0F) + 9.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 22.0F;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      float cy = y + h / 2.0F;
      vg.rect(x, y, w, h, 4.0F, Colors.withAlpha(-16777216, 0.82F));
      vg.rectOutline(x, y, w, h, 4.0F, 1.5F, -1);
      float tx = x + 9.0F;
      tx += vg.textGradient(this.label, tx, cy, 13.0F, theme.accentBright(), theme.accent());
      vg.text(this.currentValue(), tx + 5.0F, cy, 13.0F, theme.textPrimary());
   }
}
