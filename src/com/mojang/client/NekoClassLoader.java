package com.mojang.client;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.regex.Pattern;

public final class NekoClassLoader extends URLClassLoader {
   public NekoClassLoader(ClassLoader parent) throws Exception {
      super(classpathUrls(), parent);
      Thread.currentThread().setContextClassLoader(this);
      System.out.println("[NekoAgent] sysloader up urls=" + classpathUrls().length);
   }

   public NekoClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
   }

   public NekoClassLoader(URL[] urls) {
      super(urls);
   }

   private static URL[] classpathUrls() throws Exception {
      String[] parts = System.getProperty("java.class.path").split(Pattern.quote(File.pathSeparator));
      URL[] urls = new URL[parts.length];

      for (int i = 0; i < parts.length; i++) {
         urls[i] = new File(parts[i]).toURI().toURL();
      }

      return urls;
   }
}
