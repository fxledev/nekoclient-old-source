package dev.neko.client.gametest;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.gui.widget.StringWidget;
import dev.neko.client.module.impl.FakePayModule;
import dev.neko.client.settings.StringSetting;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.class_437;

public class StringInputGameTest implements FabricClientGameTest {
   private static final String EURO = codePoint(8364);
   private static final String POUND = codePoint(163);
   private static final String YEN = codePoint(165);

   public void runTest(ClientGameTestContext context) {
      TestSingleplayerContext world = context.worldBuilder().create();

      try {
         world.getClientWorld().waitForChunksRender();
         context.runOnClient(mc -> mc.field_1690.field_1837 = false);
         context.getInput().resizeWindow(1600, 900);
         context.waitTicks(2);
         context.runOnClient(mc -> {
            FakePayModule fp = NekoClient.modules().fakePay;
            require(fp != null, "FakePay registered");
            StringSetting currency = fp.currency;
            StringWidget widget = new StringWidget(NekoClient.themes(), currency);
            widget.setBounds(0.0F, 0.0F, 200.0F);
            require(widget.mouseClicked(5.0F, 5.0F, 0), "left click focuses the field");
            require(widget.isListening(), "focused field listens for typing");
            currency.set("");
            type(widget, EURO);
            boolean var10000 = currency.get().equals(EURO);
            String var10001 = currency.get();
            require(var10000, "euro sign typed, got '" + var10001 + "'");
            currency.set("");
            var10001 = POUND;
            type(widget, var10001 + YEN);
            String var4 = currency.get();
            var10001 = POUND;
            boolean var5x = var4.equals(var10001 + YEN);
            var10001 = currency.get();
            require(var5x, "pound+yen typed, got '" + var10001 + "'");
            currency.set("$");
            widget.keyPressed(259);
            require(currency.get().isEmpty(), "backspace cleared the '$'");
            type(widget, "$");
            var5x = currency.get().equals("$");
            var10001 = currency.get();
            require(var5x, "'$' retyped after clearing, got '" + var10001 + "'");
            currency.set("");
            type(widget, "USD");
            var5x = currency.get().equals("USD");
            var10001 = currency.get();
            require(var5x, "letters still accepted, got '" + var10001 + "'");
            currency.set("");
            type(widget, "$$$$$$");
            require(currency.get().length() == 4, "maxLength caps at 4, got " + currency.get().length());
            currency.set(EURO);
         });
         context.runOnClient(mc -> mc.method_1507(new ClickGuiScreen()));
         context.waitTicks(3);
         context.takeScreenshot("stringinput-clickgui-open");
         context.runOnClient(mc -> mc.method_1507((class_437)null));
         context.waitTicks(2);
         context.runOnClient(mc -> {
            FakePayModule fp = NekoClient.modules().fakePay;
            fp.setEnabled(true);
            require(fp.currency.get().equals(EURO), "currency persisted as euro");
            mc.field_1724.field_3944.method_45730("pay Notch 250k");
         });
         context.waitTicks(2);
         context.takeScreenshot("stringinput-fakepay-euro-receipt");
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

   private static String codePoint(int cp) {
      return new String(Character.toChars(cp));
   }

   private static void type(StringWidget widget, String text) {
      IntStream var10000 = text.codePoints();
      var10000.forEach(widget::charTyped);
   }

   private static void require(boolean condition, String what) {
      if (!condition) {
         throw new AssertionError("FAILED: " + what);
      }
   }
}
