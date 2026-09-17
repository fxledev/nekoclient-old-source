package dev.neko.agent;

import dev.neko.client.NekoClient;
import net.minecraft.class_310;

public final class NekoAgent {
   private static volatile boolean ready;

   private NekoAgent() {
   }

   public static void ensureInit(class_310 client) {
      if (!ready) {
         synchronized (NekoAgent.class) {
            if (!ready) {
               try {
                  System.out.println("[NekoAgent] game init");
                  NekoClient.init();
                  Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                     try {
                        NekoEvents.fireStopping();
                     } catch (Throwable var1) {
                     }
                  }, "neko-shutdown"));
                  System.out.println("[NekoAgent] init ok");
               } catch (Throwable var4) {
                  var4.printStackTrace();
               }

               ready = true;
            }
         }
      }
   }
}
