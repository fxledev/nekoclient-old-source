package com.mojang.client;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

public final class ClientMain {
   private ClientMain() {
   }

   public static void main(String[] args) throws Exception {
      MixinBootstrap.init();
      Mixins.addConfiguration("nekoclient.mixins.json");
      MixinExtrasBootstrap.init();
      System.out.println("[NekoAgent] bootstrap ok");
      Class<?> real = Class.forName("net.minecraft.client.main.Main");
      real.getMethod("main", String[].class).invoke(null, args);
   }
}
