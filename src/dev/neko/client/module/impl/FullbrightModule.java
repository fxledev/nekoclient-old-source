package dev.neko.client.module.impl;

import dev.neko.client.module.Category;
import dev.neko.client.module.Module;
import dev.neko.client.settings.SliderSetting;

public class FullbrightModule extends Module {
   public final SliderSetting gamma = this.addSetting(new SliderSetting("Gamma", "Brightness boost", 12.0, 1.0, 15.0, 1.0));

   public FullbrightModule() {
      super("FullBright", "Maximum brightness everywhere", Category.RENDER);
   }
}
