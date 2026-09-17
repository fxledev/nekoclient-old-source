package dev.neko.client.gui.picker;

import dev.neko.client.settings.ColorSetting;
import dev.neko.client.settings.IconListSetting;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1799;
import net.minecraft.class_2960;

public final class IconListGridModel implements PickerGrid {
   private final IconListSetting setting;
   private List<PickerGrid.Cell> cells;

   public IconListGridModel(IconListSetting setting) {
      this.setting = setting;
   }

   @Override
   public String title() {
      return this.setting.getName();
   }

   @Override
   public long activeCount() {
      return this.setting.enabledCount();
   }

   @Override
   public List<PickerGrid.Cell> cells() {
      if (this.cells == null) {
         List<PickerGrid.Cell> out = new ArrayList<>(this.setting.entries().size());

         for (IconListSetting.Entry entry : this.setting.entries()) {
            out.add(new IconListGridModel.EntryCell(entry));
         }

         this.cells = out;
      }

      return this.cells;
   }

   private static final class EntryCell implements PickerGrid.Cell {
      private final IconListSetting.Entry entry;
      private final class_1799 icon;

      EntryCell(IconListSetting.Entry entry) {
         this.entry = entry;
         this.icon = new class_1799(entry.icon());
      }

      @Override
      public class_1799 icon() {
         return this.icon;
      }

      @Override
      public class_2960 fallbackTexture() {
         String var2 = this.entry.key();

         return switch (var2) {
            case "chest" -> class_2960.method_60655("minecraft", "textures/entity/chest/normal.png");
            case "trapped" -> class_2960.method_60655("minecraft", "textures/entity/chest/trapped.png");
            case "ender" -> class_2960.method_60655("minecraft", "textures/entity/chest/ender.png");
            case "barrel" -> class_2960.method_60655("minecraft", "textures/entity/barrel.png");
            case "shulker" -> class_2960.method_60655("minecraft", "textures/entity/shulker/shulker.png");
            case "spawner" -> class_2960.method_60655("minecraft", "textures/entity/mob spawner.png");
            case "hopper" -> class_2960.method_60655("minecraft", "textures/block/hopper_top.png");
            case "furnace" -> class_2960.method_60655("minecraft", "textures/block/furnace_front.png");
            default -> {
               class_2960 id = class_2960.method_12829(this.entry.key());
               yield id == null ? null : class_2960.method_60655(id.method_12836(), "textures/block/" + id.method_12832() + ".png");
            }
         };
      }

      @Override
      public String label() {
         return this.entry.label();
      }

      @Override
      public boolean matches(String lowerQuery) {
         return this.entry.matches(lowerQuery);
      }

      @Override
      public boolean tracked() {
         return true;
      }

      @Override
      public boolean enabled() {
         return this.entry.enabled.get();
      }

      @Override
      public boolean selected() {
         return this.entry.enabled.get();
      }

      @Override
      public int color() {
         return this.entry.color.get();
      }

      @Override
      public void toggle() {
         this.entry.enabled.toggle();
      }

      @Override
      public ColorSetting colorTarget() {
         return this.entry.color;
      }
   }
}
