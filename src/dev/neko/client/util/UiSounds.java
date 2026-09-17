package dev.neko.client.util;

import net.minecraft.class_1109;
import net.minecraft.class_310;
import net.minecraft.class_3414;

public final class UiSounds {
   private UiSounds() {
   }

   public static void init() {
   }

   private static void play(class_3414 event, float pitch, float volume) {
      class_310.method_1551().method_1483().method_4873(class_1109.method_4757(event, pitch, volume));
   }

   public static void guiOpen() {
      play(UiSoundEvents.GUI_OPEN, 1.0F, 0.85F);
   }

   public static void guiClose() {
      play(UiSoundEvents.GUI_CLOSE, 1.0F, 0.75F);
   }

   public static void hover() {
      play(UiSoundEvents.HOVER, 0.95F, 0.5F);
   }

   public static void toggle(boolean enabled) {
   }

   public static void checkbox(boolean checked) {
   }

   public static void select() {
   }

   public static void sliderTick(float normalized) {
   }

   public static void keybindListen() {
   }

   public static void keybindSet() {
   }

   public static void notification(boolean enabled) {
      play(enabled ? UiSoundEvents.NOTIFY_ON : UiSoundEvents.NOTIFY_OFF, 1.0F, 0.7F);
   }

   public static void panelCollapse() {
   }

   public static void playStartup() {
      play(UiSoundEvents.STARTUP_SONG, 1.0F, 0.9F);
   }
}
