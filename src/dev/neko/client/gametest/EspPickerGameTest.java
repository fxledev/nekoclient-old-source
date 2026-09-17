package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.gui.IconPickerScreen;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.impl.BlockEntityEspModule;
import dev.neko.client.render.BlockEspRenderer;
import dev.neko.client.render.StorageEspRenderer;
import dev.neko.client.settings.IconListSetting;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_310;
import net.minecraft.class_437;

public class EspPickerGameTest implements FabricClientGameTest {
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
            require(m.blockEntityEsp != null, "BlockEntityESP registered");
            require(m.storageEsp != null, "StorageESP registered");
            require(m.blockEntityEsp.blockEntities.size() > 15, "BlockEntityESP seeded its type list");
            require(m.storageEsp.containers.size() == 8, "StorageESP seeded its 8 container groups");
         });
         server.runCommand("fill -8 0 -2 8 0 12 minecraft:stone");
         server.runCommand("setblock 0 1 5 minecraft:chest");
         server.runCommand("setblock 2 1 5 minecraft:trapped_chest");
         server.runCommand("setblock 4 1 5 minecraft:ender_chest");
         server.runCommand("setblock -2 1 5 minecraft:barrel");
         server.runCommand("setblock -4 1 5 minecraft:shulker_box");
         server.runCommand("setblock 6 1 5 minecraft:furnace");
         server.runCommand("setblock -6 1 5 minecraft:hopper");
         server.runCommand("setblock 0 1 8 minecraft:spawner");
         server.runCommand("setblock 2 1 8 minecraft:oak_sign");
         server.runCommand("summon minecraft:zombie 3 1 8 {NoAI:1b}");
         server.runCommand("tp @a 0 3 -2 0 18");
         context.waitTicks(20);
         context.runOnClient(mc -> {
            BlockEntityEspModule be = NekoClient.modules().blockEntityEsp;
            be.mode.set("Full");
            be.setEnabled(false);
            be.setEnabled(true);
         });
         context.waitTicks(6);
         context.runOnClient(mc -> {
            int n = NekoClient.modules().blockEntityEsp.cachedCount();
            require(n >= 6, "BlockEntityESP cached block entities on enable, got " + n);
         });
         context.takeScreenshot("be-esp-boxes");
         context.runOnClient(mc -> NekoClient.modules().blockEntityEsp.tracers.set(true));
         context.waitTicks(3);
         context.takeScreenshot("be-esp-tracers");
         server.runCommand("setblock 6 1 8 minecraft:diamond_ore");
         context.runOnClient(mc -> {
            ModuleManager m = NekoClient.modules();
            m.storageEsp.setEnabled(false);
            m.storageEsp.setEnabled(true);
            m.blockEsp.setEnabled(false);
            m.blockEsp.setEnabled(true);
         });
         context.waitTicks(30);
         context.runOnClient(mc -> {
            int s = StorageEspRenderer.cachedCount();
            int b = BlockEspRenderer.cachedCount();
            require(s >= 6, "StorageESP incremental scan found containers, got " + s);
            require(b >= 1, "BlockESP incremental scan found the diamond ore, got " + b);
         });
         context.takeScreenshot("storage-blockesp-incremental");
         context.runOnClient(mc -> {
            NekoClient.modules().storageEsp.setEnabled(false);
            NekoClient.modules().blockEsp.setEnabled(false);
         });
         context.runOnClient(mc -> {
            NekoClient.modules().blockEntityEsp.clear();
            require(NekoClient.modules().blockEntityEsp.cachedCount() == 0, "cache cleared");
         });
         server.runCommand("tp @a 3000 -60 3000");
         context.waitTicks(40);
         server.runCommand("tp @a 0 3 -2 0 18");
         context.waitTicks(50);
         context.runOnClient(mc -> {
            int n = NekoClient.modules().blockEntityEsp.cachedCount();
            require(n >= 6, "raw chunk packet refilled BlockEntityESP cache, got " + n);
         });
         context.takeScreenshot("be-esp-after-reload");
         context.runOnClient(mc -> {
            ClickGuiScreen.state().setExpanded("Block Entity Debug@RENDER", true);
            mc.method_1507(new ClickGuiScreen());
         });
         context.waitTicks(3);
         context.takeScreenshot("be-esp-settings");
         openPicker(context, mc -> NekoClient.modules().blockEntityEsp.blockEntities);
         context.takeScreenshot("be-esp-picker-grid");
         context.runOnClient(mc -> {
            if (mc.field_1755 instanceof IconPickerScreen p) {
               p.debugSetSearch("chest");
            }
         });
         context.waitTicks(4);
         context.takeScreenshot("be-esp-picker-search");
         context.runOnClient(mc -> {
            if (mc.field_1755 instanceof IconPickerScreen p) {
               p.debugSetSearch("");
            }
         });
         context.waitTicks(4);
         context.runOnClient(mc -> {
            if (mc.field_1755 instanceof IconPickerScreen p) {
               p.debugOpenColor(0);
            }
         });
         context.waitTicks(4);
         context.takeScreenshot("be-esp-picker-color");
         context.runOnClient(mc -> mc.method_1507(new ClickGuiScreen()));
         context.waitTicks(2);
         openPicker(context, mc -> NekoClient.modules().storageEsp.containers);
         context.takeScreenshot("storage-esp-picker-grid");
         context.runOnClient(mc -> {
            if (mc.field_1755 instanceof IconPickerScreen var2x) {
               ;
            }
         });
         context.waitTicks(4);
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

   private static void openPicker(ClientGameTestContext context, Function<class_310, IconListSetting> pick) {
      context.runOnClient(mc -> {
         if (mc.field_1755 instanceof ClickGuiScreen cg) {
            cg.openIconPicker(pick.apply(mc));
         }
      });
      context.waitForScreen(IconPickerScreen.class);
      context.waitTicks(6);
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: " + what);
      }
   }
}
