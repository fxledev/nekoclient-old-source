package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_2350;
import net.minecraft.class_310;
import net.minecraft.class_746;

public class DirectionHud extends HudComponent {
   private static final String[] PTS = new String[]{"N", "E", "S", "W"};
   private final ThemeManager themes;

   public DirectionHud(ThemeManager themes, BooleanSupplier visible) {
      super("direction", 0.27F, 0.9F, visible);
      this.themes = themes;
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return 88.0F + vg.textWidth("+Z", 10.0F) + 8.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 24.0F;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_2350 facing = null;

      try {
         class_746 p = class_310.method_1551().field_1724;
         if (p != null) {
            facing = p.method_5735();
         }
      } catch (Exception var14) {
      }

      vg.neuRaised(x, y, w, h, 11.0F, theme.card());
      float cy = y + h / 2.0F;
      String facingId = facing == null ? "" : facing.method_10151();
      String[] ids = new String[]{"north", "east", "south", "west"};

      for (int i = 0; i < 4; i++) {
         boolean active = ids[i].equals(facingId);
         float lx = x + 2.0F + i * 22.0F;
         if (active) {
            vg.text(PTS[i], lx + 1.0F, cy + 1.0F, 14.0F, Colors.withAlpha(-16777216, 0.6F));
            vg.text(PTS[i], lx, cy, 14.0F, theme.accentBright());
         } else {
            vg.text(PTS[i], lx + 1.0F, cy + 1.0F, 12.0F, Colors.withAlpha(-16777216, 0.6F));
            vg.text(PTS[i], lx, cy, 12.0F, theme.textDisabled());
         }
      }

      String axis = "--";
      if (facing != null) {
         axis = switch (facing) {
            case field_11043 -> "-Z";
            case field_11035 -> "+Z";
            case field_11039 -> "-X";
            case field_11034 -> "+X";
            default -> facing.method_10151().toUpperCase();
         };
      }

      vg.text(axis, x + w - 8.0F - vg.textWidth(axis, 10.0F) + 1.0F, cy + 1.0F, 10.0F, Colors.withAlpha(-16777216, 0.6F));
      vg.text(axis, x + w - 8.0F - vg.textWidth(axis, 10.0F), cy, 10.0F, Colors.withAlpha(theme.textMuted(), 0.8F));
   }
}
