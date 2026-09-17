package dev.neko.client.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(class_310.class)
public interface MinecraftAccessor {
   @Accessor("itemUseCooldown")
   void nekoclient$setRightClickDelay(int var1);

   @Invoker("doAttack")
   boolean nekoclient$startAttack();

   @Invoker("doItemUse")
   void nekoclient$startUseItem();
}
