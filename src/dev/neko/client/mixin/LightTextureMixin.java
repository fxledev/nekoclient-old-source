package dev.neko.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import net.minecraft.class_765;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(class_765.class)
public class LightTextureMixin {
   @ModifyExpressionValue(
      method = "update",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"),
      slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getGamma()Lnet/minecraft/client/option/SimpleOption;"))
   )
   private Object nekoclient$fullbrightGamma(Object original) {
      ModuleManager modules = NekoClient.modules();
      return modules != null && modules.fullbright != null && modules.fullbright.isEnabled() ? (double)modules.fullbright.gamma.getFloat() : original;
   }
}
