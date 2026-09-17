package dev.neko.client;

import net.fabricmc.api.ClientModInitializer;

public final class NekoModInit implements ClientModInitializer {
   public void onInitializeClient() {
      NekoClient.init();
   }
}
