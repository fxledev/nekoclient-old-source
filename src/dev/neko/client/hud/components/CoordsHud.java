package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_746;

public class CoordsHud extends HudComponent {
   private final ThemeManager themes;

   public CoordsHud(ThemeManager themes, BooleanSupplier visible) {
      super("coords", 0.4F, 0.905F, visible);
      this.themes = themes;
   }

   private static int[] pos() {
      try {
         class_746 p = class_310.method_1551().field_1724;
         if (p == null) {
            return null;
         } else {
            class_2338 b = p.method_24515();
            return new int[]{b.method_10263(), b.method_10264(), b.method_10260()};
         }
      } catch (Exception var2) {
         return null;
      }
   }

   private static String dim() {
      try {
         String path = class_310.method_1551().field_1687.method_27983().method_29177().method_12832();

         return switch (path) {
            case "the_nether" -> "N";
            case "the_end" -> "E";
            default -> "OW";
         };
      } catch (Exception var3) {
         return "?";
      }
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      int[] p = pos();
      String s = p == null ? "X 0  Y 0  Z 0" : "X " + p[0] + "  Y " + p[1] + "  Z " + p[2] + "  [OW]";
      return 26.0F + vg.textWidth(s, 12.5F);
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 20.0F;
   }

   private void ghost(NVGRenderer vg, String s, float x, float y, float size, int col) {
      vg.text(s, x + 1.0F, y + 1.0F, size, Colors.withAlpha(-16777216, 0.6F));
      vg.text(s, x, y, size, col);
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      int[] p = pos();
      float cy = y + h / 2.0F;
      float tx = x;
      if (p == null) {
         this.ghost(vg, "X 0  Y 0  Z 0", x, cy, 12.5F, theme.textDisabled());
      } else {
         String[] axes = new String[]{"X", "Y", "Z"};
         int[] cols = new int[]{-495247, -11870592, -10443270};

         for (int i = 0; i < 3; i++) {
            this.ghost(vg, axes[i], tx, cy, 12.5F, cols[i]);
            tx += vg.textWidth(axes[i], 12.5F) + 4.0F;
            String num = Integer.toString(p[i]);
            this.ghost(vg, num, tx, cy, 12.5F, theme.textPrimary());
            tx += vg.textWidth(num, 12.5F) + 10.0F;
         }

         String d = dim();
         this.ghost(vg, "[" + d + "]", tx, cy, 11.0F, theme.accentBright());
      }
   }
}
