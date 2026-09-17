package dev.neko.client.hud.components;

import dev.neko.client.hud.HudComponent;
import dev.neko.client.render.nanovg.NVGImages;
import dev.neko.client.render.nanovg.NVGRenderer;
import dev.neko.client.theme.Theme;
import dev.neko.client.theme.ThemeManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_310;

public class PotionsHud extends HudComponent {
   private static final float ROW = 24.0F;
   private static final float ICON = 18.0F;
   private static final float PAD = 7.0F;
   private static final float FONT = 12.5F;
   private final ThemeManager themes;

   public PotionsHud(ThemeManager themes, BooleanSupplier visible) {
      super("potions", 0.86F, 0.48F, visible);
      this.themes = themes;
   }

   private List<class_1293> effects() {
      class_1657 player = class_310.method_1551().field_1724;
      if (player == null) {
         return List.of();
      } else {
         List<class_1293> list = new ArrayList<>(player.method_6026());
         list.sort(Comparator.comparingInt(class_1293::method_5584).reversed());
         return list;
      }
   }

   private static String label(class_1293 effect) {
      String name = ((class_1291)effect.method_5579().comp_349()).method_5560().getString();
      int amp = effect.method_5578();
      return amp > 0 ? name + " " + (amp + 1) : name;
   }

   private static String timer(class_1293 effect) {
      if (effect.method_48559()) {
         return "∞";
      } else {
         int seconds = effect.method_5584() / 20;
         return String.format("%d:%02d", seconds / 60, seconds % 60);
      }
   }

   @Override
   public float measureWidth(NVGRenderer vg) {
      float max = 110.0F;

      for (class_1293 effect : this.effects()) {
         max = Math.max(max, 31.0F + vg.textWidth(label(effect), 12.5F) + 10.0F + vg.textWidth(timer(effect), 12.5F) + 7.0F);
      }

      return max;
   }

   @Override
   public float measureHeight(NVGRenderer vg) {
      return Math.max(24.0F, this.effects().size() * 24.0F);
   }

   @Override
   public void render(NVGRenderer vg, float x, float y, float w, float h) {
      Theme theme = this.themes.current();
      List<class_1293> effects = this.effects();
      if (!effects.isEmpty()) {
         boolean right = this.rightAnchored();
         vg.rect(x, y, w, h, 9.0F, -16777216);
         float rowY = y;

         for (class_1293 effect : effects) {
            float labelW = vg.textWidth(label(effect), 12.5F);
            float timerW = vg.textWidth(timer(effect), 12.5F);
            float rowW = 31.0F + labelW + 10.0F + timerW + 7.0F;
            float rowX = right ? x + w - rowW : x;
            float cy = rowY + 12.0F;
            class_2960 effectId = effect.method_5579().method_40230().map(k -> k.method_29177()).orElse(null);
            if (effectId != null) {
               int image = NVGImages.fromResource(class_2960.method_60655(effectId.method_12836(), "textures/mob_effect/" + effectId.method_12832() + ".png"));
               if (image > 0) {
                  vg.imagePattern(image, rowX + 7.0F, cy - 9.0F, 18.0F, 18.0F, rowX + 7.0F, cy - 9.0F, 18.0F, 18.0F, 1.0F);
               }
            }

            vg.text(label(effect), rowX + 7.0F + 18.0F + 6.0F, cy, 12.5F, theme.textPrimary());
            vg.textGradient(timer(effect), rowX + rowW - 7.0F - timerW, cy, 12.5F, theme.accentBright(), theme.accent());
            rowY += 24.0F;
         }
      }
   }
}
