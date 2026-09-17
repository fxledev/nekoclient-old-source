package dev.neko.client.module.impl;

import dev.neko.client.NekoClient;
import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.module.ModuleManager;
import dev.neko.client.settings.StringSetting;
import dev.neko.client.util.Amounts;
import net.minecraft.class_1109;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_5250;

public class FakePayModule extends Module {
   private static final int WHITE = 16777215;
   private static final int RED = 16733525;
   private static final int CURRENCY_COLOR = -16740609;
   public final StringSetting command = this.addSetting(new StringSetting("Command", "Command name to hijack, without the slash.", "pay", 32, "pay"));
   public final StringSetting currency = this.addSetting(new StringSetting("Currency", "Symbol before the amount (colored).", "$", 4, "$"));

   public FakePayModule() {
      super("FakePay", "Fakes a /pay for clips — blocks the real command", Category.MISC);
   }

   public boolean tryIntercept(String rawCommand) {
      if (this.isEnabled() && rawCommand != null) {
         String[] parts = rawCommand.trim().split("\\s+");
         if (parts.length == 0) {
            return false;
         } else {
            String name = parts[0];
            if (name.startsWith("/")) {
               name = name.substring(1);
            }

            String target = this.command.get().trim();
            if (!target.isEmpty() && name.equalsIgnoreCase(target)) {
               this.handle(parts);
               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   private void handle(String[] parts) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1724 != null) {
         if (parts.length < 3) {
            this.show(mc, class_2561.method_43470("Usage: /" + parts[0] + " <player> <amount>").method_54663(16733525));
            this.fail(mc);
         } else {
            String player = parts[1];
            double amount = Amounts.parse(parts[2]);
            if (Double.isNaN(amount) || amount <= 0.0) {
               this.show(mc, class_2561.method_43470("Invalid amount: " + parts[2]).method_54663(16733525));
               this.fail(mc);
            } else if (player.equalsIgnoreCase(mc.field_1724.method_7334().name())) {
               this.show(mc, class_2561.method_43470("You can't pay yourself!").method_54663(16733525));
               this.fail(mc);
            } else {
               FakeStatsModule stats = this.stats();
               boolean useStats = stats != null && stats.isEnabled();
               if (useStats && stats.getLiveBalance() < amount) {
                  this.show(mc, class_2561.method_43470("You don't have enough money!").method_54663(16733525));
                  this.fail(mc);
               } else {
                  if (useStats) {
                     stats.deduct(amount);
                  }

                  class_5250 line = class_2561.method_43473().method_10852(class_2561.method_43470("You paid " + player).method_54663(16777215));
                  String symbol = this.currency.get();
                  if (!symbol.isEmpty()) {
                     line.method_10852(class_2561.method_43470(" " + symbol).method_54663(36607))
                        .method_10852(class_2561.method_43470(Amounts.shortForm(amount)).method_54663(16777215));
                  } else {
                     line.method_10852(class_2561.method_43470(" " + Amounts.shortForm(amount)).method_54663(16777215));
                  }

                  this.show(mc, line);
                  this.succeed(mc);
               }
            }
         }
      }
   }

   private void show(class_310 mc, class_2561 component) {
      if (mc.field_1724 != null) {
         mc.field_1724.method_7353(component, true);
         mc.field_1724.method_7353(component, false);
      }
   }

   private void succeed(class_310 mc) {
      this.play(mc, class_3417.field_14709, 1.0F);
   }

   private void fail(class_310 mc) {
      this.play(mc, class_3417.field_15008, 1.0F);
   }

   private void play(class_310 mc, class_3414 event, float pitch) {
      mc.method_1483().method_4873(class_1109.method_4757(event, pitch, 1.0F));
   }

   private FakeStatsModule stats() {
      ModuleManager modules = NekoClient.modules();
      return modules == null ? null : modules.fakeStats;
   }
}
