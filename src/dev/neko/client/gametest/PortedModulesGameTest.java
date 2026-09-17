package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.impl.FreeLookModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_437;
import net.minecraft.class_5498;

public class PortedModulesGameTest implements FabricClientGameTest {
   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         TestServerContext server = world.getServer();
         context.runOnClient(mc -> {
            ModuleManager m = NekoClient.modules();
            require(m.freeLook != null, "FreeLook registered");
         });
         server.runCommand("fill -45 0 -45 45 0 45 minecraft:stone");
         server.runCommand("fill 6 0 6 22 0 22 minecraft:red_wool");
         server.runCommand("fill -22 0 6 -6 0 22 minecraft:blue_wool");
         server.runCommand("fill 6 0 -22 22 0 -6 minecraft:yellow_wool");
         server.runCommand("fill -22 0 -22 -6 0 -6 minecraft:water");
         server.runCommand("fill -4 0 -30 4 0 -24 minecraft:lime_wool");
         server.runCommand("fill -4 0 24 4 0 30 minecraft:white_wool");
         server.runCommand("fill 24 1 -4 30 3 4 minecraft:gold_block");
         server.runCommand("fill -30 1 -4 -24 3 4 minecraft:diamond_block");
         server.runCommand("tp @a 0 1 0");
         context.waitTicks(10);
         context.runOnClient(mc -> {
            NekoClient.modules().freeLook.setEnabled(true);
            require(mc.field_1690.method_31044() == class_5498.field_26665, "FreeLook switched to third person");
         });
         context.waitTicks(3);
         context.runOnClient(mc -> NekoClient.modules().freeLook.setEnabled(false));
         server.runCommand("fill -2 1 -2 2 4 -2 minecraft:stone");
         server.runCommand("fill -2 1 2 2 4 2 minecraft:stone");
         server.runCommand("fill -2 1 -2 -2 4 2 minecraft:stone");
         server.runCommand("fill 2 1 -2 2 4 2 minecraft:stone");
         server.runCommand("tp @a 0 1 0");
         context.waitTicks(5);
         double[] wallOff = new double[1];
         context.runOnClient(mc -> {
            FreeLookModule fl = NekoClient.modules().freeLook;
            mc.field_1724.method_36457(0.0F);
            fl.setEnabled(true);
         });
         context.waitTicks(5);
         context.runOnClient(mc -> wallOff[0] = mc.field_1773.method_19418().method_71156().method_1022(mc.field_1724.method_5836(1.0F)));
         context.takeScreenshot("freelook-camera");
         context.runOnClient(mc -> {
            require(wallOff[0] < 2.5, "FreeLook camera clipped to the wall, got " + wallOff[0]);
            NekoClient.modules().freeLook.setEnabled(false);
         });
         server.runCommand("fill -2 1 -2 2 4 2 minecraft:air");
         server.runCommand("tp @a 0 1 0");
         context.waitTicks(3);
         context.runOnClient(mc -> mc.method_1507(new ClickGuiScreen()));
         context.waitTicks(3);
         context.takeScreenshot("clickgui-open");
         context.runOnClient(mc -> mc.method_1507((class_437)null));
         context.waitTicks(2);
      } catch (Throwable var61) {
         if (world != null) {
            try {
               world.close();
            } catch (Throwable var5) {
               var61.addSuppressed(var5);
            }
         }

         throw var61;
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
