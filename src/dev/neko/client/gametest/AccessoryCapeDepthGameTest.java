package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.module.impl.CustomAccessoriesModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_5498;

public class AccessoryCapeDepthGameTest implements FabricClientGameTest {
   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         world.getServer().runCommand("tp @a 500 -60 500 -90 0");
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> {
            NekoClient.modules().susChunkFinder.setEnabled(false);
            NekoClient.modules().motionBlur.setEnabled(false);
            if (mc.field_1724 != null) {
               mc.field_1724.method_36456(-90.0F);
               mc.field_1724.method_5636(-90.0F);
               mc.field_1724.method_36457(0.0F);
            }

            CustomAccessoriesModule acc = NekoClient.modules().customAccessories;
            require(acc != null, "CustomAccessories registered");
            acc.firstPerson.set(false);
            acc.color.set(-49508);
            acc.rainbow.set(false);
            acc.glow.set(75.0);
            acc.cape.set(true);
            acc.capeStyle.set("Solid");
            acc.capePhysics.set(false);
            acc.trail.set(false);
            acc.aura.set(false);
            acc.crown.set(false);
            acc.setEnabled(true);
         });
         context.waitTicks(20);
         context.runOnClient(mc -> mc.field_1690.method_31043(class_5498.field_26665));
         context.waitTicks(6);
         context.takeScreenshot("cape-depth-1-solid-back");
         context.runOnClient(mc -> mc.field_1690.method_31043(class_5498.field_26666));
         context.waitTicks(6);
         context.takeScreenshot("cape-depth-2-solid-front");
         context.runOnClient(mc -> NekoClient.modules().customAccessories.capeStyle.set("67"));
         context.waitTicks(4);
         context.runOnClient(mc -> mc.field_1690.method_31043(class_5498.field_26665));
         context.waitTicks(6);
         context.takeScreenshot("cape-depth-3-67-back");
         context.runOnClient(mc -> mc.field_1690.method_31043(class_5498.field_26666));
         context.waitTicks(6);
         context.takeScreenshot("cape-depth-4-67-front");
         context.runOnClient(mc -> {
            CustomAccessoriesModule acc = NekoClient.modules().customAccessories;
            acc.cape.set(false);
            acc.aura.set(true);
            acc.auraStyle.set("Orbit");
            acc.firstPerson.set(false);
            mc.field_1690.method_31043(class_5498.field_26664);
            if (mc.field_1724 != null) {
               mc.field_1724.method_36457(60.0F);
            }
         });
         context.waitTicks(6);
         context.takeScreenshot("cape-depth-5-firstperson-aura");
         context.runOnClient(mc -> {
            CustomAccessoriesModule acc = NekoClient.modules().customAccessories;
            acc.firstPerson.set(false);
            acc.aura.set(false);
            acc.cape.set(true);
            acc.capeStyle.set("67");
            acc.capePhysics.set(true);
            acc.setEnabled(false);
            mc.field_1690.method_31043(class_5498.field_26664);
            if (mc.field_1724 != null) {
               mc.field_1724.method_36457(0.0F);
            }
         });
      } catch (Throwable var6) {
         if (world != null) {
            try {
               world.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (world != null) {
         world.close();
      }
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: " + what);
      }
   }
}
