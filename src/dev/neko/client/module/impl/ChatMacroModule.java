package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.BooleanSetting;
import dev.neko.client.settings.KeybindSetting;
import dev.neko.client.settings.ModeSetting;
import dev.neko.client.settings.StringSetting;
import net.minecraft.class_310;
import net.minecraft.class_408;

public class ChatMacroModule extends Module {
   private static final int SLOTS = 3;
   public final ModeSetting slot = this.addSetting(new ModeSetting("Slot", "Macro slot to edit.", "1", "1", "2", "3"));
   public final BooleanSetting sendInstantly = this.addSetting(new BooleanSetting("Send Instantly", "Skip the chat preview and send immediately.", true));
   private final StringSetting[] messages = new StringSetting[3];
   private final KeybindSetting[] keys = new KeybindSetting[3];

   public ChatMacroModule() {
      super("ChatMacro", "Bindable chat command macros.", Category.CLIENT);

      for (int i = 0; i < 3; i++) {
         String num = Integer.toString(i + 1);
         this.messages[i] = this.addSetting(new StringSetting("Message " + num, "Message or /command for slot " + num, "", 256, "/say hi"));
         this.keys[i] = this.addSetting(new KeybindSetting("Key " + num, "Key that runs slot " + num, -1));
         this.messages[i].visibleWhen(() -> this.slot.is(num));
         this.keys[i].visibleWhen(() -> this.slot.is(num));
      }
   }

   @Override
   public boolean onKeyPress(int keyCode) {
      boolean handled = false;

      for (int i = 0; i < 3; i++) {
         if (this.keys[i].matches(keyCode)) {
            this.run(this.messages[i].get());
            handled = true;
         }
      }

      return handled;
   }

   private void run(String message) {
      if (message != null && !message.isBlank()) {
         class_310 mc = class_310.method_1551();
         if (mc.field_1724 != null && mc.field_1724.field_3944 != null) {
            if (!this.sendInstantly.get()) {
               mc.method_1507(new class_408(message, false));
            } else if (message.startsWith("/")) {
               mc.field_1724.field_3944.method_45730(message.substring(1));
            } else {
               mc.field_1724.field_3944.method_45729(message);
            }
         }
      }
   }
}
