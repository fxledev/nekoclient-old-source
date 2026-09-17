package dev.neko.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.class_1297;
import net.minecraft.class_238;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1297.class)
public class EntityHitboxMixin {
   @ModifyReturnValue(method = "getBoundingBox", at = @At("RETURN"))
   private class_238 nekoclient$expandHitbox(class_238 original) {
      return original;
   }
}
