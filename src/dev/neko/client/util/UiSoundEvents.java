package dev.neko.client.util;

import net.minecraft.class_2378;
import net.minecraft.class_2960;
import net.minecraft.class_3414;
import net.minecraft.class_7923;

public final class UiSoundEvents {
   public static final class_3414 GUI_OPEN = register("ui.gui_open");
   public static final class_3414 GUI_CLOSE = register("ui.gui_close");
   public static final class_3414 HOVER = register("ui.hover");
   public static final class_3414 TOGGLE_ON = register("ui.toggle_on");
   public static final class_3414 TOGGLE_OFF = register("ui.toggle_off");
   public static final class_3414 SLIDER = register("ui.slider");
   public static final class_3414 SELECT = register("ui.select");
   public static final class_3414 KEYBIND = register("ui.keybind");
   public static final class_3414 NOTIFY_ON = register("ui.notify_on");
   public static final class_3414 NOTIFY_OFF = register("ui.notify_off");
   public static final class_3414 STARTUP_SAD = register("startup.sad");
   public static final class_3414 STARTUP_SONG = register("startup.song");
   public static final class_3414 STARTUP_TIKI = register("startup.tiki");
   public static final class_3414 STARTUP_67 = register("startup.67");

   private UiSoundEvents() {
   }

   private static class_3414 register(String name) {
      class_2960 id = class_2960.method_60655("nekoclient", name);
      return (class_3414)class_2378.method_10230(class_7923.field_41172, id, class_3414.method_47908(id));
   }

   public static void bootstrap() {
   }
}
