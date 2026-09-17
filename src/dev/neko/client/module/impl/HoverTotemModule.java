package dev.neko.client.module.impl;

import dev.neko.client.mixin.AbstractContainerScreenAccessor;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.util.InventoryHelper;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_465;

public class HoverTotemModule extends Module {
   public final SliderSetting tickDelay = this.addSetting(new SliderSetting("Tick Delay", "", 0.0, 0.0, 20.0, 1.0));
   public final BooleanSetting hotbarTotem = this.addSetting(new BooleanSetting("Hotbar Totem", "", true));
   public final SliderSetting hotbarSlot = this.addSetting(new SliderSetting("Hotbar Slot", "", 1.0, 1.0, 9.0, 1.0));
   public final BooleanSetting autoSwitchToTotem = this.addSetting(new BooleanSetting("Auto Switch To Totem", "", false));
   private int tickCounter;

   public HoverTotemModule() {
      super("Hover Totem", "Equip totem when hovering over one in inventory", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.tickCounter = 0;
   }

   @Override
   protected void onDisable() {
      this.tickCounter = 0;
   }

   @Override
   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1761 != null) {
         if (this.autoSwitchToTotem.get() && this.hotbarTotem.get()) {
            int slot = this.hotbarSlot.getInt() - 1;
            if (client.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8288)) {
               InventoryHelper.swap(slot);
            }
         }

         if (client.field_1755 instanceof class_465<?> handledScreen) {
            this.tickCounter++;
            if (this.tickCounter >= this.tickDelay.getInt()) {
               this.tickCounter = 0;
               class_1735 focusedSlot = this.getFocusedSlot(handledScreen);
               if (focusedSlot != null && focusedSlot.method_7681() && focusedSlot.method_7677().method_31574(class_1802.field_8288)) {
                  int slotId = focusedSlot.field_7874;
                  client.field_1761.method_2906(handledScreen.method_17577().field_7763, slotId, 0, class_1713.field_7790, client.field_1724);
                  client.field_1761.method_2906(handledScreen.method_17577().field_7763, 45, 0, class_1713.field_7790, client.field_1724);
                  client.field_1761.method_2906(handledScreen.method_17577().field_7763, slotId, 0, class_1713.field_7790, client.field_1724);
               }
            }
         }
      }
   }

   private class_1735 getFocusedSlot(class_465<?> screen) {
      return ((AbstractContainerScreenAccessor)screen).getHoveredSlot();
   }
}
