package dev.neko.client.gui.picker;

import dev.neko.client.settings.ColorSetting;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public interface PickerGrid {
   String title();

   long activeCount();

   List<PickerGrid.Cell> cells();

   public interface Cell {
      class_1799 icon();

      default class_2960 iconTexture() {
         class_2960 itemId = class_7923.field_41178.method_10221(this.icon().method_7909());
         return class_2960.method_60655(itemId.method_12836(), "textures/item/" + itemId.method_12832() + ".png");
      }

      default class_2960 fallbackTexture() {
         return null;
      }

      default List<class_2960> fallbackTextures() {
         class_2960 texture = this.fallbackTexture();
         return texture == null ? List.of() : List.of(texture);
      }

      String label();

      boolean matches(String var1);

      boolean tracked();

      boolean enabled();

      boolean selected();

      int color();

      void toggle();

      ColorSetting colorTarget();
   }
}
