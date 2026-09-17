package dev.neko.client.util;

import java.util.function.Predicate;
import net.minecraft.class_1268;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2846;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_2846.class_2847;

public final class InventoryHelper {
   private InventoryHelper() {
   }

   private static class_310 mc() {
      return class_310.method_1551();
   }

   public static int toScreenSlot(int invIndex) {
      return invIndex < 9 ? invIndex + 36 : invIndex;
   }

   public static void selectHotbarSlot(int slot) {
      class_310 mc = mc();
      if (slot >= 0 && slot <= 8 && mc.field_1724 != null && mc.field_1724.method_31548().method_67532() != slot) {
         mc.field_1724.method_31548().method_61496(slot);
         if (mc.method_1562() != null) {
            mc.method_1562().method_52787(new class_2868(slot));
         }
      }
   }

   public static void swapOffhand() {
      class_310 mc = mc();
      if (mc.field_1724 != null && mc.method_1562() != null) {
         mc.method_1562().method_52787(new class_2846(class_2847.field_12969, class_2338.field_10980, class_2350.field_11033));
      }
   }

   public static void swapInventoryToHotbar(int invIndex, int hotbarIndex) {
      class_310 mc = mc();
      if (mc.field_1724 != null && mc.field_1761 != null && invIndex >= 9 && invIndex <= 35 && hotbarIndex >= 0 && hotbarIndex <= 8) {
         mc.field_1761.method_2906(mc.field_1724.field_7512.field_7763, toScreenSlot(invIndex), hotbarIndex, class_1713.field_7791, mc.field_1724);
      }
   }

   public static void swap(int slot) {
      selectHotbarSlot(slot);
   }

   public static class_1268 handHolding(class_1792 item) {
      class_746 player = mc().field_1724;
      if (player == null) {
         return null;
      } else if (player.method_6047().method_31574(item)) {
         return class_1268.field_5808;
      } else {
         return player.method_6079().method_31574(item) ? class_1268.field_5810 : null;
      }
   }

   public static int getHotbarSlot(class_1792 item) {
      class_746 player = mc().field_1724;
      if (player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (player.method_31548().method_5438(i).method_31574(item)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static int findItemSlot(class_1792 item) {
      class_746 player = mc().field_1724;
      if (player == null) {
         return -1;
      } else {
         for (int i = 0; i < 36; i++) {
            if (player.method_31548().method_5438(i).method_31574(item)) {
               return i;
            }
         }

         return -1;
      }
   }

   public static boolean swapToItem(class_1792 item) {
      int slot = getHotbarSlot(item);
      if (slot < 0) {
         return false;
      } else {
         selectHotbarSlot(slot);
         return true;
      }
   }

   public static boolean swapToStack(Predicate<class_1799> predicate) {
      class_746 player = mc().field_1724;
      if (player == null) {
         return false;
      } else {
         for (int i = 0; i < 9; i++) {
            if (predicate.test(player.method_31548().method_5438(i))) {
               selectHotbarSlot(i);
               return true;
            }
         }

         return false;
      }
   }

   public static int findEmptyHotbarSlot() {
      class_746 player = mc().field_1724;
      if (player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (player.method_31548().method_5438(i).method_7960()) {
               return i;
            }
         }

         return -1;
      }
   }
}
