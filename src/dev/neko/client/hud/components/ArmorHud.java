package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGImages;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.Colors;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_7923;

public class ArmorHud extends HudComponent {
   private static final class_1304[] SLOTS = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};
   private static final float ICON = 22.0F;
   private static final float PAD = 7.0F;
   private static final float GAP = 6.0F;
   private static final float BAR_H = 3.0F;
   private final ThemeManager themes;

   public ArmorHud(ThemeManager themes, BooleanSupplier visible) {
      super("armor", 0.46F, 0.76F, visible);
      this.themes = themes;
      this.setScale(1.22F);
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      return 14.0F + SLOTS.length * 22.0F + (SLOTS.length - 1) * 6.0F;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return 38.0F;
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      class_1657 player = class_310.method_1551().field_1724;
      if (player != null) {
         vg.rect(x, y, w, h, 9.0F, Colors.withAlpha(theme.background(), 0.94F));
         vg.rectOutline(x, y, w, h, 9.0F, 1.5F, -1);
         float ix = x + 7.0F;

         for (class_1304 slot : SLOTS) {
            class_1799 stack = player.method_6118(slot);
            float iy = y + 4.0F;
            if (stack.method_7960()) {
               vg.rectOutline(ix, iy, 22.0F, 22.0F, 5.0F, 1.0F, Colors.withAlpha(theme.textDisabled(), 0.5F));
            } else {
               class_2960 itemId = class_7923.field_41178.method_10221(stack.method_7909());
               int image = NVGImages.fromResource(class_2960.method_60655(itemId.method_12836(), "textures/item/" + itemId.method_12832() + ".png"));
               if (image > 0) {
                  vg.imagePattern(image, ix, iy, 22.0F, 22.0F, ix, iy, 22.0F, 22.0F, 1.0F);
               } else {
                  vg.rect(ix, iy, 22.0F, 22.0F, 5.0F, Colors.withAlpha(theme.accent(), 0.3F));
               }

               if (stack.method_7963()) {
                  float frac = 1.0F - (float)stack.method_7919() / stack.method_7936();
                  int barColor = Colors.lerp(-1684147, -11671924, frac);
                  float barY = iy + 22.0F + 3.0F;
                  vg.rect(ix, barY, 22.0F, 3.0F, 1.5F, Colors.withAlpha(-16777216, 0.45F));
                  vg.rect(ix, barY, Math.max(3.0F, 22.0F * frac), 3.0F, 1.5F, barColor);
               }
            }

            ix += 28.0F;
         }
      }
   }
}
