package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.ModeSetting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.util.InventoryHelper;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;

public class ShieldBreakerModule extends Module {
   public final BooleanSetting switchBack = this.addSetting(new BooleanSetting("Switch Back", "", true));
   public final ModeSetting axePriority = this.addSetting(new ModeSetting("Axe Priority", "", "Best", "Best", "Nearest"));
   public final SliderSetting switchDelay = this.addSetting(new SliderSetting("Switch Delay", "", 150.0, 0.0, 500.0, 10.0));
   private int previousSlot = -1;
   private int tickCounter;
   private boolean waitingToSwapBack;

   public ShieldBreakerModule() {
      super("Shield Breaker", "Auto-switch to axe when target is blocking", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.resetState();
   }

   @Override
   protected void onDisable() {
      this.resetState();
   }

   @Override
   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1761 != null) {
         if (this.waitingToSwapBack) {
            this.tickCounter++;
            int delayTicks = Math.max(1, this.switchDelay.getInt() / 50);
            if (this.tickCounter >= delayTicks) {
               InventoryHelper.swap(this.previousSlot);
               this.resetState();
            }
         } else if (client.field_1765 != null
            && client.field_1765.method_17783() == class_240.field_1331
            && ((class_3966)client.field_1765).method_17782() instanceof class_1657 targetPlayer
            && targetPlayer.method_6039()
            && !(client.field_1724.method_6047().method_7909() instanceof class_1743)) {
            int axeSlot = this.findAxeSlot(client);
            if (axeSlot != -1) {
               this.previousSlot = client.field_1724.method_31548().method_67532();
               InventoryHelper.swap(axeSlot);
               client.field_1761.method_2918(client.field_1724, targetPlayer);
               client.field_1724.method_6104(class_1268.field_5808);
               if (this.switchBack.get()) {
                  this.waitingToSwapBack = true;
                  this.tickCounter = 0;
               } else {
                  this.previousSlot = -1;
               }
            }
         }
      }
   }

   private int findAxeSlot(class_310 client) {
      if (this.axePriority.is("Nearest")) {
         for (int i = 0; i < 9; i++) {
            if (client.field_1724.method_31548().method_5438(i).method_7909() instanceof class_1743) {
               return i;
            }
         }

         return -1;
      } else {
         int bestSlot = -1;
         float bestDamage = -1.0F;

         for (int ix = 0; ix < 9; ix++) {
            class_1799 stack = client.field_1724.method_31548().method_5438(ix);
            if (stack.method_7909() instanceof class_1743) {
               float damage = this.getAxeTier(stack);
               if (damage > bestDamage) {
                  bestDamage = damage;
                  bestSlot = ix;
               }
            }
         }

         return bestSlot;
      }
   }

   private float getAxeTier(class_1799 stack) {
      if (stack.method_31574(class_1802.field_22025)) {
         return 5.0F;
      } else if (stack.method_31574(class_1802.field_8556)) {
         return 4.0F;
      } else if (stack.method_31574(class_1802.field_8475)) {
         return 3.0F;
      } else if (stack.method_31574(class_1802.field_8825)) {
         return 2.0F;
      } else if (stack.method_31574(class_1802.field_8062)) {
         return 1.0F;
      } else {
         return stack.method_31574(class_1802.field_8406) ? 0.0F : -1.0F;
      }
   }

   private void resetState() {
      this.previousSlot = -1;
      this.tickCounter = 0;
      this.waitingToSwapBack = false;
   }
}
