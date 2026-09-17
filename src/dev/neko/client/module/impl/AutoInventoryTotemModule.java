package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.SliderSetting;
import net.minecraft.class_1713;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_490;

public class AutoInventoryTotemModule extends Module {
   public final SliderSetting delay = this.addSetting(new SliderSetting("Delay", "", 4.0, 1.0, 40.0, 1.0));
   public final BooleanSetting forceTotem = this.addSetting(new BooleanSetting("Force Totem", "", false));
   public final BooleanSetting hotbarTotem = this.addSetting(new BooleanSetting("Hotbar Totem", "", false));
   public final SliderSetting hotbarSlot = this.addSetting(new SliderSetting("Hotbar Slot", "", 1.0, 1.0, 9.0, 1.0));
   public final SliderSetting hotbarDelay = this.addSetting(new SliderSetting("Hotbar Delay", "", 4.0, 1.0, 40.0, 1.0));
   private int tickCounter;

   public AutoInventoryTotemModule() {
      super("Inv Totem", "Moves totems only while inventory (E) is open — click-based, not world-auto", Category.COMBAT);
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
      if (client.field_1724 != null && client.field_1761 != null && client.field_1755 instanceof class_490) {
         this.tickCounter++;
         int wait = this.hotbarTotem.get() && this.needsHotbarWork(client) && !this.needsOffhandWork(client) ? this.hotbarDelay.getInt() : this.delay.getInt();
         if (this.tickCounter >= wait) {
            this.tickCounter = 0;
            if (this.hotbarTotem.get() && this.needsHotbarWork(client)) {
               int source = findTotemInMainInventory(client);
               if (source != -1) {
                  int hotbarIndex = this.hotbarSlot.getInt() - 1;
                  if (hotbarIndex >= 0 && hotbarIndex <= 8) {
                     this.performHotbarSwap(client, source, hotbarIndex);
                  }
               }
            } else if (this.needsOffhandWork(client)) {
               int totemIdx = findTotemForOffhand(client);
               if (totemIdx != -1) {
                  this.performOffhandSwap(client, totemIdx);
               }
            }
         }
      }
   }

   private boolean needsOffhandWork(class_310 client) {
      boolean hasTotemOffhand = client.field_1724.method_6079().method_31574(class_1802.field_8288);
      return this.forceTotem.get() || !hasTotemOffhand;
   }

   private boolean needsHotbarWork(class_310 client) {
      if (!this.hotbarTotem.get()) {
         return false;
      } else {
         int slot = this.hotbarSlot.getInt() - 1;
         return !client.field_1724.method_31548().method_5438(slot).method_31574(class_1802.field_8288);
      }
   }

   private static int findTotemInMainInventory(class_310 client) {
      for (int i = 9; i < 36; i++) {
         if (client.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8288)) {
            return i;
         }
      }

      return -1;
   }

   private static int findTotemInHotbar(class_310 client) {
      for (int i = 0; i < 9; i++) {
         if (client.field_1724.method_31548().method_5438(i).method_31574(class_1802.field_8288)) {
            return i;
         }
      }

      return -1;
   }

   private static int findTotemForOffhand(class_310 client) {
      int main = findTotemInMainInventory(client);
      return main != -1 ? main : findTotemInHotbar(client);
   }

   private static int toScreenSlot(int invIndex) {
      return invIndex < 9 ? invIndex + 36 : invIndex;
   }

   private void performOffhandSwap(class_310 client, int totemInvIndex) {
      int syncId = client.field_1724.field_7512.field_7763;
      int screenSlot = toScreenSlot(totemInvIndex);
      client.field_1761.method_2906(syncId, screenSlot, 0, class_1713.field_7790, client.field_1724);
      client.field_1761.method_2906(syncId, 45, 0, class_1713.field_7790, client.field_1724);
      client.field_1761.method_2906(syncId, screenSlot, 0, class_1713.field_7790, client.field_1724);
   }

   private void performHotbarSwap(class_310 client, int sourceInvIndex, int hotbarIndex) {
      int syncId = client.field_1724.field_7512.field_7763;
      client.field_1761.method_2906(syncId, sourceInvIndex, hotbarIndex, class_1713.field_7791, client.field_1724);
   }
}
