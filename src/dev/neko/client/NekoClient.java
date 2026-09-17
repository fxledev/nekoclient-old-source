package dev.neko.client;

import dev.neko.agent.NekoEvents;
import dev.neko.agent.NekoScreens;
import dev.neko.client.config.ConfigManager;
import dev.neko.client.config.ConfigStore;
import dev.neko.client.gui.ClickGuiScreen;
import dev.neko.client.gui.ClickGuiState;
import dev.neko.client.gui.GambleRiggerOverlay;
import dev.neko.client.hud.HudManager;
import dev.neko.client.module.Category;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.module.impl.FreecamModule;
import dev.neko.client.notification.NotificationManager;
import dev.neko.client.render.MotionBlurRenderer;
import dev.neko.client.render.OverlayRenderer;
import dev.neko.client.render.SusChunkRenderer;
import dev.neko.client.spotify.SpotifyService;
import dev.neko.client.suschunk.ServerLightCache;
import dev.neko.client.theme.ThemeManager;
import dev.neko.client.util.UiSoundEvents;
import dev.neko.client.util.UiSounds;
import java.util.function.Supplier;
import net.minecraft.class_310;

public class NekoClient {
   public static final String MOD_ID = "nekoclient";
   public static final String NAME = "NekoClient";
   public static final String VERSION = "1.0.0-neko.1";
   private static ModuleManager modules;
   private static ThemeManager themes;
   private static ConfigManager config;
   private static ConfigStore configStore;
   private static HudManager hud;
   private static NotificationManager notifications;
   private static SpotifyService spotify;
   private static boolean startupSoundPlayed;
   public static volatile long sessionStartMs = -1L;
   private static volatile boolean inited;

   public static ModuleManager modules() {
      return modules;
   }

   public static ThemeManager themes() {
      return themes;
   }

   public static ConfigManager config() {
      return config;
   }

   public static ConfigStore configStore() {
      return configStore;
   }

   public static HudManager hud() {
      return hud;
   }

   public static SpotifyService spotify() {
      return spotify;
   }

   public static NotificationManager notifications() {
      return notifications;
   }

   public static void init() {
      if (!inited) {
         synchronized (NekoClient.class) {
            if (inited) {
               return;
            }

            inited = true;
         }

         UiSoundEvents.bootstrap();
         themes = new ThemeManager();
         modules = new ModuleManager();
         spotify = new SpotifyService();
         notifications = new NotificationManager(themes, modules.hud);
         hud = new HudManager(modules, themes, spotify, notifications);
         config = new ConfigManager(modules, themes);
         ConfigManager var10000 = config;
         HudManager var10002 = hud;
         Supplier var4 = var10002::toJson;
         HudManager var10003 = hud;
         var10000.addSection("hud", var4, var10003::fromJson);
         var10000 = config;
         ClickGuiState var5 = ClickGuiScreen.state();
         Supplier var6 = var5::toJson;
         ClickGuiState var9 = ClickGuiScreen.state();
         var10000.addSection("panels", var6, var9::fromJson);
         var10000 = config;
         Supplier var7 = ClickGuiScreen::guiPrefsToJson;
         var10000.addSection("gui", var7, ClickGuiScreen::guiPrefsFromJson);
         config.load();
         configStore = new ConfigStore(config);
         configStore.loadAll();
         modules.setOpenGuiAction(() -> class_310.method_1551().method_1507(new ClickGuiScreen()));
         modules.setToggleListener((module, enabled) -> {
            if (!ConfigStore.applying && module.getCategory() != Category.CLIENT && class_310.method_1551().field_1687 != null) {
               notifications.push(module.getName(), enabled);
               if (!modules.isKeybindToggle()) {
                  UiSounds.notification(enabled);
               }
            }
         });
         OverlayRenderer.init(hud, notifications);
         if (hasLoader()) {
            GambleRiggerOverlay.register();
         }

         NekoEvents.onStartTick(c -> NekoScreens.tick());
         spotify.start();
         NekoEvents.onJoin(() -> {
            clearSusState();
            sessionStartMs = System.currentTimeMillis();
         });
         NekoEvents.onLeave(() -> {
            clearSusState();
            sessionStartMs = -1L;
         });
         NekoEvents.onEndTick(client -> modules.onTick());
         NekoEvents.onStartTick(client -> {
            FreecamModule f = modules.freecam;
            if (f != null && f.isActive()) {
               FreecamModule.reapplyBodyInput(client);
            }

            modules.onCombatTick();
         });
         NekoEvents.onStopping(() -> shutdown());
      }
   }

   public static void shutdown() {
      try {
         if (config != null) {
            config.save();
         }
      } catch (Throwable var2) {
      }

      try {
         if (spotify != null) {
            spotify.stop();
         }
      } catch (Throwable var1) {
      }
   }

   private static boolean hasLoader() {
      try {
         Class.forName("net.fabricmc.loader.api.FabricLoader");
         return true;
      } catch (Throwable var1) {
         return false;
      }
   }

   private static void clearSusState() {
      ServerLightCache.get().clear();
      modules.susChunkFinder.scanner.clear();
      SusChunkRenderer.reset();
      if (modules.blockEntityEsp != null) {
         modules.blockEntityEsp.clear();
      }

      if (modules.hitParticles != null) {
         modules.hitParticles.clear();
      }

      if (modules.customAccessories != null) {
         modules.customAccessories.clear();
      }

      MotionBlurRenderer.reset();
   }
}
