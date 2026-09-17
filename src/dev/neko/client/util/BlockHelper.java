package dev.neko.client.util;

import net.minecraft.class_1268;
import net.minecraft.class_1269;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_4969;

public final class BlockHelper {
   private BlockHelper() {
   }

   public static boolean isBlockAt(class_2338 pos, class_2248 block) {
      class_310 mc = class_310.method_1551();
      return mc.field_1687 != null && mc.field_1687.method_8320(pos).method_27852(block);
   }

   public static boolean isAnchorCharged(class_2338 pos) {
      class_310 mc = class_310.method_1551();
      return isBlockAt(pos, class_2246.field_23152) && (Integer)mc.field_1687.method_8320(pos).method_11654(class_4969.field_23153) != 0;
   }

   public static boolean isAnchorUncharged(class_2338 pos) {
      class_310 mc = class_310.method_1551();
      return isBlockAt(pos, class_2246.field_23152) && (Integer)mc.field_1687.method_8320(pos).method_11654(class_4969.field_23153) == 0;
   }

   public static void interactBlock(class_3965 hit, boolean swing) {
      interactBlock(hit, class_1268.field_5808, swing);
   }

   public static void interactBlock(class_3965 hit, class_1268 hand, boolean swing) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1761 != null) {
         class_1269 result = mc.field_1761.method_2896(mc.field_1724, hand, hit);
         if (result.method_23665() && swing) {
            mc.field_1724.method_6104(hand);
         }
      }
   }
}
