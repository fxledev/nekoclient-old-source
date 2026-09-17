package dev.neko.agent;

import java.nio.file.Path;

public final class NekoPaths {
   private NekoPaths() {
   }

   public static Path configDir() {
      return Path.of(System.getProperty("user.dir"), "config");
   }
}
