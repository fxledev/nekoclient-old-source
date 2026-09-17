package dev.neko.client.gui;

import dev.neko.client.NekoClient;
import dev.neko.client.module.impl.GambleRiggerModule;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterRender;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents.AllowMouseClick;
import net.minecraft.class_480;

public final class GambleRiggerOverlay {
   private GambleRiggerOverlay() {
   }

   public static void register() {
      ScreenEvents.AFTER_INIT
         .register(
            (AfterInit)(client, screen, scaledWidth, scaledHeight) -> {
               if (screen instanceof class_480 container) {
                  GambleRiggerModule mod = NekoClient.modules() == null ? null : NekoClient.modules().gambleRigger;
                  if (mod != null && mod.isEnabled()) {
                     GamblePanel panel = new GamblePanel(container, mod);
                     ScreenEvents.afterRender(screen).register((AfterRender)(s, graphics, mouseX, mouseY, tickDelta) -> panel.render(graphics, mouseX, mouseY));
                     ScreenMouseEvents.allowMouseClick(screen)
                        .register((AllowMouseClick)(s, ctx) -> !panel.handleClick(ctx.comp_4798(), ctx.comp_4799(), ctx.method_74245()));
                  }
               }
            }
         );
   }
}
