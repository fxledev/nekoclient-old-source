package dev.neko.client.gui.picker;

import dev.neko.client.settings.BlockListSetting;
import dev.neko.client.settings.ColorSetting;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

public final class BlockGridModel implements PickerGrid {
   private static List<class_2248> allBlocks;
   private final BlockListSetting setting;
   private final IntSupplier defaultColor;
   private final String title;
   private List<PickerGrid.Cell> cells;

   public BlockGridModel(BlockListSetting setting, IntSupplier defaultColor, String title) {
      this.setting = setting;
      this.defaultColor = defaultColor;
      this.title = title;
   }

   private static List<class_2248> allBlocks() {
      if (allBlocks == null) {
         List<class_2248> list = new ArrayList<>();

         for (class_2248 block : class_7923.field_41175) {
            if (block != class_2246.field_10124 && block != class_2246.field_10543 && block != class_2246.field_10243) {
               list.add(block);
            }
         }

         allBlocks = list;
      }

      return allBlocks;
   }

   @Override
   public String title() {
      return this.title;
   }

   @Override
   public long activeCount() {
      return this.setting.enabledCount();
   }

   @Override
   public List<PickerGrid.Cell> cells() {
      if (this.cells == null) {
         List<PickerGrid.Cell> out = new ArrayList<>(allBlocks().size());

         for (class_2248 block : allBlocks()) {
            out.add(new BlockGridModel.BlockCell(block));
         }

         this.cells = out;
      }

      return this.cells;
   }

   private final class BlockCell implements PickerGrid.Cell {
      private final class_2248 block;
      private final String search;
      private class_1799 icon;

      BlockCell(class_2248 block) {
         this.block = block;
         class_2960 id = class_7923.field_41175.method_10221(block);
         String path = id != null ? id.method_12832() : "";
         String ns = id != null ? id.method_12836() : "";
         this.search = (path + " " + ns + " " + BlockListSetting.displayName(block)).toLowerCase(Locale.ROOT);
      }

      @Override
      public class_1799 icon() {
         if (this.icon == null) {
            class_1792 item = this.block.method_8389();
            this.icon = new class_1799(item == class_1802.field_8162 ? class_1802.field_8077 : item);
         }

         return this.icon;
      }

      @Override
      public class_2960 iconTexture() {
         class_2960 itemId = class_7923.field_41178.method_10221(this.icon().method_7909());
         return class_2960.method_60655(itemId.method_12836(), "textures/item/" + itemId.method_12832() + ".png");
      }

      @Override
      public class_2960 fallbackTexture() {
         class_2960 blockId = class_7923.field_41175.method_10221(this.block);
         return class_2960.method_60655(blockId.method_12836(), "textures/block/" + blockId.method_12832() + ".png");
      }

      @Override
      public List<class_2960> fallbackTextures() {
         class_2960 id = class_7923.field_41175.method_10221(this.block);
         String path = id.method_12832();
         List<class_2960> textures = new ArrayList<>();
         textures.add(class_2960.method_60655(id.method_12836(), "textures/block/" + path + ".png"));
         if (path.equals("grass_block")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/grass_block_top.png"));
         } else if (path.equals("dirt_path")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/dirt_path_top.png"));
         } else if (path.startsWith("potted_")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/flower_pot.png"));
         } else if (path.endsWith("_wall_torch")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/torch.png"));
         } else if (path.endsWith("_wall_sign") || path.endsWith("_wall_hanging_sign")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/oak_planks.png"));
         } else if (path.equals("water")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/water_still.png"));
         } else if (path.equals("lava")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/lava_still.png"));
         } else if (path.equals("fire")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/fire_0.png"));
         } else if (path.equals("soul_fire")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/soul_fire_0.png"));
         } else if (path.equals("redstone_wire")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/redstone_dust_dot.png"));
         } else if (path.equals("tripwire")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/tripwire.png"));
         } else if (path.equals("bubble_column")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/bubble_column.png"));
         } else if (path.equals("nether_portal")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/nether_portal.png"));
         } else if (path.equals("end_portal")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/end_portal.png"));
         } else if (path.equals("end_gateway")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/end_gateway.png"));
         } else if (path.equals("piston_head") || path.equals("moving_piston")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/piston_top.png"));
         } else if (path.endsWith("_crop")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/" + path + "_stage_3.png"));
         } else if (path.endsWith("_stem")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/" + path + "_straight.png"));
         } else if (path.endsWith("_plant")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/" + path.replace("_plant", "") + ".png"));
         } else if (path.endsWith("_wall")) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/stone.png"));
         }

         String[] suffixes = new String[]{
            "_top",
            "_side",
            "_front",
            "_bottom",
            "_back",
            "_0",
            "_1",
            "_stage_0",
            "_stage_1",
            "_stage_2",
            "_stage_3",
            "_stage_4",
            "_stage_5",
            "_stage_6",
            "_stage_7"
         };

         for (String suffix : suffixes) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/" + path + suffix + ".png"));
         }

         String base = path;

         for (String prefix : new String[]{"potted_", "wall_", "cut_", "waxed_", "exposed_", "weathered_", "oxidized_"}) {
            if (base.startsWith(prefix)) {
               base = base.substring(prefix.length());
            }
         }

         if (!base.equals(path)) {
            textures.add(class_2960.method_60655(id.method_12836(), "textures/block/" + base + ".png"));

            for (String suffix : suffixes) {
               textures.add(class_2960.method_60655(id.method_12836(), "textures/block/" + base + suffix + ".png"));
            }
         }

         return textures;
      }

      @Override
      public String label() {
         return BlockListSetting.displayName(this.block);
      }

      @Override
      public boolean matches(String lowerQuery) {
         return this.search.contains(lowerQuery);
      }

      @Override
      public boolean tracked() {
         return BlockGridModel.this.setting.find(this.block) != null;
      }

      @Override
      public boolean enabled() {
         BlockListSetting.Target t = BlockGridModel.this.setting.find(this.block);
         return t != null && t.enabled.get();
      }

      @Override
      public boolean selected() {
         return BlockGridModel.this.setting.find(this.block) != null;
      }

      @Override
      public int color() {
         BlockListSetting.Target t = BlockGridModel.this.setting.find(this.block);
         return t != null ? t.color.get() : BlockGridModel.this.defaultColor.getAsInt();
      }

      @Override
      public void toggle() {
         BlockListSetting.Target t = BlockGridModel.this.setting.find(this.block);
         if (t == null) {
            BlockGridModel.this.setting.add(this.block, true, BlockGridModel.this.defaultColor.getAsInt());
         } else {
            t.enabled.toggle();
         }
      }

      @Override
      public ColorSetting colorTarget() {
         BlockListSetting.Target t = BlockGridModel.this.setting.find(this.block);
         if (t == null) {
            t = BlockGridModel.this.setting.add(this.block, true, BlockGridModel.this.defaultColor.getAsInt());
         }

         return t != null ? t.color : null;
      }
   }
}
