package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.render.StorageEspRenderer;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.IconListSetting;
import dev.neko.client.settings.SliderSetting;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_2281;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2595;
import net.minecraft.class_2680;
import net.minecraft.class_2745;
import net.minecraft.class_310;

public class StorageEspModule extends Module {
   public final SliderSetting range = this.addSetting(new SliderSetting("Range", "Max distance a container is highlighted", 128.0, 16.0, 256.0, 8.0));
   public final SliderSetting highlightAlpha = this.addSetting(new SliderSetting("Highlight Alpha", "Box opacity (0-255)", 200.0, 0.0, 255.0, 1.0));
   public final BooleanSetting tracers = this.addSetting(new BooleanSetting("Tracers", "Draw lines from the crosshair to each container", false));
   public final IconListSetting containers = this.addSetting(new IconListSetting("Containers", "Which container types to highlight"));
   private final Set<class_2338> interactedBlocks = new HashSet<>();

   public StorageEspModule() {
      super("StorageESP", "Highlights chests, barrels, shulkers", Category.RENDER);

      for (StorageEspModule.StorageType type : StorageEspModule.StorageType.values()) {
         this.containers.add(type.key, type.label, type.icon, type.defaultEnabled, type.defaultColor);
      }
   }

   public boolean isTypeEnabled(StorageEspModule.StorageType type) {
      return this.containers.isEnabled(type.key);
   }

   public int colorFor(StorageEspModule.StorageType type) {
      return this.containers.color(type.key);
   }

   public boolean isInteracted(int x, int y, int z) {
      return this.isInteracted(new class_2338(x, y, z));
   }

   public boolean isInteracted(class_2338 pos) {
      return !this.interactedBlocks.isEmpty() && this.interactedBlocks.contains(pos);
   }

   public void trackInteraction(class_2338 pos) {
      if (pos != null) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1687 != null) {
            this.interactedBlocks.add(pos.method_10062());
            class_2586 be = mc.field_1687.method_8321(pos);
            if (be instanceof class_2595) {
               class_2680 state = mc.field_1687.method_8320(pos);
               if (state.method_26204() instanceof class_2281) {
                  class_2745 ct = (class_2745)state.method_11654(class_2281.field_10770);
                  if (ct != class_2745.field_12569) {
                     class_2350 facing = (class_2350)state.method_11654(class_2281.field_10768);
                     class_2338 other = pos.method_10093(ct == class_2745.field_12574 ? facing.method_10170() : facing.method_10160());
                     this.interactedBlocks.add(other);
                  }
               }
            }
         }
      }
   }

   @Override
   protected void onEnable() {
      this.interactedBlocks.clear();
      StorageEspRenderer.resetScan();
   }

   @Override
   public void onTick() {
      StorageEspRenderer.scan(this);
   }

   @Override
   protected void onDisable() {
      StorageEspRenderer.resetScan();
   }

   public static enum StorageType {
      CHEST("chest", "Chest", class_1802.field_8106, -22016, true),
      TRAPPED("trapped", "Trapped Chest", class_1802.field_8247, -65536, true),
      ENDER("ender", "Ender Chest", class_1802.field_8466, -8912641, true),
      SHULKER("shulker", "Shulker Box", class_1802.field_8545, -47873, true),
      BARREL("barrel", "Barrel", class_1802.field_16307, -7842560, true),
      SPAWNER("spawner", "Spawner", class_1802.field_8849, -16711936, true),
      HOPPER("hopper", "Hopper", class_1802.field_8239, -7829368, false),
      FURNACE("furnace", "Furnace", class_1802.field_8732, -7566196, false),
      BREWING_STAND("brewing_stand", "Brewing Stand", class_1802.field_8740, -13374209, false),
      DISPENSER("dispenser", "Dispenser", class_1802.field_8357, -16727604, false),
      DROPPER("dropper", "Dropper", class_1802.field_8878, -16727604, false);

      final String key;
      final String label;
      final class_1792 icon;
      final int defaultColor;
      final boolean defaultEnabled;

      private StorageType(String key, String label, class_1792 icon, int defaultColor, boolean defaultEnabled) {
         this.key = key;
         this.label = label;
         this.icon = icon;
         this.defaultColor = defaultColor;
         this.defaultEnabled = defaultEnabled;
      }

      private static StorageEspModule.StorageType[] $values() {
         return new StorageEspModule.StorageType[]{CHEST, TRAPPED, ENDER, SHULKER, BARREL, SPAWNER, HOPPER, FURNACE, BREWING_STAND, DISPENSER, DROPPER};
      }

      private static StorageEspModule.StorageType[] $values$() {
         return new StorageEspModule.StorageType[]{CHEST, TRAPPED, ENDER, SHULKER, BARREL, SPAWNER, HOPPER, FURNACE, BREWING_STAND, DISPENSER, DROPPER};
      }
   }
}
