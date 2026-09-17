package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.util.InventoryHelper;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;

public class AutoToolModule extends Module {
   public AutoToolModule() {
      super("AutoTool", "Automatically selects the best hotbar tool for the block being mined", Category.MISC);
   }

   @Override
   public void onTick() {
      class_310 client = class_310.method_1551();
      class_746 player = client.field_1724;
      if (player != null
         && client.field_1687 != null
         && client.field_1761 != null
         && client.field_1690.field_1886.method_1434()
         && client.field_1765 != null
         && client.field_1765.method_17783() == class_240.field_1332) {
         class_2338 blockPos = ((class_3965)client.field_1765).method_17777();
         float currentSpeed = speed(player.method_6047(), client.field_1687.method_8320(blockPos));
         int bestSlot = player.method_31548().method_67532();
         float bestSpeed = currentSpeed;

         for (int slot = 0; slot < 9; slot++) {
            float candidateSpeed = speed(player.method_31548().method_5438(slot), client.field_1687.method_8320(blockPos));
            if (candidateSpeed > bestSpeed) {
               bestSpeed = candidateSpeed;
               bestSlot = slot;
            }
         }

         if (bestSlot != player.method_31548().method_67532()) {
            InventoryHelper.selectHotbarSlot(bestSlot);
         }
      }
   }

   private static float speed(class_1799 stack, class_2680 state) {
      return !stack.method_7960() && stack.method_7951(state) ? stack.method_7924(state) : 0.0F;
   }
}
