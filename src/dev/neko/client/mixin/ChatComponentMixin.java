package dev.neko.client.mixin;

import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import net.minecraft.class_2561;
import net.minecraft.class_338;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(class_338.class)
public class ChatComponentMixin {
   @ModifyVariable(
      method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0
   )
   private class_2561 nekoclient$censorChat(class_2561 component) {
      ModuleManager modules = NekoClient.modules();
      if (modules == null) {
         return component;
      } else {
         class_2561 result = component;
         if (modules.fakeRoles != null) {
            result = modules.fakeRoles.decorateChat(component);
         }

         if (modules.nameProtect != null && modules.nameProtect.isEnabled()) {
            result = modules.nameProtect.censorChat(result);
         }

         return result;
      }
   }
}
