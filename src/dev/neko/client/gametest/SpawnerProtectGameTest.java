package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.module.impl.SpawnerProtectModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_2338;
import net.minecraft.class_437;

public class SpawnerProtectGameTest implements FabricClientGameTest {
   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         TestServerContext server = world.getServer();
         context.runOnClient(mc -> {
            SpawnerProtectModule sp = NekoClient.modules().spawnerProtect;
            require(sp != null, "SpawnerProtect registered");
            require(sp.targetStackCount.getInt() == 3, "default Stacks To Deposit = 3");
            require(sp.scanRange.get() == 64.0, "default Trigger Range = 64");
            require(sp.breakRange.get() == 5.5, "default Break Range = 5.5");
            require(sp.detectBlockUpdates.get(), "Detect Block Updates on by default");
            require(!sp.isEnabled(), "starts disabled");
         });
         server.runCommand("fill 100 0 -25 200 0 25 minecraft:stone");
         server.runCommand("setblock 152 1 0 minecraft:spawner");
         server.runCommand("setblock 148 1 0 minecraft:spawner");
         server.runCommand("setblock 150 2 0 minecraft:spawner");
         server.runCommand("setblock 150 1 3 minecraft:ender_chest");
         server.runCommand("tp @a 150 1 0");
         context.waitTicks(40);
         world.getClientWorld().waitForChunksRender();
         context.takeScreenshot("spawnerprotect-scene");
         context.runOnClient(mc -> {
            SpawnerProtectModule sp = NekoClient.modules().spawnerProtect;
            sp.setEnabled(true);
            require(!sp.isTriggered(), "arms un-triggered");
            require(sp.phase().equals("WAITING_FOR_STRANGER"), "starts waiting, got " + sp.phase());
            sp.onBlockDestructionPacket(Integer.MAX_VALUE, new class_2338(170, 1, 0));
            require(sp.isTriggered(), "break packet detected a stranger");
            require(sp.phase().equals("WORKING"), "advanced to WORKING, got " + sp.phase());
            sp.onTick();
            require(mc.field_1690.field_1832.method_1434(), "holds the sneak key while working");
         });
         context.takeScreenshot("spawnerprotect-working");
         context.runOnClient(mc -> {
            SpawnerProtectModule sp = NekoClient.modules().spawnerProtect;
            sp.setEnabled(false);
            require(!mc.field_1690.field_1832.method_1434(), "releases sneak key on disable");
            require(!mc.field_1690.field_1894.method_1434(), "releases forward key on disable");
         });
         server.runCommand("tp @a 0 100 0");
         context.waitTicks(10);
         context.runOnClient(mc -> {
            SpawnerProtectModule sp = NekoClient.modules().spawnerProtect;
            require(Math.abs(mc.field_1724.method_23317()) < 100.0 && Math.abs(mc.field_1724.method_23321()) < 100.0, "player is at spawn");
            sp.setEnabled(true);
            sp.onBlockDestructionPacket(Integer.MAX_VALUE, new class_2338(20, 100, 0));
            require(!sp.isTriggered(), "detection suppressed inside the spawn safe-zone");
            sp.setEnabled(false);
         });
         context.runOnClient(mc -> mc.method_1507(new ClickGuiScreen()));
         context.waitTicks(3);
         context.takeScreenshot("spawnerprotect-clickgui");
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
