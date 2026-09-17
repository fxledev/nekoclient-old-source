package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.KeybindSetting;
import dev.neko.client.settings.SliderSetting;
import net.minecraft.class_10192;
import net.minecraft.class_1304;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_9334;
import org.lwjgl.glfw.GLFW;

public class ElytraSwapModule extends Module {
   public final KeybindSetting activateKey = this.addSetting(new KeybindSetting("Activate Key", "", 71));
   public final SliderSetting swapDelay = this.addSetting(new SliderSetting("Swap Delay", "", 0.0, 0.0, 20.0, 1.0));
   public final BooleanSetting switchBack = this.addSetting(new BooleanSetting("Switch Back", "", true));
   public final SliderSetting switchDelay = this.addSetting(new SliderSetting("Switch Delay", "", 0.0, 0.0, 20.0, 1.0));
   public final BooleanSetting moveToSlot = this.addSetting(new BooleanSetting("Move To Slot", "", true));
   public final SliderSetting elytraSlot = this.addSetting(new SliderSetting("Elytra Slot", "", 9.0, 1.0, 9.0, 1.0));
   private boolean keyWasDown;
   private boolean swappedToElytra;
   private int tickCounter;
   private boolean waitingForSwap;
   private boolean waitingForSwitchBack;

   public ElytraSwapModule() {
      super("Elytra Swap", "Swap between Elytra and Chestplate with a keybind", Category.COMBAT);
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
         if (this.waitingForSwap) {
            this.tickCounter++;
            if (this.tickCounter >= this.swapDelay.getInt()) {
               this.performSwap(client);
               this.waitingForSwap = false;
               if (this.switchBack.get()) {
                  this.waitingForSwitchBack = true;
                  this.tickCounter = 0;
               }
            }
         } else if (this.waitingForSwitchBack) {
            this.tickCounter++;
            if (this.tickCounter >= this.switchDelay.getInt()) {
               this.performSwap(client);
               this.waitingForSwitchBack = false;
            }
         } else {
            int key = this.activateKey.get();
            if (key != -1) {
               long handle = client.method_22683().method_4490();
               boolean pressed = key <= 7 ? GLFW.glfwGetMouseButton(handle, key) == 1 : GLFW.glfwGetKey(handle, key) == 1;
               if (pressed && !this.keyWasDown) {
                  this.waitingForSwap = true;
                  this.tickCounter = 0;
               }

               this.keyWasDown = pressed;
            }
         }
      }
   }

   private void performSwap(class_310 client) {
      class_1799 chestSlot = client.field_1724.method_6118(class_1304.field_6174);
      boolean wearingElytra = chestSlot.method_31574(class_1802.field_8833);
      int targetSlot = -1;
      if (wearingElytra) {
         for (int i = 0; i < 36; i++) {
            class_1799 stack = client.field_1724.method_31548().method_5438(i);
            class_10192 equippable = (class_10192)stack.method_58694(class_9334.field_54196);
            if (equippable != null && equippable.comp_3174() == class_1304.field_6174 && !stack.method_31574(class_1802.field_8833)) {
               targetSlot = i;
               break;
            }
         }
      } else {
         if (this.moveToSlot.get()) {
            int slotIdx = this.elytraSlot.getInt() - 1;
            class_1799 stack = client.field_1724.method_31548().method_5438(slotIdx);
            if (stack.method_31574(class_1802.field_8833)) {
               targetSlot = slotIdx;
            }
         }

         if (targetSlot == -1) {
            for (int ix = 0; ix < 36; ix++) {
               if (client.field_1724.method_31548().method_5438(ix).method_31574(class_1802.field_8833)) {
                  targetSlot = ix;
                  break;
               }
            }
         }
      }

      if (targetSlot != -1) {
         int screenSlot = targetSlot < 9 ? targetSlot + 36 : targetSlot;
         int armorScreenSlot = 6;
         client.field_1761.method_2906(client.field_1724.field_7512.field_7763, screenSlot, 0, class_1713.field_7790, client.field_1724);
         client.field_1761.method_2906(client.field_1724.field_7512.field_7763, armorScreenSlot, 0, class_1713.field_7790, client.field_1724);
         client.field_1761.method_2906(client.field_1724.field_7512.field_7763, screenSlot, 0, class_1713.field_7790, client.field_1724);
         this.swappedToElytra = !wearingElytra;
      }
   }

   private void resetState() {
      this.keyWasDown = false;
      this.swappedToElytra = false;
      this.tickCounter = 0;
      this.waitingForSwap = false;
      this.waitingForSwitchBack = false;
   }
}
