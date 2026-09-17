package dev.neko.client.mixin;

import dev.neko.client.NekoClient;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.impl.FakeStatsModule;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_5348;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_329.class)
public class GuiSidebarMixin {
   @Inject(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At("HEAD"))
   private void nekoclient$beginSidebar(class_332 guiGraphics, class_266 objective, CallbackInfo ci) {
      FakeStatsModule fs = fakeStats();
      if (fs != null) {
         fs.beginSidebar();
      }
   }

   @Redirect(
      method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Lnet/minecraft/text/StringVisitable;)I", ordinal = 1)
   )
   private int nekoclient$lineWidth(class_327 font, class_5348 text) {
      FakeStatsModule fs = fakeStats();
      if (fs != null && text instanceof class_2561 component) {
         try {
            return font.method_27525(fs.rewriteForWidth(component));
         } catch (Exception var6) {
         }
      }

      return font.method_27525(text);
   }

   @ModifyArg(
      method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)V",
         ordinal = 1
      ),
      index = 1
   )
   private class_2561 nekoclient$lineText(class_2561 text) {
      FakeStatsModule fs = fakeStats();
      if (fs != null) {
         try {
            return fs.rewriteForDraw(text);
         } catch (Exception var4) {
         }
      }

      return text;
   }

   private static FakeStatsModule fakeStats() {
      ModuleManager m = NekoClient.modules();
      return m != null && m.fakeStats != null && m.fakeStats.isEnabled() ? m.fakeStats : null;
   }
}
