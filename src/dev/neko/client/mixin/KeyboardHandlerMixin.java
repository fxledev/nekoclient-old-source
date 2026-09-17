package dev.neko.client.mixin;

import dev.neko.client.NekoClient;
import dev.neko.client.gui.ClickGuiScreen;
import net.minecraft.class_11908;
import net.minecraft.class_309;
import net.minecraft.class_310;
import net.minecraft.class_342;
import net.minecraft.class_408;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_473;
import net.minecraft.class_7743;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_309.class)
public class KeyboardHandlerMixin {
   @Inject(method = "onKey(JILnet/minecraft/client/input/KeyInput;)V", at = @At("HEAD"), cancellable = true)
   private void nekoclient$dispatchModuleKeybinds(long window, int action, class_11908 keyEvent, CallbackInfo ci) {
      class_310 minecraft = class_310.method_1551();
      if (NekoClient.modules() != null && action == 1) {
         if (minecraft.field_1755 == null && minecraft.field_1687 != null) {
            if (NekoClient.modules().onKeyPressed(keyEvent.comp_4795())) {
               ci.cancel();
            }
         } else if (minecraft.field_1755 != null
            && shiftOpensGui(minecraft.field_1755)
            && NekoClient.modules() != null
            && (
               NekoClient.modules().clickGui.getKeybind().matches(keyEvent.comp_4795())
                  || !NekoClient.modules().clickGui.getKeybind().isBound() && keyEvent.comp_4795() == 261
            )) {
            minecraft.method_1507(new ClickGuiScreen(minecraft.field_1755));
            ci.cancel();
         }
      }
   }

   private static boolean shiftOpensGui(class_437 screen) {
      return !(screen instanceof ClickGuiScreen)
            && !(screen instanceof class_408)
            && !(screen instanceof class_465)
            && !(screen instanceof class_7743)
            && !(screen instanceof class_473)
         ? !(screen.method_25399() instanceof class_342 editBox && editBox.method_25370())
         : false;
   }
}
