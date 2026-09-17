package dev.neko.client.mixin;

import net.minecraft.class_1735;
import net.minecraft.class_465;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_465.class)
public interface AbstractContainerScreenAccessor {
   @Accessor("focusedSlot")
   class_1735 getHoveredSlot();

   @Accessor("x")
   int getLeftPos();

   @Accessor("y")
   int getTopPos();

   @Accessor("backgroundWidth")
   int getImageWidth();

   @Accessor("backgroundHeight")
   int getImageHeight();
}
