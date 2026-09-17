package dev.neko.agent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.class_310;

public final class NekoEvents {
   private static final List<NekoEvents.Tick> START_TICK = new CopyOnWriteArrayList<>();
   private static final List<NekoEvents.Tick> END_TICK = new CopyOnWriteArrayList<>();
   private static final List<NekoEvents.Join> JOIN = new CopyOnWriteArrayList<>();
   private static final List<NekoEvents.Leave> LEAVE = new CopyOnWriteArrayList<>();
   private static final List<NekoEvents.Stopping> STOPPING = new CopyOnWriteArrayList<>();
   private static volatile boolean hadWorld;

   private NekoEvents() {
   }

   public static void onStartTick(NekoEvents.Tick l) {
      START_TICK.add(l);
   }

   public static void onEndTick(NekoEvents.Tick l) {
      END_TICK.add(l);
   }

   public static void onJoin(NekoEvents.Join l) {
      JOIN.add(l);
   }

   public static void onLeave(NekoEvents.Leave l) {
      LEAVE.add(l);
   }

   public static void onStopping(NekoEvents.Stopping l) {
      STOPPING.add(l);
   }

   public static void fireStartTick(class_310 client) {
      pollWorld(client);

      for (NekoEvents.Tick l : START_TICK) {
         try {
            l.tick(client);
         } catch (Throwable var4) {
         }
      }
   }

   public static void fireEndTick(class_310 client) {
      for (NekoEvents.Tick l : END_TICK) {
         try {
            l.tick(client);
         } catch (Throwable var4) {
         }
      }
   }

   public static void pollWorld(class_310 client) {
      boolean inWorld = false;

      try {
         inWorld = client.field_1687 != null;
      } catch (Throwable var7) {
      }

      if (inWorld && !hadWorld) {
         hadWorld = true;

         for (NekoEvents.Join l : JOIN) {
            try {
               l.join();
            } catch (Throwable var6) {
            }
         }
      } else if (!inWorld && hadWorld) {
         hadWorld = false;

         for (NekoEvents.Leave l : LEAVE) {
            try {
               l.leave();
            } catch (Throwable var5) {
            }
         }
      }
   }

   public static void fireStopping() {
      for (NekoEvents.Stopping l : STOPPING) {
         try {
            l.stopping();
         } catch (Throwable var3) {
         }
      }
   }

   public interface Join {
      void join();
   }

   public interface Leave {
      void leave();
   }

   public interface Stopping {
      void stopping();
   }

   public interface Tick {
      void tick(class_310 var1);
   }
}
