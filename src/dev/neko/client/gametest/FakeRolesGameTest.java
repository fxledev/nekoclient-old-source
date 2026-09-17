package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.module.impl.FakeRolesModule;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_2561;

public class FakeRolesGameTest implements FabricClientGameTest {
   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1280, 720);
         context.waitTicks(2);
         TestServerContext server = world.getServer();
         context.runOnClient(mc -> require(NekoClient.modules().fakeRoles != null, "FakeRoles registered"));
         context.runOnClient(
            mc -> {
               FakeRolesModule fr = NekoClient.modules().fakeRoles;
               fr.setEnabled(true);
               String me = mc.field_1724.method_7334().name();
               fr.role.set("SR.MOD");
               String srmod = fr.decorateChat(class_2561.method_43470(me + ": gg")).getString();
               require(srmod.equals("[SR.MOD] " + me + ": gg"), "SR.MOD chat, got: " + srmod);
               fr.role.set("MEDIA");
               require(fr.decorateChat(class_2561.method_43470(me + ": gg")).getString().startsWith("[MEDIA] "), "MEDIA chat tag");
               fr.role.set("SR.ADMIN");
               require(fr.decorateChat(class_2561.method_43470(me + ": gg")).getString().startsWith("[SR.ADMIN] "), "SR.ADMIN chat tag");
               fr.role.set("MEDIA");
               require(
                  fr.decorateChat(class_2561.method_43470("<" + me + "> hi")).getString().equals("<[MEDIA] " + me + "> hi"),
                  "tag spliced right before a mid-line name"
               );
               require(fr.decorateTab(class_2561.method_43470(me), me).getString().equals("[MEDIA] " + me), "tab decorated for self");
               require(fr.decorateTab(class_2561.method_43470("Notch"), "Notch").getString().equals("Notch"), "tab NOT decorated for others");
               fr.role.set("None");
               require(fr.decorateChat(class_2561.method_43470(me + ": gg")).getString().equals(me + ": gg"), "None role adds nothing");
            }
         );
         context.runOnClient(mc -> {
            FakeRolesModule fr = NekoClient.modules().fakeRoles;
            String me = mc.field_1724.method_7334().name();
            fr.role.set("SR.MOD");
            mc.field_1705.method_1743().method_1812(class_2561.method_43470(me + ": sr.mod flex"));
            fr.role.set("MEDIA");
            mc.field_1705.method_1743().method_1812(class_2561.method_43470(me + ": media flex"));
            fr.role.set("SR.ADMIN");
            mc.field_1705.method_1743().method_1812(class_2561.method_43470(me + ": sr.admin flex"));
         });
         context.waitTicks(4);
         context.takeScreenshot("fakeroles-chat-all-roles");
         server.runCommand("scoreboard objectives add tabinfo dummy {\"text\":\"Players\"}");
         server.runCommand("scoreboard objectives modify tabinfo numberformat blank");
         server.runCommand("scoreboard objectives setdisplay list tabinfo");
         context.waitTicks(3);
         shootTab(context, "SR.MOD", "fakeroles-tab-srmod-green");
         shootTab(context, "MEDIA", "fakeroles-tab-media-pink");
         shootTab(context, "SR.ADMIN", "fakeroles-tab-sradmin-red");
         context.runOnClient(mc -> mc.field_1690.field_1907.method_23481(false));
         context.runOnClient(mc -> {
            NekoClient.modules().fakeRoles.role.set("MEDIA");
            ClickGuiScreen.state().setExpanded("FakeRoles@MISC", true);
         });
         context.getInput().pressKey(344);
         context.waitForScreen(ClickGuiScreen.class);
         context.waitTicks(8);
         context.takeScreenshot("fakeroles-settings-panel");
         context.runOnClient(mc -> ClickGuiScreen.state().setExpanded("FakeRoles@MISC", false));
         context.getInput().pressKey(344);
         context.waitForScreen((Class)null);
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

   private static void shootTab(ClientGameTestContext context, String role, String shot) {
      context.runOnClient(mc -> {
         FakeRolesModule fr = NekoClient.modules().fakeRoles;
         fr.role.set(role);
         fr.setEnabled(true);
         mc.field_1690.field_1907.method_23481(true);
      });
      context.waitTicks(3);
      context.takeScreenshot(shot);
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: " + what);
      }
   }
}
