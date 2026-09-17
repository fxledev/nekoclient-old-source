package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.ModeSetting;
import dev.neko.client.settings.SliderSetting;
import dev.neko.client.util.InventoryHelper;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_3966;
import net.minecraft.class_239.class_240;

public class MaceBomberModule extends Module {
   public final SliderSetting height = this.addSetting(new SliderSetting("Height", "Minimum fall distance before it smashes", 8.0, 3.0, 30.0, 1.0, "m"));
   public final ModeSetting mode = this.addSetting(new ModeSetting("Mode", "Dive style", "Direct", "Direct", "Spiral", "Delayed"));
   public final BooleanSetting autoMace = this.addSetting(new BooleanSetting("Auto Mace", "Swap to a mace in your hotbar before smashing", true));
   public final BooleanSetting switchBack = this.addSetting(new BooleanSetting("Switch Back", "Return to the previous slot after the smash", false));
   private int spiralDir = 1;
   private int spiralTimer;
   private int cooldown;
   private int returnSlot = -1;
   private boolean strafing;

   public MaceBomberModule() {
      super("Mace Bomber", "Times a fully-charged mace smash on the way down", Category.COMBAT);
   }

   @Override
   protected void onDisable() {
      this.releaseStrafe();
      this.cooldown = 0;
      this.returnSlot = -1;
   }

   @Override
   public void onTick() {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null && client.field_1761 != null && client.field_1755 == null) {
         if (this.cooldown > 0) {
            this.cooldown--;
         }

         if (client.field_1724.method_24828()) {
            this.releaseStrafe();
            this.cooldown = 0;
            this.returnSlot = -1;
         } else {
            class_1309 target = this.crosshairTarget(client);
            if (target == null) {
               this.releaseStrafe();
            } else {
               if (this.mode.is("Spiral")) {
                  this.spiral(client);
               } else {
                  this.releaseStrafe();
               }

               if (this.cooldown <= 0
                  && !(client.field_1724.field_6017 < this.height.getFloat())
                  && (!this.mode.is("Delayed") || !(client.field_1724.method_18798().field_1351 > -0.5))) {
                  if (!client.field_1724.method_6047().method_31574(class_1802.field_49814)) {
                     if (!this.autoMace.get()) {
                        return;
                     }

                     if (this.returnSlot < 0) {
                        this.returnSlot = client.field_1724.method_31548().method_67532();
                     }

                     if (!InventoryHelper.swapToItem(class_1802.field_49814)) {
                        return;
                     }
                  }

                  if (!(client.field_1724.method_7261(0.5F) < 1.0F)) {
                     client.field_1761.method_2918(client.field_1724, target);
                     client.field_1724.method_6104(class_1268.field_5808);
                     this.cooldown = 6;
                     if (this.switchBack.get() && this.returnSlot >= 0) {
                        InventoryHelper.swap(this.returnSlot);
                     }

                     this.returnSlot = -1;
                  }
               }
            }
         }
      } else {
         this.releaseStrafe();
      }
   }

   private class_1309 crosshairTarget(class_310 client) {
      if (client.field_1765 != null && client.field_1765.method_17783() == class_240.field_1331) {
         if (!(((class_3966)client.field_1765).method_17782() instanceof class_1309 living)) {
            return null;
         } else {
            return living != client.field_1724 && living.method_5805() ? living : null;
         }
      } else {
         return null;
      }
   }

   private void spiral(class_310 client) {
      if (--this.spiralTimer <= 0) {
         this.spiralDir = -this.spiralDir;
         this.spiralTimer = 6 + client.field_1724.field_6012 % 7;
      }

      client.field_1690.field_1913.method_23481(this.spiralDir < 0);
      client.field_1690.field_1849.method_23481(this.spiralDir > 0);
      this.strafing = true;
   }

   private void releaseStrafe() {
      if (this.strafing) {
         class_310 client = class_310.method_1551();
         if (client.field_1690 != null) {
            client.field_1690.field_1913.method_23481(false);
            client.field_1690.field_1849.method_23481(false);
         }

         this.strafing = false;
      }
   }
}
