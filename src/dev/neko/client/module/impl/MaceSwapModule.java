package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.util.InventoryHelper;
import net.minecraft.class_1268;
import net.minecraft.class_1743;
import net.minecraft.class_1792;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;
import org.lwjgl.glfw.GLFW;

public class MaceSwapModule extends Module {
   public final BooleanSetting windBurst = this.addSetting(new BooleanSetting("Wind Burst", "", true));
   public final BooleanSetting breach = this.addSetting(new BooleanSetting("Breach", "", true));
   public final BooleanSetting onlySword = this.addSetting(new BooleanSetting("Only Sword", "", false));
   public final BooleanSetting onlyAxe = this.addSetting(new BooleanSetting("Only Axe", "", false));
   public final BooleanSetting switchBack = this.addSetting(new BooleanSetting("Switch Back", "", true));
   public final SliderSetting switchDelay = this.addSetting(new SliderSetting("Switch Delay", "", 0.0, 0.0, 20.0, 1.0));
   private int previousSlot = -1;
   private int tickCounter;
   private boolean waitingToSwapBack;
   private boolean attackedThisTick;

   public MaceSwapModule() {
      super("Mace Swap", "Switches to mace when attacking", Category.COMBAT);
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
            if (this.tickCounter >= this.switchDelay.getInt()) {
               InventoryHelper.swap(this.previousSlot);
               this.resetState();
            }
         } else {
            long handle = client.method_22683().method_4490();
            boolean lmb = GLFW.glfwGetMouseButton(handle, 0) == 1;
            if (lmb
               && client.field_1755 == null
               && client.field_1765 != null
               && client.field_1765.method_17783() == class_240.field_1331
               && !(client.field_1724.method_7261(0.5F) < 1.0F)
               && (!this.onlySword.get() || this.isSword(client.field_1724.method_6047().method_7909()))
               && (!this.onlyAxe.get() || client.field_1724.method_6047().method_7909() instanceof class_1743)
               && !client.field_1724.method_6047().method_31574(class_1802.field_49814)
               && InventoryHelper.getHotbarSlot(class_1802.field_49814) != -1) {
               this.previousSlot = client.field_1724.method_31548().method_67532();
               InventoryHelper.swapToItem(class_1802.field_49814);
               class_3966 entityHit = (class_3966)client.field_1765;
               client.field_1761.method_2918(client.field_1724, entityHit.method_17782());
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

   private boolean isSword(class_1792 item) {
      return item == class_1802.field_8091
         || item == class_1802.field_8528
         || item == class_1802.field_8371
         || item == class_1802.field_8845
         || item == class_1802.field_8802
         || item == class_1802.field_22022;
   }

   private void resetState() {
      this.previousSlot = -1;
      this.tickCounter = 0;
      this.waitingToSwapBack = false;
      this.attackedThisTick = false;
   }
}
