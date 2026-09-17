package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.KeybindSetting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.util.BlockHelper;
import dev.neko.client.util.InventoryHelper;
import java.util.List;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3675;
import net.minecraft.class_3965;
import net.minecraft.class_3966;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class AutoCrystalModule extends Module {
   public final KeybindSetting activateKey = this.addSetting(new KeybindSetting("Activate Key", "Hold to place + break (default RMB)", 1));
   public final SliderSetting breakDelay = this.addSetting(new SliderSetting("Break Delay", "Ticks between crystal breaks", 1.0, 0.0, 20.0, 1.0, "t"));
   public final SliderSetting placeDelay = this.addSetting(new SliderSetting("Place Delay", "Ticks between crystal places", 1.0, 0.0, 20.0, 1.0, "t"));
   public final SliderSetting range = this.addSetting(
      new SliderSetting("Break Range", "Max distance to a crystal you'll break (vanilla reach ~3)", 3.0, 1.0, 6.0, 0.5, "m")
   );
   private int breakCooldown;
   private int placeCooldown;
   private int savedSlot = -1;
   private int lastBrokenId = -1;
   private boolean wasActive;

   public AutoCrystalModule() {
      super("Auto Crystal", "Hold RMB to auto place + break crystals on obsidian or bedrock", Category.COMBAT);
   }

   @Override
   protected void onEnable() {
      this.resetAll();
   }

   @Override
   protected void onDisable() {
      this.restoreSlot();
      this.resetAll();
   }

   @Override
   public void onTick() {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null && mc.field_1761 != null && mc.field_1687 != null && mc.field_1755 == null && this.isTriggerHeld(mc)) {
         this.wasActive = true;
         this.tickCooldowns();
         if (this.activateKey.get() == 1) {
            mc.field_1690.field_1904.method_23481(false);
         }

         class_239 hitResult = mc.field_1765;
         if (hitResult instanceof class_3966 ehr && ehr.method_17782() instanceof class_1511 crystal) {
            if (this.tryBreak(mc, crystal)) {
               this.lastBrokenId = crystal.method_5628();
            }

            return;
         }

         if (hitResult instanceof class_3965 hit && hit.method_17783() == class_240.field_1332) {
            class_2338 clicked = hit.method_17777();
            if (isCrystalBase(mc.field_1687, clicked)) {
               this.serviceBase(mc, hit, clicked);
            }
         }
      } else {
         this.endActiveHold();
      }
   }

   private void serviceBase(class_310 mc, class_3965 hit, class_2338 base) {
      class_1511 crystal = crystalOn(mc.field_1687, base);
      if (crystal != null) {
         if (this.tryBreak(mc, crystal)) {
            this.lastBrokenId = crystal.method_5628();
         }
      } else {
         this.lastBrokenId = -1;
         if (this.placeCooldown == 0 && canFitCrystal(mc.field_1687, base)) {
            class_1268 hand = this.equip(mc, class_1802.field_8301);
            if (hand != null) {
               BlockHelper.interactBlock(hit, hand, true);
               this.placeCooldown = this.placeDelay.getInt();
            }
         }
      }
   }

   private boolean tryBreak(class_310 mc, class_1511 crystal) {
      if (this.breakCooldown != 0) {
         return false;
      } else if (crystal.method_5628() == this.lastBrokenId) {
         return false;
      } else if (mc.field_1724.method_33571().method_1022(crystal.method_73189()) > this.range.get()) {
         return false;
      } else {
         mc.field_1761.method_2918(mc.field_1724, crystal);
         mc.field_1724.method_6104(class_1268.field_5808);
         this.breakCooldown = this.breakDelay.getInt();
         return true;
      }
   }

   @Nullable
   private class_1268 equip(class_310 mc, class_1792 item) {
      class_746 player = mc.field_1724;
      if (player.method_6047().method_31574(item)) {
         return class_1268.field_5808;
      } else if (player.method_6079().method_31574(item)) {
         return class_1268.field_5810;
      } else {
         int slot = InventoryHelper.getHotbarSlot(item);
         if (slot < 0) {
            return null;
         } else {
            if (this.savedSlot < 0) {
               this.savedSlot = player.method_31548().method_67532();
            }

            InventoryHelper.selectHotbarSlot(slot);
            return class_1268.field_5808;
         }
      }
   }

   private static boolean isCrystalBase(class_1937 level, class_2338 pos) {
      class_2680 state = level.method_8320(pos);
      return state.method_27852(class_2246.field_10540) || state.method_27852(class_2246.field_9987);
   }

   @Nullable
   private static class_1511 crystalOn(class_1937 level, class_2338 base) {
      class_2338 up = base.method_10084();
      class_238 region = new class_238(
         up.method_10263() + 0.125,
         up.method_10264() - 0.1,
         up.method_10260() + 0.125,
         up.method_10263() + 0.875,
         up.method_10264() + 2.5,
         up.method_10260() + 0.875
      );
      List<class_1511> found = level.method_8390(class_1511.class, region, e -> true);
      return found.isEmpty() ? null : found.get(0);
   }

   private static boolean canFitCrystal(class_1937 level, class_2338 base) {
      class_2338 up = base.method_10084();
      if (!level.method_8320(up).method_26215()) {
         return false;
      } else {
         class_238 box = new class_238(
            up.method_10263(), up.method_10264(), up.method_10260(), up.method_10263() + 1.0, up.method_10264() + 2.0, up.method_10260() + 1.0
         );
         return level.method_18467(class_1297.class, box).isEmpty();
      }
   }

   private void tickCooldowns() {
      if (this.breakCooldown > 0) {
         this.breakCooldown--;
      }

      if (this.placeCooldown > 0) {
         this.placeCooldown--;
      }
   }

   private boolean isTriggerHeld(class_310 mc) {
      int key = this.activateKey.get();
      if (key == -1) {
         return false;
      } else {
         return key <= 7 ? GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), key) == 1 : class_3675.method_15987(mc.method_22683(), key);
      }
   }

   private void endActiveHold() {
      if (this.wasActive) {
         this.restoreSlot();
         this.breakCooldown = 0;
         this.placeCooldown = 0;
         this.lastBrokenId = -1;
         this.wasActive = false;
      }
   }

   private void restoreSlot() {
      if (this.savedSlot >= 0) {
         InventoryHelper.selectHotbarSlot(this.savedSlot);
      }

      this.savedSlot = -1;
   }

   private void resetAll() {
      this.breakCooldown = 0;
      this.placeCooldown = 0;
      this.savedSlot = -1;
      this.lastBrokenId = -1;
      this.wasActive = false;
   }
}
